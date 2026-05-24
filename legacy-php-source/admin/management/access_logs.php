<?php

declare(strict_types=1);

require_once __DIR__ . '/../core/auth.php';
require_once __DIR__ . '/../../lib/storage.php';

require_admin();

$defaultDateFrom = (new DateTimeImmutable('-60 days'))->format('Y-m-d');
$defaultDateTo = (new DateTimeImmutable('+1 day'))->format('Y-m-d');

function admin_access_text_contains(string $value, string $keyword): bool
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
if (!in_array($filters['keyword_type'], ['all', 'name', 'login_id', 'location'], true)) {
    $filters['keyword_type'] = 'all';
}

$logs = all_admin_access_logs();
$filteredLogs = array_values(array_filter($logs, function (array $log) use ($filters): bool {
    $accessDate = substr((string) ($log['accessed_at'] ?? ''), 0, 10);
    $name = (string) ($log['admin_name'] ?? '');
    $loginId = (string) ($log['admin_login_id'] ?? '');
    $location = (string) ($log['location'] ?? '');

    if ($filters['date_from'] !== '' && $accessDate < $filters['date_from']) {
        return false;
    }
    if ($filters['date_to'] !== '' && $accessDate > $filters['date_to']) {
        return false;
    }
    if ($filters['keyword'] === '') {
        return true;
    }

    $matchesName = admin_access_text_contains($name, $filters['keyword']);
    $matchesLoginId = admin_access_text_contains($loginId, $filters['keyword']);
    $matchesLocation = admin_access_text_contains($location, $filters['keyword']);

    if ($filters['keyword_type'] === 'name') {
        return $matchesName;
    }
    if ($filters['keyword_type'] === 'login_id') {
        return $matchesLoginId;
    }
    if ($filters['keyword_type'] === 'location') {
        return $matchesLocation;
    }

    return $matchesName || $matchesLoginId || $matchesLocation;
}));

$pageTitle = '관리자 접속이력';
require __DIR__ . '/../../partials/header.php';
?>

<section class="mb-4">
    <div>
        <h1>관리자 접속이력</h1>
        <p class="text-secondary mb-0">관리자가 접근한 메뉴와 시각을 확인합니다.</p>
    </div>
</section>

<form class="card shadow-sm mb-4" method="get">
    <div class="card-body">
        <div class="search-inline-grid">
            <div class="search-inline-field">
                <div class="search-inline-label">접속일자</div>
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
                                <option value="name" <?= $filters['keyword_type'] === 'name' ? 'selected' : '' ?>>이름</option>
                                <option value="login_id" <?= $filters['keyword_type'] === 'login_id' ? 'selected' : '' ?>>아이디</option>
                                <option value="location" <?= $filters['keyword_type'] === 'location' ? 'selected' : '' ?>>접속위치</option>
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
                        <a class="btn btn-outline-secondary" href="/admin/management/access_logs.php">초기화</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</form>

<div class="content-section-title d-flex justify-content-between align-items-end mb-2">
    <h2 class="h5 mb-0">접속 이력 목록</h2>
    <span class="text-secondary small"><?= count($filteredLogs) ?>건</span>
</div>

<div class="card shadow-sm">
    <div class="card-body px-0">
    <?php if (!$filteredLogs): ?>
        <p class="text-secondary mb-0">검색 조건에 맞는 접속 이력이 없습니다.</p>
    <?php else: ?>
        <div class="table-responsive">
        <table class="table align-middle">
            <thead>
            <tr><th>순번</th><th>이름</th><th>아이디</th><th>접속위치</th><th>시간</th></tr>
            </thead>
            <tbody>
            <?php foreach ($filteredLogs as $index => $log): ?>
                <tr>
                    <td><?= $index + 1 ?></td>
                    <td><?= e($log['admin_name'] ?? '') ?></td>
                    <td><?= e($log['admin_login_id'] ?? '') ?></td>
                    <td><?= e($log['location'] ?? '') ?></td>
                    <td><?= e(display_datetime($log['accessed_at'] ?? '')) ?></td>
                </tr>
            <?php endforeach; ?>
            </tbody>
        </table>
        </div>
    <?php endif; ?>
    </div>
</div>

<?php require __DIR__ . '/../../partials/footer.php'; ?>
