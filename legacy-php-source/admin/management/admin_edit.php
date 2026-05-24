<?php

declare(strict_types=1);

require_once __DIR__ . '/../core/auth.php';
require_once __DIR__ . '/../../lib/storage.php';

require_admin();

$admSeq = trim((string) ($_GET['id'] ?? $_POST['id'] ?? ''));
$admin = $admSeq !== '' ? find_admin($admSeq) : null;
$errors = [];

if ($admSeq !== '' && !$admin) {
    http_response_code(404);
    exit;
}

if ($admin && ($admin['role'] ?? 'admin') === 'super') {
    http_response_code(404);
    exit;
}

if (request_method() === 'POST') {
    $name = trim((string) ($_POST['name'] ?? ''));
    $loginId = $admin ? ($admin['login_id'] ?? '') : trim((string) ($_POST['login_id'] ?? ''));
    $status = (string) ($_POST['status'] ?? 'active');
    $password = (string) ($_POST['password'] ?? '');
    $passwordConfirm = (string) ($_POST['password_confirm'] ?? '');

    if ($name === '') {
        $errors[] = '관리자명을 입력해주세요.';
    }
    if ($loginId === '') {
        $errors[] = '아이디를 입력해주세요.';
    }

    if (!$admin) {
        $duplicate = find_admin_by_login_id($loginId);
        if ($duplicate && (int) ($duplicate['adm_seq'] ?? 0) !== (int) $admSeq) {
            $errors[] = '이미 사용 중인 아이디입니다.';
        }
    }

    if ($admSeq === '' && $password === '') {
        $errors[] = '비밀번호를 입력해주세요.';
    }
    if ($password !== '' && $password !== $passwordConfirm) {
        $errors[] = '비밀번호와 비밀번호 확인이 일치하지 않습니다.';
    }
    if (!in_array($status, ['active', 'inactive'], true)) {
        $status = 'active';
    }

    if (!$errors) {
        $isCreate = $admSeq === '';
        $passwordHash = $admin['password_hash'] ?? '';
        if ($password !== '') {
            $passwordHash = password_hash($password, PASSWORD_DEFAULT);
        }

        $actorAdmin = current_admin_record();
        if (!$actorAdmin) {
            $actorAdmin = find_admin_by_login_id('admin');
        }
        $actorIdx = (int) ($actorAdmin['adm_seq'] ?? 0);
        $actorId = $actorIdx > 0 ? (string) $actorIdx : (string) ($_SESSION['admin_id'] ?? '');
        $actorLoginId = (string) ($actorAdmin['login_id'] ?? ($_SESSION['admin_login_id'] ?? ''));
        $actorName = (string) ($actorAdmin['name'] ?? ($_SESSION['admin_name'] ?? ''));
        $now = current_time();
        $regDtm = $admin['reg_dtm'] ?? $now;
        $regId = $admin['reg_id'] ?? $actorId;
        $modDtm = $now;
        $modId = $actorId;

        $savedAdmin = [
            'adm_seq' => $isCreate ? 0 : (int) $admSeq,
            'login_id' => $loginId,
            'name' => $name,
            'role' => 'admin',
            'status' => $status,
            'password_hash' => $passwordHash,
            'reg_dtm' => $regDtm,
            'reg_id' => $regId,
            'mod_dtm' => $modDtm,
            'mod_id' => $modId,
        ];

        $savedSeq = $isCreate ? insert_admin($savedAdmin) : (int) $admSeq;
        if ($isCreate) {
            $savedAdmin['adm_seq'] = $savedSeq;
        } else {
            update_admin($savedAdmin);
        }

        if ($isCreate) {
            $createChanges = [
                ['field' => 'login_id', 'label' => '아이디', 'value' => $savedAdmin['login_id']],
                ['field' => 'name', 'label' => '관리자명', 'value' => $savedAdmin['name']],
                ['field' => 'status', 'label' => '사용여부', 'value' => admin_status_label($savedAdmin['status'])],
            ];
            if ($password !== '') {
                $createChanges[] = ['field' => 'password', 'label' => '비밀번호', 'summary' => '초기 설정됨'];
            }

            save_admin_change_log([
                'target_adm_seq' => (int) $savedSeq,
                'target_adm_login_id' => $savedAdmin['login_id'],
                'target_adm_name' => $savedAdmin['name'],
                'changer_adm_seq' => $actorIdx,
                'changer_adm_login_id' => $actorLoginId,
                'changer_adm_name' => $actorName,
                'action' => 'create',
                'reg_dtm' => current_time(),
                'reg_id' => $actorLoginId,
                'mod_dtm' => current_time(),
                'mod_id' => $actorLoginId,
                'changed_at' => current_time(),
                'changes' => $createChanges,
            ]);
        } else {
            $changes = admin_change_diff(admin_create_snapshot($admin), admin_create_snapshot($savedAdmin));
            if ($changes) {
                save_admin_change_log([
                    'target_adm_seq' => (int) $savedSeq,
                    'target_adm_login_id' => $savedAdmin['login_id'],
                    'target_adm_name' => $savedAdmin['name'],
                    'changer_adm_seq' => $actorIdx,
                    'changer_adm_login_id' => $actorLoginId,
                    'changer_adm_name' => $actorName,
                    'action' => 'update',
                    'reg_dtm' => current_time(),
                    'reg_id' => $actorLoginId,
                    'mod_dtm' => current_time(),
                    'mod_id' => $actorLoginId,
                    'changed_at' => current_time(),
                    'changes' => $changes,
                ]);
            }
        }

        redirect('/admin/management/admins.php');
    }
}

