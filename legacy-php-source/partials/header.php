<?php
require_once __DIR__ . '/../config.php';
require_once __DIR__ . '/../lib/helpers.php';
$pageTitle = $pageTitle ?? APP_NAME;
$scriptName = $_SERVER['SCRIPT_NAME'] ?? '';
$isAdminArea = strpos($scriptName, '/admin/') === 0;
$isAdminLoggedIn = function_exists('is_admin') && is_admin();
$showAdminShell = $isAdminArea && $isAdminLoggedIn && basename($scriptName) !== 'login.php';
$currentPath = parse_url($_SERVER['REQUEST_URI'] ?? '/', PHP_URL_PATH) ?: '/';
$adminMenu = [
    'management' => [
        'label' => '관리자 관리',
        'href' => '/admin/management/admins.php',
        'children' => [
            ['label' => '관리자 목록', 'href' => '/admin/management/admins.php'],
            ['label' => '관리자 접속이력', 'href' => '/admin/management/access_logs.php'],
            ['label' => '관리자 수정이력', 'href' => '/admin/management/change_logs.php'],
            ['label' => '메뉴 관리', 'href' => '/admin/management/menus.php'],
            ['label' => '대시보드 수정', 'href' => '/admin/management/dashboard_edit.php'],
        ],
    ],
    'forms' => [
        'label' => '설문 운영',
        'href' => '/admin/survey/forms.php',
        'children' => [
            ['label' => '설문 관리', 'href' => '/admin/survey/forms.php'],
            ['label' => '설문 이력 관리', 'href' => '/admin/survey/submissions.php'],
        ],
    ],
    'news' => [
        'label' => '뉴스',
        'href' => '/admin/news/ai_news.php',
        'children' => [
            ['label' => 'AI News', 'href' => '/admin/news/ai_news.php'],
        ],
    ],
];
$activeAdminMenu = null;
if (in_array($currentPath, ['/admin/management/admins.php', '/admin/management/admin_edit.php', '/admin/management/access_logs.php', '/admin/management/change_logs.php', '/admin/management/change_log_view.php', '/admin/management/menus.php', '/admin/management/dashboard_edit.php'], true)) {
    $activeAdminMenu = 'management';
} elseif (in_array($currentPath, ['/admin/survey/forms.php', '/admin/survey/form_edit.php', '/admin/survey/submissions.php', '/admin/survey/submission_view.php', '/admin/survey/submissions_export.php'], true)) {
    $activeAdminMenu = 'forms';
} elseif (in_array($currentPath, ['/admin/news/ai_news.php', '/admin/news/ai_news_view.php', '/admin/news/ai_news_edit.php'], true)) {
    $activeAdminMenu = 'news';
}
$adminLnbActiveHref = $currentPath;
$adminLnbActiveMap = [
    'management' => [
        '/admin/management/admin_edit.php' => '/admin/management/admins.php',
        '/admin/management/change_log_view.php' => '/admin/management/change_logs.php',
    ],
    'forms' => [
        '/admin/survey/form_edit.php' => '/admin/survey/forms.php',
        '/admin/survey/submission_view.php' => '/admin/survey/submissions.php',
        '/admin/survey/submissions_export.php' => '/admin/survey/submissions.php',
    ],
    'news' => [
        '/admin/news/ai_news_view.php' => '/admin/news/ai_news.php',
        '/admin/news/ai_news_edit.php' => '/admin/news/ai_news.php',
    ],
];
if ($activeAdminMenu && isset($adminLnbActiveMap[$activeAdminMenu][$currentPath])) {
    $adminLnbActiveHref = $adminLnbActiveMap[$activeAdminMenu][$currentPath];
}
$breadcrumbs = ['사용자 화면'];
if ($isAdminArea) {
    $breadcrumbs = ['관리자'];
    if ($activeAdminMenu === 'management') {
        $breadcrumbs[] = '관리자 관리';
        if ($currentPath === '/admin/management/admins.php') {
            $breadcrumbs[] = '관리자 목록';
        } elseif ($currentPath === '/admin/management/access_logs.php') {
            $breadcrumbs[] = '관리자 접속이력';
        } elseif ($currentPath === '/admin/management/change_logs.php') {
            $breadcrumbs[] = '관리자 수정이력';
        } elseif ($currentPath === '/admin/management/change_log_view.php') {
            $breadcrumbs[] = '관리자 수정이력';
            $breadcrumbs[] = '상세';
        } elseif ($currentPath === '/admin/management/admin_edit.php') {
            $breadcrumbs[] = isset($_GET['id']) && trim((string) $_GET['id']) !== '' ? '관리자 상세' : '관리자 등록';
        } elseif ($currentPath === '/admin/management/menus.php') {
            $breadcrumbs[] = '메뉴 관리';
        } elseif ($currentPath === '/admin/management/dashboard_edit.php') {
            $breadcrumbs[] = '대시보드 수정';
        }
    } elseif ($activeAdminMenu === 'forms') {
        $breadcrumbs[] = '설문 운영';
        if ($currentPath === '/admin/survey/forms.php') {
            $breadcrumbs[] = '설문 관리';
        } elseif ($currentPath === '/admin/survey/form_edit.php') {
            $breadcrumbs[] = isset($_GET['id']) && trim((string) $_GET['id']) !== '' ? '설문 상세' : '설문 등록';
        } elseif ($currentPath === '/admin/survey/submissions.php') {
            $breadcrumbs[] = '설문 이력 관리';
        } elseif ($currentPath === '/admin/survey/submission_view.php') {
            $breadcrumbs[] = '설문 이력 상세';
        } elseif ($currentPath === '/admin/survey/submissions_export.php') {
            $breadcrumbs[] = '설문 이력 CSV';
        }
    } elseif ($activeAdminMenu === 'news') {
        $breadcrumbs[] = '뉴스';
        if ($currentPath === '/admin/news/ai_news.php') {
            $breadcrumbs[] = 'AI News';
        } elseif ($currentPath === '/admin/news/ai_news_view.php') {
            $breadcrumbs[] = 'AI News 상세';
        } elseif ($currentPath === '/admin/news/ai_news_edit.php') {
            $breadcrumbs[] = isset($_GET['id']) && trim((string) $_GET['id']) !== '' ? 'AI News 수정' : 'AI News 등록';
        }
    } elseif ($currentPath === '/admin/') {
        $breadcrumbs[] = '대시보드';
    } elseif ($currentPath === '/admin/session/login.php') {
        $breadcrumbs[] = '로그인';
    }
} elseif ($currentPath === '/') {
    $breadcrumbs[] = '설문 목록';
} elseif ($currentPath === '/submit.php') {
    $breadcrumbs[] = '설문 작성';
} elseif ($currentPath === '/thanks.php') {
    $breadcrumbs[] = '접수 완료';
}

