<?php

declare(strict_types=1);

require_once __DIR__ . '/../core/auth.php';
require_once __DIR__ . '/../../lib/storage.php';

require_admin();

$defaultDateFrom = (new DateTimeImmutable('-60 days'))->format('Y-m-d');
$defaultDateTo = (new DateTimeImmutable('+1 day'))->format('Y-m-d');

function ai_news_text_contains(string $value, string $keyword): bool
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
    'status' => $_GET['status'] ?? [],
];
if (!in_array($filters['keyword_type'], ['all', 'title', 'tag', 'status'], true)) {
    $filters['keyword_type'] = 'all';
}
if (!is_array($filters['status'])) {
    $filters['status'] = [];
}
$filters['status'] = array_values(array_unique(array_filter(array_map('strtoupper', array_map('trim', $filters['status'])), static fn (string $value): bool => $value !== '' && array_key_exists($value, ai_news_status_options()))));
if (!$filters['status']) {
    $filters['status'] = array_keys(ai_news_status_options());
}

if (request_method() === 'POST') {
    $action = (string) ($_POST['action'] ?? '');
    $postFilters = [
        'date_from' => trim((string) ($_POST['date_from'] ?? $defaultDateFrom)),
        'date_to' => trim((string) ($_POST['date_to'] ?? $defaultDateTo)),
        'keyword_type' => trim((string) ($_POST['keyword_type'] ?? 'all')),
        'keyword' => trim((string) ($_POST['keyword'] ?? '')),
        'status' => $_POST['status'] ?? [],
    ];
    if (!in_array($postFilters['keyword_type'], ['all', 'title', 'tag', 'status'], true)) {
        $postFilters['keyword_type'] = 'all';
    }
    if (!is_array($postFilters['status'])) {
        $postFilters['status'] = [];
    }
    $postFilters['status'] = array_values(array_unique(array_filter(array_map('strtoupper', array_map('trim', $postFilters['status'])), static fn (string $value): bool => $value !== '' && array_key_exists($value, ai_news_status_options()))));
    if (!$postFilters['status']) {
        $postFilters['status'] = array_keys(ai_news_status_options());
    }
    $redirectQuery = $postFilters;
    $redirectQuery['page'] = max(1, (int) ($_POST['page'] ?? 1));

    if ($action === 'crawl') {
        $result = crawl_pending_ai_news_files();
        $_SESSION['ai_news_flash'] = [
            'type' => 'success',
            'message' => sprintf('크롤링 완료. %d건 처리, %d건 성공, %d건 실패.', $result['total'], $result['success'], $result['failed']),
        ];
        redirect('/admin/news/ai_news.php?' . http_build_query($redirectQuery));
    }

    if ($action === 'publish') {
        $ids = $_POST['news_ids'] ?? [];
        $ids = is_array($ids) ? $ids : [];
        $published = update_ai_news_status_bulk($ids, 'Y');

        $_SESSION['ai_news_flash'] = [
            'type' => $published > 0 ? 'success' : 'warning',
            'message' => $published > 0 ? sprintf('%d건 게시 확정했습니다.', $published) : '게시 확정할 항목을 선택해 주세요.',
        ];
        redirect('/admin/news/ai_news.php?' . http_build_query($redirectQuery));
    }

    if ($action === 'delete') {
        $ids = $_POST['news_ids'] ?? [];
        $ids = is_array($ids) ? $ids : [];
        $deleted = delete_ai_news_bulk($ids);

        $_SESSION['ai_news_flash'] = [
            'type' => $deleted > 0 ? 'success' : 'warning',
            'message' => $deleted > 0 ? sprintf('%d건 삭제 처리했습니다.', $deleted) : '삭제할 항목을 선택해 주세요.',
        ];
        redirect('/admin/news/ai_news.php?' . http_build_query($redirectQuery));
    }
}

$newsList = array_values(array_filter(all_ai_news(), function (array $news) use ($filters): bool {
    if (($news['delete_flg'] ?? 'N') === 'Y') {
        return false;
    }

    $newsDate = substr((string) ($news['published_at'] ?? $news['reg_dtm'] ?? ''), 0, 10);
    if ($filters['date_from'] !== '' && $newsDate < $filters['date_from']) {
        return false;
    }
    if ($filters['date_to'] !== '' && $newsDate > $filters['date_to']) {
        return false;
    }

    $status = strtoupper((string) ($news['status'] ?? 'Y'));
    if (!in_array($status, $filters['status'], true)) {
        return false;
    }

    $keyword = $filters['keyword'];
    if ($keyword === '') {
        return true;
    }

    $title = (string) ($news['title'] ?? '');
    $tags = implode(', ', $news['tags'] ?? []);
    $status = (string) ($news['status'] ?? 'Y');
    $statusLabel = ai_news_status_label($status);

    if ($filters['keyword_type'] === 'title') {
        return ai_news_text_contains($title, $keyword);
    }

    if ($filters['keyword_type'] === 'tag') {
        return ai_news_text_contains($tags, $keyword);
    }

    if ($filters['keyword_type'] === 'status') {
        return ai_news_text_contains($status, $keyword) || ai_news_text_contains($statusLabel, $keyword);
    }

    return ai_news_text_contains($title, $keyword)
        || ai_news_text_contains($tags, $keyword)
        || ai_news_text_contains($status, $keyword)
        || ai_news_text_contains($statusLabel, $keyword);
}));

