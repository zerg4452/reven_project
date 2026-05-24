# PHP Survey Intake Site

관리자가 설문을 등록하고, 사용자가 인적사항과 함께 제출하면 관리자에서 설문 이력을 확인하는 PHP 사이트입니다.

## 실행 방법

PHP가 설치된 환경에서 프로젝트 루트에서 실행합니다.

```bash
php -S localhost:8000
```

브라우저에서 아래 주소를 엽니다.

- 사용자 화면: `http://localhost:8000/`
- 관리자 화면: `http://localhost:8000/admin/`

## Docker로 실행

macOS와 Windows 모두 Docker Desktop 설치 후 같은 명령으로 실행할 수 있습니다.

```bash
docker compose up -d
```

브라우저에서 아래 주소를 엽니다.

- 사용자 화면: `http://localhost:8000/`
- 관리자 화면: `http://localhost:8000/admin/`

Docker 구성에는 PHP 앱 컨테이너와 MariaDB 컨테이너가 함께 포함되어 있습니다.

중지하려면 아래 명령을 실행합니다.

```bash
docker compose down
```

PHP 문법 검사는 컨테이너에서 실행할 수 있습니다.

```bash
docker compose exec app php -l index.php
```

## 기본 관리자 계정

- 아이디: `admin`
- 비밀번호: `admin123`

기본 `admin` 계정은 슈퍼계정이며 관리자 목록에는 표시되지 않습니다.

운영 전에 [config.php](config.php)의 `ADMIN_PASSWORD` 값을 반드시 변경하세요.

## 데이터 저장

설문과 제출 이력, 관리자 접속이력은 현재 `data/` 폴더의 JSON 파일에 저장됩니다.
관리자 수정이력은 MariaDB `co_adm_mod_mst`와 `co_adm_mod_dtl`에 저장됩니다.
호스팅 서버에서는 `data/` 폴더에 쓰기 권한이 필요합니다.

DB 연동 준비:

- MariaDB 접속 정보는 `config.php`와 `docker-compose.yml`에 함께 정의되어 있습니다.
- 호스트에서 HeidiSQL로 붙을 때는 `127.0.0.1:3307`을 사용합니다.
- `lib/database.php`에 PDO 연결 헬퍼를 준비해 두었습니다.
- 관리자 계정은 MariaDB `co_adm_mst` 테이블로 전환했으며, 키는 `adm_seq INT` 입니다.
- 관리자 수정이력은 MariaDB `co_adm_mod_mst`와 `co_adm_mod_dtl`로 저장하며, 마스터는 `co_adm_mst`와 FK로 연결합니다.
- 새 테이블은 `adm_seq`, `mod_seq`, `mod_dtl_seq`와 `reg_dtm`, `reg_id`, `mod_dtm`, `mod_id` 감사 컬럼을 사용하는 방식으로 맞춥니다.
- 설문/제출 이력과 관리자 접속이력은 아직 JSON 저장 방식입니다.

## 주요 기능

- 관리자 설문 등록/수정
- 관리자 계정 관리
- 사용자 인적사항 포함 제출
- 관리자 설문 이력 조회
- 설문 이력 열람
- 설문 이력 CSV 다운로드

## 화면 구성

- Bootstrap 5 기반 UI
- 관리자 상단 GNB
- `admin/management/`에 관리자 관리 화면 분리
- `admin/management/access_logs.php` 관리자 접속이력
- `admin/management/change_logs.php` 관리자 수정이력
- `admin/survey/`에 설문 운영 화면 분리
- 관리자 좌측 LNB
