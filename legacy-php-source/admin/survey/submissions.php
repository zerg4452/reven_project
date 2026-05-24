<?php

declare(strict_types=1);

require_once __DIR__ . '/../core/auth.php';
require_once __DIR__ . '/../../lib/storage.php';
require_once __DIR__ . '/../core/submission_status.php';

require_admin();

$defaultDateFrom = (new DateTimeImmutable('-60 days'))->format('Y-m-d');
$defaultDateTo = (new DateTimeImmutable('+1 day'))->format('Y-m-d');

function admin_submission_text_contains(string $value, string $keyword): bool
{
    if ($keyword === '') {
        return true;
    }

    if (function_exists('mb_stripos')) {
        return mb_stripos($value, $keyword) !== false;
    }

    return stripos($value, $keyword) !== false || str_contains($value, $keyword);
}

$formId = trim((string) ($_GET['form'] ?? ''));
$form = $formId !== '' ? find_form($formId) : null;
$filters = [
    'date_from' => trim((string) ($_GET['date_from'] ?? $defaultDateFrom)),
    'date_to' => trim((string) ($_GET['date_to'] ?? $defaultDateTo)),
    'keyword_type' => trim((string) ($_GET['keyword_type'] ?? 'all')),
    'keyword' => trim((string) ($_GET['keyword'] ?? '')),
];
if (!in_array($filters['keyword_type'], ['all', 'title', 'submitter'], true)) {
    $filters['keyword_type'] = 'all';
}

$sourceSubmissions = $formId !== '' ? submissions_for_form($formId) : all_submissions();
$submissions = array_values(array_filter($sourceSubmissions, function (array $submission) use ($filters): bool {
    $submittedDate = substr((string) ($submission['submitted_at'] ?? ''), 0, 10);
    $title = (string) ($submission['form_title'] ?? '');
    $submitter = (string) ($submission['person']['name'] ?? '');

    if ($filters['date_from'] !== '' && $submittedDate < $filters['date_from']) {
        return false;
    }
    if ($filters['date_to'] !== '' && $submittedDate > $filters['date_to']) {
        return false;
    }

    if ($filters['keyword'] === '') {
        return true;
    }

    $matchesTitle = admin_submission_text_contains($title, $filters['keyword']);
    $matchesSubmitter = admin_submission_text_contains($submitter, $filters['keyword']);

    if ($filters['keyword_type'] === 'title') {
        return $matchesTitle;
    }
    if ($filters['keyword_type'] === 'submitter') {
        return $matchesSubmitter;
    }

    return $matchesTitle || $matchesSubmitter;
}));
$pageTitle = '설문 이력 관리';
require __DIR__ . '/../../partials/header.php';
?>

<section class="mb-4">
    <div>
        <h1>설문 이력 관리</h1>
        <p class="text-secondary mb-0"><?= $form ? e($form['title'] ?? '') : '전체 설문 이력' ?></p>
    </div>
</section>

<form class="card shadow-sm mb-4" method="get">
    <div class="card-body">
        <?php if ($formId !== ''): ?>
            <input type="hidden" name="form" value="<?= e($formId) ?>">
        <?php endif; ?>
        <div class="search-inline-grid">
            <div class="search-inline-field">
                <div class="search-inline-label">제출일자</div>
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
                                <option value="submitter" <?= $filters['keyword_type'] === 'submitter' ? 'selected' : '' ?>>작성자명</option>
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
                        <a class="btn btn-outline-secondary" href="/admin/survey/submissions.php<?= $formId !== '' ? '?form=' . e($formId) : '' ?>">초기화</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</form>

<div class="list-action-bar d-flex justify-content-end gap-2 mb-3">
    <a class="btn btn-outline-secondary" href="/admin/survey/submissions_export.php<?= $formId !== '' ? '?form=' . e($formId) : '' ?>">CSV 다운로드</a>
</div>

<div class="content-section-title d-flex justify-content-between align-items-end mb-2">
    <h2 class="h5 mb-0">설문 이력 목록</h2>
    <span class="text-secondary small"><?= count($submissions) ?>개</span>
</div>

<div class="card shadow-sm">
    <div class="card-body px-0">
    <?php if (!$submissions): ?>
        <p class="text-secondary mb-0">설문 이력이 없습니다.</p>
    <?php else: ?>
        <div class="table-responsive">
        <table class="table align-middle">
            <thead>
            <tr><th>순번</th><th>설문명</th><th>제출자명</th><th>연락처</th><th>상태</th><th>제출일</th></tr>
            </thead>
            <tbody>
            <?php foreach ($submissions as $index => $submission): ?>
                <tr>
                    <td><?= $index + 1 ?></td>
                    <td><a class="table-link" href="/admin/survey/submission_view.php?id=<?= e($submission['id']) ?>"><?= e($submission['form_title'] ?? '') ?></a></td>
                    <td><a class="table-link" href="/admin/survey/submission_view.php?id=<?= e($submission['id']) ?>"><?= e($submission['person']['name'] ?? '') ?></a></td>
                    <td><?= e($submission['person']['phone'] ?? '') ?></td>
                    <td><span class="badge text-bg-info"><?= e(submission_status_text($submission['status'] ?? 'new')) ?></span></td>
                    <td><?= e(display_date($submission['submitted_at'] ?? '')) ?></td>
                </tr>
            <?php endforeach; ?>
            </tbody>
        </table>
        </div>
    <?php endif; ?>
    </div>
</div>

<?php require __DIR__ . '/../../partials/footer.php'; ?>
