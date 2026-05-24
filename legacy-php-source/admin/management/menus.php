<?php

declare(strict_types=1);

require_once __DIR__ . '/../core/auth.php';
require_once __DIR__ . '/../../lib/storage.php';

require_admin();

$error = '';
$flash = $_SESSION['admin_menu_flash'] ?? '';
unset($_SESSION['admin_menu_flash']);

function admin_menu_match_urls_text(array $menu): string
{
    return implode("\n", $menu['match_urls'] ?? []);
}

function admin_menu_tree_nodes(array $menus): array
{
    $nodes = [];
    foreach ($menus as $menu) {
        $nodes[] = [
            'id' => (string) ($menu['adm_menu_seq'] ?? 0),
            'parent' => (int) ($menu['parent_adm_menu_seq'] ?? 0) > 0 ? (string) $menu['parent_adm_menu_seq'] : '#',
            'text' => (string) ($menu['menu_nm'] ?? ''),
            'state' => [
                'opened' => true,
                'disabled' => ($menu['use_yn'] ?? 'Y') === 'N',
            ],
        ];
    }

    return $nodes;
}

$selectedId = trim((string) ($_GET['id'] ?? $_POST['id'] ?? ''));

if (request_method() === 'POST') {
    $action = (string) ($_POST['action'] ?? 'save');
    $menuSeq = (int) ($_POST['id'] ?? 0);

    if ($action === 'delete') {
        if (delete_admin_menu_record($menuSeq)) {
            $_SESSION['admin_menu_flash'] = '메뉴를 삭제 처리했습니다.';
            redirect('/admin/management/menus.php');
        }
        $error = '하위 메뉴가 있거나 삭제할 수 없는 메뉴입니다.';
    } else {
        $parentSeq = (int) ($_POST['parent_adm_menu_seq'] ?? 0);
        $menuCd = trim((string) ($_POST['menu_cd'] ?? ''));
        $menuNm = trim((string) ($_POST['menu_nm'] ?? ''));
        $menuUrl = trim((string) ($_POST['menu_url'] ?? ''));
        $menuType = trim((string) ($_POST['menu_type'] ?? 'page'));
        $boardKey = trim((string) ($_POST['board_key'] ?? ''));
        $useYn = (string) ($_POST['use_yn'] ?? 'Y') === 'N' ? 'N' : 'Y';
        $sortOrd = (int) ($_POST['sort_ord'] ?? 0);
        $matchUrlsText = trim((string) ($_POST['match_urls_text'] ?? ''));
        $matchUrls = array_values(array_filter(array_map('trim', preg_split('/\r\n|\r|\n/', $matchUrlsText) ?: []), static fn (string $value): bool => $value !== ''));
        $existingByCode = $menuCd !== '' ? find_admin_menu_by_code($menuCd) : null;

        if ($menuCd === '' || $menuNm === '') {
            $error = '메뉴 코드와 메뉴명을 입력해 주세요.';
        } elseif ($existingByCode && (int) ($existingByCode['adm_menu_seq'] ?? 0) !== $menuSeq) {
            $error = '이미 사용 중인 메뉴 코드입니다.';
        } elseif ($menuSeq > 0 && $parentSeq === $menuSeq) {
            $error = '자기 자신을 상위 메뉴로 선택할 수 없습니다.';
        } else {
            $menusForCycleCheck = all_admin_menus();
            $childrenByParent = [];
            foreach ($menusForCycleCheck as $menu) {
                $childrenByParent[(int) ($menu['parent_adm_menu_seq'] ?? 0)][] = (int) ($menu['adm_menu_seq'] ?? 0);
            }
            $descendantIds = [];
            $stack = $childrenByParent[$menuSeq] ?? [];
            while ($stack) {
                $childId = array_pop($stack);
                $descendantIds[] = $childId;
                foreach ($childrenByParent[$childId] ?? [] as $grandChildId) {
                    $stack[] = $grandChildId;
                }
            }

            if ($menuSeq > 0 && in_array($parentSeq, $descendantIds, true)) {
                $error = '하위 메뉴를 상위 메뉴로 선택할 수 없습니다.';
            } else {
                $savedId = save_admin_menu_record([
                    'adm_menu_seq' => $menuSeq,
                    'parent_adm_menu_seq' => $parentSeq,
                    'menu_cd' => $menuCd,
                    'menu_nm' => $menuNm,
                    'menu_url' => $menuUrl,
                    'match_urls' => $matchUrls,
                    'menu_type' => $menuType,
                    'board_key' => $boardKey,
                    'use_yn' => $useYn,
                    'sort_ord' => $sortOrd,
                ]);
                $_SESSION['admin_menu_flash'] = '메뉴를 저장했습니다.';
                redirect('/admin/management/menus.php?id=' . $savedId);
            }
        }
    }
}