if ($isAdminArea && $isAdminLoggedIn && function_exists('admin_navigation_context')) {
    $navigationContext = admin_navigation_context($currentPath);
    $adminMenu = $navigationContext['adminMenu'] ?? $adminMenu;
    $activeAdminMenu = $navigationContext['activeAdminMenu'] ?? $activeAdminMenu;
    $adminLnbActiveHref = $navigationContext['adminLnbActiveHref'] ?? $adminLnbActiveHref;
    if (!empty($navigationContext['breadcrumbs'])) {
        $breadcrumbs = $navigationContext['breadcrumbs'];
    }
}

$pageNotes = [
    '/' => ['screen' => '설문 목록', 'feature' => '사용 가능한 설문을 목록으로 보여주는 화면', 'created' => '2026-05-21'],
    '/submit.php' => ['screen' => '설문 작성', 'feature' => '선택한 설문에 인적사항과 답변을 제출하는 화면', 'created' => '2026-05-21'],
    '/thanks.php' => ['screen' => '접수 완료', 'feature' => '설문 제출 완료를 안내하는 화면', 'created' => '2026-05-21'],
    '/admin/' => ['screen' => '관리자 대시보드', 'feature' => '설문과 설문 이력을 한눈에 확인하는 관리자 요약 화면', 'created' => '2026-05-21'],
    '/admin/management/admins.php' => ['screen' => '관리자 관리', 'feature' => '관리자 계정 목록을 관리하는 화면', 'created' => '2026-05-21'],
    '/admin/management/admin_edit.php' => ['screen' => isset($_GET['id']) && trim((string) $_GET['id']) !== '' ? '관리자 상세' : '관리자 등록', 'feature' => '관리자 계정과 사용 상태를 편집하는 화면', 'created' => '2026-05-21'],
    '/admin/management/access_logs.php' => ['screen' => '관리자 접속이력', 'feature' => '관리자가 접속한 메뉴와 시각을 확인하는 화면', 'created' => '2026-05-21'],
    '/admin/management/change_logs.php' => ['screen' => '관리자 수정이력', 'feature' => '관리자 계정 변경 이력을 확인하는 화면', 'created' => '2026-05-21'],
    '/admin/management/change_log_view.php' => ['screen' => '관리자 수정이력 상세', 'feature' => '관리자 변경 이력의 상세 내용을 확인하는 화면', 'created' => '2026-05-21'],
    '/admin/management/menus.php' => ['screen' => '메뉴 관리', 'feature' => '관리자 메뉴 계층과 게시판 연결을 관리하는 화면', 'created' => '2026-05-23'],
    '/admin/management/dashboard_edit.php' => ['screen' => '대시보드 수정', 'feature' => '대시보드 현황판 노출 게시판을 설정하는 화면', 'created' => '2026-05-23'],
    '/admin/survey/forms.php' => ['screen' => '설문 관리', 'feature' => '설문 마스터를 관리하는 화면', 'created' => '2026-05-21'],
    '/admin/survey/form_edit.php' => ['screen' => isset($_GET['id']) && trim((string) $_GET['id']) !== '' ? '설문 상세' : '설문 등록', 'feature' => '설문 문항, 보기, 사용여부를 편집하는 화면', 'created' => '2026-05-21'],
    '/admin/survey/submissions.php' => ['screen' => '설문 이력 관리', 'feature' => '사용자가 제출한 설문 이력을 목록으로 확인하는 화면', 'created' => '2026-05-21'],
    '/admin/survey/submission_view.php' => ['screen' => '설문 이력 상세', 'feature' => '제출된 설문 내용을 열람하는 화면', 'created' => '2026-05-21'],
    '/admin/survey/submissions_export.php' => ['screen' => '설문 이력 CSV', 'feature' => '설문 이력 CSV를 내려받는 기능 화면', 'created' => '2026-05-21'],
    '/admin/news/ai_news.php' => ['screen' => 'AI News', 'feature' => 'AI 뉴스 원고와 크롤링 결과를 관리하는 목록 화면', 'created' => '2026-05-23'],
    '/admin/news/ai_news_view.php' => ['screen' => 'AI News 상세', 'feature' => 'AI 뉴스 원고의 상세 내용을 확인하는 화면', 'created' => '2026-05-23'],
    '/admin/news/ai_news_edit.php' => ['screen' => isset($_GET['id']) && trim((string) $_GET['id']) !== '' ? 'AI News 수정' : 'AI News 등록', 'feature' => 'AI 뉴스 원고를 등록하고 수정하는 화면', 'created' => '2026-05-23'],
    '/admin/session/login.php' => ['screen' => '관리자 로그인', 'feature' => '관리자 인증을 수행하는 화면', 'created' => '2026-05-21'],
    '/admin/session/logout.php' => ['screen' => '관리자 로그아웃', 'feature' => '관리자 세션을 종료하는 화면', 'created' => '2026-05-21'],
];
$pageNote = $pageNotes[$currentPath] ?? null;
?>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><?= e($pageTitle) ?></title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <?= $extraHead ?? '' ?>
    <link rel="stylesheet" href="/assets/style.css">