$pageTitle = $admin ? '관리자 상세' : '관리자 등록';
require __DIR__ . '/../../partials/header.php';
?>

<section class="mb-4">
    <div>
        <h1><?= $admin ? '관리자 상세' : '관리자 등록' ?></h1>
        <p class="text-secondary mb-0">관리자 계정과 사용 상태를 관리합니다.</p>
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
    <input type="hidden" name="id" value="<?= e($admSeq) ?>">
    <div class="row g-3 mb-4">
        <div class="col-12 col-md-4">
            <label class="form-label">관리자명 <span class="text-danger">*</span></label>
            <input class="form-control" name="name" value="<?= e($_POST['name'] ?? $admin['name'] ?? '') ?>" required>
        </div>
        <div class="col-12 col-md-4">
            <label class="form-label">아이디 <span class="text-danger">*</span></label>
            <?php $loginIdValue = $_POST['login_id'] ?? $admin['login_id'] ?? ''; ?>
            <input class="form-control <?= $admin ? 'bg-light text-secondary' : '' ?>" name="login_id" value="<?= e($loginIdValue) ?>" <?= $admin ? 'readonly aria-readonly="true"' : 'required' ?>>
            <?php if ($admin): ?>
                <div class="form-text">등록된 관리자의 아이디는 변경할 수 없습니다.</div>
            <?php endif; ?>
        </div>
        <div class="col-12 col-md-4">
            <label class="form-label">사용여부</label>
            <?php $selectedStatus = $_POST['status'] ?? $admin['status'] ?? 'active'; ?>
            <select class="form-select" name="status">
                <option value="active" <?= $selectedStatus === 'active' ? 'selected' : '' ?>>사용중</option>
                <option value="inactive" <?= $selectedStatus === 'inactive' ? 'selected' : '' ?>>미사용</option>
            </select>
        </div>
        <div class="col-12 col-md-4">
            <label class="form-label">비밀번호 <?= $admin ? '' : '<span class="text-danger">*</span>' ?></label>
            <input class="form-control" type="password" name="password" <?= $admin ? '' : 'required' ?>>
            <div class="form-text"><?= $admin ? '비워두면 기존 비밀번호를 유지합니다.' : '새 계정 생성 시 비밀번호를 입력해야 합니다.' ?></div>
        </div>
        <div class="col-12 col-md-4">
            <label class="form-label">비밀번호 확인 <?= $admin ? '' : '<span class="text-danger">*</span>' ?></label>
            <input class="form-control" type="password" name="password_confirm" <?= $admin ? '' : 'required' ?>>
        </div>
    </div>

    </div>
    </div>
    <div class="detail-action-bar mt-4">
        <div>
            <a class="btn btn-outline-secondary" href="/admin/management/admins.php">목록</a>
        </div>
        <div>
            <?php if ($admin): ?>
                <button class="btn btn-success" type="submit">수정하기</button>
            <?php else: ?>
                <button class="btn btn-primary" type="submit">등록하기</button>
            <?php endif; ?>
        </div>
    </div>
</form>

<?php require __DIR__ . '/../../partials/footer.php'; ?>
