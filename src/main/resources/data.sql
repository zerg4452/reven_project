INSERT INTO co_adm_mst (
    login_id, name, role, status, password_hash, reg_dtm, reg_id, mod_dtm, mod_id
)
SELECT 'admin', '관리자', 'super', 'active',
       '{noop}admin123', NOW(), 'system', NOW(), 'system'
WHERE NOT EXISTS (SELECT 1 FROM co_adm_mst WHERE login_id = 'admin');

INSERT INTO co_adm_menu_mst (
    menu_cd, parent_menu_cd, depth_no, menu_nm, menu_url, match_urls_json,
    menu_type, board_key, use_yn, delete_flg, sort_ord, reg_dtm, reg_id, mod_dtm, mod_id
)
SELECT 'survey_operation', '', 1, '설문 운영', '/admin/surveys',
       '["/admin/surveys","/admin/survey-submissions"]',
       'group', '', 'Y', 'N', 10, NOW(), 'system', NOW(), 'system'
WHERE NOT EXISTS (SELECT 1 FROM co_adm_menu_mst WHERE menu_cd = 'survey_operation');

INSERT INTO co_adm_menu_mst (
    menu_cd, parent_menu_cd, depth_no, menu_nm, menu_url, match_urls_json,
    menu_type, board_key, use_yn, delete_flg, sort_ord, reg_dtm, reg_id, mod_dtm, mod_id
)
SELECT 'survey_manage', 'survey_operation', 2, '설문 관리', '/admin/surveys',
       '["/admin/surveys"]',
       'page', '', 'Y', 'N', 10, NOW(), 'system', NOW(), 'system'
WHERE NOT EXISTS (SELECT 1 FROM co_adm_menu_mst WHERE menu_cd = 'survey_manage');

INSERT INTO co_adm_menu_mst (
    menu_cd, parent_menu_cd, depth_no, menu_nm, menu_url, match_urls_json,
    menu_type, board_key, use_yn, delete_flg, sort_ord, reg_dtm, reg_id, mod_dtm, mod_id
)
SELECT 'survey_history', 'survey_operation', 2, '설문 이력 관리', '/admin/survey-submissions',
       '["/admin/survey-submissions"]',
       'page', '', 'Y', 'N', 20, NOW(), 'system', NOW(), 'system'
WHERE NOT EXISTS (SELECT 1 FROM co_adm_menu_mst WHERE menu_cd = 'survey_history');

INSERT INTO co_dashboard_panel_mst (
    board_key, panel_title, use_yn, item_limit, sort_ord, reg_dtm, reg_id, mod_dtm, mod_id
)
SELECT 'ai_news', 'AI News', 'Y', 5, 10, NOW(), 'system', NOW(), 'system'
WHERE NOT EXISTS (SELECT 1 FROM co_dashboard_panel_mst WHERE board_key = 'ai_news');
