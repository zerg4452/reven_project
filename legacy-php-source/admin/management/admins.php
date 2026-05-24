<?php

declare(strict_types=1);

require_once __DIR__ . '/../core/auth.php';
require_once __DIR__ . '/../../lib/storage.php';

require_admin();

$defaultDateFrom = (new DateTimeImmutable('-60 days'))->format('Y-m-d');
$defaultDateTo = (new DateTimeImmutable('+1 day'))->format('Y-m-d');

function admin_management_text_contains(string $value, string $keyword): bool
{
    if ($keyword === '') {
        return true;
    }

    if (function_exists('mb_stripos')) {
        return mb_stripos($value, $keyword) !== false;
    }

    return stripos($value, $keyword) !== false || str_contains($value, $keyword);
}

$filters = [
    'date_from' => trim((string) ($_GET['date_from'] ?? $defaultDateFrom)),
    'date_to' => trim((string) ($_GET['date_to'] ?? $defaultDateTo)),
    'keyword_type' => trim((string) ($_GET['keyword_type'] ?? 'all')),
    'keyword' => trim((string) ($_GET['keyword'] ?? '')),
];
if (!in_array($filters['keyword_type'], ['all', 'name', 'login_id'], true)) {
    $filters['keyword_type'] = 'all';
}

$admins = managed_admins();
$filteredAdmins = array_values(array_filter($admins, function (array $admin) use ($filters): bool {
    $name = (string) ($admin['name'] ?? '');
    $loginId = (string) ($admin['login_id'] ?? '');
    $createdDate = substr((string) ($admin['reg_dtm'] ?? ''), 0, 10);

    if ($filters['date_from'] !== '' && $createdDate < $filters['date_from']) {
        return false;
    }
    if ($filters['date_to'] !== '' && $createdDate > $filters['date_to']) {
        return false;
    }
    if ($filters['keyword'] === '') {
        return true;
    }

    $matchesName = admin_management_text_contains($name, $filters['keyword']);
    $matchesLoginId = admin_management_text_contains($loginId, $filters['keyword']);

    if ($filters['keyword_type'] === 'name') {
        return $matchesName;
    }
    if ($filters['keyword_type'] === 'login_id') {
        return $matchesLoginId;
    }

    return $matchesName || $matchesLoginId;
}));

$pageTitle = '관리자 관리';
require __DIR__ . '/../../partials/header.php';
?>

<section class="mb-4">
    <div>
        <h1>관리자 관리</h1>
        <p class="text-secondary mb-0">관리자 계정 목록과 사용 상태를 관리합니다.</p>
    </div>
</section>

<form class="card shadow-sm mb-4" method="get">
    <div class="card-body">
        <div class="search-inline-grid">
            <div class="search-inline-field">
                <div class="search-inline-label">등록일자</div>
                <div class="search-inline-control search-inline-range">
                    <input class="form-control" type="date" name="date_from" value="<?= e($filters['date_from']) ?>">
                    <span class="search-date-separator">~</span>
                    <input class="form-control" type="date" name="date_to" value="<?= e($filters['date_to']) ?>">
                </div>
            </div>
            <div class="search-inline-field search-inline-field-pair">
                <div class="search-inline-pair">
                    <div class="search-inline-subfield">
                        <div class="search-inline-label">검색조건</div>
                        <div class="search-inline-control search-inline-select">
                            <select class="form-select" name="keyword_type">
                                <option value="all" <?= $filters['keyword_type'] === 'all' ? 'selected' : '' ?>>전체</option>
                                <option value="name" <?= $filters['keyword_type'] === 'name' ? 'selected' : '' ?>>관리자명</option>
                                <option value="login_id" <?= $filters['keyword_type'] === 'login_id' ? 'selected' : '' ?>>아이디</option>
                            </select>
                        </div>
                    </div>
                    <div class="search-inline-subfield">
                        <div class="search-inline-label">검색어</div>
                        <div class="search-inline-control search-inline-text">
                            <input class="form-control" name="keyword" value="<?= e($filters['keyword']) ?>">
                        </div>
                    </div>
                    <div class="search-inline-actions">
                        <button class="btn btn-primary" type="submit">검색</button>
                        <a class="btn btn-outline-secondary" href="/admin/management/admins.php">초기화</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</form>

<div class="list-action-bar d-flex justify-content-end gap-2 mb-3">
    <a class="btn btn-primary" href="/admin/management/admin_edit.php">관리자 등록</a>
</div>

<div class="content-section-title d-flex justify-content-between align-items-end mb-2">
    <h2 class="h5 mb-0">관리자 목록</h2>
    <span class="text-secondary small"><?= count($filteredAdmins) ?>명</span>
</div>

<div class="card shadow-sm">
    <div class="card-body px-0">
    <?php if (!$filteredAdmins): ?>
        <p class="text-secondary mb-0">검색 조건에 맞는 관리자가 없습니다.</p>
    <?php else: ?>
        <div class="table-responsive">
        <table class="table align-middle">
            <thead>
            <tr><th>순번</th><th>관리자명</th><th>아이디</th><th>권한</th><th>사용여부</th><th>등록일</th><th>수정일</th></tr>
            </thead>
            <tbody>
            <?php foreach ($filteredAdmins as $index => $admin): ?>
                <?php $adminIdx = (int) ($admin['adm_seq'] ?? 0); ?>
                <tr>
                    <td><?= $index + 1 ?></td>
                    <td><a class="table-link" href="/admin/management/admin_edit.php?id=<?= e((string) $adminIdx) ?>"><?= e($admin['name'] ?? '') ?></a></td>
                    <td><?= e($admin['login_id'] ?? '') ?></td>
                    <td><?= e(($admin['role'] ?? 'admin') === 'super' ? '최고관리자' : '관리자') ?></td>
                    <td><span class="badge <?= ($admin['status'] ?? 'active') === 'active' ? 'text-bg-success' : 'text-bg-secondary' ?>"><?= e(($admin['status'] ?? 'active') === 'active' ? '사용중' : '미사용') ?></span></td>
                    <td><?= e(display_date($admin['reg_dtm'] ?? '')) ?></td>
                    <td><?= e(display_date($admin['mod_dtm'] ?? '')) ?></td>
                </tr>
            <?php endforeach; ?>
            </tbody>
        </table>
        </div>
    <?php endif; ?>
    </div>
</div>

<?php require __DIR__ . '/../../partials/footer.php'; ?>
