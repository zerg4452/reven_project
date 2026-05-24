<?php

declare(strict_types=1);

require_once __DIR__ . '/../core/auth.php';
require_once __DIR__ . '/../../lib/storage.php';
require_once __DIR__ . '/../core/submission_status.php';

require_admin();

$formId = trim((string) ($_GET['form'] ?? ''));
$submissions = $formId !== '' ? submissions_for_form($formId) : all_submissions();
$filename = 'submissions_' . date('Ymd_His') . '.csv';

header('Content-Type: text/csv; charset=UTF-8');
header('Content-Disposition: attachment; filename="' . $filename . '"');

$output = fopen('php://output', 'w');
fwrite($output, "\xEF\xBB\xBF");

fputcsv($output, [
    '접수일',
    '처리상태',
    '설문',
    '이름',
    '연락처',
    '이메일',
    '생년월일',
    '주소',
    '관리자메모',
    '제출내용',
    'IP',
]);

foreach ($submissions as $submission) {
    $answerText = [];
    foreach (($submission['answers'] ?? []) as $answer) {
        $value = $answer['value'] ?? '';
        $answerText[] = ($answer['label'] ?? '') . ': ' . (is_array($value) ? implode(', ', $value) : (string) $value);
    }

    fputcsv($output, [
        display_date($submission['submitted_at'] ?? ''),
        submission_status_text((string) ($submission['status'] ?? 'new')),
        $submission['form_title'] ?? '',
        $submission['person']['name'] ?? '',
        $submission['person']['phone'] ?? '',
        $submission['person']['email'] ?? '',
        $submission['person']['birthdate'] ?? '',
        $submission['person']['address'] ?? '',
        $submission['admin_memo'] ?? '',
        implode("\n", $answerText),
        $submission['ip'] ?? '',
    ]);
}

fclose($output);
