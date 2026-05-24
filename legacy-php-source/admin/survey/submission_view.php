<?php

declare(strict_types=1);

require_once __DIR__ . '/../core/auth.php';
require_once __DIR__ . '/../../lib/storage.php';
require_once __DIR__ . '/../core/submission_status.php';

require_admin();

$submissionId = trim((string) ($_GET['id'] ?? $_POST['id'] ?? ''));
$submission = find_submission($submissionId);
if (!$submission) {
    http_response_code(404);
}

$pageTitle = '설문 이력 상세';
require __DIR__ . '/../../partials/header.php';
?>

<section class="mb-4">
    <div>
        <h1>설문 이력 상세</h1>
        <p class="text-secondary mb-0"><?= $submission ? e(display_date($submission['submitted_at'] ?? '')) : '이력을 찾을 수 없습니다.' ?></p>
    </div>
</section>

<?php if (!$submission): ?>
    <div class="alert alert-light border">설문 이력을 찾을 수 없습니다.</div>
<?php else: ?>
    <section class="row g-3">
        <div class="col-12 col-xl-5">
            <div class="card shadow-sm h-100">
            <div class="card-body">
            <h2 class="h5">인적사항</h2>
            <dl class="detail-list">
                <dt>설문</dt><dd><?= e($submission['form_title'] ?? '') ?></dd>
                <dt>처리상태</dt><dd><span class="badge text-bg-info"><?= e(submission_status_text($submission['status'] ?? 'new')) ?></span></dd>
                <dt>이름</dt><dd><?= e($submission['person']['name'] ?? '') ?></dd>
                <dt>연락처</dt><dd><?= e($submission['person']['phone'] ?? '') ?></dd>
                <dt>이메일</dt><dd><?= e($submission['person']['email'] ?? '') ?></dd>
                <dt>생년월일</dt><dd><?= e($submission['person']['birthdate'] ?? '') ?></dd>
                <dt>주소</dt><dd><?= e($submission['person']['address'] ?? '') ?></dd>
                <dt>IP</dt><dd><?= e($submission['ip'] ?? '') ?></dd>
            </dl>
            </div>
            </div>
        </div>
        <div class="col-12 col-xl-7">
            <div class="card shadow-sm h-100">
            <div class="card-body">
            <h2 class="h5">제출 내용</h2>
            <dl class="detail-list">
                <?php foreach (($submission['answers'] ?? []) as $answer): ?>
                    <dt><?= e($answer['label'] ?? '') ?></dt>
                    <dd>
                        <?php $value = $answer['value'] ?? ''; ?>
                        <?= e(is_array($value) ? implode(', ', $value) : (string) $value) ?>
                    </dd>
                <?php endforeach; ?>
            </dl>
            </div>
            </div>
        </div>
    </section>
    <div class="detail-action-bar mt-4">
        <div>
            <a class="btn btn-outline-secondary" href="/admin/survey/submissions.php">목록</a>
        </div>
        <div></div>
    </div>
<?php endif; ?>

<?php require __DIR__ . '/../../partials/footer.php'; ?>
