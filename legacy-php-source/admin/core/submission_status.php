<?php

declare(strict_types=1);

function submission_status_options(): array
{
    return [
        'new' => '신규',
        'reviewing' => '확인중',
        'contacted' => '연락완료',
        'done' => '처리완료',
        'hold' => '보류',
    ];
}

function submission_status_text(string $status): string
{
    $options = submission_status_options();
    return $options[$status] ?? $options['new'];
}