</head>
<body>
<nav class="navbar navbar-expand-lg admin-gnb-bar border-bottom sticky-top">
    <div class="container-fluid px-3 px-lg-4">
        <a class="navbar-brand fw-bold" href="/"><?= e(APP_NAME) ?></a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainGnb" aria-controls="mainGnb" aria-expanded="false" aria-label="메뉴 열기">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="mainGnb">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0 admin-gnb">
                <?php if (!$isAdminLoggedIn): ?>
                    <li class="nav-item"><a class="nav-link <?= $currentPath === '/' ? 'active' : '' ?>" href="/">접수 화면</a></li>
                <?php endif; ?>
                <li class="nav-item"><a class="nav-link <?= $isAdminArea && $currentPath === '/admin/' ? 'active' : '' ?>" href="/admin/">관리자 홈</a></li>
                <?php if ($isAdminLoggedIn): ?>
                    <li class="nav-item"><a class="nav-link <?= $activeAdminMenu === 'management' ? 'active' : '' ?>" href="/admin/management/admins.php">관리자 관리</a></li>
                <?php endif; ?>
                <?php if ($isAdminLoggedIn): ?>
                    <?php foreach ($adminMenu as $menuKey => $menu): ?>
                        <?php if ($menuKey === 'management') { continue; } ?>
                        <?php $menuFirstChildHref = $menu['children'][0]['href'] ?? $menu['href']; ?>
                        <li class="nav-item dropdown gnb-tree">
                            <div class="gnb-tree-head">
                                <a class="nav-link gnb-tree-link <?= $activeAdminMenu === $menuKey ? 'active' : '' ?>" href="<?= e($menuFirstChildHref) ?>"><?= e($menu['label']) ?></a>
                                <button class="gnb-tree-toggle dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                                    <span class="visually-hidden"><?= e($menu['label']) ?> 하위 메뉴 펼치기</span>
                                </button>
                            </div>
                            <div class="dropdown-menu gnb-tree-menu shadow-sm">
                                <div class="gnb-tree-title"><?= e($menu['label']) ?></div>
                                <ul class="gnb-tree-list">
                                    <?php foreach ($menu['children'] as $child): ?>
                                        <li>
                                            <a class="<?= $adminLnbActiveHref === $child['href'] ? 'active' : '' ?>" href="<?= e($child['href']) ?>"><?= e($child['label']) ?></a>
                                        </li>
                                    <?php endforeach; ?>
                                </ul>
                            </div>
                        </li>
                    <?php endforeach; ?>
                <?php endif; ?>
            </ul>
            <div class="d-flex gap-2 ms-lg-auto">
                <a class="btn btn-outline-primary btn-sm" href="/">사용자 화면</a>
                <?php if ($isAdminLoggedIn): ?>
                    <a class="btn btn-outline-secondary btn-sm" href="/admin/session/logout.php">로그아웃</a>
                <?php else: ?>
                    <a class="btn btn-primary btn-sm" href="/admin/session/login.php">관리자 로그인</a>
                <?php endif; ?>
            </div>
        </div>
    </div>