$perPage = 10;
$totalCount = count($newsList);
$totalPages = max(1, (int) ceil($totalCount / $perPage));
$currentPage = max(1, (int) ($_GET['page'] ?? 1));
$currentPage = min($currentPage, $totalPages);
$pageOffset = ($currentPage - 1) * $perPage;
$pagedNews = array_slice($newsList, $pageOffset, $perPage);

function ai_news_page_query(array $filters, int $page): string
{
    return http_build_query([
        'date_from' => $filters['date_from'],
        'date_to' => $filters['date_to'],
        'keyword_type' => $filters['keyword_type'],
        'keyword' => $filters['keyword'],
        'status' => $filters['status'],
        'page' => $page,
    ]);
}

$pageTitle = 'AI News';
$flash = $_SESSION['ai_news_flash'] ?? null;
unset($_SESSION['ai_news_flash']);
require __DIR__ . '/../../partials/header.php';
?>

<section class="mb-4">
    <div>
        <h1>AI News</h1>
        <p class="text-secondary mb-0">AI 뉴스 JSON과 수동 등록 원고를 함께 관리합니다.</p>
    </div>
</section>

<?php if (is_array($flash) && ($flash['message'] ?? '') !== ''): ?>
    <div class="alert <?= ($flash['type'] ?? '') === 'success' ? 'alert-success' : 'alert-warning' ?>">
        <?= e((string) ($flash['message'] ?? '')) ?>
    </div>
<?php endif; ?>

<form class="card shadow-sm mb-4" method="get">
    <div class="card-body">
        <div class="search-inline-grid">
            <div class="search-inline-field">
                <div class="search-inline-label">게시일자</div>
                <div class="search-inline-control search-inline-range">
                    <input class="form-control" type="date" name="date_from" value="<?= e($filters['date_from']) ?>">
                    <span class="search-date-separator">~</span>
                    <input class="form-control" type="date" name="date_to" value="<?= e($filters['date_to']) ?>">
                </div>
            </div>
            <div class="search-inline-field">
                <div class="search-inline-label">상태</div>
                <div class="search-inline-control search-inline-checkboxes">
                    <?php foreach (ai_news_status_options() as $value => $label): ?>
                        <label class="form-check form-check-inline mb-0 search-checklist-item">
                            <input class="form-check-input" type="checkbox" name="status[]" value="<?= e($value) ?>" <?= in_array($value, $filters['status'], true) ? 'checked' : '' ?>>
                            <span class="form-check-label"><?= e($label) ?></span>
                        </label>
                    <?php endforeach; ?>
                </div>
            </div>
            <div class="search-inline-field search-inline-field-pair">
                <div class="search-inline-pair">
                    <div class="search-inline-subfield">
                        <div class="search-inline-label">검색조건</div>
                        <div class="search-inline-control search-inline-select">
                            <select class="form-select" name="keyword_type">
                                <option value="all" <?= $filters['keyword_type'] === 'all' ? 'selected' : '' ?>>전체</option>
                                <option value="title" <?= $filters['keyword_type'] === 'title' ? 'selected' : '' ?>>제목</option>
                                <option value="tag" <?= $filters['keyword_type'] === 'tag' ? 'selected' : '' ?>>태그</option>
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
                        <a class="btn btn-outline-secondary" href="/admin/news/ai_news.php">초기화</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</form>

