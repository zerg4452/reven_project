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
- Docker Compose로 앱과 MariaDB를 빌드/기동 검증했다. `docker compose build app`는 성공했고, `/admin/login.do`는 200, `/admin/home.do`는 로그인 리다이렉트 302를 반환했다.
- 기존 PHP MariaDB 포트와 충돌하지 않도록 Spring Boot compose의 MariaDB 호스트 포트를 `13307`로 설정했다.
- DB 계정 row가 없거나 관리자 조회가 실패해도 로그인 가능한 fallback 슈퍼 관리자 계정(`admin` / `admin123`)을 추가했다.
- 관리자 로그인 폼의 Spring Security 파라미터명을 `loginId`로 맞추고 CSRF hidden input을 추가했다.
- Spring Boot 패키지 구조를 업무 접두어 우선 구조에서 `admin`, `client`, `service`, `common` 기준 구조로 재배치했다. 컨트롤러는 `admin`/`client`, 서비스·DTO·Mapper는 `service`, 보안/설정은 `common` 아래에 위치하도록 정리했다.
- 사용자 Thymeleaf 화면은 `templates/client/survey`, 관리자 전용 JS는 `static/admin/js`, 공통 CSS/JS는 `static/common` 아래로 이동했다.
- 관리자 GNB에서 업무 메뉴를 좌측에 정렬하고 `사용자 화면`, `로그아웃`은 우측 유틸 영역으로 분리했다. GNB에 `뉴스` 메뉴를 추가했다.
- class 기반 설문 DTO의 보일러플레이트를 줄이기 위해 Lombok 의존성을 추가하고 `SADto` 내부 DTO에 `@Getter`, `@Setter`를 적용했다.
- 컨트롤러, 서비스, 보안 필터/설정, MyBatis mapper, 설문 DTO에 메서드 역할과 주요 로직 의도를 설명하는 주석을 보강했다.
- 2026-05-24: 뉴스 메뉴 진입 시 검색 DTO의 offset/limit가 primitive int라서 null 바인딩 시 400이 발생하던 문제를 수정했다. BDAiNewsSearchRequestDto를 Integer로 변경하고, BDAiNewsService에서 기본값을 보정하도록 바꿨다.
- 2026-05-24: 관리자 GNB 업무 메뉴 순서를 `관리자 홈`, `관리자 관리`, `뉴스`, `설문 운영`으로 조정했다.
- 2026-05-24: 레거시 PHP의 `관리자 관리 > 메뉴 관리` 누락을 확인하고 Spring Boot에 `/admin/management/menus` 메뉴 관리 화면, 서비스, MyBatis 매퍼를 추가했다. `co_adm_menu_mst` 기반으로 메뉴 트리 조회, 신규/수정, 하위 메뉴가 없는 항목의 soft delete, 메뉴 코드 중복 및 순환 부모 검증을 처리한다. 관리자 관리 LNB와 기본 메뉴 seed에도 `메뉴 관리`를 반영했다.
- 2026-05-24: AI News 목록 진입 시 keywordType이 null일 때 List.contains(null)에서 발생하던 NPE를 수정했다. BDAiNewsService.normalizeSearch에서 keywordType null-safe 처리를 추가했다.
- 2026-05-24: AI News 목록 검색 영역을 레거시 흐름에 맞게 재구성했다. 날짜/상태/키워드 검색을 분리하고, 상태 체크박스(N/P/Y/E)를 복원했으며, 검색 입력 폭을 줄여 버튼이 깨지지 않도록 전용 CSS를 적용했다.
- 2026-05-24: 관리자 관리 목록에 레거시 PHP와 같은 검색 영역을 추가하고 `co_adm_mst` 조회를 MyBatis로 연결했다. 등록일자, 검색조건(전체/관리자명/아이디), 검색어 필터를 서버에서 처리하고 목록의 등록일/수정일을 함께 노출한다.
- 2026-05-24: 관리자 목록 조회가 DB/매핑 오류로 화면을 깨지 않도록 컨트롤러에서 예외를 잡아 빈 목록과 오류 메시지를 보여주게 했다. 검색 쿼리는 레거시와 같은 날짜 범위 방식으로 정리했다.
- 2026-05-25: 관리자 메뉴 관리의 좌측 트리를 레거시 PHP처럼 `jsTree` 기반으로 교체했다. 메뉴 노드 JSON을 서버에서 생성해 초기 확장/선택 상태를 전달하고, 노드 클릭 시 해당 메뉴 편집 화면으로 이동하도록 맞췄다.
- 2026-05-25: `jsTree`를 CDN 의존 대신 `static/vendor`로 로컬 반영했고, jsTree CSS가 기본 스타일을 유지하도록 custom tree 스타일을 제거했다. 메뉴 트리의 parent id도 jsTree 규격에 맞게 관리자 메뉴 일련번호로 전달한다.
- 2026-05-24: 뉴스 관리 검색 영역을 다시 조정해 라벨-입력 간격을 줄이고, 검색조건 셀렉트와 검색어 폭을 재배치했으며, 검색/초기화 버튼을 분리했다. 또한 레거시 AI News JSON을 읽어 DB에 upsert하는 크롤링 버튼과 처리 결과 메시지를 추가했고, Docker 이미지에 legacy-php-source/croll 데이터를 포함하도록 조정했다.
- 2026-05-24: 프로젝트 전체 URL 정책을 `.do` 종결형으로 통일하고 상세/수정/목록 이동을 쿼리 파라미터 기반으로 정리했다. 관리자 로그인/홈, 설문 관리/이력, 뉴스, 관리자 관리, 사용자 화면의 컨트롤러와 Thymeleaf 링크를 모두 `.do`로 맞췄으며, 메뉴 관리의 `id` 쿼리도 `adminMenuSeq`로 교정했다. 설문 저장 후 상세 이동이 끊기지 않도록 `detail.do` 별칭을 추가하고 Docker 빌드로 재검증했다.
- 2026-05-24: 관리자 영역 URL 규칙을 다시 정리해 목록은 `/list.do`, 상세 화면은 `/write.do`, 등록은 `/insert.do`, 수정은 `/update.do`, 삭제는 `/delete.do`로 분리했다. 사용자 영역은 목록 `/list.do`, 상세 `/detail.do`로 단순화하고, 설문과 AI News의 저장/삭제 폼 및 메뉴 관리 화면도 같은 패턴으로 맞췄다. 사용자 화면 링크는 `/surveys/list.do`로 이동하도록 조정했다.
- 2026-05-25: 목록형 화면의 빈 상태 문구를 `현재 등록된 내용이 없습니다.`로 통일했다. 뉴스, 관리자 목록, 접속 이력, 최근 설문 이력, 설문 관리 목록, 설문 이력 관리 목록은 테이블 `tbody` 안에 빈 `td` 행이 나오도록 바꿔서 데이터가 0건이어도 화면이 깨지지 않게 정리했다.
- 2026-05-25: 관리자 접속이력 화면의 진입 오류를 수정했다. `COAccessLogSearchRequestDto`의 `offset`/`limit`를 `Integer`로 바꾸고, `COAdminManagementController`에서 접속 이력 검색 조건을 null-safe 하게 보정해 검색 파라미터가 없어도 `/admin/management/access-logs/list.do`가 정상 진입하도록 했다.
