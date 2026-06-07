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
- class 기반 설문 DTO의 보일러플레이트를 줄이기 위해 Lombok 의존성을 추가하고 `SASurveyDto` 내부 DTO에 `@Getter`, `@Setter`를 적용했다.
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
- 2026-05-25: 관리자 영역 CRUD 정상 동작을 재점검했다. 관리자 로그인 후 뉴스, 설문, 관리자 메뉴 관리에 대해 각각 insert/detail(delete 포함) 흐름을 실제로 수행해 302 리다이렉트와 상세 조회 성공을 확인했고, 삭제 후에는 목록 검색 결과에서 노출되지 않음을 재검증했다. 관리자 목록과 접속이력 목록도 200으로 정상 진입했다.
- 2026-05-25: 뉴스 관리 검색 영역을 기준으로 관리자 관리와 설문 관리 검색 폼을 다시 맞췄다. 두 화면 모두 날짜/검색조건/검색어/검색 버튼/초기화 버튼의 배치를 뉴스 목록과 동일한 비율로 정리하고, 같은 CSS 토큰을 재사용하도록 조정했다. Docker 이미지를 다시 빌드해 반영을 확인했다.
- 2026-05-25: 설문 이력 관리 검색 영역도 뉴스 목록 기준으로 다시 정리했다. 날짜/상태/검색조건/검색어/검색 버튼/초기화 버튼을 한 화면 안에서 재배치하고, 상태 필터는 복수 체크박스로 바꿔 `SubmissionSearchRequest.statuses`와 MyBatis `IN` 검색으로 연결했다. CSV 출력도 동일한 검색 조건을 공유하도록 맞췄다.
- 2026-05-25: 레거시 PHP의 관리자 메뉴 관리 화면을 다시 대조해 Spring Boot 메뉴 관리의 jsTree 이벤트와 레이아웃을 보정했다. 초기 선택 복원은 화면 이동을 일으키지 않고 실제 사용자 클릭만 편집 화면으로 이동하도록 바꿨으며, 트리/폼 영역은 레거시의 4:8 구성에 가깝게 조정했다.
- 2026-05-25: 관리자 등록/상세 화면의 입력 박스가 지나치게 길게 늘어나지 않도록 전용 폼 클래스를 추가했다. 데스크톱에서는 최대 폭을 제한한 3열 입력 배치로, 좁은 화면에서는 2열과 1열로 자연스럽게 줄어들도록 CSS를 보정했다.
- 2026-05-25: 관리자 등록/상세 화면의 필드 배치에서 `*`가 다음 줄로 밀리는 문제가 보여서, 입력을 한 줄에 하나씩 쌓는 단순한 세로 폼으로 다시 정리했다. 레이블과 입력을 분리해 줄바꿈 깨짐 없이 보이도록 수정했다.
- 2026-05-25: 관리자 등록/상세 화면의 라벨 내 `*`가 또 줄바꿈되는 현상을 막기 위해, 필드 라벨을 inline-flex로 고정하고 required 마커를 inline-block으로 맞췄다.
- 2026-05-26: 관리자 관리 화면의 등록/수정/삭제를 실제 `co_adm_mst` DB에 연결했다. 관리자 상세 조회를 추가하고, 아이디 중복 검사, 비밀번호 해시 저장, 수정 시 기존 비밀번호 유지, 삭제, 성공/오류 메시지 표시를 붙였다. 로그인 실패는 `/admin/login.do?error`를 화면 메시지로 보여주도록 정리했다.
- 2026-05-26: 관리자 GNB의 로그아웃 링크를 POST 폼으로 바꿔 Spring Security logout 처리와 맞췄다. 기존 GET 링크 때문에 발생하던 `/admin/logout.do` 404를 없애고, 모바일 GNB에서도 버튼이 링크처럼 보이도록 공통 스타일을 보강했다.
- 2026-05-26: 사용자 메인 화면을 추가해 `/`, `/main.do`, `/index.do`에서 진입하도록 했다. 사용자 전용 GNB는 `뉴스 > AI`, `설문 > 진행중인 설문` hover 메뉴로 구성하고 관리자 이동 버튼을 제거했다. 메인에는 16:9 IT 이미지를 상단에 배치하고 진행중인 설문/신규 AI 뉴스 각 최대 3건, 하단 미정 이미지 영역을 추가했다.
- 2026-05-26: 사용자 AI 뉴스 화면을 게시 상태(`Y`) 글만 보이는 FAQ/아코디언 목록으로 추가하고 제목/내용 검색을 연결했다. 사용자 설문 목록은 삭제되지 않은 설문을 모두 표시하되 진행중인 설문을 우선 배치하고 미사용 설문은 마감 카드로 딤드 처리해 참여할 수 없게 했다.
- 2026-05-26: 사용자 화면에서는 관리자용 위치 표시가 필요하지 않아 메인, AI 뉴스, 설문 목록/작성/완료 화면의 breadcrumb 영역을 제거했다.
- 2026-05-26: 사용자 메인 하단 미정 영역 이미지를 치오리풍 침대 일러스트 이미지로 교체했다.
- 2026-05-26: 사용자 화면의 이미지/패널/카드 박스 테두리를 관리자 화면보다 더 둥글게 보이도록 public 전용 radius 값을 추가했다.
- 2026-05-26: 사용자 메인 상단 hero 이미지를 좌우 여백 없이 viewport 폭을 채우는 full-bleed 영역으로 조정했다.
- 2026-05-26: 사용자 화면 디자인 컨셉을 치오리풍으로 잡고 public 전용 검정/금색/붉은색 톤, 꽃잎형 제목 마커, 더보기 장식, 둥근 그라데이션 버튼, 카드 hover, 드롭다운/아코디언 애니메이션을 추가했다.
- 2026-05-26: 사용자 GNB를 치오리풍으로 강화해 금장 하단 라인, 꽃잎형 브랜드 장식, 둥근 pill 메뉴, 금색 다이아 메뉴 표시, 크림톤 드롭다운과 모바일 전개 스타일을 추가했다.
- 2026-05-26: 사용자 메인 상단 hero 비주얼을 폰타인 실내 배경의 폰타인 캐릭터 일러스트로 교체했다.
- 2026-05-27: 사용자 GNB의 1뎁스 hover와 하위 메뉴 사이 간격을 제거해 마우스 이동 시 dropdown이 끊기지 않도록 조정했다.
- 2026-05-27: 관리자 GNB/LNB가 메뉴 관리(`co_adm_menu_mst`)의 사용 메뉴 기준으로 동적 렌더링되도록 변경했다. 관리자 요청마다 현재 URL과 메뉴의 URL/match URL을 비교해 active GNB와 LNB를 계산하고, 관리자 홈/관리자 관리/뉴스/설문 운영 화면의 하드코딩 LNB를 공통 동적 fragment로 통일했다. 기존 기본 seed의 예전 URL 값은 사용자 정의를 덮지 않는 조건부 update로 `.do` URL에 맞춰 보정했다.
- 2026-05-27: 관리자 GNB를 메뉴관리의 계층 구조까지 반영하도록 다시 조정했다. GNB는 상위 메뉴와 자식 메뉴를 함께 렌더링하고, board 타입 메뉴도 메뉴관리에서 바꾼 라벨/순서/활성 경로가 그대로 보이도록 관리자 전용 드롭다운 스타일을 추가했다.
- 2026-05-27: 관리자 메뉴 저장 시 메뉴 코드를 수정할 수 없도록 막아 부모-자식 연결이 끊기는 사례를 방지했다. 관리자 GNB는 Thymeleaf 재귀 fragment로 재구성해 메뉴관리의 중첩 트리도 그대로 렌더링하도록 바꿨고, 메뉴 코드 입력칸은 기존 메뉴 편집 시 읽기 전용으로 표시한다.
- 2026-05-27: 관리자 GNB 재귀 fragment가 Thymeleaf 평가 중 500을 유발해, 3-depth까지의 중첩을 명시적 반복 렌더링으로 다시 바꿨다. 실제 `/admin/management/menus/list.do`와 메뉴 수정 화면을 HTTP로 재검증해 200 응답과 읽기 전용 메뉴 코드 표시를 확인했다.
- 2026-05-27: 게시판 경로를 `/admin/board` 1-depth로 재정리하고 AI News를 `/admin/board/ai-news/*`, `/board/ai-news/*`로 이동했다. 관리자용 `게시판` 허브 화면과 포토게시판 CRUD를 추가했으며, 포토게시판은 제목/게시 여부/첨부파일만 편집하고 최대 5개까지 이미지, GIF, WEBP, MP4 업로드가 가능하도록 DB 테이블, MyBatis 매퍼, 파일 저장소, 파일 서빙 엔드포인트를 새로 넣었다. 포토게시판은 수정 시 새 첨부 집합으로 교체되도록 저장 정책을 맞췄고, 공통 GNB와 메인 링크도 새 경로로 갱신했다. Docker Gradle 테스트를 다시 돌려 `BUILD SUCCESSFUL`을 확인했다.
- 2026-05-27: 포토게시판 저장 후 새 글 저장 시에도 기존 첨부 삭제 로직이 타서 방금 업로드한 파일이 소거되던 버그를 수정했다. 신규 저장과 수정 저장을 분리해 수정 시에만 기존 첨부를 soft delete + 물리 파일 정리하도록 바꿨고, 저장 성공 후에는 목록으로 redirect하면서 1회성 알럿이 뜨도록 처리했다. 상세 화면에서는 업로드한 이미지가 `/admin/board/photo/file.do`를 통해 실제로 렌더링되는지 라이브 검증까지 완료했다.
- 2026-05-27: 포토게시판 업로드 파일이 앱 컨테이너 내부에만 저장되어 재빌드/재기동 시 사라지는 문제를 확인하고, `docker-compose.yml`의 app 서비스에도 `./storage:/app/storage` 볼륨을 추가해 업로드 파일이 호스트 볼륨에 남도록 정리했다. 동시에 상세 화면에는 파일 용량을 최소 KB 단위로 표기하고, 원본 다운로드 버튼을 추가했다.
- 2026-05-27: 포토게시판 상세의 첨부 미리보기 비율을 더 크게, 더 자연스럽게 보이도록 다시 조정했다. 상세 첨부 영역은 1열 중심으로 바꾸고, 이미지/비디오는 `contain` 유지 상태에서 자연 비율로 최대 높이를 더 넉넉하게 잡았으며, 첨부 목록 영역에는 내부 스크롤을 유지해 최대 5개 파일이 들어와도 화면이 무너지지 않도록 했다.
- 2026-05-27: 포토게시판 상세의 이미지 미리보기에 클릭형 레이어팝업을 추가했다. 이미지 클릭 시 딤드 오버레이가 뜨고 큰 이미지가 중앙에 표시되며, 배경 클릭이나 ESC로 닫히도록 처리했다. 동영상은 기존처럼 상세에서 직접 재생만 유지한다.
- 2026-05-28: 포토게시판 첨부 카드 UI를 세로 목록 + 좌측 대형 미리보기/우측 파일명·용량·다운로드 메타 영역 구조로 재배치했다. 등록/수정/상세 화면 모두 동일한 카드 레이아웃을 사용한다.
- 2026-05-28: AI News 크롤 JSON 경로를 저장소 루트 `croll/ai-news`로 통일하고 Docker 이미지에도 동일 경로를 포함하도록 수정했다. 포토게시판 저장 검증 실패 시 제목·게시여부를 flash로 복원하고, 이미지 20MB·동영상 50MB 업로드 제한을 서비스에서 검증하도록 추가했다. 메뉴 네비게이션 테스트는 `/admin/board/*` 경로 기준으로 갱신했다.
- 2026-05-29: Java `record` DTO 레이아웃 규칙(`.cursor/rules/java-record-schema.mdc`)을 추가했다. record 컴포넌트는 기존 여러 줄 형태를 유지하되 필드마다 `@Schema(description, example)` 한글 메타와 템플릿 값을 붙이고, 컴포넌트 사이에 빈 줄 2줄을 둔다. `io.swagger.core.v3:swagger-annotations` 의존성을 추가하고 CO/BD record DTO 30개에 일괄 반영했다.
- 2026-05-30: 사용자 GNB의 게시판 드롭다운에 `AI News`와 `포토 게시판`을 함께 노출하도록 구조를 확장했다. 사용자 포토 게시판은 관리자 포토 게시판 데이터를 사용하되 게시 중인 글만 공개하고, 목록은 9개 단위 카드형 썸네일 그리드로 구성했다. 검색은 제목 검색과 이미지 포함/동영상 포함 체크박스를 제공하며, 두 체크박스는 OR 조건으로 처리한다. 파일 유형은 새 컬럼 없이 첨부 파일의 `content_type`으로 판단한다. 상세 화면에서는 이미지 확대와 동영상 재생을 같은 딤드 레이어 모달에서 지원한다. 비정상 상세 접근은 `비정상적인 접근입니다.` 알럿 후 목록으로 이동하고, 비정상 파일 직접 접근은 404로 응답하도록 정리했다.
- 2026-05-30: 로컬 Java 런타임과 Gradle wrapper가 없어 Docker의 `gradle:8.14.3-jdk21` 이미지로 변경 테스트와 전체 테스트를 실행해 `BUILD SUCCESSFUL`을 확인했다. 같은 이미지로 앱을 임시 기동해 `/board/photo/list.do`, 검색 조건 포함 목록, 공개 상세, 공개 파일 200 응답, 비정상 상세 알럿 페이지, 비정상 파일 404를 HTTP로 확인했다.

