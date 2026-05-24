<?php

declare(strict_types=1);

require_once __DIR__ . '/../core/auth.php';
require_once __DIR__ . '/../../lib/storage.php';

require_admin();

$defaultDateFrom = (new DateTimeImmutable('-60 days'))->format('Y-m-d');
$defaultDateTo = (new DateTimeImmutable('+1 day'))->format('Y-m-d');

function admin_text_contains(string $value, string $keyword): bool
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
if (!in_array($filters['keyword_type'], ['all', 'title'], true)) {
    $filters['keyword_type'] = 'all';
}

$forms = all_forms();
$filteredForms = array_values(array_filter($forms, function (array $form) use ($filters): bool {
    $title = (string) ($form['title'] ?? '');
    $createdDate = substr((string) ($form['created_at'] ?? ''), 0, 10);

    if ($filters['date_from'] !== '' && $createdDate < $filters['date_from']) {
        return false;
    }
    if ($filters['date_to'] !== '' && $createdDate > $filters['date_to']) {
        return false;
    }
    if ($filters['keyword'] !== '' && !admin_text_contains($title, $filters['keyword'])) {
        return false;
    }

    return true;
}));

$pageTitle = '설문 관리';
require __DIR__ . '/../../partials/header.php';
?>

<section class="mb-4">
    <div>
        <h1>설문 관리</h1>
        <p class="text-secondary mb-0">사용할 설문과 문항, 보기, 사용여부를 관리합니다.</p>
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
                                <option value="title" <?= $filters['keyword_type'] === 'title' ? 'selected' : '' ?>>설문명</option>
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
                        <a class="btn btn-outline-secondary" href="/admin/survey/forms.php">초기화</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</form>

<div class="list-action-bar d-flex justify-content-end gap-2 mb-3">
    <a class="btn btn-primary" href="/admin/survey/form_edit.php">설문 등록</a>
</div>

<div class="content-section-title d-flex justify-content-between align-items-end mb-2">
    <h2 class="h5 mb-0">설문 목록</h2>
    <span class="text-secondary small"><?= count($filteredForms) ?>개</span>
</div>

<div class="card shadow-sm">
    <div class="card-body px-0">
    <?php if (!$filteredForms): ?>
        <p class="text-secondary mb-0">검색 조건에 맞는 설문이 없습니다.</p>
    <?php else: ?>
        <div class="table-responsive">
        <table class="table align-middle">
            <thead>
            <tr><th>순번</th><th>설문 제목</th><th>문항 수</th><th>사용여부</th><th>등록일</th><th>수정일</th></tr>
            </thead>
            <tbody>
            <?php foreach ($filteredForms as $index => $form): ?>
                <?php
                $formId = (string) ($form['id'] ?? '');
                ?>
                <tr>
                    <td><?= $index + 1 ?></td>
                    <td><a class="table-link" href="/admin/survey/form_edit.php?id=<?= e($formId) ?>"><?= e($form['title'] ?? '') ?></a></td>
                    <td><?= count($form['fields'] ?? []) ?></td>
                    <td><span class="badge <?= ($form['status'] ?? 'active') === 'active' ? 'text-bg-success' : 'text-bg-secondary' ?>"><?= e(($form['status'] ?? 'active') === 'active' ? '접수중' : '비활성') ?></span></td>
                    <td><?= e(display_date($form['created_at'] ?? '')) ?></td>
                    <td><?= e(display_date($form['updated_at'] ?? '')) ?></td>
                </tr>
            <?php endforeach; ?>
            </tbody>
        </table>
        </div>
    <?php endif; ?>
    </div>
</div>

<?php require __DIR__ . '/../../partials/footer.php'; ?>
