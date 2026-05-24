<?php

declare(strict_types=1);

require_once __DIR__ . '/../core/auth.php';

session_destroy();
redirect('/admin/session/login.php');
