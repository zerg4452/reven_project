<?php

declare(strict_types=1);

require_once __DIR__ . '/../core/auth.php';
require_once __DIR__ . '/../../lib/storage.php';

require_admin();

$modSeq = trim((string) ($_GET['id'] ?? ''));
$log = $modSeq !== '' ? find_admin_change_log($modSeq) : null;

if (!$log) {
    http_response_code(404);
    exit;
}

$pageTitle = '관리자 수정이력 상세';
require __DIR__ . '/../../partials/header.php';

$details = is_array($log['details'] ?? null) ? $log['details'] : [];
$isCreate = ($log['action'] ?? '') === 'create';
$targetAdmin = ($log['target_adm_seq'] ?? 0) > 0 ? find_admin((string) ($log['target_adm_seq'] ?? 0)) : null;
$changerAdmin = ($log['changer_adm_seq'] ?? 0) > 0 ? find_admin((string) ($log['changer_adm_seq'] ?? 0)) : null;
?>

<section class="mb-4">
    <div>
        <h1>관리자 수정이력 상세</h1>
        <p class="text-secondary mb-0">관리자 계정 변경 내역을 확인합니다.</p>
    </div>
</section>

<div class="card shadow-sm mb-4">
    <div class="card-body">
        <div class="row g-3">
            <div class="col-12 col-md-3">
                <div class="small text-secondary">변경 대상 아이디</div>
                <div class="fw-bold"><?= e($log['target_adm_login_id'] ?? ($targetAdmin['login_id'] ?? '')) ?></div>
            </div>
            <div class="col-12 col-md-3">
                <div class="small text-secondary">변경 대상 이름</div>
                <div class="fw-bold"><?= e($log['target_adm_name'] ?? ($targetAdmin['name'] ?? '')) ?></div>
            </div>
            <div class="col-12 col-md-3">
                <div class="small text-secondary">변경자 아이디</div>
                <div class="fw-bold"><?= e($log['changer_adm_login_id'] ?? ($changerAdmin['login_id'] ?? '')) ?></div>
            </div>
            <div class="col-12 col-md-3">
                <div class="small text-secondary">변경자 이름</div>
                <div class="fw-bold"><?= e($log['changer_adm_name'] ?? ($changerAdmin['name'] ?? '')) ?></div>
            </div>
            <div class="col-12 col-md-3">
                <div class="small text-secondary">변경시각</div>
                <div class="fw-bold"><?= e(display_datetime($log['changed_at'] ?? '')) ?></div>
            </div>
            <div class="col-12 col-md-3">
                <div class="small text-secondary">순번</div>
                <div class="fw-bold"><?= e((string) ($log['mod_seq'] ?? '')) ?></div>
            </div>
            <div class="col-12 col-md-3">
                <div class="small text-secondary">구분</div>
                <div class="fw-bold"><?= e($isCreate ? '등록' : '수정') ?></div>
            </div>
        </div>
    </div>
</div>

<div class="card shadow-sm">
    <div class="card-body">
        <h2 class="h5 mb-3">변경된 내용</h2>
        <?php if (!$details): ?>
            <p class="text-secondary mb-0">변경 내용이 없습니다.</p>
        <?php else: ?>
            <div class="table-responsive">
                <table class="table align-middle">
                    <thead>
                    <tr><th>항목</th><th>변경 전</th><th>변경 후</th></tr>
                    </thead>
                    <tbody>
                    <?php foreach ($details as $detail): ?>
                        <tr>
                            <td><?= e((string) ($detail['column_label'] ?? '')) ?></td>
                            <td><?= e((string) ($detail['before_value'] ?? '')) ?></td>
                            <td><?= e((string) ($detail['after_value'] ?? '')) ?></td>
                        </tr>
                    <?php endforeach; ?>
                    </tbody>
                </table>
            </div>
        <?php endif; ?>
    </div>
</div>

<div class="detail-action-bar mt-4">
    <div>
        <a class="btn btn-outline-secondary" href="/admin/management/change_logs.php">목록</a>
    </div>
    <div></div>
</div>

<?php require __DIR__ . '/../../partials/footer.php'; ?>
