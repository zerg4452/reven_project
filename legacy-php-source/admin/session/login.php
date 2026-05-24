<?php

declare(strict_types=1);

require_once __DIR__ . '/../core/auth.php';
require_once __DIR__ . '/../../lib/storage.php';

$error = '';
if (request_method() === 'POST') {
    $loginId = trim((string) ($_POST['login_id'] ?? ''));
    $password = (string) ($_POST['password'] ?? '');
    $admin = $loginId !== '' ? find_admin_by_login_id($loginId) : null;

    if ($admin && ($admin['status'] ?? 'active') === 'active' && password_verify($password, (string) ($admin['password_hash'] ?? ''))) {
        $_SESSION['is_admin'] = true;
        $_SESSION['admin_id'] = (string) ($admin['adm_seq'] ?? '');
        $_SESSION['admin_login_id'] = $admin['login_id'] ?? '';
        $_SESSION['admin_name'] = $admin['name'] ?? '';
        redirect('/admin/');
    }

    $error = '아이디 또는 비밀번호가 올바르지 않습니다.';
}

$pageTitle = '관리자 로그인';
require __DIR__ . '/../../partials/header.php';
?>

<section class="mb-4">
    <h1>관리자 로그인</h1>
</section>

<form class="card shadow-sm login-box" method="post">
    <div class="card-body">
    <?php if ($error): ?><div class="alert alert-danger"><?= e($error) ?></div><?php endif; ?>
    <label class="form-label">관리자 아이디</label>
    <input class="form-control mb-3" type="text" name="login_id" value="<?= e($_POST['login_id'] ?? '') ?>" required autofocus>
    <label class="form-label">관리자 비밀번호</label>
    <input class="form-control mb-3" type="password" name="password" required>
    <button class="btn btn-primary" type="submit">로그인</button>
    </div>
</form>

<?php require __DIR__ . '/../../partials/footer.php'; ?>
