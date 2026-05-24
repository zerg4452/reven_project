<?php

declare(strict_types=1);

function e(?string $value): string
{
    return htmlspecialchars((string) $value, ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
}

function redirect(string $path): void
{
    header('Location: ' . $path);
    exit;
}

function request_method(): string
{
    return strtoupper($_SERVER['REQUEST_METHOD'] ?? 'GET');
}

function current_time(): string
{
    return date('Y-m-d H:i:s');
}

function display_date(?string $value): string
{
    $value = trim((string) $value);
    if ($value === '') {
        return '';
    }

    return substr($value, 0, 10);
}

function display_datetime(?string $value): string
{
    $value = trim((string) $value);
    if ($value === '') {
        return '';
    }

    return substr($value, 0, 19);
}

function field_label(array $field): string
{
    return trim((string) ($field['label'] ?? ''));
}

function field_options(array $field): array
{
    $options = $field['options'] ?? [];
    return is_array($options) ? array_values(array_filter(array_map('trim', $options))) : [];
}
