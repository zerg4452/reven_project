<?php

declare(strict_types=1);

require_once __DIR__ . '/lib/storage.php';
require_once __DIR__ . '/lib/helpers.php';

$formId = trim((string) ($_GET['form'] ?? $_POST['form_id'] ?? ''));
$form = $formId !== '' ? find_form($formId) : null;

if (!$form || ($form['status'] ?? 'active') !== 'active') {
    http_response_code(404);
    $pageTitle = '설문을 찾을 수 없습니다';
    require __DIR__ . '/partials/header.php';
    echo '<div class="alert alert-light border">접수 가능한 설문을 찾을 수 없습니다.</div>';
    require __DIR__ . '/partials/footer.php';
    exit;
}

$errors = [];
$old = $_POST;

if (request_method() === 'POST') {
    $person = [
        'name' => trim((string) ($_POST['name'] ?? '')),
        'phone' => trim((string) ($_POST['phone'] ?? '')),
        'email' => trim((string) ($_POST['email'] ?? '')),
        'birthdate' => trim((string) ($_POST['birthdate'] ?? '')),
        'address' => trim((string) ($_POST['address'] ?? '')),
    ];

    if ($person['name'] === '') {
        $errors[] = '이름을 입력해주세요.';
    }
    if ($person['phone'] === '') {
        $errors[] = '연락처를 입력해주세요.';
    }

    $answers = [];
    foreach (($form['fields'] ?? []) as $field) {
        $key = (string) ($field['key'] ?? '');
        $label = field_label($field);
        $type = (string) ($field['type'] ?? 'text');
        $required = (bool) ($field['required'] ?? false);
        $value = $_POST['answers'][$key] ?? ($type === 'checkbox' ? [] : '');

        if ($type === 'checkbox') {
            $value = is_array($value) ? array_values(array_map('trim', $value)) : [];
            if ($required && !$value) {
                $errors[] = $label . ' 항목을 선택해주세요.';
            }
        } else {
            $value = trim((string) $value);
            if ($required && $value === '') {
                $errors[] = $label . ' 항목을 입력해주세요.';
            }
        }

        $answers[$key] = [
            'label' => $label,
            'type' => $type,
            'value' => $value,
        ];
    }

    if (!$errors) {
        save_submission([
            'id' => bin2hex(random_bytes(8)),
            'form_id' => $form['id'],
            'form_title' => $form['title'],
            'person' => $person,
            'answers' => $answers,
            'status' => 'new',
            'admin_memo' => '',
            'submitted_at' => current_time(),
            'ip' => $_SERVER['REMOTE_ADDR'] ?? '',
        ]);

        redirect('/thanks.php');
    }
}

$pageTitle = ($form['title'] ?? '설문 작성') . ' 작성';
require __DIR__ . '/partials/header.php';
?>

<section class="mb-4">
    <h1><?= e($form['title'] ?? '') ?></h1>
    <p class="text-secondary mb-0"><?= e($form['description'] ?? '') ?></p>
</section>

<?php if ($errors): ?>
    <div class="alert alert-danger">
        <?php foreach ($errors as $error): ?>
            <p><?= e($error) ?></p>
        <?php endforeach; ?>
    </div>
<?php endif; ?>

<form class="card shadow-sm form-stack" method="post">
    <div class="card-body">
    <input type="hidden" name="form_id" value="<?= e($form['id']) ?>">

    <h2 class="h5 mb-3">인적사항</h2>
    <div class="row g-3 mb-4">
        <div class="col-12 col-md-6"><label class="form-label">이름 <span class="text-danger">*</span></label><input class="form-control" name="name" value="<?= e($old['name'] ?? '') ?>" required></div>
        <div class="col-12 col-md-6"><label class="form-label">연락처 <span class="text-danger">*</span></label><input class="form-control" name="phone" value="<?= e($old['phone'] ?? '') ?>" required></div>
        <div class="col-12 col-md-6"><label class="form-label">이메일</label><input class="form-control" type="email" name="email" value="<?= e($old['email'] ?? '') ?>"></div>
        <div class="col-12 col-md-6"><label class="form-label">생년월일</label><input class="form-control" type="date" name="birthdate" value="<?= e($old['birthdate'] ?? '') ?>"></div>
        <div class="col-12"><label class="form-label">주소</label><input class="form-control" name="address" value="<?= e($old['address'] ?? '') ?>"></div>
    </div>

    <h2 class="h5 mb-3">제출 내용</h2>
    <?php foreach (($form['fields'] ?? []) as $field): ?>
        <?php
        $key = (string) ($field['key'] ?? '');
        $type = (string) ($field['type'] ?? 'text');
        $required = (bool) ($field['required'] ?? false);
        $answer = $old['answers'][$key] ?? null;
        ?>
        <div class="mb-3">
            <label class="form-label fw-bold"><?= e(field_label($field)) ?> <?= $required ? '<span class="text-danger">*</span>' : '' ?></label>
            <?php if ($type === 'textarea'): ?>
                <textarea class="form-control" name="answers[<?= e($key) ?>]" <?= $required ? 'required' : '' ?>><?= e((string) $answer) ?></textarea>
            <?php elseif ($type === 'select'): ?>
                <select class="form-select" name="answers[<?= e($key) ?>]" <?= $required ? 'required' : '' ?>>
                    <option value="">선택</option>
                    <?php foreach (field_options($field) as $option): ?>
                        <option value="<?= e($option) ?>" <?= $answer === $option ? 'selected' : '' ?>><?= e($option) ?></option>
                    <?php endforeach; ?>
                </select>
            <?php elseif ($type === 'radio'): ?>
                <div class="d-flex gap-3 flex-wrap">
                    <?php foreach (field_options($field) as $option): ?>
                        <label class="form-check"><input class="form-check-input" type="radio" name="answers[<?= e($key) ?>]" value="<?= e($option) ?>" <?= $answer === $option ? 'checked' : '' ?> <?= $required ? 'required' : '' ?>> <span class="form-check-label"><?= e($option) ?></span></label>
                    <?php endforeach; ?>
                </div>
            <?php elseif ($type === 'checkbox'): ?>
                <div class="d-flex gap-3 flex-wrap">
                    <?php foreach (field_options($field) as $option): ?>
                        <label class="form-check"><input class="form-check-input" type="checkbox" name="answers[<?= e($key) ?>][]" value="<?= e($option) ?>" <?= is_array($answer) && in_array($option, $answer, true) ? 'checked' : '' ?>> <span class="form-check-label"><?= e($option) ?></span></label>
                    <?php endforeach; ?>
                </div>
            <?php else: ?>
                <input class="form-control" type="<?= e(in_array($type, ['date', 'number', 'email'], true) ? $type : 'text') ?>" name="answers[<?= e($key) ?>]" value="<?= e((string) $answer) ?>" <?= $required ? 'required' : '' ?>>
            <?php endif; ?>
        </div>
    <?php endforeach; ?>

    <button class="btn btn-primary" type="submit">제출하기</button>
    </div>
</form>

<?php require __DIR__ . '/partials/footer.php'; ?>
