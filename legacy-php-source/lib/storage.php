<?php

declare(strict_types=1);

require_once __DIR__ . '/../config.php';
require_once __DIR__ . '/database.php';

function admin_table_name(): string
{
    return 'co_adm_mst';
}

function admin_change_table_name(): string
{
    return 'co_adm_mod_mst';
}

function admin_change_detail_table_name(): string
{
    return 'co_adm_mod_dtl';
}

function admin_menu_table_name(): string
{
    return 'co_adm_menu_mst';
}

function dashboard_panel_table_name(): string
{
    return 'co_dashboard_panel_mst';
}

function default_admin_menu_seed(): array
{
    return [
        ['menu_cd' => 'admin_home', 'parent_menu_cd' => '', 'depth_no' => 1, 'menu_nm' => '관리자 홈', 'menu_url' => '/admin/', 'match_urls' => ['/admin/'], 'menu_type' => 'page', 'board_key' => '', 'use_yn' => 'Y', 'sort_ord' => 10],
        ['menu_cd' => 'management', 'parent_menu_cd' => '', 'depth_no' => 1, 'menu_nm' => '관리자 관리', 'menu_url' => '/admin/management/admins.php', 'match_urls' => [], 'menu_type' => 'group', 'board_key' => '', 'use_yn' => 'Y', 'sort_ord' => 20],
        ['menu_cd' => 'management_admins', 'parent_menu_cd' => 'management', 'depth_no' => 2, 'menu_nm' => '관리자 목록', 'menu_url' => '/admin/management/admins.php', 'match_urls' => ['/admin/management/admins.php', '/admin/management/admin_edit.php'], 'menu_type' => 'page', 'board_key' => '', 'use_yn' => 'Y', 'sort_ord' => 10],
        ['menu_cd' => 'management_access_logs', 'parent_menu_cd' => 'management', 'depth_no' => 2, 'menu_nm' => '관리자 접속이력', 'menu_url' => '/admin/management/access_logs.php', 'match_urls' => ['/admin/management/access_logs.php'], 'menu_type' => 'page', 'board_key' => '', 'use_yn' => 'Y', 'sort_ord' => 20],
        ['menu_cd' => 'management_change_logs', 'parent_menu_cd' => 'management', 'depth_no' => 2, 'menu_nm' => '관리자 수정이력', 'menu_url' => '/admin/management/change_logs.php', 'match_urls' => ['/admin/management/change_logs.php', '/admin/management/change_log_view.php'], 'menu_type' => 'page', 'board_key' => '', 'use_yn' => 'Y', 'sort_ord' => 30],
        ['menu_cd' => 'management_menus', 'parent_menu_cd' => 'management', 'depth_no' => 2, 'menu_nm' => '메뉴 관리', 'menu_url' => '/admin/management/menus.php', 'match_urls' => ['/admin/management/menus.php'], 'menu_type' => 'page', 'board_key' => '', 'use_yn' => 'Y', 'sort_ord' => 40],
        ['menu_cd' => 'management_dashboard_edit', 'parent_menu_cd' => 'management', 'depth_no' => 2, 'menu_nm' => '대시보드 수정', 'menu_url' => '/admin/management/dashboard_edit.php', 'match_urls' => ['/admin/management/dashboard_edit.php'], 'menu_type' => 'page', 'board_key' => '', 'use_yn' => 'Y', 'sort_ord' => 50],
        ['menu_cd' => 'forms', 'parent_menu_cd' => '', 'depth_no' => 1, 'menu_nm' => '설문 운영', 'menu_url' => '/admin/survey/forms.php', 'match_urls' => [], 'menu_type' => 'group', 'board_key' => '', 'use_yn' => 'Y', 'sort_ord' => 30],
        ['menu_cd' => 'forms_manage', 'parent_menu_cd' => 'forms', 'depth_no' => 2, 'menu_nm' => '설문 관리', 'menu_url' => '/admin/survey/forms.php', 'match_urls' => ['/admin/survey/forms.php', '/admin/survey/form_edit.php'], 'menu_type' => 'page', 'board_key' => '', 'use_yn' => 'Y', 'sort_ord' => 10],
        ['menu_cd' => 'forms_submissions', 'parent_menu_cd' => 'forms', 'depth_no' => 2, 'menu_nm' => '설문 이력 관리', 'menu_url' => '/admin/survey/submissions.php', 'match_urls' => ['/admin/survey/submissions.php', '/admin/survey/submission_view.php', '/admin/survey/submissions_export.php'], 'menu_type' => 'page', 'board_key' => '', 'use_yn' => 'Y', 'sort_ord' => 20],
        ['menu_cd' => 'news', 'parent_menu_cd' => '', 'depth_no' => 1, 'menu_nm' => '뉴스', 'menu_url' => '/admin/news/ai_news.php', 'match_urls' => [], 'menu_type' => 'group', 'board_key' => '', 'use_yn' => 'Y', 'sort_ord' => 40],
        ['menu_cd' => 'news_ai_news', 'parent_menu_cd' => 'news', 'depth_no' => 2, 'menu_nm' => 'AI News', 'menu_url' => '/admin/news/ai_news.php', 'match_urls' => ['/admin/news/ai_news.php', '/admin/news/ai_news_view.php', '/admin/news/ai_news_edit.php'], 'menu_type' => 'board', 'board_key' => 'ai_news', 'use_yn' => 'Y', 'sort_ord' => 10],
    ];
}

function default_admin_account(): array
{
    $now = date('Y-m-d H:i:s');

    return [
        'login_id' => 'admin',
        'name' => '관리자',
        'role' => 'super',
        'status' => 'active',
        'password_hash' => password_hash(ADMIN_PASSWORD, PASSWORD_DEFAULT),
        'reg_dtm' => $now,
        'reg_id' => 'admin',
        'mod_dtm' => $now,
        'mod_id' => 'admin',
    ];
}

function normalize_admin_record(array $admin): array
{
    $admin['adm_seq'] = isset($admin['adm_seq']) ? (int) $admin['adm_seq'] : (isset($admin['idx']) ? (int) $admin['idx'] : 0);
    $admin['idx'] = $admin['adm_seq'];

    if (($admin['login_id'] ?? '') === 'admin') {
        $admin['role'] = 'super';
    }

    if (!in_array(($admin['role'] ?? 'admin'), ['super', 'admin'], true)) {
        $admin['role'] = 'admin';
    }

    if (!in_array(($admin['status'] ?? 'active'), ['active', 'inactive'], true)) {
        $admin['status'] = 'active';
    }

    $admin['reg_dtm'] = (string) ($admin['reg_dtm'] ?? current_time());
    $admin['reg_id'] = (string) ($admin['reg_id'] ?? $admin['login_id'] ?? 'system');
    $admin['mod_dtm'] = (string) ($admin['mod_dtm'] ?? $admin['reg_dtm'] ?? current_time());
    $admin['mod_id'] = (string) ($admin['mod_id'] ?? $admin['reg_id'] ?? $admin['login_id'] ?? 'system');

    return $admin;
}

function admin_sequence_column_name(): string
{
    return db_column_exists(admin_table_name(), 'adm_seq') ? 'adm_seq' : 'idx';
}

