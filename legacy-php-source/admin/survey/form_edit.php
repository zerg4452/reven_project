<?php

declare(strict_types=1);

require_once __DIR__ . '/../core/auth.php';
require_once __DIR__ . '/../../lib/storage.php';

require_admin();

$id = trim((string) ($_GET['id'] ?? $_POST['id'] ?? ''));
$form = $id !== '' ? find_form($id) : null;
$errors = [];

if (request_method() === 'POST') {
    $title = trim((string) ($_POST['title'] ?? ''));
    $description = trim((string) ($_POST['description'] ?? ''));
    $status = (string) ($_POST['status'] ?? 'active');
    $labels = $_POST['field_label'] ?? [];
    $types = $_POST['field_type'] ?? [];
    $required = $_POST['field_required'] ?? [];
    $options = $_POST['field_options'] ?? [];
    $fields = [];

    if ($title === '') {
        $errors[] = '설문 제목을 입력해주세요.';
    }

    foreach ($labels as $index => $label) {
        $label = trim((string) $label);
        if ($label === '') {
            continue;
        }

        $type = (string) ($types[$index] ?? 'text');
        $optionLines = preg_split('/\r\n|\r|\n/', (string) ($options[$index] ?? '')) ?: [];

        $fields[] = [
            'key' => 'f_' . ($index + 1) . '_' . substr(md5($label), 0, 6),
            'label' => $label,
            'type' => in_array($type, ['text', 'textarea', 'select', 'radio', 'checkbox', 'date', 'number', 'email'], true) ? $type : 'text',
            'required' => isset($required[$index]),
            'options' => array_values(array_filter(array_map('trim', $optionLines))),
        ];
    }

    if (!$fields) {
        $errors[] = '최소 1개 이상의 제출 항목을 입력해주세요.';
    }

    if (!$errors) {
        save_form([
            'id' => $id !== '' ? $id : bin2hex(random_bytes(8)),
            'title' => $title,
            'description' => $description,
            'status' => $status === 'inactive' ? 'inactive' : 'active',
            'fields' => $fields,
            'created_at' => $form['created_at'] ?? current_time(),
            'updated_at' => current_time(),
        ]);

        redirect('/admin/survey/forms.php');
    }
}

$editingFields = $_POST['field_label'] ?? array_column($form['fields'] ?? [], 'label');
$fieldCount = max(1, count($editingFields));

$pageTitle = $form ? '설문 상세' : '설문 등록';
require __DIR__ . '/../../partials/header.php';
?>

<section class="mb-4">
    <div>
        <h1><?= $form ? '설문 상세' : '설문 등록' ?></h1>
        <p class="text-secondary mb-0">설문에 사용될 문항, 보기, 사용여부를 제어합니다.</p>
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
    <input type="hidden" name="id" value="<?= e($id) ?>">
    <div class="row g-3 mb-4">
        <div class="col-12"><label class="form-label">설문 제목 <span class="text-danger">*</span></label><input class="form-control" name="title" value="<?= e($_POST['title'] ?? $form['title'] ?? '') ?>" required></div>
        <div class="col-12"><label class="form-label">설명</label><textarea class="form-control" name="description"><?= e($_POST['description'] ?? $form['description'] ?? '') ?></textarea></div>
        <div class="col-12 col-md-4"><label class="form-label">상태</label>
            <select class="form-select" name="status">
                <?php $selectedStatus = $_POST['status'] ?? $form['status'] ?? 'active'; ?>
                <option value="active" <?= $selectedStatus === 'active' ? 'selected' : '' ?>>접수중</option>
                <option value="inactive" <?= $selectedStatus === 'inactive' ? 'selected' : '' ?>>비활성</option>
            </select>
        </div>
    </div>

    <div class="d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-2 mb-3">
        <h2 class="h5 mb-0">제출 항목</h2>
        <button class="btn btn-outline-primary btn-sm" type="button" id="addFieldButton">항목 추가</button>
    </div>
    <p class="text-secondary small mb-3">선택형 항목은 옵션을 줄바꿈으로 입력합니다.</p>

    <div class="field-editor" id="fieldEditor">
        <?php for ($i = 0; $i < $fieldCount; $i++): ?>
            <?php
            $field = $form['fields'][$i] ?? [];
            $label = $_POST['field_label'][$i] ?? $field['label'] ?? '';
            $type = $_POST['field_type'][$i] ?? $field['type'] ?? 'text';
            $isRequired = isset($_POST['field_required'][$i]) || (!$_POST && (bool) ($field['required'] ?? false));
            $optionText = $_POST['field_options'][$i] ?? implode("\n", field_options($field));
            ?>
            <div class="field-row">
                <div class="field-row-head">
                    <strong>문항</strong>
                    <button class="btn btn-outline-danger btn-sm remove-field-button" type="button">삭제</button>
                </div>
                <div><label class="form-label">항목명</label><input class="form-control" name="field_label[<?= $i ?>]" value="<?= e((string) $label) ?>"></div>
                <div><label class="form-label">유형</label>
                    <select class="form-select" name="field_type[<?= $i ?>]">
                        <?php foreach (['text' => '단답형', 'textarea' => '장문형', 'select' => '선택목록', 'radio' => '라디오', 'checkbox' => '체크박스', 'date' => '날짜', 'number' => '숫자', 'email' => '이메일'] as $value => $text): ?>
                            <option value="<?= e($value) ?>" <?= $type === $value ? 'selected' : '' ?>><?= e($text) ?></option>
                        <?php endforeach; ?>
                    </select>
                </div>
                <label class="form-check field-required"><input class="form-check-input" type="checkbox" name="field_required[<?= $i ?>]" <?= $isRequired ? 'checked' : '' ?>> <span class="form-check-label">필수</span></label>
                <div class="wide"><label class="form-label">옵션</label><textarea class="form-control" name="field_options[<?= $i ?>]" placeholder="선택형 항목일 때만 입력"><?= e((string) $optionText) ?></textarea></div>
            </div>
        <?php endfor; ?>
    </div>

    </div>
    </div>
    <div class="detail-action-bar mt-4">
        <div>
            <a class="btn btn-outline-secondary" href="/admin/survey/forms.php">목록</a>
        </div>
        <div>
            <?php if ($form): ?>
                <button class="btn btn-success" type="submit">수정하기</button>
            <?php else: ?>
                <button class="btn btn-primary" type="submit">등록하기</button>
            <?php endif; ?>
        </div>
    </div>