## 2026-05-31

- 설문 DTO holder 클래스명을 도메인 의미에 맞게 `SADto`에서 `SASurveyDto`로 변경했다. 컨트롤러·서비스·MyBatis mapper XML·`COMainControllerTest`의 import와 FQCN을 함께 갱신했다.
- 공지사항(BDNotice) 게시판을 추가했다. 관리자 `/admin/board/notice/*`, 사용자 `/board/notice/*`, `bd_notice_mst`/`bd_notice_file_dtl`, 썸네일·첨부 업로드, 상단 고정·미래 게시일 미노출, 공개 목록(고정+페이징)을 구현했다.
- 포토게시판·AI News 공개 상세에 조회수 증가와 `BDBoardViewCountSupport` 쿠키 중복 방지를 연결했다.
- 공지 관리 목록에 게시일 범위 검색(기본 60일~내일)을 추가했고, 공개 상세 첨부 조회의 중복 DB 호출을 `findPublicNoticeFilesForDetail`로 줄였다. `BDNoticeAdminControllerTest`, `BDNoticePublicControllerTest`를 추가했다.

## 2026-06-01

- 사용자 설문 화면에 `objective`/`subjective` 문항 렌더링을 반영했다.
- `SASurveyDto.AnswerRequest`에 `values` 리스트를 추가해 체크박스처럼 여러 값이 들어오는 제출을 보존하도록 바꿨다.
- `SAPublicSurveyController.submit()`은 `MultiValueMap` 기반으로 `answers[fieldKey]`를 그룹핑하고, `SASurveySubmitService`는 survey field 순서대로 답변 스냅샷을 저장한다.
- 단일 객관식은 보기 라벨을 `answer_value`에, 체크박스는 라벨 조인 문자열과 raw value JSON 배열을 각각 `answer_value`와 `answer_json`에 저장하도록 정규화했다.
- 사용자 설문 문항 템플릿을 `client/survey/field.html` fragment로 분리했고, 공통 CSS에 질문/보기 레이아웃을 최소한으로 추가했다.
- 세부 테스트와 전체 Gradle 테스트를 모두 성공시켜 public survey P1 변경이 기존 테스트를 깨지 않는 것을 확인했다.

