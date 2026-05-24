# Worklog

## 2026-05-23

- Spring Boot + Thymeleaf 전환을 위한 신규 레포지토리 작업 공간을 준비했다.
- 기존 PHP 프로젝트를 `legacy-php-source/` 아래에 참고용으로 복사했다.
- 런타임 JSON 데이터(`data/*.json`)는 커밋 대상에서 제외하기 위해 복사하지 않았다.
- 이관 개요, 기존 PHP 소스 맵, 설문 DB 모델 초안 문서를 추가했다.
- 향후 작업은 현재 PHP 기능을 Spring Boot에서 먼저 재현한 뒤 사용자 메인/설문화면 고도화/뉴스탭을 확장하는 방향으로 정리했다.
- Spring Boot + Thymeleaf 관리자 전체 이관 계획을 `docs/springboot-migration-plan.md`에 저장했다.
- Java 21, Spring Boot 3.5.14, Gradle, MariaDB, MyBatis, DTO 기반 입출력, REST형 URL 기준으로 신규 구현 작업을 시작했다.
- Gradle 기반 Spring Boot 프로젝트 골격, `application.yml`, `schema.sql`, `data.sql`을 추가했다.
- `CO`, `SA`, `BD` 패키지 기준으로 관리자 인증/홈, 설문 관리/제출/이력/CSV, AI News 관리의 1차 컨트롤러/서비스/DTO/Mapper 골격을 추가했다.
- Thymeleaf 관리자/사용자 화면과 공통 GNB/LNB/breadcrumb fragment, CSS/JS 정적 리소스를 추가했다.
- 설문 JSON 저장 흐름을 DB 저장 흐름으로 전환하기 위해 `sa_survey_*` 테이블과 MyBatis XML 쿼리를 작성하고 제출 이력 snapshot 저장 기준을 반영했다.
- 로컬 Java/Gradle이 없는 환경에서도 빌드/실행할 수 있도록 Spring Boot용 `Dockerfile`, `docker-compose.yml`, `.dockerignore`를 추가했다.
- Docker Compose로 앱과 MariaDB를 빌드/기동 검증했다. `docker compose build app`는 성공했고, `/`는 200, `/admin/login`은 200, `/admin`은 로그인 리다이렉트 302를 반환했다.
- 기존 PHP MariaDB 포트와 충돌하지 않도록 Spring Boot compose의 MariaDB 호스트 포트를 `13307`로 설정했다.
- DB 계정 row가 없거나 관리자 조회가 실패해도 로그인 가능한 fallback 슈퍼 관리자 계정(`admin` / `admin123`)을 추가했다.
- 관리자 로그인 폼의 Spring Security 파라미터명을 `loginId`로 맞추고 CSRF hidden input을 추가했다.
- Spring Boot 패키지 구조를 업무 접두어 우선 구조에서 `admin`, `client`, `service`, `common` 기준 구조로 재배치했다. 컨트롤러는 `admin`/`client`, 서비스·DTO·Mapper는 `service`, 보안/설정은 `common` 아래에 위치하도록 정리했다.
- 사용자 Thymeleaf 화면은 `templates/client/survey`, 관리자 전용 JS는 `static/admin/js`, 공통 CSS/JS는 `static/common` 아래로 이동했다.
- 관리자 GNB에서 업무 메뉴를 좌측에 정렬하고 `사용자 화면`, `로그아웃`은 우측 유틸 영역으로 분리했다. GNB에 `뉴스` 메뉴를 추가했다.
- class 기반 설문 DTO의 보일러플레이트를 줄이기 위해 Lombok 의존성을 추가하고 `SADto` 내부 DTO에 `@Getter`, `@Setter`를 적용했다.
- 컨트롤러, 서비스, 보안 필터/설정, MyBatis mapper, 설문 DTO에 메서드 역할과 주요 로직 의도를 설명하는 주석을 보강했다.