</nav>

<?php if ($showAdminShell): ?>
<div class="admin-shell container-fluid">
    <div class="row g-0">
        <aside class="admin-lnb col-12 col-lg-2 border-end">
            <div class="p-3">
                <?php if ($activeAdminMenu && isset($adminMenu[$activeAdminMenu])): ?>
                    <div class="small text-uppercase text-secondary fw-bold mb-2"><?= e($adminMenu[$activeAdminMenu]['label']) ?></div>
                    <div class="nav nav-pills flex-column lnb-tree">
                        <?php foreach ($adminMenu[$activeAdminMenu]['children'] as $child): ?>
                            <a class="nav-link <?= $adminLnbActiveHref === $child['href'] ? 'active' : '' ?>" href="<?= e($child['href']) ?>"><?= e($child['label']) ?></a>
                        <?php endforeach; ?>
                    </div>
                <?php else: ?>
                    <div class="small text-uppercase text-secondary fw-bold mb-2">관리자 메뉴</div>
                    <div class="nav nav-pills flex-column lnb-tree">
                        <a class="nav-link active" href="/admin/">관리자 홈</a>
                    </div>
                <?php endif; ?>
            </div>
        </aside>
        <main class="admin-content col-12 col-lg-10">
            <div class="container-fluid py-4 px-3 px-lg-4">
                <nav class="page-breadcrumb" aria-label="현재 위치"><?= e(implode(' > ', $breadcrumbs)) ?></nav>
                <!-- 화면: <?= e($pageNote['screen'] ?? $pageTitle) ?> | 기능: <?= e($pageNote['feature'] ?? $pageTitle) ?> | 생성일: <?= e($pageNote['created'] ?? '2026-05-21') ?> -->
<?php else: ?>
<main class="container py-4">
    <nav class="page-breadcrumb" aria-label="현재 위치"><?= e(implode(' > ', $breadcrumbs)) ?></nav>
    <!-- 화면: <?= e($pageNote['screen'] ?? $pageTitle) ?> | 기능: <?= e($pageNote['feature'] ?? $pageTitle) ?> | 생성일: <?= e($pageNote['created'] ?? '2026-05-21') ?> -->
<?php endif; ?>