## 2026-06-02

- 설문 P4 문항 순서 변경을 구현했다. 관리자 설문 상세 `detail.html`에 ▲/▼ 버튼을 추가하고 `survey-field-editor.js`에서 DOM swap 후 `fields[n]` name을 재부여한다. 저장은 기존 full form POST로 `sort_ord` 1..N에 반영된다.
- TDD로 `SASurveyServiceTest`, `SAAdminSurveyControllerTest`, `SASurveyFieldReorderViewTest`를 추가한 뒤 UI/JS/CSS를 구현했다. `./gradlew test` 성공.
- `docs/` 정리. 루트에는 checklist/progress/context-notes/worklog만 두고, 완료 설계·계획은 `docs/clear/` 분류(migration, survey, photo-board, user-main, notice-board)로 이동. `docs/superpowers/` 제거.

## 2026-06-03

- 관리자 설문 등록 저장 후 `write.do` 상세로 남던 흐름을 공지사항과 같이 목록 리다이렉트 + `surveySavedMessage` 알럿으로 변경했다. 수정 저장은 상세 화면에서 알럿만 표시한다.
- 객관식 보기 입력을 줄바꿈 textarea 대신 보기별 입력란 + `보기 추가`/`삭제` UI로 바꿨다. form은 `fields[n].options[m].optionLabel`로 전송하고 기존 `normalizedOptions()`가 그대로 처리한다.
- AI News 편집 화면의 `status` 셀렉트가 현재 `news.status`를 반영하도록 `th:selected`를 추가했다. 이후 상태 체계를 `P/Y/E`로 다시 정리하면서 편집 화면도 `처리중/완료/에러`만 선택 가능하게 맞췄다. 템플릿과 서비스/컨트롤러 회귀 테스트를 추가했고 `./gradlew test`로 전체 회귀를 확인했다.
- AI News 상태를 레거시 파일 단계와 DB 단계로 다시 분리했다. JSON 수집 시 `N`은 DB 저장용 `P`로 정규화하고, 관리자 목록/상세/편집에서는 `P/Y/E`만 노출하도록 바꿨다. `statusText`도 `처리중/완료/에러` 기준으로 정리했고, 목록 필터와 편집 셀렉트에서 `N`을 제거했다. 서비스/컨트롤러/템플릿 회귀 테스트를 추가한 뒤 `./gradlew test`로 확인했다.
- AI News 크롤링 재수집 버그를 수정했다. Spring 크롤러가 이제 `N` 상태 JSON만 한 번 처리하고, 성공 시 소스 JSON을 `P`와 `inserted_at`으로 갱신한다. 같은 slug의 DB 글이 이미 `Y`이면 DB 상태를 되돌리지 않고 JSON만 소비 처리한다. 재수집 회귀 테스트를 추가했고 `./gradlew test`로 전체 회귀를 다시 확인했다.
- AI News 크롤링 인입 제목을 JSON `published_at` 날짜 기준 `[yyyy-MM-dd] 제목` 형식으로 저장하도록 바꿨다. 날짜는 크롤링 시점이 아니라 JSON 생성 시점 메타를 쓰고, 수동 등록/수정에는 영향이 없도록 크롤링 경로에만 적용했다. 회귀 테스트와 전체 Gradle 테스트를 다시 통과했다.
- AI News 편집 화면에서 저장/삭제 후 목록으로 돌아가고, 목록 화면에서 `aiNewsSavedMessage` flash attribute를 브라우저 알럿으로 띄우도록 바꿨다. 저장/수정/삭제 모두 목록으로 리다이렉트되며, 기존 크롤링 완료 메시지는 목록 상단 안내로 유지한다. 회귀 테스트와 전체 Gradle 테스트를 다시 통과했다.
- AI News 수집 방식 견적 문서를 추가했다. 배치형은 기존 JSON import에 cron, 제한값, 중복 실행 방지, 실행 로그만 붙이는 작은 범위로 보고, 인앱 크롤링형은 파서, 페이지네이션, 재시도, 중복 제거, 운영 로그까지 포함하는 별도 서브시스템으로 정리했다. 1차 권장안은 배치형이다.
- AI News 수집 방식 견적 문서를 완료 문서 폴더에서 진행 예정 문서 폴더로 옮겼다. `docs/clear`에는 완료 문서만 두고, `docs/planned`에는 작업 예정 문서를 둔다.
- `.cursor/rules/html-markup-layout.mdc` 규칙(요소 단위 줄바꿈, 블록 구분 빈 줄, 구역 시작·끝 주석)을 기존 Thymeleaf 템플릿 32개에 일괄 반영했다. redirect 전용 `invalid-access` 2개와 `fragments/layout.html`은 제외했다. 템플릿·설문 상세 회귀 테스트를 통과했다.
- Thymeleaf Layout Dialect pilot을 적용했다. `thymeleaf-layout-dialect` 의존성, `layouts/admin.html`, `admin/news/detail.html`만 `layout:decorate`로 전환했고 detail 컨트롤러에 `pageTitle`·`layoutGnbActive`·`layoutLnbActive`를 추가했다. 나머지 admin/client 화면은 기존 fragment shell을 유지한다.
- 관리자 템플릿 19개 전체를 Layout Dialect로 전환했다. `layouts/admin.html`(title/pageExtras/pageScripts fragment), `layouts/auth.html`(로그인), admin 페이지는 shell 제거 후 `layout:decorate`만 사용. `AdminLayoutTemplateTest` 추가.
- 관리자 CSS 미적용 원인은 실행 중인 앱이 Layout Dialect 반영 전 classpath로 동작한 것이었다. `COThymeleafLayoutConfig`로 dialect bean을 명시 등록하고, admin `<title layout:fragment="title">`를 보강했다. `AdminLayoutRenderIntegrationTest`로 shell·CSS 렌더링을 검증한다.
- 설문 P5 미리보기 화면을 구현했다. 저장된 관리자 설문 상세에서 새 창 미리보기 버튼을 제공하고, 공개 설문 폼을 재사용하되 `previewMode`에서 안내 표시, 제출 버튼 제거, form submit 차단, 제출자·문항 입력 비활성화를 적용했다. 잘못된 `surveyUid`는 관리자 설문 목록으로 돌려보내고 비정상 접근 알림을 표시한다. `SAAdminSurveyControllerTest`, `SASurveyPreviewViewTest`를 추가했고 `./gradlew test`를 통과했다.
- 관리자 전 화면 레이아웃 깨짐을 수정했다. `layouts/admin.html`의 `layout:fragment="content"`가 `<main class="admin-content">`에 직접 붙어 있어 페이지의 `<th:block layout:fragment="content">`가 main wrapper를 통째로 대체하고, 본문 노드가 `.admin-shell` grid 직속 자식으로 흩어졌다. layout 쪽은 `<main>` 안에 `<th:block layout:fragment="content">`를 두도록 바꿨고, `AdminLayoutRenderIntegrationTest`에 main wrapper 검증을 추가했다. IDE 기동 실패를 유발하던 중복 `COThymeleafLayoutConfig`는 제거했다(Spring Boot auto-config 사용).
- 설문 P6 검색 강화를 구현했다. `LenientLocalDateEditor`로 잘못된 날짜 바인딩을 null로 흡수하고, 설문 관리·이력 컨트롤러에서 날짜·keywordType·useYn·statuses를 허용값 기준으로 보정한다. 설문 관리 목록에 사용여부 select를 추가했고, 컨트롤러·이력 회귀 테스트를 추가한 뒤 `./gradlew test`를 통과했다.

