CREATE TABLE IF NOT EXISTS co_adm_mst (
    adm_seq BIGINT NOT NULL AUTO_INCREMENT COMMENT '관리자 일련번호',
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
    UNIQUE KEY uq_co_adm_mst_login_id (login_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='관리자 계정 마스터';

CREATE TABLE IF NOT EXISTS co_adm_menu_mst (
    adm_menu_seq BIGINT NOT NULL AUTO_INCREMENT COMMENT '관리자 메뉴 일련번호',
    menu_cd VARCHAR(100) NOT NULL COMMENT '메뉴 코드',
    parent_menu_cd VARCHAR(100) NOT NULL DEFAULT '' COMMENT '상위 메뉴 코드',
    depth_no INT NOT NULL COMMENT '메뉴 깊이',
    menu_nm VARCHAR(100) NOT NULL COMMENT '메뉴명',
    menu_url VARCHAR(255) NOT NULL DEFAULT '' COMMENT '메뉴 URL',
    match_urls_json TEXT NULL COMMENT '매칭 URL JSON',
    menu_type VARCHAR(20) NOT NULL DEFAULT 'page' COMMENT '메뉴 유형',
    board_key VARCHAR(100) NOT NULL DEFAULT '' COMMENT '게시판 키',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    delete_flg CHAR(1) NOT NULL DEFAULT 'N' COMMENT '삭제 여부',
    sort_ord INT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    reg_dtm DATETIME NOT NULL COMMENT '등록 일시',
    reg_id VARCHAR(100) NOT NULL COMMENT '등록자 아이디',
    mod_dtm DATETIME NOT NULL COMMENT '수정 일시',
    mod_id VARCHAR(100) NOT NULL COMMENT '수정자 아이디',
    PRIMARY KEY (adm_menu_seq),
    UNIQUE KEY uq_co_adm_menu_mst_menu_cd (menu_cd)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='관리자 메뉴 마스터';

CREATE TABLE IF NOT EXISTS co_adm_access_log_mst (
    access_log_seq BIGINT NOT NULL AUTO_INCREMENT COMMENT '접속 이력 일련번호',
    adm_seq BIGINT NULL COMMENT '관리자 일련번호',
    login_id VARCHAR(100) NOT NULL DEFAULT '' COMMENT '관리자 로그인 아이디',
    adm_nm VARCHAR(100) NOT NULL DEFAULT '' COMMENT '관리자명',
    location_nm VARCHAR(200) NOT NULL DEFAULT '' COMMENT '접속 위치',
    path VARCHAR(500) NOT NULL DEFAULT '' COMMENT '접속 경로',
    access_dtm DATETIME NOT NULL COMMENT '접속 일시',
    PRIMARY KEY (access_log_seq),
    KEY idx_co_adm_access_log_mst_access_dtm (access_dtm)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='관리자 접속 이력';

CREATE TABLE IF NOT EXISTS co_adm_mod_mst (
    mod_seq BIGINT NOT NULL AUTO_INCREMENT COMMENT '수정 이력 일련번호',
    adm_seq BIGINT NULL COMMENT '대상 관리자 일련번호',
    login_id VARCHAR(100) NOT NULL DEFAULT '' COMMENT '대상 로그인 아이디',
    mod_type VARCHAR(30) NOT NULL COMMENT '수정 유형',
    mod_dtm DATETIME NOT NULL COMMENT '수정 일시',
    mod_id VARCHAR(100) NOT NULL COMMENT '수정자 아이디',
    PRIMARY KEY (mod_seq),
    KEY idx_co_adm_mod_mst_mod_dtm (mod_dtm)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='관리자 수정 이력 마스터';

CREATE TABLE IF NOT EXISTS co_adm_mod_dtl (
    mod_dtl_seq BIGINT NOT NULL AUTO_INCREMENT COMMENT '수정 이력 상세 일련번호',
    mod_seq BIGINT NOT NULL COMMENT '수정 이력 일련번호',
    column_name VARCHAR(100) NOT NULL COMMENT '컬럼명',
    before_value TEXT NULL COMMENT '변경 전 값',
    after_value TEXT NULL COMMENT '변경 후 값',
    PRIMARY KEY (mod_dtl_seq),
    KEY idx_co_adm_mod_dtl_mod_seq (mod_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='관리자 수정 이력 상세';

CREATE TABLE IF NOT EXISTS co_dashboard_panel_mst (
    panel_seq BIGINT NOT NULL AUTO_INCREMENT COMMENT '대시보드 패널 일련번호',
    board_key VARCHAR(100) NOT NULL COMMENT '게시판 키',
    panel_title VARCHAR(100) NOT NULL COMMENT '패널 제목',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    item_limit INT NOT NULL DEFAULT 5 COMMENT '노출 건수',
    sort_ord INT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    reg_dtm DATETIME NOT NULL COMMENT '등록 일시',
    reg_id VARCHAR(100) NOT NULL COMMENT '등록자 아이디',
    mod_dtm DATETIME NOT NULL COMMENT '수정 일시',
    mod_id VARCHAR(100) NOT NULL COMMENT '수정자 아이디',
    PRIMARY KEY (panel_seq),
    UNIQUE KEY uq_co_dashboard_panel_mst_board_key (board_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='대시보드 패널 마스터';

CREATE TABLE IF NOT EXISTS sa_survey_mst (
    survey_seq BIGINT NOT NULL AUTO_INCREMENT COMMENT '설문 일련번호',
    survey_uid VARCHAR(32) NOT NULL COMMENT '설문 공개 식별자',
    title VARCHAR(200) NOT NULL COMMENT '설문 제목',
    description TEXT NULL COMMENT '설문 설명',
    start_date DATE NULL COMMENT '설문 접수 시작일',
    end_date DATE NULL COMMENT '설문 접수 종료일',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    delete_flg CHAR(1) NOT NULL DEFAULT 'N' COMMENT '삭제 여부',
    reg_dtm DATETIME NOT NULL COMMENT '등록 일시',
    reg_id VARCHAR(100) NOT NULL COMMENT '등록자 아이디',
    mod_dtm DATETIME NOT NULL COMMENT '수정 일시',
    mod_id VARCHAR(100) NOT NULL COMMENT '수정자 아이디',
    PRIMARY KEY (survey_seq),
    UNIQUE KEY uq_sa_survey_mst_survey_uid (survey_uid),
    KEY idx_sa_survey_mst_reg_dtm (reg_dtm)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='설문 마스터';

ALTER TABLE sa_survey_mst ADD COLUMN IF NOT EXISTS start_date DATE NULL COMMENT '설문 접수 시작일' AFTER description;
ALTER TABLE sa_survey_mst ADD COLUMN IF NOT EXISTS end_date DATE NULL COMMENT '설문 접수 종료일' AFTER start_date;

CREATE TABLE IF NOT EXISTS sa_survey_field_dtl (
    field_seq BIGINT NOT NULL AUTO_INCREMENT COMMENT '설문 문항 일련번호',
    survey_seq BIGINT NOT NULL COMMENT '설문 일련번호',
    field_key VARCHAR(100) NOT NULL COMMENT '문항 키',
    field_label VARCHAR(200) NOT NULL COMMENT '문항 라벨',
    survey_type VARCHAR(20) NOT NULL DEFAULT 'objective' COMMENT '설문 유형',
    field_type VARCHAR(30) NOT NULL COMMENT '문항 유형',
    required_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '필수 여부',
    sort_ord INT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    reg_dtm DATETIME NOT NULL COMMENT '등록 일시',
    reg_id VARCHAR(100) NOT NULL COMMENT '등록자 아이디',
    mod_dtm DATETIME NOT NULL COMMENT '수정 일시',
    mod_id VARCHAR(100) NOT NULL COMMENT '수정자 아이디',
    PRIMARY KEY (field_seq),
    KEY idx_sa_survey_field_dtl_survey_seq (survey_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='설문 문항 상세';

ALTER TABLE sa_survey_field_dtl ADD COLUMN IF NOT EXISTS survey_type VARCHAR(20) NOT NULL DEFAULT 'objective' COMMENT '설문 유형' AFTER field_label;

CREATE TABLE IF NOT EXISTS sa_survey_field_opt_dtl (
    field_opt_seq BIGINT NOT NULL AUTO_INCREMENT COMMENT '설문 문항 보기 일련번호',
    field_seq BIGINT NOT NULL COMMENT '설문 문항 일련번호',
    option_label VARCHAR(200) NOT NULL COMMENT '보기 라벨',
    option_value VARCHAR(200) NOT NULL COMMENT '보기 값',
    sort_ord INT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    reg_dtm DATETIME NOT NULL COMMENT '등록 일시',
    reg_id VARCHAR(100) NOT NULL COMMENT '등록자 아이디',
    mod_dtm DATETIME NOT NULL COMMENT '수정 일시',
    mod_id VARCHAR(100) NOT NULL COMMENT '수정자 아이디',
    PRIMARY KEY (field_opt_seq),
    KEY idx_sa_survey_field_opt_dtl_field_seq (field_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='설문 문항 보기 상세';

CREATE TABLE IF NOT EXISTS sa_survey_submit_mst (
    submit_seq BIGINT NOT NULL AUTO_INCREMENT COMMENT '설문 제출 일련번호',
    submit_uid VARCHAR(32) NOT NULL COMMENT '제출 공개 식별자',
    survey_seq BIGINT NULL COMMENT '설문 일련번호',
    survey_uid VARCHAR(32) NULL COMMENT '설문 공개 식별자',
    survey_title_snapshot VARCHAR(200) NOT NULL COMMENT '설문 제목 스냅샷',
    submitter_name VARCHAR(100) NOT NULL COMMENT '제출자명',
    phone VARCHAR(100) NOT NULL COMMENT '연락처',
    email VARCHAR(200) NULL COMMENT '이메일',
    birthdate DATE NULL COMMENT '생년월일',
    address VARCHAR(500) NULL COMMENT '주소',
    status VARCHAR(30) NOT NULL DEFAULT 'new' COMMENT '처리 상태',
    admin_memo TEXT NULL COMMENT '관리자 메모',
    submitted_dtm DATETIME NOT NULL COMMENT '제출 일시',
    ip VARCHAR(100) NULL COMMENT 'IP',
    reg_dtm DATETIME NOT NULL COMMENT '등록 일시',
    reg_id VARCHAR(100) NOT NULL COMMENT '등록자 아이디',
    mod_dtm DATETIME NOT NULL COMMENT '수정 일시',
    mod_id VARCHAR(100) NOT NULL COMMENT '수정자 아이디',
    PRIMARY KEY (submit_seq),
    UNIQUE KEY uq_sa_survey_submit_mst_submit_uid (submit_uid),
    KEY idx_sa_survey_submit_mst_submitted_dtm (submitted_dtm)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='설문 제출 마스터';

CREATE TABLE IF NOT EXISTS sa_survey_answer_dtl (
    answer_seq BIGINT NOT NULL AUTO_INCREMENT COMMENT '설문 답변 일련번호',
    submit_seq BIGINT NOT NULL COMMENT '설문 제출 일련번호',
    field_seq BIGINT NULL COMMENT '설문 문항 일련번호',
    field_key_snapshot VARCHAR(100) NOT NULL COMMENT '문항 키 스냅샷',
    field_label_snapshot VARCHAR(200) NOT NULL COMMENT '문항 라벨 스냅샷',
    field_type_snapshot VARCHAR(30) NOT NULL COMMENT '문항 유형 스냅샷',
    survey_type_snapshot VARCHAR(20) NULL COMMENT '설문 유형 스냅샷',
    required_yn_snapshot CHAR(1) NOT NULL DEFAULT 'N' COMMENT '필수 여부 스냅샷',
    answer_value TEXT NULL COMMENT '답변 표시 값',
    answer_json TEXT NULL COMMENT '답변 JSON',
    sort_ord INT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    reg_dtm DATETIME NOT NULL COMMENT '등록 일시',
    reg_id VARCHAR(100) NOT NULL COMMENT '등록자 아이디',
    mod_dtm DATETIME NOT NULL COMMENT '수정 일시',
    mod_id VARCHAR(100) NOT NULL COMMENT '수정자 아이디',
    PRIMARY KEY (answer_seq),
    KEY idx_sa_survey_answer_dtl_submit_seq (submit_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='설문 답변 상세';

ALTER TABLE sa_survey_answer_dtl ADD COLUMN IF NOT EXISTS survey_type_snapshot VARCHAR(20) NULL COMMENT '설문 유형 스냅샷' AFTER field_type_snapshot;

CREATE TABLE IF NOT EXISTS bd_ai_news_mst (
    news_seq BIGINT NOT NULL AUTO_INCREMENT COMMENT 'AI News 일련번호',
    title VARCHAR(300) NOT NULL COMMENT '제목',
    slug VARCHAR(200) NOT NULL COMMENT '슬러그',
    summary TEXT NULL COMMENT '요약',
    content LONGTEXT NULL COMMENT '본문',
    category VARCHAR(100) NULL COMMENT '분류',
    tags_json TEXT NULL COMMENT '태그 JSON',
    source_url VARCHAR(1000) NULL COMMENT '출처 URL',
    status CHAR(1) NOT NULL DEFAULT 'P' COMMENT '상태',
    view_cnt BIGINT NOT NULL DEFAULT 0 COMMENT '조회수',
    delete_flg CHAR(1) NOT NULL DEFAULT 'N' COMMENT '삭제 여부',
    published_dtm DATETIME NULL COMMENT '게시 일시',
    reg_dtm DATETIME NOT NULL COMMENT '등록 일시',
    reg_id VARCHAR(100) NOT NULL COMMENT '등록자 아이디',
    mod_dtm DATETIME NOT NULL COMMENT '수정 일시',
    mod_id VARCHAR(100) NOT NULL COMMENT '수정자 아이디',
    PRIMARY KEY (news_seq),
    UNIQUE KEY uq_bd_ai_news_mst_slug (slug),
    KEY idx_bd_ai_news_mst_published_dtm (published_dtm)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI News 마스터';

CREATE TABLE IF NOT EXISTS bd_photo_board_mst (
    photo_seq BIGINT NOT NULL AUTO_INCREMENT COMMENT '포토 게시판 일련번호',
    title VARCHAR(300) NOT NULL COMMENT '제목',
    publish_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '게시 여부',
    view_cnt BIGINT NOT NULL DEFAULT 0 COMMENT '조회수',
    delete_flg CHAR(1) NOT NULL DEFAULT 'N' COMMENT '삭제 여부',
    reg_dtm DATETIME NOT NULL COMMENT '등록 일시',
    reg_id VARCHAR(100) NOT NULL COMMENT '등록자 아이디',
    mod_dtm DATETIME NOT NULL COMMENT '수정 일시',
    mod_id VARCHAR(100) NOT NULL COMMENT '수정자 아이디',
    PRIMARY KEY (photo_seq),
    KEY idx_bd_photo_board_mst_reg_dtm (reg_dtm)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='포토 게시판 마스터';

CREATE TABLE IF NOT EXISTS bd_photo_board_file_dtl (
    photo_file_seq BIGINT NOT NULL AUTO_INCREMENT COMMENT '포토 게시판 첨부 일련번호',
    photo_seq BIGINT NOT NULL COMMENT '포토 게시판 일련번호',
    original_file_name VARCHAR(255) NOT NULL COMMENT '원본 파일명',
    stored_file_name VARCHAR(255) NOT NULL COMMENT '저장 파일명',
    stored_path VARCHAR(500) NOT NULL COMMENT '저장 경로',
    content_type VARCHAR(100) NOT NULL COMMENT 'MIME 타입',
    file_size BIGINT NOT NULL DEFAULT 0 COMMENT '파일 크기',
    sort_ord INT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    delete_flg CHAR(1) NOT NULL DEFAULT 'N' COMMENT '삭제 여부',
    reg_dtm DATETIME NOT NULL COMMENT '등록 일시',
    reg_id VARCHAR(100) NOT NULL COMMENT '등록자 아이디',
    mod_dtm DATETIME NOT NULL COMMENT '수정 일시',
    mod_id VARCHAR(100) NOT NULL COMMENT '수정자 아이디',
    PRIMARY KEY (photo_file_seq),
    KEY idx_bd_photo_board_file_dtl_photo_seq (photo_seq),
    KEY idx_bd_photo_board_file_dtl_sort_ord (photo_seq, sort_ord)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='포토 게시판 첨부 파일';

CREATE TABLE IF NOT EXISTS bd_notice_mst (
    notice_seq BIGINT NOT NULL AUTO_INCREMENT COMMENT '공지사항 일련번호',
    title VARCHAR(300) NOT NULL COMMENT '제목',
    content LONGTEXT NULL COMMENT '본문',
    publish_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '노출 여부',
    pin_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '상단 고정 여부',
    publish_dtm DATETIME NOT NULL COMMENT '게시일',
    view_cnt BIGINT NOT NULL DEFAULT 0 COMMENT '조회수',
    delete_flg CHAR(1) NOT NULL DEFAULT 'N' COMMENT '삭제 여부',
    reg_dtm DATETIME NOT NULL COMMENT '등록 일시',
    reg_id VARCHAR(100) NOT NULL COMMENT '등록자 아이디',
    mod_dtm DATETIME NOT NULL COMMENT '수정 일시',
    mod_id VARCHAR(100) NOT NULL COMMENT '수정자 아이디',
    PRIMARY KEY (notice_seq),
    KEY idx_bd_notice_mst_publish_dtm (publish_dtm),
    KEY idx_bd_notice_mst_pin_publish (pin_yn, publish_dtm)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='공지사항 마스터';

CREATE TABLE IF NOT EXISTS bd_notice_file_dtl (
    notice_file_seq BIGINT NOT NULL AUTO_INCREMENT COMMENT '공지사항 첨부 일련번호',
    notice_seq BIGINT NOT NULL COMMENT '공지사항 일련번호',
    file_type VARCHAR(20) NOT NULL DEFAULT 'ATTACH' COMMENT '파일 유형(THUMB/ATTACH)',
    original_file_name VARCHAR(255) NOT NULL COMMENT '원본 파일명',
    stored_file_name VARCHAR(255) NOT NULL COMMENT '저장 파일명',
    stored_path VARCHAR(500) NOT NULL COMMENT '저장 경로',
    content_type VARCHAR(100) NOT NULL COMMENT 'MIME 타입',
    file_size BIGINT NOT NULL DEFAULT 0 COMMENT '파일 크기',
    sort_ord INT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    delete_flg CHAR(1) NOT NULL DEFAULT 'N' COMMENT '삭제 여부',
    reg_dtm DATETIME NOT NULL COMMENT '등록 일시',
    reg_id VARCHAR(100) NOT NULL COMMENT '등록자 아이디',
    mod_dtm DATETIME NOT NULL COMMENT '수정 일시',
    mod_id VARCHAR(100) NOT NULL COMMENT '수정자 아이디',
    PRIMARY KEY (notice_file_seq),
    KEY idx_bd_notice_file_dtl_notice_seq (notice_seq),
    KEY idx_bd_notice_file_dtl_type (notice_seq, file_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='공지사항 첨부 파일';

-- 기존 게시판 테이블에 조회수 컬럼이 없을 경우 추가한다(기존 DB 대응, MariaDB IF NOT EXISTS).
ALTER TABLE bd_photo_board_mst ADD COLUMN IF NOT EXISTS view_cnt BIGINT NOT NULL DEFAULT 0 COMMENT '조회수' AFTER publish_yn;
ALTER TABLE bd_ai_news_mst ADD COLUMN IF NOT EXISTS view_cnt BIGINT NOT NULL DEFAULT 0 COMMENT '조회수' AFTER status;
