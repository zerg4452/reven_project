<?php

declare(strict_types=1);

require_once __DIR__ . '/core/auth.php';
require_once __DIR__ . '/../lib/storage.php';
require_once __DIR__ . '/core/submission_status.php';

require_admin();

$forms = all_forms();
$submissions = all_submissions();
$dashboardPanelCards = dashboard_panel_cards();
$pageTitle = '관리자';
require __DIR__ . '/../partials/header.php';
?>

<section class="mb-4">
    <div>
        <h1>관리자</h1>
        <p class="text-secondary mb-0">설문과 제출 현황을 확인합니다.</p>
    </div>
</section>

<section class="row g-3 mb-4">
    <div class="col-12 col-md-4"><div class="card admin-dashboard-card"><div class="card-body"><strong class="stat-number"><?= count($forms) ?></strong><span class="text-secondary">등록 설문</span></div></div></div>
    <div class="col-12 col-md-4"><div class="card admin-dashboard-card"><div class="card-body"><strong class="stat-number"><?= count(active_forms()) ?></strong><span class="text-secondary">사용중 설문</span></div></div></div>
    <div class="col-12 col-md-4"><div class="card admin-dashboard-card"><div class="card-body"><strong class="stat-number"><?= count($submissions) ?></strong><span class="text-secondary">전체 제출</span></div></div></div>
</section>

<section class="row g-3 mb-4">
    <?php foreach ($dashboardPanelCards as $panelCard): ?>
        <div class="col-12">
            <div class="card admin-dashboard-card h-100">
                <div class="card-header bg-white d-flex justify-content-between align-items-center">
                    <h2 class="h5 mb-0"><?= e($panelCard['title']) ?></h2>
                    <a class="dashboard-more-link" href="<?= e($panelCard['href']) ?>">more</a>
                </div>
                <div class="card-body">
                    <div class="dashboard-status-summary mb-3">
                        <span class="badge text-bg-warning">처리중 <?= (int) ($panelCard['counts']['P'] ?? 0) ?></span>
                        <span class="badge text-bg-success">완료 <?= (int) ($panelCard['counts']['Y'] ?? 0) ?></span>
                    </div>
                    <?php if (!$panelCard['items']): ?>
                        <p class="text-secondary mb-0">노출할 뉴스가 없습니다.</p>
                    <?php else: ?>
                        <div class="list-group list-group-flush dashboard-news-list">
                            <?php foreach ($panelCard['items'] as $news): ?>
                                <?php $newsSeq = (int) ($news['news_seq'] ?? 0); ?>
                                <a class="list-group-item list-group-item-action px-0 dashboard-news-item" href="/admin/news/ai_news_view.php?id=<?= e((string) $newsSeq) ?>">
                                    <span class="dashboard-news-title"><?= e($news['title'] ?? '') ?></span>
                                    <span class="dashboard-news-meta">
                                        <span><?= e($news['category'] ?? '') ?></span>
                                        <span class="badge <?= e(ai_news_status_badge_class($news['status'] ?? 'Y')) ?>"><?= e(ai_news_status_label($news['status'] ?? 'Y')) ?></span>
                                        <span><?= e(display_date($news['published_at'] ?? $news['reg_dtm'] ?? '')) ?></span>
                                    </span>
                                </a>
                            <?php endforeach; ?>
                        </div>
                    <?php endif; ?>
                </div>
            </div>
        </div>
    <?php endforeach; ?>
</section>

<section class="row g-3">
    <div class="col-12">
        <div class="card admin-dashboard-card h-100">
        <div class="card-header bg-white d-flex justify-content-between align-items-center">
            <h2 class="h5 mb-0">최근 설문 이력</h2>
            <a href="/admin/survey/submissions.php">전체 보기</a>
        </div>
        <div class="card-body">
        <?php if (!$submissions): ?>
            <p class="text-secondary mb-0">설문 이력이 없습니다.</p>
        <?php else: ?>
            <div class="list-group list-group-flush">
                <?php foreach (array_slice($submissions, 0, 8) as $submission): ?>
                    <a class="list-group-item list-group-item-action px-0" href="/admin/survey/submission_view.php?id=<?= e($submission['id']) ?>">
                        <strong><?= e($submission['person']['name'] ?? '') ?></strong>
                        <span class="d-block text-secondary small"><?= e($submission['form_title'] ?? '') ?> · <?= e(display_date($submission['submitted_at'] ?? '')) ?> · <?= e(submission_status_text($submission['status'] ?? 'new')) ?></span>
                    </a>
                <?php endforeach; ?>
            </div>
        <?php endif; ?>
        </div>
        </div>
    </div>
</section>

<?php require __DIR__ . '/../partials/footer.php'; ?>
