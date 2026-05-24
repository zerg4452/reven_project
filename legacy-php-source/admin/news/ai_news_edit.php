<?php

declare(strict_types=1);

require_once __DIR__ . '/../core/auth.php';
require_once __DIR__ . '/../../lib/storage.php';

require_admin();

$newsSeq = trim((string) ($_GET['id'] ?? $_POST['id'] ?? ''));
$news = $newsSeq !== '' ? find_ai_news($newsSeq) : null;
$errors = [];

if ($newsSeq !== '' && !$news) {
    http_response_code(404);
    exit;
}

if (request_method() === 'POST') {
    $title = trim((string) ($_POST['title'] ?? ''));
    $slug = trim((string) ($_POST['slug'] ?? ''));
    $category = trim((string) ($_POST['category'] ?? ''));
    $summary = trim((string) ($_POST['summary'] ?? ''));
    $contentMarkdown = trim((string) ($_POST['content_markdown'] ?? ''));
    $publishedAt = trim((string) ($_POST['published_at'] ?? ''));
    $status = strtoupper(trim((string) ($_POST['status'] ?? 'Y')));
    $tagsText = (string) ($_POST['tags_text'] ?? '');
    $sourcesText = (string) ($_POST['sources_text'] ?? '');

    if ($title === '') {
        $errors[] = '제목을 입력해주세요.';
    }
    if ($slug === '') {
        $errors[] = '슬러그를 입력해주세요.';
    }
    if ($summary === '') {
        $errors[] = '요약을 입력해주세요.';
    }
    if ($contentMarkdown === '') {
        $errors[] = '본문을 입력해주세요.';
    }
    if (!in_array($status, ['N', 'P', 'Y', 'E'], true)) {
        $status = 'Y';
    }

    $existingBySlug = $slug !== '' ? find_ai_news_by_slug($slug) : null;
    if ($news) {
        if ($existingBySlug && (int) ($existingBySlug['news_seq'] ?? 0) !== (int) $newsSeq) {
            $errors[] = '이미 사용 중인 슬러그입니다.';
        }
    } elseif ($existingBySlug) {
        $errors[] = '이미 사용 중인 슬러그입니다.';
    }

    if (!$errors) {
        $actor = current_admin_record() ?? find_admin_by_login_id('admin') ?? [];
        $actorLoginId = (string) ($actor['login_id'] ?? ($_SESSION['admin_login_id'] ?? 'system'));
        $now = current_time();
        $payload = [
            'news_seq' => $news ? (int) ($news['news_seq'] ?? 0) : 0,
            'slug' => $slug,
            'title' => $title,
            'category' => $category !== '' ? $category : 'AI News',
            'summary' => $summary,
            'content_markdown' => $contentMarkdown,
            'tags' => ai_news_tags_from_text($tagsText),
            'sources' => ai_news_sources_from_text($sourcesText),
            'published_at' => $publishedAt !== '' ? $publishedAt . ' 00:00:00' : $now,
            'status' => $status,
            'delete_flg' => (string) ($news['delete_flg'] ?? 'N'),
            'source_file' => (string) ($news['source_file'] ?? ''),
            'crawl_error' => '',
            'reg_dtm' => (string) ($news['reg_dtm'] ?? $now),
            'reg_id' => (string) ($news['reg_id'] ?? $actorLoginId),
            'mod_dtm' => $now,
            'mod_id' => $actorLoginId,
        ];

        if ($news) {
            update_ai_news($payload);
            $savedId = (int) ($news['news_seq'] ?? 0);
        } else {
            $savedId = insert_ai_news($payload);
        }

        redirect('/admin/news/ai_news_view.php?id=' . $savedId);
    }
}

$editingTags = $_POST['tags_text'] ?? ai_news_tags_to_text($news['tags'] ?? []);
$editingSources = $_POST['sources_text'] ?? ai_news_sources_to_text($news['sources'] ?? []);
$editingPublishedAt = $_POST['published_at'] ?? display_date($news['published_at'] ?? current_time());
$editingStatus = strtoupper((string) ($_POST['status'] ?? ($news['status'] ?? 'Y')));
if (!in_array($editingStatus, ['N', 'P', 'Y', 'E'], true)) {
    $editingStatus = 'Y';
}

