<?php

declare(strict_types=1);

require_once __DIR__ . '/../core/auth.php';
require_once __DIR__ . '/../../lib/storage.php';

require_admin();

$newsSeq = trim((string) ($_GET['id'] ?? $_POST['id'] ?? ''));
$news = $newsSeq !== '' ? find_ai_news($newsSeq) : null;

if (!$news) {
    http_response_code(404);
    exit;
}

if (request_method() === 'POST') {
    $action = (string) ($_POST['action'] ?? '');
    if ($action === 'delete') {
        delete_ai_news($newsSeq);
        $_SESSION['ai_news_flash'] = [
            'type' => 'success',
            'message' => '선택한 뉴스를 삭제 처리했습니다.',
        ];
        redirect('/admin/news/ai_news.php');
    }

    if ($action === 'publish') {
        update_ai_news_status_bulk([$newsSeq], 'Y');
        $_SESSION['ai_news_flash'] = [
            'type' => 'success',
            'message' => '게시 확정했습니다.',
        ];
        redirect('/admin/news/ai_news_view.php?id=' . (string) ($news['news_seq'] ?? 0));
    }
}

$pageTitle = 'AI News 상세';
require __DIR__ . '/../../partials/header.php';

$tags = $news['tags'] ?? [];
$sources = $news['sources'] ?? [];
?>

<section class="mb-4">
    <div>
        <h1>AI News 상세</h1>
        <p class="text-secondary mb-0"><?= e(display_date($news['published_at'] ?? '')) ?></p>
    </div>
</section>

<?php if (($news['crawl_error'] ?? '') !== ''): ?>
    <div class="alert alert-danger"><?= e($news['crawl_error'] ?? '') ?></div>
<?php endif; ?>

<div class="card shadow-sm mb-4">
    <div class="card-body">
        <div class="row g-3">
            <div class="col-12 col-md-4">
                <div class="small text-secondary">제목</div>
                <div class="fw-bold"><?= e($news['title'] ?? '') ?></div>
            </div>
            <div class="col-12 col-md-4">
                <div class="small text-secondary">슬러그</div>
                <div class="fw-bold"><?= e($news['slug'] ?? '') ?></div>
            </div>
            <div class="col-12 col-md-4">
                <div class="small text-secondary">카테고리</div>
                <div class="fw-bold"><?= e($news['category'] ?? '') ?></div>
            </div>
            <div class="col-12 col-md-4">
                <div class="small text-secondary">상태</div>
                <div><span class="badge <?= e(ai_news_status_badge_class($news['status'] ?? 'Y')) ?>"><?= e(ai_news_status_label($news['status'] ?? 'Y')) ?></span></div>
            </div>
            <div class="col-12 col-md-4">
                <div class="small text-secondary">게시일</div>
                <div class="fw-bold"><?= e(display_date($news['published_at'] ?? '')) ?></div>
            </div>
            <div class="col-12 col-md-4">
                <div class="small text-secondary">삭제 여부</div>
                <div class="fw-bold"><?= e(($news['delete_flg'] ?? 'N') === 'Y' ? '삭제' : '정상') ?></div>
            </div>
            <div class="col-12">
                <div class="small text-secondary">태그</div>
                <div><?= $tags ? e(ai_news_tags_to_text($tags)) : '<span class="text-secondary">없음</span>' ?></div>
            </div>
            <div class="col-12">
                <div class="small text-secondary">출처</div>
                <?php if (!$sources): ?>
                    <div class="text-secondary">없음</div>
                <?php else: ?>
                    <ul class="mb-0 ps-3">
                        <?php foreach ($sources as $source): ?>
                            <?php
                            $sourceTitle = trim((string) ($source['title'] ?? ''));
                            $sourceName = trim((string) ($source['source'] ?? ''));
                            $sourceUrl = trim((string) ($source['url'] ?? ''));
                            ?>
                            <li>
                                <?php if ($sourceUrl !== ''): ?>
                                    <a class="table-link" href="<?= e($sourceUrl) ?>" target="_blank" rel="noreferrer noopener"><?= e($sourceTitle !== '' ? $sourceTitle : $sourceUrl) ?></a>
                                    <?php if ($sourceName !== ''): ?>
                                        <a class="table-link ms-1" href="<?= e($sourceUrl) ?>" target="_blank" rel="noreferrer noopener">· <?= e($sourceName) ?></a>
                                    <?php endif; ?>
                                <?php else: ?>
                                    <span class="fw-bold"><?= e($sourceTitle) ?></span>
                                    <?php if ($sourceName !== ''): ?>
                                        <span class="text-secondary">· <?= e($sourceName) ?></span>
                                    <?php endif; ?>
                                <?php endif; ?>
                            </li>
                        <?php endforeach; ?>
                    </ul>
                <?php endif; ?>
            </div>
            <div class="col-12">
                <div class="small text-secondary">요약</div>
                <div><?= nl2br(e($news['summary'] ?? '')) ?></div>
            </div>
            <div class="col-12">
                <div class="small text-secondary">본문</div>
                <div class="border rounded p-3 bg-light" style="white-space: pre-wrap;"><?= e($news['content_markdown'] ?? '') ?></div>
            </div>
            <div class="col-12 col-md-4">
                <div class="small text-secondary">등록일</div>
                <div class="fw-bold"><?= e(display_date($news['reg_dtm'] ?? '')) ?></div>
            </div>
            <div class="col-12 col-md-4">
                <div class="small text-secondary">수정일</div>
                <div class="fw-bold"><?= e(display_date($news['mod_dtm'] ?? '')) ?></div>
            </div>
            <div class="col-12 col-md-4">
                <div class="small text-secondary">등록자 / 수정자</div>
                <div class="fw-bold"><?= e(($news['reg_id'] ?? '') . ' / ' . ($news['mod_id'] ?? '')) ?></div>
            </div>
            <?php if (($news['source_file'] ?? '') !== ''): ?>
                <div class="col-12">
                    <div class="small text-secondary">원본 파일</div>
                    <div class="fw-bold"><?= e($news['source_file'] ?? '') ?></div>
                </div>
            <?php endif; ?>
        </div>
    </div>
</div>

<div class="detail-action-bar mt-4">
    <div>
        <a class="btn btn-outline-secondary" href="/admin/news/ai_news.php">목록</a>
    </div>
    <div class="d-flex gap-2">
        <a class="btn btn-success" href="/admin/news/ai_news_edit.php?id=<?= e((string) ($news['news_seq'] ?? 0)) ?>">수정하기</a>
        <?php if (($news['status'] ?? 'Y') === 'P'): ?>
            <form class="m-0" method="post">
                <input type="hidden" name="id" value="<?= e((string) ($news['news_seq'] ?? 0)) ?>">
                <input type="hidden" name="action" value="publish">
                <button class="btn btn-success" type="submit" onclick="return confirm('이 게시물을 게시 확정하시겠습니까?');">게시 확정</button>
            </form>
        <?php endif; ?>
        <form class="m-0" method="post" onsubmit="return confirm('이 뉴스를 삭제 처리하시겠습니까?');">
            <input type="hidden" name="id" value="<?= e((string) ($news['news_seq'] ?? 0)) ?>">
            <input type="hidden" name="action" value="delete">
            <button class="btn btn-danger" type="submit">삭제</button>
        </form>
    </div>
</div>

<?php require __DIR__ . '/../../partials/footer.php'; ?>