$menus = all_admin_menus();
$selectedMenu = $selectedId !== '' ? find_admin_menu($selectedId) : null;
$editingMenu = [
    'adm_menu_seq' => (int) ($_POST['id'] ?? $selectedMenu['adm_menu_seq'] ?? 0),
    'parent_adm_menu_seq' => (int) ($_POST['parent_adm_menu_seq'] ?? $selectedMenu['parent_adm_menu_seq'] ?? 0),
    'menu_cd' => (string) ($_POST['menu_cd'] ?? $selectedMenu['menu_cd'] ?? ''),
    'menu_nm' => (string) ($_POST['menu_nm'] ?? $selectedMenu['menu_nm'] ?? ''),
    'menu_url' => (string) ($_POST['menu_url'] ?? $selectedMenu['menu_url'] ?? ''),
    'match_urls_text' => (string) ($_POST['match_urls_text'] ?? ($selectedMenu ? admin_menu_match_urls_text($selectedMenu) : '')),
    'menu_type' => (string) ($_POST['menu_type'] ?? $selectedMenu['menu_type'] ?? 'page'),
    'board_key' => (string) ($_POST['board_key'] ?? $selectedMenu['board_key'] ?? ''),
    'use_yn' => (string) ($_POST['use_yn'] ?? $selectedMenu['use_yn'] ?? 'Y'),
    'sort_ord' => (int) ($_POST['sort_ord'] ?? $selectedMenu['sort_ord'] ?? 0),
];
$treeNodesJson = json_encode(admin_menu_tree_nodes($menus), JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
$selectedIdJson = json_encode($selectedId, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
$pageTitle = '메뉴 관리';
$extraHead = '<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/jstree@3.3.17/dist/themes/default/style.min.css">';
$extraScripts = <<<HTML
<script src="https://cdn.jsdelivr.net/npm/jquery@3.7.1/dist/jquery.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jstree@3.3.17/dist/jstree.min.js"></script>
<script>
document.addEventListener('DOMContentLoaded', function () {
    var selectedId = {$selectedIdJson};
    $('#adminMenuTree').jstree({
        core: { data: {$treeNodesJson} }
    }).on('select_node.jstree', function (event, data) {
        if (data && data.node && data.node.id && data.node.id !== selectedId) {
            window.location.href = '/admin/management/menus.php?id=' + encodeURIComponent(data.node.id);
        }
    }).on('ready.jstree', function () {
        if (selectedId) {
            $('#adminMenuTree').jstree('select_node', selectedId);
        }
    });
});
</script>
HTML;
require __DIR__ . '/../../partials/header.php';
?>

<section class="mb-4">
    <div>
        <h1>메뉴 관리</h1>
        <p class="text-secondary mb-0">관리자 메뉴 계층과 게시판 연결을 관리합니다.</p>
    </div>
</section>

<?php if ($flash !== ''): ?><div class="alert alert-success"><?= e((string) $flash) ?></div><?php endif; ?>
<?php if ($error !== ''): ?><div class="alert alert-danger"><?= e($error) ?></div><?php endif; ?>

<section class="row g-3">
    <div class="col-12 col-lg-4">
        <div class="card shadow-sm h-100">
            <div class="card-header bg-white d-flex justify-content-between align-items-center">
                <h2 class="h5 mb-0">메뉴 트리</h2>
                <a class="btn btn-primary btn-sm" href="/admin/management/menus.php">신규</a>
            </div>
            <div class="card-body">
                <div id="adminMenuTree" class="admin-menu-tree"></div>
            </div>
        </div>
    </div>
    <div class="col-12 col-lg-8">
        <form class="card shadow-sm" method="post">
            <div class="card-header bg-white"><h2 class="h5 mb-0"><?= (int) $editingMenu['adm_menu_seq'] > 0 ? '메뉴 수정' : '메뉴 등록' ?></h2></div>
            <div class="card-body">
                <input type="hidden" name="id" value="<?= e((string) $editingMenu['adm_menu_seq']) ?>">
                <div class="row g-3">
                    <div class="col-12 col-md-6">
                        <label class="form-label">상위 메뉴</label>
                        <select class="form-select" name="parent_adm_menu_seq">
                            <option value="0">최상위 메뉴</option>
                            <?php foreach ($menus as $menu): ?>
                                <?php if ((int) ($menu['adm_menu_seq'] ?? 0) === (int) $editingMenu['adm_menu_seq'] || (int) ($menu['depth_no'] ?? 1) >= 3) { continue; } ?>
                                <option value="<?= e((string) ($menu['adm_menu_seq'] ?? 0)) ?>" <?= (int) $editingMenu['parent_adm_menu_seq'] === (int) ($menu['adm_menu_seq'] ?? 0) ? 'selected' : '' ?>>
                                    <?= e(str_repeat('└ ', max(0, (int) ($menu['depth_no'] ?? 1) - 1)) . ($menu['menu_nm'] ?? '')) ?>
                                </option>
                            <?php endforeach; ?>
                        </select>
                    </div>
                    <div class="col-12 col-md-6">
                        <label class="form-label">표시 순서</label>
                        <input class="form-control" type="number" name="sort_ord" value="<?= e((string) $editingMenu['sort_ord']) ?>">
                    </div>
                    <div class="col-12 col-md-6">
                        <label class="form-label">메뉴 코드</label>
                        <input class="form-control" name="menu_cd" value="<?= e($editingMenu['menu_cd']) ?>" required>
                    </div>
                    <div class="col-12 col-md-6">
                        <label class="form-label">메뉴명</label>
                        <input class="form-control" name="menu_nm" value="<?= e($editingMenu['menu_nm']) ?>" required>
                    </div>
                    <div class="col-12">
                        <label class="form-label">메뉴 URL</label>
                        <input class="form-control" name="menu_url" value="<?= e($editingMenu['menu_url']) ?>">
                    </div>
                    <div class="col-12">
                        <label class="form-label">활성 경로</label>
                        <textarea class="form-control" name="match_urls_text" rows="4"><?= e($editingMenu['match_urls_text']) ?></textarea>
                    </div>
                    <div class="col-12 col-md-4">
                        <label class="form-label">메뉴 유형</label>
                        <select class="form-select" name="menu_type">
                            <?php foreach (['group' => '그룹', 'page' => '페이지', 'board' => '게시판'] as $value => $label): ?>
                                <option value="<?= e($value) ?>" <?= $editingMenu['menu_type'] === $value ? 'selected' : '' ?>><?= e($label) ?></option>
                            <?php endforeach; ?>
                        </select>
                    </div>
                    <div class="col-12 col-md-4">
                        <label class="form-label">게시판 키</label>
                        <input class="form-control" name="board_key" value="<?= e($editingMenu['board_key']) ?>" placeholder="ai_news">
                    </div>
                    <div class="col-12 col-md-4">
                        <label class="form-label">사용여부</label>
                        <select class="form-select" name="use_yn">
                            <option value="Y" <?= $editingMenu['use_yn'] === 'Y' ? 'selected' : '' ?>>사용</option>
                            <option value="N" <?= $editingMenu['use_yn'] === 'N' ? 'selected' : '' ?>>미사용</option>
                        </select>
                    </div>
                </div>
            </div>
            <div class="card-footer bg-white detail-action-bar">
                <div>
                    <a class="btn btn-outline-secondary" href="/admin/management/admins.php">목록</a>
                </div>
                <div class="d-flex gap-2">
                    <?php if ((int) $editingMenu['adm_menu_seq'] > 0): ?>
                        <button class="btn btn-danger" type="submit" name="action" value="delete" onclick="return confirm('이 메뉴를 삭제 처리하시겠습니까?');">삭제</button>
                    <?php endif; ?>
                    <button class="btn btn-success" type="submit" name="action" value="save">저장</button>
                </div>
            </div>
        </form>
    </div>
</section>

<?php require __DIR__ . '/../../partials/footer.php'; ?>