<form method="post">
    <input type="hidden" name="date_from" value="<?= e($filters['date_from']) ?>">
    <input type="hidden" name="date_to" value="<?= e($filters['date_to']) ?>">
    <input type="hidden" name="keyword_type" value="<?= e($filters['keyword_type']) ?>">
    <input type="hidden" name="keyword" value="<?= e($filters['keyword']) ?>">
    <input type="hidden" name="page" value="<?= e((string) $currentPage) ?>">
    <?php foreach ($filters['status'] as $statusValue): ?>
        <input type="hidden" name="status[]" value="<?= e($statusValue) ?>">
    <?php endforeach; ?>

    <div class="list-action-bar d-flex justify-content-end gap-2 mb-3">
        <button class="btn btn-outline-secondary" type="submit" name="action" value="crawl">크롤링</button>
        <a class="btn btn-primary" href="/admin/news/ai_news_edit.php">등록</a>
        <button class="btn btn-success" type="submit" name="action" value="publish" onclick="return confirm('선택한 게시물을 게시 확정하시겠습니까?');">게시 확정</button>
        <button class="btn btn-danger" type="submit" name="action" value="delete" onclick="return confirm('선택한 뉴스를 삭제 처리하시겠습니까?');">삭제</button>
    </div>

    <div class="content-section-title d-flex justify-content-between align-items-end mb-2">
        <h2 class="h5 mb-0">AI News 목록</h2>
        <span class="text-secondary small"><?= $totalCount ?>건</span>
    </div>

    <div class="card shadow-sm">
        <div class="card-body px-0">
        <?php if (!$pagedNews): ?>
            <p class="text-secondary mb-0">검색 조건에 맞는 AI News가 없습니다.</p>
        <?php else: ?>
            <div class="table-responsive">
            <table class="table align-middle">
                <thead>
                <tr>
                    <th style="width: 44px;"><input type="checkbox" id="checkAll"></th>
                    <th>순번</th>
                    <th>제목</th>
                    <th>카테고리</th>
                    <th>태그</th>
                    <th>상태</th>
                    <th>게시일</th>
                    <th>등록일</th>
                    <th>수정일</th>
                </tr>
                </thead>
                <tbody>
                <?php foreach ($pagedNews as $index => $news): ?>
                    <?php $newsSeq = (int) ($news['news_seq'] ?? 0); ?>
                    <tr>
                        <td><input type="checkbox" name="news_ids[]" value="<?= e((string) $newsSeq) ?>" class="news-row-check"></td>
                        <td><?= $pageOffset + $index + 1 ?></td>
                        <td><a class="table-link" href="/admin/news/ai_news_view.php?id=<?= e((string) $newsSeq) ?>"><?= e($news['title'] ?? '') ?></a></td>
                        <td><?= e($news['category'] ?? '') ?></td>
                        <td><?= e(ai_news_tags_to_text($news['tags'] ?? [])) ?></td>
                        <td><span class="badge <?= e(ai_news_status_badge_class($news['status'] ?? 'Y')) ?>"><?= e(ai_news_status_label($news['status'] ?? 'Y')) ?></span></td>
                        <td><?= e(display_date($news['published_at'] ?? '')) ?></td>
                        <td><?= e(display_date($news['reg_dtm'] ?? '')) ?></td>
                        <td><?= e(display_date($news['mod_dtm'] ?? '')) ?></td>
                    </tr>
                <?php endforeach; ?>
                </tbody>
            </table>
            </div>
        <?php endif; ?>

        <?php if ($totalPages > 1): ?>
            <?php $startPage = max(1, $currentPage - 2); $endPage = min($totalPages, $startPage + 4); $startPage = max(1, $endPage - 4); ?>
            <nav class="px-3 pb-3" aria-label="페이지 이동">
                <ul class="pagination mb-0 justify-content-center">
                    <li class="page-item <?= $currentPage <= 1 ? 'disabled' : '' ?>">
                        <a class="page-link" href="/admin/news/ai_news.php?<?= e(ai_news_page_query($filters, max(1, $currentPage - 1))) ?>">이전</a>
                    </li>
                    <?php for ($page = $startPage; $page <= $endPage; $page++): ?>
                        <li class="page-item <?= $page === $currentPage ? 'active' : '' ?>">
                            <a class="page-link" href="/admin/news/ai_news.php?<?= e(ai_news_page_query($filters, $page)) ?>"><?= $page ?></a>
                        </li>
                    <?php endfor; ?>
                    <li class="page-item <?= $currentPage >= $totalPages ? 'disabled' : '' ?>">
                        <a class="page-link" href="/admin/news/ai_news.php?<?= e(ai_news_page_query($filters, min($totalPages, $currentPage + 1))) ?>">다음</a>
                    </li>
                </ul>
            </nav>
        <?php endif; ?>
        </div>
    </div>
</form>

<script>
document.addEventListener('DOMContentLoaded', function () {
    const checkAll = document.getElementById('checkAll');
    const rowChecks = Array.from(document.querySelectorAll('.news-row-check'));

    if (checkAll) {
        checkAll.addEventListener('change', function () {
            rowChecks.forEach(function (checkbox) {
                checkbox.checked = checkAll.checked;
            });
        });
    }
});
</script>

<?php require __DIR__ . '/../../partials/footer.php'; ?>