## 2026-06-04

- 관리자 설문 상세 저장 오류를 고쳤다. 수정 화면에서 `surveyUid`가 action query param과 hidden input으로 중복 전송되던 것을, 기존 설문일 때는 hidden input을 렌더링하지 않도록 바꿔서 정리했다.
- `SASurveyPreviewViewTest`에 템플릿 회귀를 추가해 `surveyUid` hidden input이 신규 설문에서만 렌더링되는지 확인했다.
- 8081 임시 서버에서 실제 저장 후 `/admin/surveys/list.do`로 302 리다이렉트되는 흐름을 브라우저로 확인했다.

## 2026-06-05

- 사용자 게시판 공지사항과 포토 게시판의 페이징을 `<< < 1..10 > >>` 형태의 그룹 단위 네비게이션으로 바꿨다. 페이지 DTO에 10개 묶음 계산과 그룹 이동 메서드를 추가하고, 두 공개 목록 템플릿은 첫/이전/다음/마지막과 현재 페이지 강조를 렌더링하도록 정리했다.
- `BDPublicPaginationTest`와 `BDPublicPaginationViewTest`를 추가해 페이지 묶음 계산과 템플릿 마크업을 회귀로 고정했다.
- 8081 임시 서버의 공지사항 목록을 브라우저에서 확인해 `<< < 1 2 3 4 5 6 7 > >>` 형태로 렌더되고, 첫/이전/다음/마지막 화살표는 현재 데이터에서는 비활성 상태로 표시되는 것을 검증했다.
- 관리자 공지사항 목록에 페이징(10건/페이지)과 목록 복귀 시 검색·페이지 유지를 추가했다. `페이징테스트-01`~`34` 데이터로 4페이지·검색+상세+목록 복귀 흐름을 브라우저·HTTP로 검증했다.
- 설문 P7 복사 기능을 구현했다. 설문 관리 목록 행의 `복사` 링크가 `/admin/surveys/copy.do`로 이동하면, `SASurveyService.copySurveyForm`이 원본 설문을 in-memory로 복제해 신규 등록 화면에 채운다. 복제본은 새 UID, `원본제목 + ' 사본'`, 사용여부 미사용(N), 모든 seq를 null로 두어 저장 시 신규 INSERT된다. 클릭만으로는 DB에 저장하지 않고 운영자가 검토 후 저장하는 prefill 방식이다. 서비스·컨트롤러·목록 뷰 회귀 테스트를 추가하고 `./gradlew test`를 통과했다.