$pageTitle = $news ? 'AI News 수정' : 'AI News 등록';
require __DIR__ . '/../../partials/header.php';
?>

<section class="mb-4">
    <div>
        <h1><?= $news ? 'AI News 수정' : 'AI News 등록' ?></h1>
        <p class="text-secondary mb-0">AI 뉴스 원고를 등록하고 수정합니다.</p>
    </div>
</section>

<?php if ($errors): ?>
    <div class="alert alert-danger">
        <?php foreach ($errors as $error): ?><p><?= e($error) ?></p><?php endforeach; ?>
    </div>
<?php endif; ?>

<form class="form-stack" method="post">
    <div class="card shadow-sm">
    <div class="card-body">
    <input type="hidden" name="id" value="<?= e($newsSeq) ?>">
    <div class="row g-3 mb-4">
        <div class="col-12 col-md-6">
            <label class="form-label">제목 <span class="text-danger">*</span></label>
            <input class="form-control" name="title" value="<?= e((string) ($_POST['title'] ?? $news['title'] ?? '')) ?>" required>
        </div>
        <div class="col-12 col-md-6">
            <label class="form-label">슬러그 <span class="text-danger">*</span></label>
            <input class="form-control" name="slug" value="<?= e((string) ($_POST['slug'] ?? $news['slug'] ?? '')) ?>" required>
        </div>
        <div class="col-12 col-md-4">
            <label class="form-label">카테고리</label>
            <input class="form-control" name="category" value="<?= e((string) ($_POST['category'] ?? $news['category'] ?? 'AI News')) ?>">
        </div>
        <div class="col-12 col-md-4">
            <label class="form-label">게시일</label>
            <input class="form-control" type="date" name="published_at" value="<?= e((string) $editingPublishedAt) ?>">
        </div>
        <div class="col-12 col-md-4">
            <label class="form-label">상태</label>
            <select class="form-select" name="status">
                <?php foreach (ai_news_status_options() as $value => $label): ?>
                    <option value="<?= e($value) ?>" <?= $editingStatus === $value ? 'selected' : '' ?>><?= e($label) ?></option>
                <?php endforeach; ?>
            </select>
        </div>
        <div class="col-12">
            <label class="form-label">태그</label>
            <textarea class="form-control" name="tags_text" rows="2" placeholder="콤마로 구분"><?= e((string) $editingTags) ?></textarea>
        </div>
        <div class="col-12">
            <label class="form-label">출처</label>
            <textarea class="form-control" name="sources_text" rows="4" placeholder="한 줄에 하나씩 제목 | 출처 | URL 형태로 입력"><?= e((string) $editingSources) ?></textarea>
        </div>
        <div class="col-12">
            <label class="form-label">요약 <span class="text-danger">*</span></label>
            <textarea class="form-control" name="summary" rows="3" required><?= e((string) ($_POST['summary'] ?? $news['summary'] ?? '')) ?></textarea>
        </div>
        <div class="col-12">
            <label class="form-label">본문(markdown) <span class="text-danger">*</span></label>
            <textarea class="form-control" name="content_markdown" rows="14" required><?= e((string) ($_POST['content_markdown'] ?? $news['content_markdown'] ?? '')) ?></textarea>
        </div>
    </div>
    </div>
    </div>

    <div class="detail-action-bar mt-4">
        <div>
            <a class="btn btn-outline-secondary" href="<?= $news ? '/admin/news/ai_news_view.php?id=' . e((string) ($news['news_seq'] ?? 0)) : '/admin/news/ai_news.php' ?>">목록</a>
        </div>
        <div>
            <?php if ($news): ?>
                <button class="btn btn-success" type="submit">수정하기</button>
            <?php else: ?>
                <button class="btn btn-primary" type="submit">등록하기</button>
            <?php endif; ?>
        </div>
    </div>
</form>

<?php require __DIR__ . '/../../partials/footer.php'; ?>
