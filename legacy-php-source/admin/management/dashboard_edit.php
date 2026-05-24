<?php

declare(strict_types=1);

require_once __DIR__ . '/../core/auth.php';
require_once __DIR__ . '/../../lib/storage.php';

require_admin();

$flash = $_SESSION['dashboard_panel_flash'] ?? '';
unset($_SESSION['dashboard_panel_flash']);

if (request_method() === 'POST') {
    $settings = [];
    $postedPanels = $_POST['panels'] ?? [];
    $postedPanels = is_array($postedPanels) ? $postedPanels : [];

    foreach (dashboard_board_menus() as $menu) {
        $boardKey = (string) ($menu['board_key'] ?? '');
        if ($boardKey === '') {
            continue;
        }
        $settings[$boardKey] = [
            'display_yn' => isset($postedPanels[$boardKey]['display_yn']),
            'sort_ord' => (int) ($postedPanels[$boardKey]['sort_ord'] ?? 0),
            'item_limit' => (int) ($postedPanels[$boardKey]['item_limit'] ?? 8),
        ];
    }

    save_dashboard_panel_settings($settings);
    $_SESSION['dashboard_panel_flash'] = '대시보드 설정을 저장했습니다.';
    redirect('/admin/management/dashboard_edit.php');
}

$boardMenus = dashboard_board_menus();
$panelByBoardKey = dashboard_panel_by_board_key();
$pageTitle = '대시보드 수정';
require __DIR__ . '/../../partials/header.php';
?>

<section class="mb-4">
    <div>
        <h1>대시보드 수정</h1>
        <p class="text-secondary mb-0">대시보드 중단 현황판에 노출할 게시판을 설정합니다.</p>
    </div>
</section>

<?php if ($flash !== ''): ?><div class="alert alert-success"><?= e((string) $flash) ?></div><?php endif; ?>

<form class="card shadow-sm" method="post">
    <div class="card-header bg-white">
        <h2 class="h5 mb-0">현황판 노출 설정</h2>
    </div>
    <div class="card-body px-0">
        <?php if (!$boardMenus): ?>
            <p class="text-secondary px-3 mb-0">설정 가능한 게시판 메뉴가 없습니다.</p>
        <?php else: ?>
            <div class="table-responsive">
                <table class="table align-middle mb-0">
                    <thead>
                    <tr>
                        <th>노출</th>
                        <th>게시판</th>
                        <th>게시판 키</th>
                        <th>표시 순서</th>
                        <th>표시 글 수</th>
                    </tr>
                    </thead>
                    <tbody>
                    <?php foreach ($boardMenus as $index => $menu): ?>
                        <?php
                        $boardKey = (string) ($menu['board_key'] ?? '');
                        $panel = $panelByBoardKey[$boardKey] ?? [];
                        $displayYn = (string) ($panel['display_yn'] ?? 'Y');
                        $sortOrd = (int) ($panel['sort_ord'] ?? (($index + 1) * 10));
                        $itemLimit = (int) ($panel['item_limit'] ?? 8);
                        ?>
                        <tr>
                            <td>
                                <input class="form-check-input" type="checkbox" name="panels[<?= e($boardKey) ?>][display_yn]" value="Y" <?= $displayYn === 'Y' ? 'checked' : '' ?>>
                            </td>
                            <td class="fw-bold"><?= e($menu['menu_nm'] ?? '') ?></td>
                            <td><?= e($boardKey) ?></td>
                            <td><input class="form-control dashboard-setting-number" type="number" name="panels[<?= e($boardKey) ?>][sort_ord]" value="<?= e((string) $sortOrd) ?>"></td>
                            <td><input class="form-control dashboard-setting-number" type="number" name="panels[<?= e($boardKey) ?>][item_limit]" min="1" max="20" value="<?= e((string) $itemLimit) ?>"></td>
                        </tr>
                    <?php endforeach; ?>
                    </tbody>
                </table>
            </div>
        <?php endif; ?>
    </div>
    <div class="card-footer bg-white detail-action-bar">
        <div>
            <a class="btn btn-outline-secondary" href="/admin/">대시보드</a>
        </div>
        <div>
            <button class="btn btn-success" type="submit">저장</button>
        </div>
    </div>
</form>

<?php require __DIR__ . '/../../partials/footer.php'; ?>