function ensure_storage(): void
{
    if (!is_dir(DATA_DIR)) {
        mkdir(DATA_DIR, 0775, true);
    }

    foreach (['admins.json', 'admin_access_logs.json', 'admin_change_logs.json', 'forms.json', 'submissions.json'] as $file) {
        $path = DATA_DIR . '/' . $file;
        if (!file_exists($path)) {
            $seed = [];
            file_put_contents($path, json_encode($seed, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
        }
    }
}

function db_apply_schema_comments(string $table, array $columnDefinitions, string $tableComment): void
{
    if (!db_table_exists($table)) {
        return;
    }

    $tableSql = '`' . str_replace('`', '``', $table) . '`';

    foreach ($columnDefinitions as $column => $definition) {
        if (!db_column_exists($table, (string) $column)) {
            continue;
        }

        $columnSql = '`' . str_replace('`', '``', (string) $column) . '`';
        db_execute(sprintf(
            'ALTER TABLE %s MODIFY COLUMN %s %s COMMENT %s',
            $tableSql,
            $columnSql,
            $definition['sql'],
            db_pdo()->quote($definition['comment'])
        ));
    }

    db_execute(sprintf('ALTER TABLE %s COMMENT = %s', $tableSql, db_pdo()->quote($tableComment)));
}

function ensure_admin_schema(): void
{
    static $initialized = false;

    if ($initialized) {
        return;
    }

    $table = admin_table_name();
    $tableSql = '`' . $table . '`';
    $legacyTableSql = '`admins`';

    if (!db_table_exists($table)) {
        if (db_table_exists('admins')) {
            db_execute(sprintf('RENAME TABLE %s TO %s', $legacyTableSql, $tableSql));
        }
    }

    if (db_table_exists($table) && db_column_exists($table, 'idx') && !db_column_exists($table, 'adm_seq')) {
        if (!(db_table_exists('co_adm_mod_dtl') && db_column_exists('co_adm_mod_dtl', 'changes_json'))) {
            db_execute(sprintf('ALTER TABLE %s CHANGE COLUMN idx adm_seq INT NOT NULL AUTO_INCREMENT', $tableSql));
        }
    }

    db_execute(
        <<<SQL
        CREATE TABLE IF NOT EXISTS {$tableSql} (
            adm_seq INT NOT NULL AUTO_INCREMENT COMMENT '관리자 일련번호',
            login_id VARCHAR(100) NOT NULL COMMENT '관리자 로그인 아이디',
            name VARCHAR(100) NOT NULL COMMENT '관리자명',
            role VARCHAR(20) NOT NULL DEFAULT 'admin' COMMENT '관리자 권한',
            status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '사용 상태',
            password_hash VARCHAR(255) NOT NULL COMMENT '비밀번호 해시',
            reg_dtm DATETIME NOT NULL COMMENT '등록 일시',
            reg_id VARCHAR(100) NOT NULL COMMENT '등록자 아이디',
            mod_dtm DATETIME NOT NULL COMMENT '수정 일시',
            mod_id VARCHAR(100) NOT NULL COMMENT '수정자 아이디',
            PRIMARY KEY (adm_seq),
            UNIQUE KEY uq_co_adm_mst_login_id (login_id),
            KEY idx_co_adm_mst_reg_dtm (reg_dtm)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='관리자 계정 마스터'
        SQL
    );

    if (db_table_exists('admins') && db_table_exists($table)) {
        db_execute('DROP TABLE IF EXISTS admins');
    }

    if (db_column_exists($table, 'created_at')) {
        db_execute(sprintf('ALTER TABLE %s DROP COLUMN created_at', $tableSql));
    }
    if (db_column_exists($table, 'updated_at')) {
        db_execute(sprintf('ALTER TABLE %s DROP COLUMN updated_at', $tableSql));
    }

    db_apply_schema_comments($table, [
        'adm_seq' => ['sql' => 'INT NOT NULL AUTO_INCREMENT', 'comment' => '관리자 일련번호'],
        'login_id' => ['sql' => 'VARCHAR(100) NOT NULL', 'comment' => '관리자 로그인 아이디'],
        'name' => ['sql' => 'VARCHAR(100) NOT NULL', 'comment' => '관리자명'],
        'role' => ['sql' => "VARCHAR(20) NOT NULL DEFAULT 'admin'", 'comment' => '관리자 권한'],
        'status' => ['sql' => "VARCHAR(20) NOT NULL DEFAULT 'active'", 'comment' => '사용 상태'],
        'password_hash' => ['sql' => 'VARCHAR(255) NOT NULL', 'comment' => '비밀번호 해시'],
        'reg_dtm' => ['sql' => 'DATETIME NOT NULL', 'comment' => '등록 일시'],
        'reg_id' => ['sql' => 'VARCHAR(100) NOT NULL', 'comment' => '등록자 아이디'],
        'mod_dtm' => ['sql' => 'DATETIME NOT NULL', 'comment' => '수정 일시'],
        'mod_id' => ['sql' => 'VARCHAR(100) NOT NULL', 'comment' => '수정자 아이디'],
    ], '관리자 계정 마스터');

    $count = (int) (db_fetch_one(sprintf('SELECT COUNT(*) AS count FROM %s', $tableSql))['count'] ?? 0);
    if ($count === 0) {
        $admin = normalize_admin_record(default_admin_account());
        db_execute(
            sprintf(
                'INSERT INTO %s (login_id, name, role, status, password_hash, reg_dtm, reg_id, mod_dtm, mod_id)
             VALUES (:login_id, :name, :role, :status, :password_hash, :reg_dtm, :reg_id, :mod_dtm, :mod_id)',
                $tableSql
            ),
            [
                ':login_id' => (string) ($admin['login_id'] ?? ''),
                ':name' => (string) ($admin['name'] ?? ''),
                ':role' => (string) ($admin['role'] ?? 'admin'),
                ':status' => (string) ($admin['status'] ?? 'active'),
                ':password_hash' => (string) ($admin['password_hash'] ?? ''),
                ':reg_dtm' => substr((string) ($admin['reg_dtm'] ?? current_time()), 0, 19),
                ':reg_id' => (string) ($admin['reg_id'] ?? $admin['login_id'] ?? 'system'),
                ':mod_dtm' => substr((string) ($admin['mod_dtm'] ?? current_time()), 0, 19),
                ':mod_id' => (string) ($admin['mod_id'] ?? $admin['login_id'] ?? 'system'),
            ]
        );
    }

    write_json_file('admins.json', []);

    $initialized = true;
}

function normalize_admin_menu_record(array $menu): array
{
    $menu['adm_menu_seq'] = isset($menu['adm_menu_seq']) ? (int) $menu['adm_menu_seq'] : 0;
    $menu['parent_adm_menu_seq'] = isset($menu['parent_adm_menu_seq']) && $menu['parent_adm_menu_seq'] !== null ? (int) $menu['parent_adm_menu_seq'] : 0;
    $menu['depth_no'] = max(1, min(3, (int) ($menu['depth_no'] ?? 1)));
    $menu['menu_cd'] = trim((string) ($menu['menu_cd'] ?? ''));
    $menu['menu_nm'] = trim((string) ($menu['menu_nm'] ?? ''));
    $menu['menu_url'] = trim((string) ($menu['menu_url'] ?? ''));
    $menu['menu_type'] = in_array(($menu['menu_type'] ?? 'page'), ['group', 'page', 'board'], true) ? (string) $menu['menu_type'] : 'page';
    $menu['board_key'] = trim((string) ($menu['board_key'] ?? ''));
    $menu['use_yn'] = strtoupper((string) ($menu['use_yn'] ?? 'Y')) === 'N' ? 'N' : 'Y';
    $menu['delete_flg'] = strtoupper((string) ($menu['delete_flg'] ?? 'N')) === 'Y' ? 'Y' : 'N';
    $menu['sort_ord'] = (int) ($menu['sort_ord'] ?? 0);
    $matchUrls = $menu['match_urls_json'] ?? $menu['match_urls'] ?? [];
    if (is_string($matchUrls)) {
        $decoded = json_decode($matchUrls, true);
        $matchUrls = is_array($decoded) ? $decoded : [];
    }
    $menu['match_urls'] = array_values(array_filter(array_map('trim', is_array($matchUrls) ? $matchUrls : []), static fn (string $value): bool => $value !== ''));
    $menu['match_urls_json'] = json_encode($menu['match_urls'], JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    $menu['reg_dtm'] = (string) ($menu['reg_dtm'] ?? current_time());
    $menu['reg_id'] = (string) ($menu['reg_id'] ?? 'system');
    $menu['mod_dtm'] = (string) ($menu['mod_dtm'] ?? $menu['reg_dtm'] ?? current_time());
    $menu['mod_id'] = (string) ($menu['mod_id'] ?? $menu['reg_id'] ?? 'system');

    return $menu;
}

function ensure_admin_menu_schema(): void
{
    static $initialized = false;

    if ($initialized) {
        return;
    }

    $table = admin_menu_table_name();
    $tableSql = '`' . $table . '`';

    db_execute(
        <<<SQL
        CREATE TABLE IF NOT EXISTS {$tableSql} (
            adm_menu_seq INT NOT NULL AUTO_INCREMENT COMMENT '관리자 메뉴 일련번호',
            parent_adm_menu_seq INT NULL COMMENT '상위 관리자 메뉴 일련번호',
            depth_no TINYINT NOT NULL DEFAULT 1 COMMENT '메뉴 깊이',
            menu_cd VARCHAR(100) NOT NULL COMMENT '메뉴 코드',
            menu_nm VARCHAR(100) NOT NULL COMMENT '메뉴명',
            menu_url VARCHAR(255) NOT NULL DEFAULT '' COMMENT '메뉴 URL',
            match_urls_json LONGTEXT NULL COMMENT '활성 경로 JSON',
            menu_type VARCHAR(20) NOT NULL DEFAULT 'page' COMMENT '메뉴 유형',
            board_key VARCHAR(100) NOT NULL DEFAULT '' COMMENT '게시판 연결 키',
            use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
            delete_flg CHAR(1) NOT NULL DEFAULT 'N' COMMENT '삭제 여부',
            sort_ord INT NOT NULL DEFAULT 0 COMMENT '표시 순서',
            reg_dtm DATETIME NOT NULL COMMENT '등록 일시',
            reg_id VARCHAR(100) NOT NULL COMMENT '등록자 아이디',
            mod_dtm DATETIME NOT NULL COMMENT '수정 일시',
            mod_id VARCHAR(100) NOT NULL COMMENT '수정자 아이디',
            PRIMARY KEY (adm_menu_seq),
            UNIQUE KEY uq_co_adm_menu_mst_menu_cd (menu_cd),
            KEY idx_co_adm_menu_mst_parent (parent_adm_menu_seq),
            KEY idx_co_adm_menu_mst_depth_sort (depth_no, sort_ord),
            KEY idx_co_adm_menu_mst_use_delete (use_yn, delete_flg)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='관리자 메뉴 마스터'
        SQL
    );

    db_apply_schema_comments($table, [
        'adm_menu_seq' => ['sql' => 'INT NOT NULL AUTO_INCREMENT', 'comment' => '관리자 메뉴 일련번호'],
        'parent_adm_menu_seq' => ['sql' => 'INT NULL', 'comment' => '상위 관리자 메뉴 일련번호'],
        'depth_no' => ['sql' => 'TINYINT NOT NULL DEFAULT 1', 'comment' => '메뉴 깊이'],
        'menu_cd' => ['sql' => 'VARCHAR(100) NOT NULL', 'comment' => '메뉴 코드'],
        'menu_nm' => ['sql' => 'VARCHAR(100) NOT NULL', 'comment' => '메뉴명'],
        'menu_url' => ['sql' => "VARCHAR(255) NOT NULL DEFAULT ''", 'comment' => '메뉴 URL'],
        'match_urls_json' => ['sql' => 'LONGTEXT NULL', 'comment' => '활성 경로 JSON'],
        'menu_type' => ['sql' => "VARCHAR(20) NOT NULL DEFAULT 'page'", 'comment' => '메뉴 유형'],
        'board_key' => ['sql' => "VARCHAR(100) NOT NULL DEFAULT ''", 'comment' => '게시판 연결 키'],
        'use_yn' => ['sql' => "CHAR(1) NOT NULL DEFAULT 'Y'", 'comment' => '사용 여부'],
        'delete_flg' => ['sql' => "CHAR(1) NOT NULL DEFAULT 'N'", 'comment' => '삭제 여부'],
        'sort_ord' => ['sql' => 'INT NOT NULL DEFAULT 0', 'comment' => '표시 순서'],
        'reg_dtm' => ['sql' => 'DATETIME NOT NULL', 'comment' => '등록 일시'],
        'reg_id' => ['sql' => 'VARCHAR(100) NOT NULL', 'comment' => '등록자 아이디'],
        'mod_dtm' => ['sql' => 'DATETIME NOT NULL', 'comment' => '수정 일시'],
        'mod_id' => ['sql' => 'VARCHAR(100) NOT NULL', 'comment' => '수정자 아이디'],
    ], '관리자 메뉴 마스터');

    $seqByCode = [];
    foreach (default_admin_menu_seed() as $seed) {
        $seed = normalize_admin_menu_record($seed);
        $existing = db_fetch_one(sprintf('SELECT * FROM %s WHERE menu_cd = :menu_cd LIMIT 1', $tableSql), [':menu_cd' => $seed['menu_cd']]);
        $parentSeq = 0;
        if ((string) ($seed['parent_menu_cd'] ?? '') !== '') {
            $parentSeq = (int) ($seqByCode[(string) $seed['parent_menu_cd']] ?? 0);
        }

        if ($existing) {
            $seqByCode[$seed['menu_cd']] = (int) ($existing['adm_menu_seq'] ?? 0);
            continue;
        }

        db_execute(
            sprintf(
                'INSERT INTO %s
                    (parent_adm_menu_seq, depth_no, menu_cd, menu_nm, menu_url, match_urls_json, menu_type, board_key, use_yn, delete_flg, sort_ord, reg_dtm, reg_id, mod_dtm, mod_id)
                 VALUES
                    (:parent_adm_menu_seq, :depth_no, :menu_cd, :menu_nm, :menu_url, :match_urls_json, :menu_type, :board_key, :use_yn, :delete_flg, :sort_ord, :reg_dtm, :reg_id, :mod_dtm, :mod_id)',
                $tableSql
            ),
            [
                ':parent_adm_menu_seq' => $parentSeq > 0 ? $parentSeq : null,
                ':depth_no' => $seed['depth_no'],
                ':menu_cd' => $seed['menu_cd'],
                ':menu_nm' => $seed['menu_nm'],
                ':menu_url' => $seed['menu_url'],
                ':match_urls_json' => $seed['match_urls_json'],
                ':menu_type' => $seed['menu_type'],
                ':board_key' => $seed['board_key'],
                ':use_yn' => $seed['use_yn'],
                ':delete_flg' => 'N',
                ':sort_ord' => $seed['sort_ord'],
                ':reg_dtm' => current_time(),
                ':reg_id' => 'system',
                ':mod_dtm' => current_time(),
                ':mod_id' => 'system',
            ]
        );
        $seqByCode[$seed['menu_cd']] = (int) db_pdo()->lastInsertId();
    }

    $initialized = true;
}

function ensure_admin_change_schema(): void
{
    static $initialized = false;

    if ($initialized) {
        return;
    }

    ensure_admin_schema();

    $adminTable = admin_table_name();
    $masterTable = admin_change_table_name();
    $detailTable = admin_change_detail_table_name();
    $adminTableSql = '`' . $adminTable . '`';
    $masterTableSql = '`' . $masterTable . '`';
    $detailTableSql = '`' . $detailTable . '`';

    $legacyRows = [];
    if (db_table_exists($detailTable) && db_column_exists($detailTable, 'changes_json')) {
        $legacyRows = db_fetch_all(sprintf('SELECT * FROM %s ORDER BY idx ASC', $detailTableSql));
        db_execute('DROP TABLE IF EXISTS `' . $detailTable . '`');
        db_execute('DROP TABLE IF EXISTS `' . $masterTable . '`');
    }

    if (db_table_exists($adminTable) && db_column_exists($adminTable, 'idx') && !db_column_exists($adminTable, 'adm_seq')) {
        db_execute(sprintf('ALTER TABLE %s CHANGE COLUMN idx adm_seq INT NOT NULL AUTO_INCREMENT', $adminTableSql));
    }

    db_execute(
        <<<SQL
        CREATE TABLE IF NOT EXISTS {$masterTableSql} (
            mod_seq INT NOT NULL AUTO_INCREMENT COMMENT '수정이력 일련번호',
            target_adm_seq INT NOT NULL COMMENT '변경 대상 관리자 일련번호',
            changer_adm_seq INT NOT NULL COMMENT '변경 처리 관리자 일련번호',
            target_adm_login_id VARCHAR(100) NOT NULL COMMENT '변경 대상 관리자 아이디',
            target_adm_name VARCHAR(100) NOT NULL COMMENT '변경 대상 관리자명',
            changer_adm_login_id VARCHAR(100) NOT NULL COMMENT '변경 처리 관리자 아이디',
            changer_adm_name VARCHAR(100) NOT NULL COMMENT '변경 처리 관리자명',
            action VARCHAR(20) NOT NULL COMMENT '변경 구분',
            changed_at DATETIME NOT NULL COMMENT '변경 일시',
            reg_dtm DATETIME NOT NULL COMMENT '등록 일시',
            reg_id VARCHAR(100) NOT NULL COMMENT '등록자 아이디',
            mod_dtm DATETIME NOT NULL COMMENT '수정 일시',
            mod_id VARCHAR(100) NOT NULL COMMENT '수정자 아이디',
            PRIMARY KEY (mod_seq),
            KEY idx_co_adm_mod_mst_target_adm_seq (target_adm_seq),
            KEY idx_co_adm_mod_mst_changer_adm_seq (changer_adm_seq),
            KEY idx_co_adm_mod_mst_changed_at (changed_at),
            CONSTRAINT fk_co_adm_mod_mst_target_adm_seq FOREIGN KEY (target_adm_seq) REFERENCES {$adminTableSql} (adm_seq),
            CONSTRAINT fk_co_adm_mod_mst_changer_adm_seq FOREIGN KEY (changer_adm_seq) REFERENCES {$adminTableSql} (adm_seq)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='관리자 수정이력 마스터'
        SQL
    );

    db_execute(
        <<<SQL
        CREATE TABLE IF NOT EXISTS {$detailTableSql} (
            mod_dtl_seq INT NOT NULL AUTO_INCREMENT COMMENT '수정이력 상세 일련번호',
            mod_seq INT NOT NULL COMMENT '수정이력 마스터 일련번호',
            column_name VARCHAR(100) NOT NULL COMMENT '변경 컬럼명',
            column_label VARCHAR(100) NOT NULL COMMENT '변경 항목명',
            before_value LONGTEXT NULL COMMENT '변경 전 값',
            after_value LONGTEXT NULL COMMENT '변경 후 값',
            reg_dtm DATETIME NOT NULL COMMENT '등록 일시',
            reg_id VARCHAR(100) NOT NULL COMMENT '등록자 아이디',
            mod_dtm DATETIME NOT NULL COMMENT '수정 일시',
            mod_id VARCHAR(100) NOT NULL COMMENT '수정자 아이디',
            PRIMARY KEY (mod_dtl_seq),
            KEY idx_co_adm_mod_dtl_mod_seq (mod_seq),
            CONSTRAINT fk_co_adm_mod_dtl_mod_seq FOREIGN KEY (mod_seq) REFERENCES {$masterTableSql} (mod_seq)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='관리자 수정이력 상세'
        SQL
    );

    db_apply_schema_comments($masterTable, [
        'mod_seq' => ['sql' => 'INT NOT NULL AUTO_INCREMENT', 'comment' => '수정이력 일련번호'],
        'target_adm_seq' => ['sql' => 'INT NOT NULL', 'comment' => '변경 대상 관리자 일련번호'],
        'changer_adm_seq' => ['sql' => 'INT NOT NULL', 'comment' => '변경 처리 관리자 일련번호'],
        'target_adm_login_id' => ['sql' => 'VARCHAR(100) NOT NULL', 'comment' => '변경 대상 관리자 아이디'],
        'target_adm_name' => ['sql' => 'VARCHAR(100) NOT NULL', 'comment' => '변경 대상 관리자명'],
        'changer_adm_login_id' => ['sql' => 'VARCHAR(100) NOT NULL', 'comment' => '변경 처리 관리자 아이디'],
        'changer_adm_name' => ['sql' => 'VARCHAR(100) NOT NULL', 'comment' => '변경 처리 관리자명'],
        'action' => ['sql' => 'VARCHAR(20) NOT NULL', 'comment' => '변경 구분'],
        'changed_at' => ['sql' => 'DATETIME NOT NULL', 'comment' => '변경 일시'],
        'reg_dtm' => ['sql' => 'DATETIME NOT NULL', 'comment' => '등록 일시'],
        'reg_id' => ['sql' => 'VARCHAR(100) NOT NULL', 'comment' => '등록자 아이디'],
        'mod_dtm' => ['sql' => 'DATETIME NOT NULL', 'comment' => '수정 일시'],
        'mod_id' => ['sql' => 'VARCHAR(100) NOT NULL', 'comment' => '수정자 아이디'],
    ], '관리자 수정이력 마스터');

    db_apply_schema_comments($detailTable, [
        'mod_dtl_seq' => ['sql' => 'INT NOT NULL AUTO_INCREMENT', 'comment' => '수정이력 상세 일련번호'],
        'mod_seq' => ['sql' => 'INT NOT NULL', 'comment' => '수정이력 마스터 일련번호'],
        'column_name' => ['sql' => 'VARCHAR(100) NOT NULL', 'comment' => '변경 컬럼명'],
        'column_label' => ['sql' => 'VARCHAR(100) NOT NULL', 'comment' => '변경 항목명'],
        'before_value' => ['sql' => 'LONGTEXT NULL', 'comment' => '변경 전 값'],
        'after_value' => ['sql' => 'LONGTEXT NULL', 'comment' => '변경 후 값'],
        'reg_dtm' => ['sql' => 'DATETIME NOT NULL', 'comment' => '등록 일시'],
        'reg_id' => ['sql' => 'VARCHAR(100) NOT NULL', 'comment' => '등록자 아이디'],
        'mod_dtm' => ['sql' => 'DATETIME NOT NULL', 'comment' => '수정 일시'],
        'mod_id' => ['sql' => 'VARCHAR(100) NOT NULL', 'comment' => '수정자 아이디'],
    ], '관리자 수정이력 상세');

    foreach ($legacyRows as $legacyRow) {
        $legacyModSeq = isset($legacyRow['idx']) ? (int) $legacyRow['idx'] : 0;
        if ($legacyModSeq <= 0) {
            continue;
        }

        $changes = [];
        $decoded = json_decode((string) ($legacyRow['changes_json'] ?? '[]'), true);
        if (is_array($decoded)) {
            $changes = $decoded;
        }

        db_execute(
            sprintf(
                'INSERT INTO %s (mod_seq, target_adm_seq, changer_adm_seq, target_adm_login_id, target_adm_name, changer_adm_login_id, changer_adm_name, action, changed_at, reg_dtm, reg_id, mod_dtm, mod_id)
                 VALUES (:mod_seq, :target_adm_seq, :changer_adm_seq, :target_adm_login_id, :target_adm_name, :changer_adm_login_id, :changer_adm_name, :action, :changed_at, :reg_dtm, :reg_id, :mod_dtm, :mod_id)',
                $masterTableSql
            ),
            [
                ':mod_seq' => $legacyModSeq,
                ':target_adm_seq' => (int) ($legacyRow['target_admin_idx'] ?? 0),
                ':changer_adm_seq' => (int) ($legacyRow['changer_admin_idx'] ?? 0),
                ':target_adm_login_id' => (string) ($legacyRow['target_admin_login_id'] ?? ''),
                ':target_adm_name' => (string) ($legacyRow['target_admin_name'] ?? ''),
                ':changer_adm_login_id' => (string) ($legacyRow['changer_admin_login_id'] ?? ''),
                ':changer_adm_name' => (string) ($legacyRow['changer_admin_name'] ?? ''),
                ':action' => (string) ($legacyRow['action'] ?? 'update'),
                ':changed_at' => substr((string) ($legacyRow['changed_at'] ?? current_time()), 0, 19),
                ':reg_dtm' => substr((string) ($legacyRow['reg_dtm'] ?? current_time()), 0, 19),
                ':reg_id' => (string) ($legacyRow['reg_id'] ?? $legacyRow['changer_admin_login_id'] ?? 'system'),
                ':mod_dtm' => substr((string) ($legacyRow['mod_dtm'] ?? current_time()), 0, 19),
                ':mod_id' => (string) ($legacyRow['mod_id'] ?? $legacyRow['changer_admin_login_id'] ?? 'system'),
            ]
        );

        foreach ($changes as $offset => $change) {
            db_execute(
                sprintf(
                    'INSERT INTO %s (mod_seq, column_name, column_label, before_value, after_value, reg_dtm, reg_id, mod_dtm, mod_id)
                     VALUES (:mod_seq, :column_name, :column_label, :before_value, :after_value, :reg_dtm, :reg_id, :mod_dtm, :mod_id)',
                    $detailTableSql
                ),
                [
                    ':mod_seq' => $legacyModSeq,
                    ':column_name' => (string) ($change['field'] ?? ('field_' . $offset)),
                    ':column_label' => (string) ($change['label'] ?? ''),
                    ':before_value' => array_key_exists('before', $change) ? (string) $change['before'] : null,
                    ':after_value' => array_key_exists('after', $change) ? (string) $change['after'] : (string) ($change['summary'] ?? $change['value'] ?? ''),
                    ':reg_dtm' => substr((string) ($legacyRow['reg_dtm'] ?? current_time()), 0, 19),
                    ':reg_id' => (string) ($legacyRow['reg_id'] ?? $legacyRow['changer_admin_login_id'] ?? 'system'),
                    ':mod_dtm' => substr((string) ($legacyRow['mod_dtm'] ?? current_time()), 0, 19),
                    ':mod_id' => (string) ($legacyRow['mod_id'] ?? $legacyRow['changer_admin_login_id'] ?? 'system'),
                ]
            );
        }
    }

    $initialized = true;
}

function read_json_file(string $file): array
{
    ensure_storage();
    $path = DATA_DIR . '/' . $file;
    $json = file_get_contents($path);
    $data = json_decode($json ?: '[]', true);
    return is_array($data) ? $data : [];
}

function write_json_file(string $file, array $data): void
{
    ensure_storage();
    $path = DATA_DIR . '/' . $file;
    file_put_contents($path, json_encode($data, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE), LOCK_EX);
}

function all_admin_menus(bool $includeDeleted = false): array
{
    ensure_admin_menu_schema();
    $where = $includeDeleted ? '' : "WHERE delete_flg <> 'Y'";
    $menus = db_fetch_all(sprintf(
        'SELECT * FROM `%s` %s ORDER BY depth_no ASC, parent_adm_menu_seq ASC, sort_ord ASC, adm_menu_seq ASC',
        admin_menu_table_name(),
        $where
    ));
    return array_map('normalize_admin_menu_record', $menus);
}

function active_admin_menus(): array
{
    return array_values(array_filter(all_admin_menus(), static fn (array $menu): bool => ($menu['use_yn'] ?? 'Y') === 'Y'));
}

function find_admin_menu(string $id): ?array
{
    ensure_admin_menu_schema();
    $menuSeq = (int) $id;
    if ($menuSeq <= 0) {
        return null;
    }

    $menu = db_fetch_one(sprintf('SELECT * FROM `%s` WHERE adm_menu_seq = :adm_menu_seq LIMIT 1', admin_menu_table_name()), [':adm_menu_seq' => $menuSeq]);
    return $menu ? normalize_admin_menu_record($menu) : null;
}

function find_admin_menu_by_code(string $menuCd): ?array
{
    ensure_admin_menu_schema();
    $menuCd = trim($menuCd);
    if ($menuCd === '') {
        return null;
    }

    $menu = db_fetch_one(sprintf('SELECT * FROM `%s` WHERE menu_cd = :menu_cd LIMIT 1', admin_menu_table_name()), [':menu_cd' => $menuCd]);
    return $menu ? normalize_admin_menu_record($menu) : null;
}

function admin_menu_has_children(int $menuSeq): bool
{
    ensure_admin_menu_schema();
    if ($menuSeq <= 0) {
        return false;
    }

    $row = db_fetch_one(sprintf("SELECT COUNT(*) AS count FROM `%s` WHERE parent_adm_menu_seq = :adm_menu_seq AND delete_flg <> 'Y'", admin_menu_table_name()), [':adm_menu_seq' => $menuSeq]);
    return (int) ($row['count'] ?? 0) > 0;
}

function admin_menu_depth_for_parent(int $parentSeq): int
{
    if ($parentSeq <= 0) {
        return 1;
    }

    $parent = find_admin_menu((string) $parentSeq);
    return min(3, ((int) ($parent['depth_no'] ?? 1)) + 1);
}

function save_admin_menu_record(array $menu): int
{
    ensure_admin_menu_schema();
    $menu = normalize_admin_menu_record($menu);
    $menuSeq = (int) ($menu['adm_menu_seq'] ?? 0);
    $parentSeq = (int) ($menu['parent_adm_menu_seq'] ?? 0);
    $depthNo = admin_menu_depth_for_parent($parentSeq);
    $now = current_time();
    $actor = (string) ($_SESSION['admin_login_id'] ?? 'system');
    $boardKey = $menu['menu_type'] === 'board' ? (string) ($menu['board_key'] ?? '') : '';

    if ($menuSeq > 0 && find_admin_menu((string) $menuSeq)) {
        $existing = find_admin_menu((string) $menuSeq) ?? [];
        db_execute(
            sprintf(
                'UPDATE `%s`
                 SET parent_adm_menu_seq = :parent_adm_menu_seq,
                     depth_no = :depth_no,
                     menu_cd = :menu_cd,
                     menu_nm = :menu_nm,
                     menu_url = :menu_url,
                     match_urls_json = :match_urls_json,
                     menu_type = :menu_type,
                     board_key = :board_key,
                     use_yn = :use_yn,
                     sort_ord = :sort_ord,
                     reg_dtm = :reg_dtm,
                     reg_id = :reg_id,
                     mod_dtm = :mod_dtm,
                     mod_id = :mod_id
                 WHERE adm_menu_seq = :adm_menu_seq',
                admin_menu_table_name()
            ),
            [
                ':adm_menu_seq' => $menuSeq,
                ':parent_adm_menu_seq' => $parentSeq > 0 ? $parentSeq : null,
                ':depth_no' => $depthNo,
                ':menu_cd' => $menu['menu_cd'],
                ':menu_nm' => $menu['menu_nm'],
                ':menu_url' => $menu['menu_url'],
                ':match_urls_json' => $menu['match_urls_json'],
                ':menu_type' => $menu['menu_type'],
                ':board_key' => $boardKey,
                ':use_yn' => $menu['use_yn'],
                ':sort_ord' => $menu['sort_ord'],
                ':reg_dtm' => substr((string) ($existing['reg_dtm'] ?? $now), 0, 19),
                ':reg_id' => (string) ($existing['reg_id'] ?? $actor),
                ':mod_dtm' => $now,
                ':mod_id' => $actor,
            ]
        );
        return $menuSeq;
    }

    db_execute(
        sprintf(
            'INSERT INTO `%s`
                (parent_adm_menu_seq, depth_no, menu_cd, menu_nm, menu_url, match_urls_json, menu_type, board_key, use_yn, delete_flg, sort_ord, reg_dtm, reg_id, mod_dtm, mod_id)
             VALUES
                (:parent_adm_menu_seq, :depth_no, :menu_cd, :menu_nm, :menu_url, :match_urls_json, :menu_type, :board_key, :use_yn, \'N\', :sort_ord, :reg_dtm, :reg_id, :mod_dtm, :mod_id)',
            admin_menu_table_name()
        ),
        [
            ':parent_adm_menu_seq' => $parentSeq > 0 ? $parentSeq : null,
            ':depth_no' => $depthNo,
            ':menu_cd' => $menu['menu_cd'],
            ':menu_nm' => $menu['menu_nm'],
            ':menu_url' => $menu['menu_url'],
            ':match_urls_json' => $menu['match_urls_json'],
            ':menu_type' => $menu['menu_type'],
            ':board_key' => $boardKey,
            ':use_yn' => $menu['use_yn'],
            ':sort_ord' => $menu['sort_ord'],
            ':reg_dtm' => $now,
            ':reg_id' => $actor,
            ':mod_dtm' => $now,
            ':mod_id' => $actor,
        ]
    );

    return (int) db_pdo()->lastInsertId();
}

function delete_admin_menu_record(int $menuSeq): bool
{
    ensure_admin_menu_schema();
    if ($menuSeq <= 0 || admin_menu_has_children($menuSeq)) {
        return false;
    }

    db_execute(
        sprintf(
            'UPDATE `%s`
             SET delete_flg = \'Y\',
                 use_yn = \'N\',
                 mod_dtm = :mod_dtm,
                 mod_id = :mod_id
             WHERE adm_menu_seq = :adm_menu_seq',
            admin_menu_table_name()
        ),
        [
            ':adm_menu_seq' => $menuSeq,
            ':mod_dtm' => current_time(),
            ':mod_id' => (string) ($_SESSION['admin_login_id'] ?? 'system'),
        ]
    );

    return true;
}

function admin_menu_fallback_tree(): array
{
    $menus = [];
    foreach (default_admin_menu_seed() as $seed) {
        $seed['adm_menu_seq'] = count($menus) + 1;
        $seed['parent_adm_menu_seq'] = 0;
        $menus[$seed['menu_cd']] = normalize_admin_menu_record($seed);
    }
    foreach ($menus as $code => &$menu) {
        $parentCode = '';
        foreach (default_admin_menu_seed() as $seed) {
            if (($seed['menu_cd'] ?? '') === $code) {
                $parentCode = (string) ($seed['parent_menu_cd'] ?? '');
                break;
            }
        }
        if ($parentCode !== '' && isset($menus[$parentCode])) {
            $menu['parent_adm_menu_seq'] = (int) $menus[$parentCode]['adm_menu_seq'];
        }
    }
    unset($menu);
    return array_values($menus);
}

function admin_menu_path_matches(array $menu, string $path): bool
{
    $urls = array_values(array_filter(array_merge([(string) ($menu['menu_url'] ?? '')], $menu['match_urls'] ?? [])));
    return in_array($path, $urls, true);
}

function admin_navigation_context(string $currentPath): array
{
    try {
        $menus = active_admin_menus();
    } catch (Throwable $e) {
        $menus = admin_menu_fallback_tree();
    }

    $bySeq = [];
    foreach ($menus as $menu) {
        $bySeq[(int) ($menu['adm_menu_seq'] ?? 0)] = $menu;
    }

    $topMenus = array_values(array_filter($menus, static fn (array $menu): bool => (int) ($menu['parent_adm_menu_seq'] ?? 0) === 0 && ($menu['menu_cd'] ?? '') !== 'admin_home'));
    usort($topMenus, static fn (array $a, array $b): int => ((int) ($a['sort_ord'] ?? 0)) <=> ((int) ($b['sort_ord'] ?? 0)));

    $childrenByParent = [];
    foreach ($menus as $menu) {
        $parentSeq = (int) ($menu['parent_adm_menu_seq'] ?? 0);
        if ($parentSeq <= 0) {
            continue;
        }
        $childrenByParent[$parentSeq][] = $menu;
    }
    foreach ($childrenByParent as &$children) {
        usort($children, static fn (array $a, array $b): int => ((int) ($a['sort_ord'] ?? 0)) <=> ((int) ($b['sort_ord'] ?? 0)));
    }
    unset($children);

    $activeMenu = null;
    foreach ($menus as $menu) {
        if (admin_menu_path_matches($menu, $currentPath)) {
            if (!$activeMenu || (int) ($menu['depth_no'] ?? 1) > (int) ($activeMenu['depth_no'] ?? 1)) {
                $activeMenu = $menu;
            }
        }
    }

    $activeTop = null;
    $activeLnb = $activeMenu;
    if ($activeMenu) {
        $cursor = $activeMenu;
        while ((int) ($cursor['parent_adm_menu_seq'] ?? 0) > 0 && isset($bySeq[(int) $cursor['parent_adm_menu_seq']])) {
            $parent = $bySeq[(int) $cursor['parent_adm_menu_seq']];
            if ((int) ($parent['parent_adm_menu_seq'] ?? 0) === 0) {
                $activeTop = $parent;
                $activeLnb = $cursor;
                break;
            }
            $cursor = $parent;
        }
        if (!$activeTop && (int) ($activeMenu['parent_adm_menu_seq'] ?? 0) === 0) {
            $activeTop = $activeMenu;
        }
    }

    $adminMenu = [];
    foreach ($topMenus as $topMenu) {
        $children = $childrenByParent[(int) ($topMenu['adm_menu_seq'] ?? 0)] ?? [];
        $adminMenu[$topMenu['menu_cd']] = [
            'label' => $topMenu['menu_nm'],
            'href' => $topMenu['menu_url'] !== '' ? $topMenu['menu_url'] : ($children[0]['menu_url'] ?? '#'),
            'menu' => $topMenu,
            'children' => array_map(static fn (array $child): array => [
                'label' => $child['menu_nm'],
                'href' => $child['menu_url'],
                'menu' => $child,
            ], $children),
        ];
    }

    $breadcrumbs = ['관리자'];
    if ($currentPath === '/admin/') {
        $breadcrumbs[] = '대시보드';
    } elseif ($activeTop) {
        $breadcrumbs[] = (string) ($activeTop['menu_nm'] ?? '');
        if ($activeLnb && (int) ($activeLnb['adm_menu_seq'] ?? 0) !== (int) ($activeTop['adm_menu_seq'] ?? 0)) {
            $breadcrumbs[] = (string) ($activeLnb['menu_nm'] ?? '');
        }
        if ($activeMenu && (int) ($activeMenu['adm_menu_seq'] ?? 0) !== (int) ($activeLnb['adm_menu_seq'] ?? 0)) {
            $breadcrumbs[] = (string) ($activeMenu['menu_nm'] ?? '');
        }
    }

    return [
        'adminMenu' => $adminMenu,
        'activeAdminMenu' => $activeTop['menu_cd'] ?? null,
        'adminLnbActiveHref' => $activeLnb['menu_url'] ?? $currentPath,
        'breadcrumbs' => $breadcrumbs,
    ];
}

function all_admins(): array
{
    ensure_admin_schema();
    $seqColumn = admin_sequence_column_name();
    $admins = array_map('normalize_admin_record', db_fetch_all(sprintf('SELECT * FROM `%s` ORDER BY %s DESC', admin_table_name(), $seqColumn)));
    return $admins;
}

function active_admins(): array
{
    ensure_admin_schema();
    $seqColumn = admin_sequence_column_name();
    return array_map('normalize_admin_record', db_fetch_all(sprintf("SELECT * FROM `%s` WHERE status = 'active' ORDER BY %s DESC", admin_table_name(), $seqColumn)));
}

function managed_admins(): array
{
    ensure_admin_schema();
    $seqColumn = admin_sequence_column_name();
    return array_map('normalize_admin_record', db_fetch_all(sprintf("SELECT * FROM `%s` WHERE role <> 'super' ORDER BY %s DESC", admin_table_name(), $seqColumn)));
}

function find_admin(string $id): ?array
{
    ensure_admin_schema();
    $admSeq = (int) $id;
    if ($admSeq <= 0) {
        return null;
    }

    $seqColumn = admin_sequence_column_name();
    $admin = db_fetch_one(sprintf('SELECT * FROM `%s` WHERE %s = :adm_seq LIMIT 1', admin_table_name(), $seqColumn), [':adm_seq' => $admSeq]);
    return $admin ? normalize_admin_record($admin) : null;
}

function find_admin_by_login_id(string $loginId): ?array
{
    ensure_admin_schema();
    $admin = db_fetch_one(sprintf('SELECT * FROM `%s` WHERE login_id = :login_id LIMIT 1', admin_table_name()), [':login_id' => $loginId]);
    return $admin ? normalize_admin_record($admin) : null;
}

function current_admin_record(): ?array
{
    $adminId = trim((string) ($_SESSION['admin_id'] ?? ''));
    if ($adminId !== '') {
        $admin = find_admin($adminId);
        if ($admin) {
            return $admin;
        }
    }

    $loginId = trim((string) ($_SESSION['admin_login_id'] ?? ''));
    if ($loginId !== '') {
        return find_admin_by_login_id($loginId);
    }

    return null;
}

function save_admin(array $admin): void
{
    if (isset($admin['adm_seq']) && (int) $admin['adm_seq'] > 0 && find_admin((string) $admin['adm_seq'])) {
        update_admin($admin);
        return;
    }

    insert_admin($admin);
}

function insert_admin(array $admin): int
{
    ensure_admin_schema();
    $admin = normalize_admin_record($admin);
    $regDtm = (string) ($admin['reg_dtm'] ?? current_time());
    $regId = (string) ($admin['reg_id'] ?? $admin['login_id'] ?? 'system');
    $modDtm = (string) ($admin['mod_dtm'] ?? $regDtm);
    $modId = (string) ($admin['mod_id'] ?? $regId);

    db_execute(
        sprintf(
            'INSERT INTO `%s` (login_id, name, role, status, password_hash, reg_dtm, reg_id, mod_dtm, mod_id)
         VALUES (:login_id, :name, :role, :status, :password_hash, :reg_dtm, :reg_id, :mod_dtm, :mod_id)',
            admin_table_name()
        ),
        [
            ':login_id' => (string) ($admin['login_id'] ?? ''),
            ':name' => (string) ($admin['name'] ?? ''),
            ':role' => (string) ($admin['role'] ?? 'admin'),
            ':status' => (string) ($admin['status'] ?? 'active'),
            ':password_hash' => (string) ($admin['password_hash'] ?? ''),
            ':reg_dtm' => substr($regDtm, 0, 19),
            ':reg_id' => $regId,
            ':mod_dtm' => substr($modDtm, 0, 19),
            ':mod_id' => $modId,
        ]
    );

    return (int) db_pdo()->lastInsertId();
}

function update_admin(array $admin): void
{
    ensure_admin_schema();
    $admin = normalize_admin_record($admin);
    $admSeq = (int) ($admin['adm_seq'] ?? 0);
    if ($admSeq <= 0) {
        return;
    }

    $existing = find_admin((string) $admSeq) ?? [];
    $regDtm = (string) ($existing['reg_dtm'] ?? $admin['reg_dtm'] ?? current_time());
    $regId = (string) ($existing['reg_id'] ?? $admin['reg_id'] ?? $admin['login_id'] ?? 'system');
    $modDtm = (string) ($admin['mod_dtm'] ?? current_time());
    $modId = (string) ($admin['mod_id'] ?? $admin['login_id'] ?? $regId);

    db_execute(
        sprintf(
            'UPDATE `%s`
         SET login_id = :login_id,
             name = :name,
             role = :role,
             status = :status,
             password_hash = :password_hash,
             reg_dtm = :reg_dtm,
             reg_id = :reg_id,
             mod_dtm = :mod_dtm,
             mod_id = :mod_id
         WHERE %s = :adm_seq',
            admin_table_name(),
            admin_sequence_column_name()
        ),
        [
            ':adm_seq' => $admSeq,
            ':login_id' => (string) ($admin['login_id'] ?? ''),
            ':name' => (string) ($admin['name'] ?? ''),
            ':role' => (string) ($admin['role'] ?? 'admin'),
            ':status' => (string) ($admin['status'] ?? 'active'),
            ':password_hash' => (string) ($admin['password_hash'] ?? ''),
            ':reg_dtm' => substr($regDtm, 0, 19),
            ':reg_id' => $regId,
            ':mod_dtm' => substr($modDtm, 0, 19),
            ':mod_id' => $modId,
        ]
    );
}

function admin_status_label(?string $status): string
{
    return $status === 'inactive' ? '미사용' : '사용중';
}

function admin_access_location_label(string $path, array $query = []): string
{
    if ($path === '/admin/') {
        return '관리자 홈';
    }
    if ($path === '/admin/management/admins.php') {
        return '관리자 관리 > 관리자 목록';
    }
    if ($path === '/admin/management/admin_edit.php') {
        return '관리자 관리 > ' . (!empty($query['id']) ? '관리자 상세' : '관리자 등록');
    }
    if ($path === '/admin/management/access_logs.php') {
        return '관리자 관리 > 관리자 접속이력';
    }
    if ($path === '/admin/management/change_logs.php') {
        return '관리자 관리 > 관리자 수정이력';
    }
    if ($path === '/admin/management/change_log_view.php') {
        return '관리자 관리 > 관리자 수정이력 > 상세';
    }
    if ($path === '/admin/management/menus.php') {
        return '관리자 관리 > 메뉴 관리';
    }
    if ($path === '/admin/management/dashboard_edit.php') {
        return '관리자 관리 > 대시보드 수정';
    }
    if ($path === '/admin/survey/forms.php') {
        return '설문 운영 > 설문 관리';
    }
    if ($path === '/admin/survey/form_edit.php') {
        return '설문 운영 > ' . (!empty($query['id']) ? '설문 상세' : '설문 등록');
    }
    if ($path === '/admin/survey/submissions.php') {
        return '설문 운영 > 설문 이력 관리';
    }
    if ($path === '/admin/survey/submission_view.php') {
        return '설문 운영 > 설문 이력 상세';
    }
    if ($path === '/admin/survey/submissions_export.php') {
        return '설문 운영 > 설문 이력 CSV';
    }
    if ($path === '/admin/news/ai_news.php') {
        return '뉴스 > AI News';
    }
    if ($path === '/admin/news/ai_news_view.php') {
        return '뉴스 > AI News > 상세';
    }
    if ($path === '/admin/news/ai_news_edit.php') {
        return '뉴스 > AI News > ' . (!empty($query['id']) ? '수정' : '등록');
    }

    return $path;
}

function all_admin_access_logs(): array
{
    $logs = read_json_file('admin_access_logs.json');
    usort($logs, fn (array $a, array $b): int => strcmp((string) ($b['accessed_at'] ?? ''), (string) ($a['accessed_at'] ?? '')));
    return $logs;
}

function save_admin_access_log(array $log): void
{
    $logs = read_json_file('admin_access_logs.json');
    $logs[] = $log;
    write_json_file('admin_access_logs.json', $logs);
}

function find_admin_access_log(string $id): ?array
{
    foreach (all_admin_access_logs() as $log) {
        if (($log['id'] ?? '') === $id) {
            return $log;
        }
    }
    return null;
}

function all_admin_change_logs(): array
{
    ensure_admin_change_schema();
    $masters = db_fetch_all(sprintf('SELECT * FROM `%s` ORDER BY changed_at DESC, mod_seq DESC', admin_change_table_name()));
    $masters = array_map('normalize_admin_change_record', $masters);
    foreach ($masters as &$master) {
        $master['details'] = admin_change_details_by_mod_seq((int) ($master['mod_seq'] ?? 0));
    }
    unset($master);
    return $masters;
}

function save_admin_change_log(array $log): void
{
    ensure_admin_change_schema();
    $log = normalize_admin_change_record($log);
    $targetAdmSeq = (int) ($log['target_adm_seq'] ?? $log['target_admin_idx'] ?? $log['target_admin_id'] ?? 0);
    $changerAdmSeq = (int) ($log['changer_adm_seq'] ?? $log['changer_admin_idx'] ?? $log['changer_admin_id'] ?? 0);
    if ($changerAdmSeq <= 0) {
        $currentAdmin = current_admin_record() ?? find_admin_by_login_id('admin');
        $changerAdmSeq = (int) ($currentAdmin['adm_seq'] ?? 0);
    }
    if ($targetAdmSeq <= 0) {
        $targetAdmSeq = $changerAdmSeq > 0 ? $changerAdmSeq : 1;
    }
    $targetAdmin = $targetAdmSeq > 0 ? find_admin((string) $targetAdmSeq) : null;
    $changerAdmin = $changerAdmSeq > 0 ? find_admin((string) $changerAdmSeq) : null;
    $regDtm = (string) ($log['reg_dtm'] ?? $log['changed_at'] ?? current_time());
    $regId = (string) ($log['reg_id'] ?? $log['changer_admin_login_id'] ?? 'system');
    $modDtm = (string) ($log['mod_dtm'] ?? $regDtm);
    $modId = (string) ($log['mod_id'] ?? $regId);

    db_execute(
        sprintf(
            'INSERT INTO `%s`
                (target_adm_seq, changer_adm_seq, target_adm_login_id, target_adm_name, changer_adm_login_id, changer_adm_name, action, changed_at, reg_dtm, reg_id, mod_dtm, mod_id)
             VALUES
                (:target_adm_seq, :changer_adm_seq, :target_adm_login_id, :target_adm_name, :changer_adm_login_id, :changer_adm_name, :action, :changed_at, :reg_dtm, :reg_id, :mod_dtm, :mod_id)',
            admin_change_table_name()
        ),
        [
            ':target_adm_seq' => $targetAdmSeq,
            ':changer_adm_seq' => $changerAdmSeq,
            ':target_adm_login_id' => (string) ($log['target_adm_login_id'] ?? $log['target_admin_login_id'] ?? ($targetAdmin['login_id'] ?? '')),
            ':target_adm_name' => (string) ($log['target_adm_name'] ?? $log['target_admin_name'] ?? ($targetAdmin['name'] ?? '')),
            ':changer_adm_login_id' => (string) ($log['changer_adm_login_id'] ?? $log['changer_admin_login_id'] ?? ($changerAdmin['login_id'] ?? '')),
            ':changer_adm_name' => (string) ($log['changer_adm_name'] ?? $log['changer_admin_name'] ?? ($changerAdmin['name'] ?? '')),
            ':action' => (string) ($log['action'] ?? 'update'),
            ':changed_at' => substr((string) ($log['changed_at'] ?? current_time()), 0, 19),
            ':reg_dtm' => substr($regDtm, 0, 19),
            ':reg_id' => $regId,
            ':mod_dtm' => substr($modDtm, 0, 19),
            ':mod_id' => $modId,
        ]
    );

    $modSeq = (int) db_pdo()->lastInsertId();
    $details = admin_change_detail_rows_from_changes($modSeq, $log['changes'] ?? []);
    foreach ($details as $detail) {
        db_execute(
            sprintf(
                'INSERT INTO `%s` (mod_seq, column_name, column_label, before_value, after_value, reg_dtm, reg_id, mod_dtm, mod_id)
                 VALUES (:mod_seq, :column_name, :column_label, :before_value, :after_value, :reg_dtm, :reg_id, :mod_dtm, :mod_id)',
                admin_change_detail_table_name()
            ),
            $detail
        );
    }
}

function find_admin_change_log(string $id): ?array
{
    ensure_admin_change_schema();
    $modSeq = (int) $id;
    if ($modSeq <= 0) {
        return null;
    }

    $log = db_fetch_one(sprintf('SELECT * FROM `%s` WHERE mod_seq = :mod_seq LIMIT 1', admin_change_table_name()), [':mod_seq' => $modSeq]);
    if (!$log) {
        return null;
    }

    $log = normalize_admin_change_record($log);
    $log['details'] = admin_change_details_by_mod_seq((int) ($log['mod_seq'] ?? 0));
    return $log;
}

function normalize_admin_change_record(array $log): array
{
    $log['mod_seq'] = isset($log['mod_seq']) ? (int) $log['mod_seq'] : (isset($log['idx']) ? (int) $log['idx'] : 0);
    $log['idx'] = $log['mod_seq'];
    $log['target_adm_seq'] = isset($log['target_adm_seq']) ? (int) $log['target_adm_seq'] : (isset($log['target_admin_idx']) ? (int) $log['target_admin_idx'] : 0);
    $log['changer_adm_seq'] = isset($log['changer_adm_seq']) ? (int) $log['changer_adm_seq'] : (isset($log['changer_admin_idx']) ? (int) $log['changer_admin_idx'] : 0);
    $log['target_adm_login_id'] = (string) ($log['target_adm_login_id'] ?? $log['target_admin_login_id'] ?? '');
    $log['target_adm_name'] = (string) ($log['target_adm_name'] ?? $log['target_admin_name'] ?? '');
    $log['changer_adm_login_id'] = (string) ($log['changer_adm_login_id'] ?? $log['changer_admin_login_id'] ?? '');
    $log['changer_adm_name'] = (string) ($log['changer_adm_name'] ?? $log['changer_admin_name'] ?? '');
    $log['action'] = (string) ($log['action'] ?? 'update');
    $log['changed_at'] = (string) ($log['changed_at'] ?? current_time());
    $log['details'] = is_array($log['details'] ?? null) ? $log['details'] : [];
    $log['reg_dtm'] = (string) ($log['reg_dtm'] ?? $log['changed_at'] ?? current_time());
    $log['reg_id'] = (string) ($log['reg_id'] ?? $log['changer_adm_login_id'] ?? 'system');
    $log['mod_dtm'] = (string) ($log['mod_dtm'] ?? $log['reg_dtm'] ?? current_time());
    $log['mod_id'] = (string) ($log['mod_id'] ?? $log['reg_id'] ?? 'system');

    return $log;
}

function admin_change_diff(array $before, array $after): array
{
    $changes = [];

    foreach ([
        'login_id' => '아이디',
        'name' => '관리자명',
        'status' => '사용여부',
    ] as $field => $label) {
        $beforeValue = (string) ($before[$field] ?? '');
        $afterValue = (string) ($after[$field] ?? '');

        if ($field === 'status') {
            $beforeValue = admin_status_label($beforeValue);
            $afterValue = admin_status_label($afterValue);
        }

        if ($beforeValue !== $afterValue) {
            $changes[] = [
                'field' => $field,
                'label' => $label,
                'before' => $beforeValue,
                'after' => $afterValue,
            ];
        }
    }

    if (($before['password_hash'] ?? '') !== ($after['password_hash'] ?? '')) {
        $changes[] = [
            'field' => 'password',
            'label' => '비밀번호',
            'summary' => '변경됨',
        ];
    }

    return $changes;
}

function admin_change_target_adm_seq(array $admin): int
{
    return (int) ($admin['adm_seq'] ?? 0);
}

function admin_change_target_idx(array $admin): int
{
    return admin_change_target_adm_seq($admin);
}

function admin_change_detail_rows_from_changes(int $modSeq, array $changes): array
{
    $rows = [];
    $now = current_time();

    foreach (array_values($changes) as $change) {
        $rows[] = [
            ':mod_seq' => $modSeq,
            ':column_name' => (string) ($change['field'] ?? ''),
            ':column_label' => (string) ($change['label'] ?? ''),
            ':before_value' => array_key_exists('before', $change) ? (string) $change['before'] : null,
            ':after_value' => array_key_exists('after', $change) ? (string) $change['after'] : (string) ($change['summary'] ?? $change['value'] ?? ''),
            ':reg_dtm' => $now,
            ':reg_id' => (string) ($_SESSION['admin_login_id'] ?? 'system'),
            ':mod_dtm' => $now,
            ':mod_id' => (string) ($_SESSION['admin_login_id'] ?? 'system'),
        ];
    }

    return $rows;
}

function admin_change_details_by_mod_seq(int $modSeq): array
{
    ensure_admin_change_schema();
    if ($modSeq <= 0) {
        return [];
    }

    $rows = db_fetch_all(sprintf('SELECT * FROM `%s` WHERE mod_seq = :mod_seq ORDER BY mod_dtl_seq ASC', admin_change_detail_table_name()), [':mod_seq' => $modSeq]);
    $details = [];
    foreach ($rows as $row) {
        $details[] = [
            'mod_dtl_seq' => (int) ($row['mod_dtl_seq'] ?? 0),
            'mod_seq' => (int) ($row['mod_seq'] ?? 0),
            'column_name' => (string) ($row['column_name'] ?? ''),
            'column_label' => (string) ($row['column_label'] ?? ''),
            'before_value' => $row['before_value'] ?? '',
            'after_value' => $row['after_value'] ?? '',
        ];
    }

    return $details;
}

function admin_create_snapshot(array $admin): array
{
    return [
        'login_id' => (string) ($admin['login_id'] ?? ''),
        'name' => (string) ($admin['name'] ?? ''),
        'status' => (string) ($admin['status'] ?? 'active'),
        'password_hash' => (string) ($admin['password_hash'] ?? ''),
    ];
}

function all_forms(): array
{
    $forms = read_json_file('forms.json');
    usort($forms, fn (array $a, array $b): int => strcmp((string) ($b['created_at'] ?? ''), (string) ($a['created_at'] ?? '')));
    return $forms;
}

function active_forms(): array
{
    return array_values(array_filter(all_forms(), fn (array $form): bool => ($form['status'] ?? 'active') === 'active'));
}

function find_form(string $id): ?array
{
    foreach (all_forms() as $form) {
        if (($form['id'] ?? '') === $id) {
            return $form;
        }
    }
    return null;
}

function save_form(array $form): void
{
    $forms = read_json_file('forms.json');
    $found = false;

    foreach ($forms as $index => $existing) {
        if (($existing['id'] ?? '') === ($form['id'] ?? '')) {
            $forms[$index] = $form;
            $found = true;
            break;
        }
    }

    if (!$found) {
        $forms[] = $form;
    }

    write_json_file('forms.json', $forms);
}

function delete_form(string $id): void
{
    $forms = array_values(array_filter(read_json_file('forms.json'), fn (array $form): bool => ($form['id'] ?? '') !== $id));
    write_json_file('forms.json', $forms);
}

function all_submissions(): array
{
    $submissions = read_json_file('submissions.json');
    usort($submissions, fn (array $a, array $b): int => strcmp((string) ($b['submitted_at'] ?? ''), (string) ($a['submitted_at'] ?? '')));
    return $submissions;
}

function submissions_for_form(string $formId): array
{
    return array_values(array_filter(all_submissions(), fn (array $submission): bool => ($submission['form_id'] ?? '') === $formId));
}

function find_submission(string $id): ?array
{
    foreach (all_submissions() as $submission) {
        if (($submission['id'] ?? '') === $id) {
            return $submission;
        }
    }
    return null;
}

function save_submission(array $submission): void
{
    $submissions = read_json_file('submissions.json');
    $submissions[] = $submission;
    write_json_file('submissions.json', $submissions);
}

function update_submission(string $id, array $changes): void
{
    $submissions = read_json_file('submissions.json');

    foreach ($submissions as $index => $submission) {
        if (($submission['id'] ?? '') === $id) {
            $submissions[$index] = array_merge($submission, $changes);
            break;
        }
    }

    write_json_file('submissions.json', $submissions);
}

function ai_news_table_name(): string
{
    return 'co_ai_news_mst';
}

function ai_news_sequence_column_name(): string
{
    return db_column_exists(ai_news_table_name(), 'news_seq') ? 'news_seq' : 'idx';
}

function ai_news_crawl_directory(): string
{
    return dirname(__DIR__) . '/croll/ai-news';
}

function ai_news_source_file_path(?string $sourceFile): string
{
    $sourceFile = basename(trim((string) $sourceFile));
    if ($sourceFile === '') {
        return '';
    }

    return ai_news_crawl_directory() . '/' . $sourceFile;
}

function ai_news_status_options(): array
{
    return [
        'N' => '대기',
        'P' => '처리중',
        'Y' => '완료',
        'E' => '에러',
    ];
}

function ai_news_status_label(?string $status): string
{
    $status = strtoupper(trim((string) $status));
    return ai_news_status_options()[$status] ?? '완료';
}

function ai_news_status_badge_class(?string $status): string
{
    return match (strtoupper(trim((string) $status))) {
        'N' => 'text-bg-secondary',
        'P' => 'text-bg-warning',
        'E' => 'text-bg-danger',
        default => 'text-bg-success',
    };
}

function ai_news_tags_from_text(string $text): array
{
    $text = trim($text);
    if ($text === '') {
        return [];
    }

    $parts = preg_split('/[,\n\r]+/', $text) ?: [];
    return array_values(array_filter(array_map('trim', $parts)));
}

function ai_news_tags_to_text(array $tags): string
{
    return implode(', ', array_values(array_filter(array_map('trim', $tags))));
}

function ai_news_sources_from_text(string $text): array
{
    $text = trim($text);
    if ($text === '') {
        return [];
    }

    $rows = [];
    $lines = preg_split('/\r\n|\r|\n/', $text) ?: [];
    foreach ($lines as $line) {
        $line = trim($line);
        if ($line === '') {
            continue;
        }

        if (str_contains($line, '|')) {
            $parts = array_map('trim', explode('|', $line));
        } else {
            $parts = [$line];
        }

        $rows[] = [
            'title' => (string) ($parts[0] ?? ''),
            'source' => (string) ($parts[1] ?? ''),
            'url' => (string) ($parts[2] ?? ''),
        ];
    }

    return $rows;
}

function ai_news_sources_to_text(array $sources): string
{
    $lines = [];

    foreach ($sources as $source) {
        if (!is_array($source)) {
            continue;
        }

        $parts = [
            trim((string) ($source['title'] ?? '')),
            trim((string) ($source['source'] ?? '')),
            trim((string) ($source['url'] ?? '')),
        ];
        $parts = array_values(array_filter($parts, static fn (string $value): bool => $value !== ''));
        if ($parts) {
            $lines[] = implode(' | ', $parts);
        }
    }

    return implode("\n", $lines);
}

function ai_news_sync_source_file(array $news, string $status, ?string $error = null): void
{
    $path = ai_news_source_file_path((string) ($news['source_file'] ?? ''));
    if ($path === '' || !is_file($path)) {
        return;
    }

    $file = read_json_path($path);
    if (!is_array($file)) {
        $file = [];
    }

    $file['status'] = strtoupper($status);
    if ($file['status'] === 'Y') {
        $file['inserted_at'] = current_time();
        $file['error'] = null;
    } elseif ($file['status'] === 'P') {
        $file['error'] = null;
    } elseif ($file['status'] === 'E') {
        $file['error'] = $error ?? (string) ($news['crawl_error'] ?? '');
    }

    write_json_path($path, $file);
}

function ai_news_normalize_datetime(?string $value = null): string
{
    $value = trim((string) $value);
    if ($value === '') {
        return current_time();
    }

    if (preg_match('/^\d{4}-\d{2}-\d{2}$/', $value) === 1) {
        return $value . ' 00:00:00';
    }

    $value = str_replace('T', ' ', $value);
    return substr($value, 0, 19);
}

function ai_news_normalize_record(array $news): array
{
    $news['news_seq'] = isset($news['news_seq']) ? (int) $news['news_seq'] : (isset($news['idx']) ? (int) $news['idx'] : 0);
    $news['idx'] = $news['news_seq'];
    $news['slug'] = trim((string) ($news['slug'] ?? ''));
    $news['title'] = trim((string) ($news['title'] ?? ''));
    $news['category'] = trim((string) ($news['category'] ?? ''));
    $news['summary'] = trim((string) ($news['summary'] ?? ''));
    $news['content_markdown'] = (string) ($news['content_markdown'] ?? '');
    $tagsSource = $news['tags_json'] ?? $news['tags'] ?? [];
    if (is_array($tagsSource)) {
        $news['tags'] = array_values(array_filter(array_map('trim', $tagsSource)));
    } elseif (is_string($tagsSource) && trim($tagsSource) !== '') {
        $decodedTags = json_decode($tagsSource, true);
        if (is_array($decodedTags)) {
            $news['tags'] = array_values(array_filter(array_map('trim', $decodedTags)));
        } else {
            $news['tags'] = ai_news_tags_from_text($tagsSource);
        }
    } else {
        $news['tags'] = [];
    }
    $news['sources'] = [];
    $sourcesSource = $news['sources_json'] ?? $news['sources'] ?? [];
    if (is_array($sourcesSource)) {
        $news['sources'] = array_values(array_filter($sourcesSource, static fn ($source): bool => is_array($source)));
    } elseif (is_string($sourcesSource) && trim($sourcesSource) !== '') {
        $decodedSources = json_decode($sourcesSource, true);
        if (is_array($decodedSources)) {
            $news['sources'] = array_values(array_filter($decodedSources, static fn ($source): bool => is_array($source)));
        } else {
            $news['sources'] = ai_news_sources_from_text($sourcesSource);
        }
    }
    $news['published_at'] = ai_news_normalize_datetime((string) ($news['published_at'] ?? ''));
    $news['status'] = in_array(strtoupper((string) ($news['status'] ?? 'Y')), array_keys(ai_news_status_options()), true) ? strtoupper((string) ($news['status'] ?? 'Y')) : 'Y';
    $news['delete_flg'] = strtoupper((string) ($news['delete_flg'] ?? 'N')) === 'Y' ? 'Y' : 'N';
    $news['source_file'] = trim((string) ($news['source_file'] ?? ''));
    $news['crawl_error'] = trim((string) ($news['crawl_error'] ?? ''));
    $news['reg_dtm'] = ai_news_normalize_datetime((string) ($news['reg_dtm'] ?? ''));
    $news['reg_id'] = trim((string) ($news['reg_id'] ?? 'system'));
    $news['mod_dtm'] = ai_news_normalize_datetime((string) ($news['mod_dtm'] ?? ''));
    $news['mod_id'] = trim((string) ($news['mod_id'] ?? $news['reg_id'] ?? 'system'));

    return $news;
}

function ai_news_db_payload(array $news, ?array $existing = null): array
{
    $tags = is_array($news['tags'] ?? null) ? $news['tags'] : [];
    $sources = is_array($news['sources'] ?? null) ? $news['sources'] : [];
    $existing = $existing ? ai_news_normalize_record($existing) : null;

    return [
        'slug' => trim((string) ($news['slug'] ?? '')),
        'title' => trim((string) ($news['title'] ?? '')),
        'category' => trim((string) ($news['category'] ?? '')),
        'summary' => trim((string) ($news['summary'] ?? '')),
        'content_markdown' => (string) ($news['content_markdown'] ?? ''),
        'tags_json' => json_encode(array_values($tags), JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES),
        'sources_json' => json_encode(array_values($sources), JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES),
        'published_at' => ai_news_normalize_datetime((string) ($news['published_at'] ?? ($existing['published_at'] ?? ''))),
        'status' => in_array(strtoupper((string) ($news['status'] ?? 'Y')), array_keys(ai_news_status_options()), true) ? strtoupper((string) ($news['status'] ?? 'Y')) : 'Y',
        'delete_flg' => strtoupper((string) ($news['delete_flg'] ?? ($existing['delete_flg'] ?? 'N'))) === 'Y' ? 'Y' : 'N',
        'source_file' => trim((string) ($news['source_file'] ?? ($existing['source_file'] ?? ''))),
        'crawl_error' => trim((string) ($news['crawl_error'] ?? '')),
        'reg_dtm' => ai_news_normalize_datetime((string) ($news['reg_dtm'] ?? ($existing['reg_dtm'] ?? current_time()))),
        'reg_id' => trim((string) ($news['reg_id'] ?? ($existing['reg_id'] ?? 'system'))),
        'mod_dtm' => ai_news_normalize_datetime((string) ($news['mod_dtm'] ?? current_time())),
        'mod_id' => trim((string) ($news['mod_id'] ?? ($existing['mod_id'] ?? ($existing['reg_id'] ?? 'system')))),
    ];
}

function ai_news_saved_status_columns(string $status, ?string $modId = null): array
{
    $modId = trim((string) $modId);
    if ($modId === '') {
        $modId = (string) ($_SESSION['admin_login_id'] ?? 'system');
    }

    return [
        'status' => strtoupper($status),
        'mod_dtm' => current_time(),
        'mod_id' => $modId,
    ];
}

function ensure_ai_news_schema(): void
{
    static $initialized = false;

    if ($initialized) {
        return;
    }

    $table = ai_news_table_name();
    $tableSql = '`' . $table . '`';

    db_execute(
        <<<SQL
        CREATE TABLE IF NOT EXISTS {$tableSql} (
            news_seq INT NOT NULL AUTO_INCREMENT COMMENT 'AI News 일련번호',
            slug VARCHAR(255) NOT NULL COMMENT '뉴스 고유 슬러그',
            title VARCHAR(255) NOT NULL COMMENT '뉴스 제목',
            category VARCHAR(100) NOT NULL DEFAULT '' COMMENT '뉴스 분류',
            summary TEXT NOT NULL COMMENT '뉴스 요약',
            content_markdown LONGTEXT NOT NULL COMMENT '마크다운 본문',
            tags_json LONGTEXT NULL COMMENT '태그 JSON',
            sources_json LONGTEXT NULL COMMENT '출처 JSON',
            published_at DATETIME NULL COMMENT '게시 일시',
            status CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '처리 상태',
            delete_flg CHAR(1) NOT NULL DEFAULT 'N' COMMENT '삭제 여부',
            source_file VARCHAR(255) NOT NULL DEFAULT '' COMMENT '원본 파일 경로',
            crawl_error LONGTEXT NULL COMMENT '크롤링 오류 내용',
            reg_dtm DATETIME NOT NULL COMMENT '등록 일시',
            reg_id VARCHAR(100) NOT NULL COMMENT '등록자 아이디',
            mod_dtm DATETIME NOT NULL COMMENT '수정 일시',
            mod_id VARCHAR(100) NOT NULL COMMENT '수정자 아이디',
            PRIMARY KEY (news_seq),
            UNIQUE KEY uq_co_ai_news_mst_slug (slug),
            KEY idx_co_ai_news_mst_status (status),
            KEY idx_co_ai_news_mst_delete_flg (delete_flg),
            KEY idx_co_ai_news_mst_published_at (published_at),
            KEY idx_co_ai_news_mst_reg_dtm (reg_dtm)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI News 마스터'
        SQL
    );

    if (db_column_exists($table, 'idx') && !db_column_exists($table, 'news_seq')) {
        db_execute(sprintf('ALTER TABLE %s CHANGE COLUMN idx news_seq INT NOT NULL AUTO_INCREMENT', $tableSql));
    }
    if (db_column_exists($table, 'created_at')) {
        db_execute(sprintf('ALTER TABLE %s DROP COLUMN created_at', $tableSql));
    }
    if (db_column_exists($table, 'updated_at')) {
        db_execute(sprintf('ALTER TABLE %s DROP COLUMN updated_at', $tableSql));
    }

    db_apply_schema_comments($table, [
        'news_seq' => ['sql' => 'INT NOT NULL AUTO_INCREMENT', 'comment' => 'AI News 일련번호'],
        'slug' => ['sql' => 'VARCHAR(255) NOT NULL', 'comment' => '뉴스 고유 슬러그'],
        'title' => ['sql' => 'VARCHAR(255) NOT NULL', 'comment' => '뉴스 제목'],
        'category' => ['sql' => "VARCHAR(100) NOT NULL DEFAULT ''", 'comment' => '뉴스 분류'],
        'summary' => ['sql' => 'TEXT NOT NULL', 'comment' => '뉴스 요약'],
        'content_markdown' => ['sql' => 'LONGTEXT NOT NULL', 'comment' => '마크다운 본문'],
        'tags_json' => ['sql' => 'LONGTEXT NULL', 'comment' => '태그 JSON'],
        'sources_json' => ['sql' => 'LONGTEXT NULL', 'comment' => '출처 JSON'],
        'published_at' => ['sql' => 'DATETIME NULL', 'comment' => '게시 일시'],
        'status' => ['sql' => "CHAR(1) NOT NULL DEFAULT 'Y'", 'comment' => '처리 상태'],
        'delete_flg' => ['sql' => "CHAR(1) NOT NULL DEFAULT 'N'", 'comment' => '삭제 여부'],
        'source_file' => ['sql' => "VARCHAR(255) NOT NULL DEFAULT ''", 'comment' => '원본 파일 경로'],
        'crawl_error' => ['sql' => 'LONGTEXT NULL', 'comment' => '크롤링 오류 내용'],
        'reg_dtm' => ['sql' => 'DATETIME NOT NULL', 'comment' => '등록 일시'],
        'reg_id' => ['sql' => 'VARCHAR(100) NOT NULL', 'comment' => '등록자 아이디'],
        'mod_dtm' => ['sql' => 'DATETIME NOT NULL', 'comment' => '수정 일시'],
        'mod_id' => ['sql' => 'VARCHAR(100) NOT NULL', 'comment' => '수정자 아이디'],
    ], 'AI News 마스터');

    $initialized = true;
}

function all_ai_news(bool $includeDeleted = false): array
{
    ensure_ai_news_schema();
    $sql = sprintf(
        'SELECT * FROM `%s` %s ORDER BY COALESCE(published_at, reg_dtm) DESC, news_seq DESC',
        ai_news_table_name(),
        $includeDeleted ? '' : "WHERE delete_flg <> 'Y'"
    );
    return array_map('ai_news_normalize_record', db_fetch_all($sql));
}

function normalize_dashboard_panel_record(array $panel): array
{
    $panel['panel_seq'] = isset($panel['panel_seq']) ? (int) $panel['panel_seq'] : 0;
    $panel['adm_menu_seq'] = isset($panel['adm_menu_seq']) ? (int) $panel['adm_menu_seq'] : 0;
    $panel['board_key'] = trim((string) ($panel['board_key'] ?? ''));
    $panel['display_yn'] = strtoupper((string) ($panel['display_yn'] ?? 'Y')) === 'N' ? 'N' : 'Y';
    $panel['sort_ord'] = (int) ($panel['sort_ord'] ?? 0);
    $panel['item_limit'] = max(1, min(20, (int) ($panel['item_limit'] ?? 8)));
    $panel['reg_dtm'] = (string) ($panel['reg_dtm'] ?? current_time());
    $panel['reg_id'] = (string) ($panel['reg_id'] ?? 'system');
    $panel['mod_dtm'] = (string) ($panel['mod_dtm'] ?? $panel['reg_dtm'] ?? current_time());
    $panel['mod_id'] = (string) ($panel['mod_id'] ?? $panel['reg_id'] ?? 'system');

    return $panel;
}

function ensure_dashboard_panel_schema(): void
{
    static $initialized = false;

    if ($initialized) {
        return;
    }

    ensure_admin_menu_schema();
    $table = dashboard_panel_table_name();
    $tableSql = '`' . $table . '`';

    db_execute(
        <<<SQL
        CREATE TABLE IF NOT EXISTS {$tableSql} (
            panel_seq INT NOT NULL AUTO_INCREMENT COMMENT '대시보드 패널 일련번호',
            adm_menu_seq INT NOT NULL COMMENT '관리자 메뉴 일련번호',
            board_key VARCHAR(100) NOT NULL COMMENT '게시판 연결 키',
            display_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '노출 여부',
            sort_ord INT NOT NULL DEFAULT 0 COMMENT '표시 순서',
            item_limit INT NOT NULL DEFAULT 8 COMMENT '표시 글 수',
            reg_dtm DATETIME NOT NULL COMMENT '등록 일시',
            reg_id VARCHAR(100) NOT NULL COMMENT '등록자 아이디',
            mod_dtm DATETIME NOT NULL COMMENT '수정 일시',
            mod_id VARCHAR(100) NOT NULL COMMENT '수정자 아이디',
            PRIMARY KEY (panel_seq),
            UNIQUE KEY uq_co_dashboard_panel_mst_board_key (board_key),
            KEY idx_co_dashboard_panel_mst_display_sort (display_yn, sort_ord)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='대시보드 패널 설정'
        SQL
    );

    db_apply_schema_comments($table, [
        'panel_seq' => ['sql' => 'INT NOT NULL AUTO_INCREMENT', 'comment' => '대시보드 패널 일련번호'],
        'adm_menu_seq' => ['sql' => 'INT NOT NULL', 'comment' => '관리자 메뉴 일련번호'],
        'board_key' => ['sql' => 'VARCHAR(100) NOT NULL', 'comment' => '게시판 연결 키'],
        'display_yn' => ['sql' => "CHAR(1) NOT NULL DEFAULT 'Y'", 'comment' => '노출 여부'],
        'sort_ord' => ['sql' => 'INT NOT NULL DEFAULT 0', 'comment' => '표시 순서'],
        'item_limit' => ['sql' => 'INT NOT NULL DEFAULT 8', 'comment' => '표시 글 수'],
        'reg_dtm' => ['sql' => 'DATETIME NOT NULL', 'comment' => '등록 일시'],
        'reg_id' => ['sql' => 'VARCHAR(100) NOT NULL', 'comment' => '등록자 아이디'],
        'mod_dtm' => ['sql' => 'DATETIME NOT NULL', 'comment' => '수정 일시'],
        'mod_id' => ['sql' => 'VARCHAR(100) NOT NULL', 'comment' => '수정자 아이디'],
    ], '대시보드 패널 설정');

    $aiNewsMenu = find_admin_menu_by_code('news_ai_news');
    if ($aiNewsMenu) {
        $existing = db_fetch_one(sprintf('SELECT * FROM %s WHERE board_key = :board_key LIMIT 1', $tableSql), [':board_key' => 'ai_news']);
        if (!$existing) {
            db_execute(
                sprintf(
                    'INSERT INTO %s (adm_menu_seq, board_key, display_yn, sort_ord, item_limit, reg_dtm, reg_id, mod_dtm, mod_id)
                     VALUES (:adm_menu_seq, :board_key, \'Y\', 10, 8, :reg_dtm, \'system\', :mod_dtm, \'system\')',
                    $tableSql
                ),
                [
                    ':adm_menu_seq' => (int) ($aiNewsMenu['adm_menu_seq'] ?? 0),
                    ':board_key' => 'ai_news',
                    ':reg_dtm' => current_time(),
                    ':mod_dtm' => current_time(),
                ]
            );
        }
    }

    $initialized = true;
}

function all_dashboard_panels(): array
{
    ensure_dashboard_panel_schema();
    $panels = db_fetch_all(sprintf('SELECT * FROM `%s` ORDER BY sort_ord ASC, panel_seq ASC', dashboard_panel_table_name()));
    return array_map('normalize_dashboard_panel_record', $panels);
}

function dashboard_panel_by_board_key(): array
{
    $panels = [];
    foreach (all_dashboard_panels() as $panel) {
        $panels[(string) ($panel['board_key'] ?? '')] = $panel;
    }
    return $panels;
}

function dashboard_board_menus(): array
{
    return array_values(array_filter(active_admin_menus(), static function (array $menu): bool {
        return ($menu['menu_type'] ?? '') === 'board' && ($menu['board_key'] ?? '') !== '';
    }));
}

function save_dashboard_panel_settings(array $settings): void
{
    ensure_dashboard_panel_schema();
    $actor = (string) ($_SESSION['admin_login_id'] ?? 'system');
    $now = current_time();
    $existingByBoardKey = dashboard_panel_by_board_key();

    foreach (dashboard_board_menus() as $menu) {
        $boardKey = (string) ($menu['board_key'] ?? '');
        if ($boardKey === '') {
            continue;
        }

        $row = is_array($settings[$boardKey] ?? null) ? $settings[$boardKey] : [];
        $displayYn = !empty($row['display_yn']) ? 'Y' : 'N';
        $sortOrd = (int) ($row['sort_ord'] ?? 0);
        $itemLimit = max(1, min(20, (int) ($row['item_limit'] ?? 8)));
        $existing = $existingByBoardKey[$boardKey] ?? null;

        if ($existing) {
            db_execute(
                sprintf(
                    'UPDATE `%s`
                     SET adm_menu_seq = :adm_menu_seq,
                         display_yn = :display_yn,
                         sort_ord = :sort_ord,
                         item_limit = :item_limit,
                         mod_dtm = :mod_dtm,
                         mod_id = :mod_id
                     WHERE panel_seq = :panel_seq',
                    dashboard_panel_table_name()
                ),
                [
                    ':panel_seq' => (int) ($existing['panel_seq'] ?? 0),
                    ':adm_menu_seq' => (int) ($menu['adm_menu_seq'] ?? 0),
                    ':display_yn' => $displayYn,
                    ':sort_ord' => $sortOrd,
                    ':item_limit' => $itemLimit,
                    ':mod_dtm' => $now,
                    ':mod_id' => $actor,
                ]
            );
            continue;
        }

        db_execute(
            sprintf(
                'INSERT INTO `%s` (adm_menu_seq, board_key, display_yn, sort_ord, item_limit, reg_dtm, reg_id, mod_dtm, mod_id)
                 VALUES (:adm_menu_seq, :board_key, :display_yn, :sort_ord, :item_limit, :reg_dtm, :reg_id, :mod_dtm, :mod_id)',
                dashboard_panel_table_name()
            ),
            [
                ':adm_menu_seq' => (int) ($menu['adm_menu_seq'] ?? 0),
                ':board_key' => $boardKey,
                ':display_yn' => $displayYn,
                ':sort_ord' => $sortOrd,
                ':item_limit' => $itemLimit,
                ':reg_dtm' => $now,
                ':reg_id' => $actor,
                ':mod_dtm' => $now,
                ':mod_id' => $actor,
            ]
        );
    }
}

function ai_news_dashboard_items(int $limit): array
{
    $items = array_values(array_filter(all_ai_news(), static function (array $news): bool {
        return in_array((string) ($news['status'] ?? ''), ['P', 'Y'], true);
    }));
    usort($items, static function (array $left, array $right): int {
        $statusRank = ['P' => 0, 'Y' => 1];
        $leftRank = $statusRank[(string) ($left['status'] ?? 'Y')] ?? 2;
        $rightRank = $statusRank[(string) ($right['status'] ?? 'Y')] ?? 2;
        if ($leftRank !== $rightRank) {
            return $leftRank <=> $rightRank;
        }

        $leftDate = (string) ($left['published_at'] ?? $left['reg_dtm'] ?? '');
        $rightDate = (string) ($right['published_at'] ?? $right['reg_dtm'] ?? '');
        if ($leftDate !== $rightDate) {
            return strcmp($rightDate, $leftDate);
        }

        return ((int) ($right['news_seq'] ?? 0)) <=> ((int) ($left['news_seq'] ?? 0));
    });

    return array_slice($items, 0, $limit);
}

function dashboard_panel_cards(): array
{
    $panels = array_values(array_filter(all_dashboard_panels(), static fn (array $panel): bool => ($panel['display_yn'] ?? 'Y') === 'Y'));
    $menusBySeq = [];
    foreach (active_admin_menus() as $menu) {
        $menusBySeq[(int) ($menu['adm_menu_seq'] ?? 0)] = $menu;
    }

    $cards = [];
    foreach ($panels as $panel) {
        $boardKey = (string) ($panel['board_key'] ?? '');
        $menu = $menusBySeq[(int) ($panel['adm_menu_seq'] ?? 0)] ?? null;
        if (!$menu || $boardKey !== 'ai_news') {
            continue;
        }

        $allItems = ai_news_dashboard_items(1000);
        $counts = ['P' => 0, 'Y' => 0];
        foreach ($allItems as $item) {
            $status = (string) ($item['status'] ?? '');
            if (isset($counts[$status])) {
                $counts[$status]++;
            }
        }

        $cards[] = [
            'title' => (string) ($menu['menu_nm'] ?? 'AI News'),
            'href' => (string) ($menu['menu_url'] ?? '/admin/news/ai_news.php'),
            'board_key' => $boardKey,
            'counts' => $counts,
            'items' => ai_news_dashboard_items((int) ($panel['item_limit'] ?? 8)),
        ];
    }

    return $cards;
}

function update_ai_news_status_bulk(array $ids, string $status): int
{
    ensure_ai_news_schema();
    $status = strtoupper(trim($status));
    if (!in_array($status, array_keys(ai_news_status_options()), true)) {
        return 0;
    }

    $updated = 0;
    $modId = (string) ($_SESSION['admin_login_id'] ?? 'system');
    $ids = array_values(array_unique(array_filter(array_map('trim', $ids), static fn (string $value): bool => $value !== '')));

    foreach ($ids as $id) {
        $news = find_ai_news($id);
        if (!$news) {
            continue;
        }

        $newsSeq = (int) ($news['news_seq'] ?? 0);
        if ($newsSeq <= 0) {
            continue;
        }

        db_execute(
            sprintf(
                'UPDATE `%s`
                 SET status = :status,
                     mod_dtm = :mod_dtm,
                     mod_id = :mod_id
                 WHERE news_seq = :news_seq',
                ai_news_table_name()
            ),
            [
                ':news_seq' => $newsSeq,
                ':status' => $status,
                ':mod_dtm' => current_time(),
                ':mod_id' => $modId,
            ]
        );

        ai_news_sync_source_file($news, $status);
        $updated++;
    }

    return $updated;
}

function find_ai_news(string $id): ?array
{
    ensure_ai_news_schema();
    $newsSeq = (int) $id;
    if ($newsSeq <= 0) {
        return null;
    }

    $news = db_fetch_one(sprintf('SELECT * FROM `%s` WHERE news_seq = :news_seq LIMIT 1', ai_news_table_name()), [':news_seq' => $newsSeq]);
    return $news ? ai_news_normalize_record($news) : null;
}

function find_ai_news_by_slug(string $slug): ?array
{
    ensure_ai_news_schema();
    $slug = trim($slug);
    if ($slug === '') {
        return null;
    }

    $news = db_fetch_one(sprintf('SELECT * FROM `%s` WHERE slug = :slug LIMIT 1', ai_news_table_name()), [':slug' => $slug]);
    return $news ? ai_news_normalize_record($news) : null;
}

function insert_ai_news(array $news): int
{
    ensure_ai_news_schema();
    $payload = ai_news_db_payload($news);

    db_execute(
        sprintf(
            'INSERT INTO `%s`
                (slug, title, category, summary, content_markdown, tags_json, sources_json, published_at, status, delete_flg, source_file, crawl_error, reg_dtm, reg_id, mod_dtm, mod_id)
             VALUES
                (:slug, :title, :category, :summary, :content_markdown, :tags_json, :sources_json, :published_at, :status, :delete_flg, :source_file, :crawl_error, :reg_dtm, :reg_id, :mod_dtm, :mod_id)',
            ai_news_table_name()
        ),
        [
            ':slug' => $payload['slug'],
            ':title' => $payload['title'],
            ':category' => $payload['category'],
            ':summary' => $payload['summary'],
            ':content_markdown' => $payload['content_markdown'],
            ':tags_json' => $payload['tags_json'],
            ':sources_json' => $payload['sources_json'],
            ':published_at' => $payload['published_at'] !== '' ? substr($payload['published_at'], 0, 19) : null,
            ':status' => $payload['status'],
            ':delete_flg' => $payload['delete_flg'],
            ':source_file' => $payload['source_file'],
            ':crawl_error' => $payload['crawl_error'] !== '' ? $payload['crawl_error'] : null,
            ':reg_dtm' => substr($payload['reg_dtm'], 0, 19),
            ':reg_id' => $payload['reg_id'],
            ':mod_dtm' => substr($payload['mod_dtm'], 0, 19),
            ':mod_id' => $payload['mod_id'],
        ]
    );

    return (int) db_pdo()->lastInsertId();
}

function update_ai_news(array $news): void
{
    ensure_ai_news_schema();
    $newsSeq = (int) ($news['news_seq'] ?? $news['idx'] ?? 0);
    if ($newsSeq <= 0) {
        return;
    }

    $existing = find_ai_news((string) $newsSeq);
    if (!$existing) {
        return;
    }

    $payload = ai_news_db_payload($news, $existing);

    db_execute(
        sprintf(
            'UPDATE `%s`
             SET slug = :slug,
                 title = :title,
                 category = :category,
                 summary = :summary,
                 content_markdown = :content_markdown,
                 tags_json = :tags_json,
                 sources_json = :sources_json,
                 published_at = :published_at,
                 status = :status,
                 delete_flg = :delete_flg,
                 source_file = :source_file,
                 crawl_error = :crawl_error,
                 reg_dtm = :reg_dtm,
                 reg_id = :reg_id,
                 mod_dtm = :mod_dtm,
                 mod_id = :mod_id
             WHERE news_seq = :news_seq',
            ai_news_table_name()
        ),
        [
            ':news_seq' => $newsSeq,
            ':slug' => $payload['slug'],
            ':title' => $payload['title'],
            ':category' => $payload['category'],
            ':summary' => $payload['summary'],
            ':content_markdown' => $payload['content_markdown'],
            ':tags_json' => $payload['tags_json'],
            ':sources_json' => $payload['sources_json'],
            ':published_at' => $payload['published_at'] !== '' ? substr($payload['published_at'], 0, 19) : null,
            ':status' => $payload['status'],
            ':delete_flg' => $payload['delete_flg'],
            ':source_file' => $payload['source_file'],
            ':crawl_error' => $payload['crawl_error'] !== '' ? $payload['crawl_error'] : null,
            ':reg_dtm' => substr($payload['reg_dtm'], 0, 19),
            ':reg_id' => $payload['reg_id'],
            ':mod_dtm' => substr($payload['mod_dtm'], 0, 19),
            ':mod_id' => $payload['mod_id'],
        ]
    );
}

function upsert_ai_news_by_slug(array $news): int
{
    ensure_ai_news_schema();
    $slug = trim((string) ($news['slug'] ?? ''));
    if ($slug === '') {
        throw new InvalidArgumentException('뉴스 슬러그가 없습니다.');
    }

    $existing = find_ai_news_by_slug($slug);
    if ($existing) {
        $news['news_seq'] = (int) ($existing['news_seq'] ?? 0);
        $news['reg_dtm'] = $existing['reg_dtm'] ?? current_time();
        $news['reg_id'] = $existing['reg_id'] ?? 'system';
        update_ai_news($news);
        return (int) ($existing['news_seq'] ?? 0);
    }

    return insert_ai_news($news);
}

function delete_ai_news(string $id): void
{
    ensure_ai_news_schema();
    $newsSeq = (int) $id;
    if ($newsSeq <= 0) {
        return;
    }

    $existing = find_ai_news((string) $newsSeq);
    if (!$existing) {
        return;
    }

    db_execute(
        sprintf(
            'UPDATE `%s`
             SET delete_flg = \'Y\',
                 mod_dtm = :mod_dtm,
                 mod_id = :mod_id
             WHERE news_seq = :news_seq',
            ai_news_table_name()
        ),
        [
            ':news_seq' => $newsSeq,
            ':mod_dtm' => current_time(),
            ':mod_id' => (string) ($_SESSION['admin_login_id'] ?? 'system'),
        ]
    );
}

function delete_ai_news_bulk(array $ids): int
{
    $deleted = 0;
    foreach (array_values(array_unique(array_map('trim', $ids))) as $id) {
        if ($id === '') {
            continue;
        }
        $existing = find_ai_news($id);
        if (!$existing) {
            continue;
        }
        delete_ai_news($id);
        $deleted++;
    }

    return $deleted;
}

function crawl_pending_ai_news_files(): array
{
    ensure_ai_news_schema();
    $directory = ai_news_crawl_directory();
    $result = [
        'total' => 0,
        'success' => 0,
        'failed' => 0,
        'files' => [],
    ];

    if (!is_dir($directory)) {
        return $result;
    }

    $paths = glob($directory . '/*.json') ?: [];
    sort($paths, SORT_NATURAL);

    foreach ($paths as $path) {
        $payload = read_json_path($path);
        if (!is_array($payload) || strtoupper((string) ($payload['status'] ?? 'N')) !== 'N') {
            continue;
        }

        $result['total']++;
        $payload['status'] = 'P';
        $payload['error'] = null;
        write_json_path($path, $payload);

        try {
            $newsSeq = upsert_ai_news_by_slug([
                'slug' => (string) ($payload['slug'] ?? ''),
                'title' => (string) ($payload['title'] ?? ''),
                'category' => (string) ($payload['category'] ?? 'AI News'),
                'summary' => (string) ($payload['summary'] ?? ''),
                'content_markdown' => (string) ($payload['content_markdown'] ?? ''),
                'tags' => ai_news_tags_from_text(is_array($payload['tags'] ?? null) ? implode(',', $payload['tags']) : (string) ($payload['tags'] ?? '')),
                'sources' => ai_news_sources_from_text(is_array($payload['sources'] ?? null) ? ai_news_sources_to_text($payload['sources']) : (string) ($payload['sources'] ?? '')),
                'published_at' => (string) ($payload['published_at'] ?? current_time()),
                'status' => 'P',
                'delete_flg' => 'N',
                'source_file' => basename($path),
                'crawl_error' => '',
                'mod_dtm' => current_time(),
                'mod_id' => (string) ($_SESSION['admin_login_id'] ?? 'system'),
                'reg_dtm' => current_time(),
                'reg_id' => (string) ($_SESSION['admin_login_id'] ?? 'system'),
            ]);

            $payload['status'] = 'P';
            $payload['inserted_at'] = current_time();
            $payload['error'] = null;
            $payload['news_seq'] = $newsSeq;
            write_json_path($path, $payload);

            $result['success']++;
            $result['files'][] = [
                'file' => basename($path),
                'status' => 'Y',
                'news_seq' => $newsSeq,
            ];
        } catch (Throwable $exception) {
            $payload['status'] = 'E';
            $payload['error'] = $exception->getMessage();
            write_json_path($path, $payload);

            $result['failed']++;
            $result['files'][] = [
                'file' => basename($path),
                'status' => 'E',
                'error' => $exception->getMessage(),
            ];
        }
    }

    return $result;
}

function read_json_path(string $path): array
{
    if (!is_file($path)) {
        return [];
    }

    $json = file_get_contents($path);
    $data = json_decode($json ?: '[]', true);
    return is_array($data) ? $data : [];
}

function write_json_path(string $path, array $data): void
{
    $directory = dirname($path);
    if (!is_dir($directory)) {
        mkdir($directory, 0775, true);
    }

    file_put_contents($path, json_encode($data, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES), LOCK_EX);
}