</form>

<template id="fieldTemplate">
    <div class="field-row">
        <div class="field-row-head">
            <strong>문항</strong>
            <button class="btn btn-outline-danger btn-sm remove-field-button" type="button">삭제</button>
        </div>
        <div><label class="form-label">항목명</label><input class="form-control" name="field_label[__INDEX__]" value=""></div>
        <div><label class="form-label">유형</label>
            <select class="form-select" name="field_type[__INDEX__]">
                <?php foreach (['text' => '단답형', 'textarea' => '장문형', 'select' => '선택목록', 'radio' => '라디오', 'checkbox' => '체크박스', 'date' => '날짜', 'number' => '숫자', 'email' => '이메일'] as $value => $text): ?>
                    <option value="<?= e($value) ?>"><?= e($text) ?></option>
                <?php endforeach; ?>
            </select>
        </div>
        <label class="form-check field-required"><input class="form-check-input" type="checkbox" name="field_required[__INDEX__]"> <span class="form-check-label">필수</span></label>
        <div class="wide"><label class="form-label">옵션</label><textarea class="form-control" name="field_options[__INDEX__]" placeholder="선택형 항목일 때만 입력"></textarea></div>
    </div>
</template>

<script>
document.addEventListener('DOMContentLoaded', function () {
    const editor = document.getElementById('fieldEditor');
    const template = document.getElementById('fieldTemplate');
    const addButton = document.getElementById('addFieldButton');

    function refreshRows() {
        const rows = editor.querySelectorAll('.field-row');
        rows.forEach(function (row, index) {
            row.querySelector('strong').textContent = '문항 ' + (index + 1);
            row.querySelector('.remove-field-button').disabled = rows.length === 1;
            row.querySelectorAll('[name]').forEach(function (field) {
                field.name = field.name.replace(/\[\d+\]|\[__INDEX__\]/, '[' + index + ']');
            });
        });
    }

    addButton.addEventListener('click', function () {
        const html = template.innerHTML.replaceAll('__INDEX__', String(editor.querySelectorAll('.field-row').length));
        editor.insertAdjacentHTML('beforeend', html);
        refreshRows();
    });

    editor.addEventListener('click', function (event) {
        if (!event.target.classList.contains('remove-field-button')) {
            return;
        }

        if (editor.querySelectorAll('.field-row').length <= 1) {
            return;
        }

        event.target.closest('.field-row').remove();
        refreshRows();
    });

    refreshRows();
});
</script>

<?php require __DIR__ . '/../../partials/footer.php'; ?>