## 2026-06-06

- 설문 P7 리뷰를 반영했다. 복사 기능과 무관한 공개 제출 이메일 형식 검증 변경을 제거했고, `/admin/surveys/copy.do`에서 `surveyUid`가 빠진 요청도 비정상 접근 알림 후 목록으로 이동하도록 보정했다. `관리` 컬럼은 기능 이상이 아니고 복사 버튼 노출 요구에 맞아 유지했다. focused Gradle 테스트는 `BUILD SUCCESSFUL`을 확인했다.

## 2026-06-07

- 설문 P8 통계 리뷰 지적을 반영했다. 문항별 통계를 현재 설문 정의의 `field_seq`와 현재 보기 테이블 기준으로 집계하던 방식에서, 제출 답변 스냅샷의 `field_key_snapshot` 기준으로 조회하고 서비스에서 표시용 통계를 조립하는 방식으로 바꿨다. 현재 문항은 0건 보기 표시용으로 유지하고, 제출 스냅샷에만 남은 삭제 문항도 통계에 포함되도록 병합한다.
- 객관식 빈도는 SQL `group by`로 집계하고, 체크박스 응답은 `answer_json` 배열 값을 SQL에서 펼쳐 집계한다. 서비스는 현재 보기의 `optionValue -> optionLabel` 매핑으로 표시 라벨을 복원하고, 매핑이 없는 과거 값은 JSON 값을 그대로 표시한다.
- 주관식 최근 답변은 SQL `limit 20`으로 제한해 대량 제출 설문에서 문항별 전건을 Java 메모리에 적재하지 않도록 보강했다.
- 통계 화면의 문항 없음 빈 상태와 컨트롤러 생성자 레이아웃 지적을 수정했다. `SASurveyStatisticsServiceTest`는 설문 수정 후 `fieldSeq`가 바뀌어도 과거 응답이 남는 경우, 삭제된 문항 스냅샷, 체크박스 JSON 값 라벨 매핑, 주관식 최근 답변 제한을 검증하도록 보강했다. `SASurveyStatisticsMapperIntegrationTest`는 실제 제출/답변 row를 넣어 MyBatis 스냅샷 조회와 체크박스 `answer_json` 집계를 검증한다. `SAAdminSurveyStatisticsControllerTest`는 빈 문항 메시지 템플릿 회귀를 추가했다. focused 테스트와 `./gradlew test` 모두 `BUILD SUCCESSFUL`을 확인했다.
- 설문 P9 제출 스냅샷 보강을 완료했다. 답변 테이블 `sa_survey_answer_dtl`에 `survey_type_snapshot` 컬럼(NULL 허용)을 추가하고, `insertAnswer`가 제출 시점의 실제 `required_yn`과 `survey_type`을 저장하도록 고쳤다. 기존 `required_yn_snapshot` 리터럴 `'N'` 하드코딩을 제거했다. 레거시 row는 NULL로 두고 통계 `selectStatisticFields`가 `field_type_snapshot` 기반 COALESCE로 유형을 파생한다. 제출 서비스 `SASurveySubmitService`에 `resolveSurveyType`을 추가해 문항의 유형·필수 여부를 답변 스냅샷에 채운다. `SASurveySubmitServiceTest`에 스냅샷 캡처 검증, `SASurveyStatisticsMapperIntegrationTest`에 스냅샷 우선·레거시 파생·insert 영속 검증을 추가했다. focused 테스트와 `./gradlew test` 모두 `BUILD SUCCESSFUL`을 확인했다.
- 설문 P10 기간 관리를 완료했다. `sa_survey_mst`에 `start_date`/`end_date`를 추가하고, 설문 DTO·매퍼·저장/복사 흐름에 기간 필드를 반영했다. 관리자 상세에는 접수 시작일/종료일 입력과 종료일 역전 검증을 추가했다. 공개 목록은 접수중/예정/마감 상태 카드로 표시하고, 사용자 메인·상세 진입·제출 저장은 `Asia/Seoul` 기준 접수중 설문만 허용한다. 제출 서비스는 insert 전에 `SurveyNotAcceptingException`으로 기간 밖 제출을 차단한다. 서비스·컨트롤러·제출 서비스·mapper 통합·템플릿 회귀 테스트를 보강했고, focused 테스트와 `./gradlew test` 모두 `BUILD SUCCESSFUL`을 확인했다.
- 설문 P10 코드 리뷰를 반영했다. mapper 통합 테스트가 고정 날짜와 DTO의 실제 오늘 계산을 섞지 않도록 상태 검증을 주입한 `today` 기준으로 바꾸고, 기존 설문 호환을 위해 시작일/종료일이 모두 NULL인 사용 설문이 공개 카드와 메인 요약에 접수중으로 조회되는 케이스를 추가했다. 공개 설문 목록 문구도 예정/마감 카드를 포함하는 실제 화면 정책에 맞춰 정리했다. focused 테스트와 `./gradlew test`를 다시 통과했다.
