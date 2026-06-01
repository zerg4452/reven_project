# 기존 PHP 소스 맵

작성일: 2026-05-23

기존 PHP 프로젝트는 `legacy-php-source/`에 참고용으로 복사되어 있습니다. `data/*.json` 런타임 데이터는 복사 대상에서 제외했습니다.

## 공통

- `legacy-php-source/config.php`: 앱 이름, 관리자 기본 비밀번호, DB 접속 정보, timezone 설정
- `legacy-php-source/lib/helpers.php`: escape, redirect, 날짜 표시, 설문 필드 helper
- `legacy-php-source/lib/database.php`: PDO 기반 MariaDB 연결 helper
- `legacy-php-source/lib/storage.php`: JSON 저장, MariaDB 스키마 생성, 관리자/메뉴/뉴스/설문 저장 함수
- `legacy-php-source/partials/header.php`: 공통 HTML head, GNB, LNB, breadcrumb, 화면별 HTML 주석 메타
- `legacy-php-source/partials/footer.php`: 공통 footer
- `legacy-php-source/assets/style.css`: Bootstrap 보완 스타일 및 관리자 UI 스타일

## 사용자 화면

- `legacy-php-source/index.php`: 사용 가능한 설문 목록
- `legacy-php-source/submit.php`: 설문 작성 및 제출
- `legacy-php-source/thanks.php`: 제출 완료

## 설문 운영

- `legacy-php-source/admin/survey/forms.php`: 설문 관리 목록
- `legacy-php-source/admin/survey/form_edit.php`: 설문 등록/상세/수정
- `legacy-php-source/admin/survey/submissions.php`: 설문 이력 관리
- `legacy-php-source/admin/survey/submission_view.php`: 설문 이력 상세
- `legacy-php-source/admin/survey/submissions_export.php`: 설문 이력 CSV 다운로드

## 관리자/메뉴/로그

- `legacy-php-source/admin/core/auth.php`: 관리자 세션 인증
- `legacy-php-source/admin/management/admins.php`: 관리자 목록
- `legacy-php-source/admin/management/admin_edit.php`: 관리자 등록/수정
- `legacy-php-source/admin/management/menus.php`: 관리자 메뉴 관리
- `legacy-php-source/admin/management/dashboard_edit.php`: 대시보드 패널 설정
- `legacy-php-source/admin/management/access_logs.php`: 접속 이력
- `legacy-php-source/admin/management/change_logs.php`: 수정 이력 목록
- `legacy-php-source/admin/management/change_log_view.php`: 수정 이력 상세

## 뉴스

- `legacy-php-source/admin/news/ai_news.php`: AI News 목록
- `legacy-php-source/admin/news/ai_news_view.php`: AI News 상세
- `legacy-php-source/admin/news/ai_news_edit.php`: AI News 등록/수정
- `legacy-php-source/croll/ai-news/`: 크롤링 결과 참고 파일

## Docker

- `legacy-php-source/docker-compose.yml`: PHP Apache + MariaDB 구성
- `legacy-php-source/Dockerfile`: PHP Apache 이미지 구성

Spring Boot 전환 시 MariaDB 서비스는 재사용 가능하지만, 앱 컨테이너는 Java 빌드/런타임에 맞게 새로 구성해야 합니다.
