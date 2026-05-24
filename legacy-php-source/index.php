<?php

declare(strict_types=1);

require_once __DIR__ . '/lib/storage.php';
require_once __DIR__ . '/lib/helpers.php';

$forms = active_forms();
$pageTitle = '접수 가능한 설문';
require __DIR__ . '/partials/header.php';
?>

<section class="mb-4">
    <h1>접수 가능한 설문</h1>
    <p class="text-secondary mb-0">관리자가 등록한 설문을 선택해 인적사항과 함께 접수할 수 있습니다.</p>
</section>

<?php if (!$forms): ?>
    <div class="alert alert-light border">현재 접수 가능한 설문이 없습니다.</div>
<?php else: ?>
    <div class="row g-3">
        <?php foreach ($forms as $form): ?>
            <div class="col-12 col-md-6 col-xl-4">
                <article class="card h-100 shadow-sm">
                    <div class="card-body">
                        <span class="badge text-bg-success mb-3"><?= e($form['status'] === 'active' ? '접수중' : '비활성') ?></span>
                        <h2 class="h5 card-title"><?= e($form['title'] ?? '') ?></h2>
                        <p class="card-text text-secondary"><?= e($form['description'] ?? '내용을 작성해 제출해주세요.') ?></p>
                        <a class="btn btn-primary" href="/submit.php?form=<?= e($form['id']) ?>">작성하기</a>
                    </div>
                </article>
            </div>
        <?php endforeach; ?>
    </div>
<?php endif; ?>

<?php require __DIR__ . '/partials/footer.php'; ?>
