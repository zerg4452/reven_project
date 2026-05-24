<?php

declare(strict_types=1);

require_once __DIR__ . '/../../config.php';
require_once __DIR__ . '/../../lib/helpers.php';
require_once __DIR__ . '/../../lib/storage.php';

session_start();

function is_admin(): bool
{
    return (bool) ($_SESSION['is_admin'] ?? false);
}

function require_admin(): void
{
    if (!is_admin()) {
        redirect('/admin/session/login.php');
    }

    $scriptName = $_SERVER['SCRIPT_NAME'] ?? '';
    if (strpos($scriptName, '/admin/') === 0 && !in_array(basename($scriptName), ['login.php', 'logout.php'], true)) {
        $path = parse_url($_SERVER['REQUEST_URI'] ?? '/', PHP_URL_PATH) ?: '/admin/';
        parse_str((string) parse_url($_SERVER['REQUEST_URI'] ?? '/', PHP_URL_QUERY), $query);
        if (request_method() === 'POST' && !empty($_POST['id'])) {
            $query['id'] = (string) $_POST['id'];
        }

        $currentAdmin = current_admin_record();
        if ($currentAdmin) {
            $_SESSION['admin_id'] = (string) ($currentAdmin['adm_seq'] ?? '');
            $_SESSION['admin_login_id'] = (string) ($currentAdmin['login_id'] ?? '');
            $_SESSION['admin_name'] = (string) ($currentAdmin['name'] ?? '');
        }

        save_admin_access_log([
            'id' => bin2hex(random_bytes(8)),
            'admin_id' => (string) ($_SESSION['admin_id'] ?? ''),
            'admin_login_id' => (string) ($_SESSION['admin_login_id'] ?? ''),
            'admin_name' => (string) ($_SESSION['admin_name'] ?? ''),
            'location' => admin_access_location_label($path, is_array($query) ? $query : []),
            'path' => $path,
            'accessed_at' => current_time(),
        ]);
    }
}
