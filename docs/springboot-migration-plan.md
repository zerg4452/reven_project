# Spring Boot + Thymeleaf 관리자 전체 이관 계획

작성일: 2026-05-23

## Summary

- `Gradle + Java 21 + Spring Boot 3.5.14` 기준으로 신규 프로젝트를 생성한다.
- 기본 패키지는 `com.reven.project`로 고정한다.
- 명명 규칙은 `CO=공통/관리자`, `SA=설문`, `BD=뉴스/게시판`으로 적용한다.
- DB는 MariaDB + MyBatis를 사용하고, 초기 스키마/seed는 `schema.sql`, `data.sql`로 관리한다.
- 모든 화면/컨트롤러/서비스/매퍼 입출력 데이터는 DTO를 통해 전달한다.

## Key Changes

- 프로젝트 의존성:
  - Spring Web, Thymeleaf, Validation, MyBatis, MariaDB Driver, Spring Security.
  - `application.yml`에 `Asia/Seoul`, MyBatis mapper XML 위치, MariaDB 연결 설정을 둔다.
- 패키지 구조:
  - `com.reven.project.co`: 공통, 관리자 인증, 관리자 계정, 메뉴, 대시보드, 접속/수정 이력.
  - `com.reven.project.sa`: 설문 마스터, 문항, 옵션, 제출, 이력, CSV.
  - `com.reven.project.bd`: AI News, 뉴스 크롤링 JSON 반영, 게시/삭제/대시보드 패널.
- DTO 규칙:
  - Controller request/response, Thymeleaf form binding, Service 입출력, Mapper parameter/result는 DTO를 사용한다.
  - Entity 또는 Map 기반 화면 전달은 사용하지 않는다.
- REST형 URL:
  - 관리자 홈: `/admin/home.do`
  - 로그인: `/admin/login.do`
  - 설문 관리: `/admin/surveys/list.do`, `/admin/surveys/write.do`, `/admin/surveys/insert.do`, `/admin/surveys/update.do`, `/admin/surveys/delete.do`
  - 설문 이력: `/admin/survey-submissions/list.do`, `/admin/survey-submissions/detail.do`, `/admin/survey-submissions/download.do`
  - 사용자 화면: `/surveys/list.do`, `/surveys/detail.do`, `/surveys/submit.do`, `/surveys/thanks.do`
  - AI News: `/admin/news/list.do`, `/admin/news/write.do`, `/admin/news/insert.do`, `/admin/news/update.do`, `/admin/news/delete.do`, `/admin/news/crawl.do`

## DB / MyBatis

- 설문 테이블:
  - `sa_survey_mst`, `sa_survey_field_dtl`, `sa_survey_field_opt_dtl`, `sa_survey_submit_mst`, `sa_survey_answer_dtl`
  - `survey_uid`, `submit_uid`를 둬 URL은 public id로 처리하고 내부 조인은 numeric seq로 처리한다.
  - 제출 시 설문 제목, 문항 라벨, 문항 타입, 정렬, 답변을 snapshot으로 저장한다.
- 공통/관리자 테이블:
  - `co_adm_mst`, `co_adm_menu_mst`, `co_adm_access_log_mst`, `co_adm_mod_mst`, `co_adm_mod_dtl`, `co_dashboard_panel_mst`
- 뉴스 테이블:
  - `bd_ai_news_mst`
- Mapper:
  - `COAdmMapper`, `COAdmMenuMapper`, `COAccessLogMapper`, `COChangeLogMapper`
  - `SASurveyMapper`, `SASurveySubmitMapper`
  - `BDAiNewsMapper`
  - XML 경로는 `resources/mapper/co`, `resources/mapper/sa`, `resources/mapper/bd`.

## Thymeleaf / Frontend

- 공통 레이아웃:
  - `layout/base.html`, `fragments/gnb.html`, `fragments/lnb.html`, `fragments/breadcrumb.html`, `fragments/page-note.html`
- 화면 템플릿:
  - `admin/survey/manage/list.html`, `admin/survey/manage/detail.html`
  - `admin/survey/history/list.html`, `admin/survey/history/detail.html`
  - `survey/public/list.html`, `survey/public/write.html`, `survey/public/thanks.html`
  - `admin/news/*`, `admin/management/*`, `admin/auth/login.html`, `admin/home.html`
- UI 규칙:
  - 모든 노출 용어는 `설문` 기준.
  - GNB는 `설문 운영`, `관리자 홈`, `사용자 화면`, `로그아웃`만 노출한다.
  - breadcrumb는 모든 페이지 상단에 `1-depth > 2-depth > 3-depth`.
  - 목록 제목은 `▶` 포함.
  - 등록 버튼 파란색, 수정/저장 버튼 초록색, 삭제 버튼 빨간색, 버튼 텍스트는 흰색.
  - 각 HTML 페이지에는 화면명/목적/생성일 `yyyy-mm-dd` 주석을 포함한다.

## Multi-Agent Execution

- 메인 에이전트:
  - 전체 구조 통합, URL 매핑 충돌 검토, 테스트 실행, worklog 업데이트, 최종 검수 담당.
- 하위 에이전트 1:
  - Gradle Spring Boot 프로젝트 골격, 설정 파일, 기본 패키지, 실행 확인 담당.
- 하위 에이전트 2:
  - `CO`, `BD` 백엔드 컨트롤러/서비스/DTO/인증/관리자 전체 기능 담당.
- 하위 에이전트 3:
  - Thymeleaf 레이아웃, GNB/LNB/breadcrumb, 설문/뉴스/관리자 화면 템플릿과 CSS/JS 담당.
- 하위 에이전트 4:
  - `SA` DB 스키마, MyBatis mapper/xml, 설문 DB 전환, 제출 snapshot, CSV 쿼리 담당.

## Test Plan

- `./gradlew test`로 서비스/매퍼/DTO 바인딩 테스트 실행.
- `./gradlew bootRun` 후 주요 URL 확인:
  - `/surveys/list.do`, `/surveys/detail.do?surveyUid=...`, `/admin/login.do`, `/admin/home.do`, `/admin/surveys/list.do`, `/admin/survey-submissions/list.do`, `/admin/news/list.do`
- 설문 시나리오:
  - 설문 등록 → 문항/옵션 저장 → 사용자 제출 → 설문 수정 → 기존 설문 이력 상세 snapshot 불변 확인.
- DTO 검증:
  - 필수 입력 누락, 날짜 검색 기본값, keyword type 값, checkbox 배열 답변이 DTO로 정상 바인딩되는지 확인.
- CSV:
  - UTF-8 BOM 포함, 한글 깨짐 없음, 답변 snapshot 출력 확인.

## Assumptions

- Java는 21로 고정한다.
- Spring Boot는 Java 21과 안정성을 고려해 `3.5.14`를 사용한다.
- URL은 REST형 새 경로를 기준으로 만들고 PHP `.php` 경로 호환 alias는 1차 범위에서 제외한다.
- DB 초기화는 `schema.sql`, `data.sql`을 사용하고 Flyway는 도입하지 않는다.
- 레거시 `legacy-php-source/data/*.json`은 import 참고용으로만 다루며 커밋하지 않는다.
- 변경이 레이아웃, 내비게이션, 데이터 흐름, 저장 방식에 영향을 주므로 구현 시 `docs/worklog.md`를 업데이트한다.
