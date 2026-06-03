# 포토 게시판 + 게시판 경로 재정비 설계서

## 1. 목적

- 관리자 게시판 영역을 `/admin/board` 1뎁스로 재구성한다.
- `포토게시판`과 `AI News`를 `/admin/board` 하위 2뎁스로 이동한다.
- 공개 프론트 AI News를 `/board/ai-news` 체계로 재정의한다.
- 포토 게시판은 제목, 첨부파일, 게시여부만 관리하는 단순 게시판으로 구현한다.
- 기존 경로는 사용하지 않고 새 canonical 경로로만 운영한다.

## 2. 경로 구조

### 2.1 관리자

- 1뎁스: `/admin/board`
- 2뎁스:
  - `/admin/board/photo/*`
  - `/admin/board/news/*`

### 2.2 공개 프론트

- canonical:
  - `/board/ai-news/list.do`
  - `/board/ai-news/detail.do`

## 3. 포토 게시판 기능 범위

### 3.1 관리 기능

- 목록
- 상세
- 등록
- 수정
- 삭제 soft delete

### 3.2 등록/수정 필드

- 제목
- 첨부파일
- 게시여부

### 3.3 첨부 정책

- 최대 5개 파일
- 허용 확장자:
  - 이미지 계열
  - `gif`
  - `webp`
  - `mp4`
- 서버에서 확장자와 MIME type을 함께 검증한다.
- 첨부 실패 시 게시글 저장 전체를 실패 처리한다.
- 하나의 게시글에 대해 파일 메타는 DB에, 실제 파일은 파일 저장소에 둔다.

### 3.4 상세 화면

- 저장된 첨부파일 목록을 노출한다.
- 이미지 파일은 미리보기 가능하게 표시한다.
- 비디오(`mp4`)는 플레이어 또는 다운로드 방식 중 하나로 표시한다.
- 첨부 삭제/추가는 수정 화면에서 체크박스로 유지/제외를 선택하고, 신규 파일은 추가 업로드한다.
- 유지할 기존 첨부 + 저장할 신규 첨부 합계는 최대 5개다.

## 4. 데이터 설계

### 4.1 신규 테이블

- `bd_photo_board_mst`
  - 게시글 기본 정보
  - 제목
  - 게시여부
  - 작성/수정 메타
- `bd_photo_board_file_dtl`
  - 첨부파일 메타
  - 게시글 FK
  - 원본 파일명
  - 저장 파일명
  - 저장 경로
  - MIME type
  - 파일 크기
  - 정렬 순서

### 4.2 스냅샷 정책

- 포토 게시판은 게시글 내용 자체가 단순하므로 별도 snapshot 테이블은 두지 않는다.
- 다만 첨부파일은 게시글 수정 시 체크 해제된 기존 파일만 삭제하고, checked 신규 파일은 기존 뒤에 append 한다.

### 4.3 파일 저장 위치

- 로컬 또는 볼륨 마운트 경로를 기준으로 저장한다.
- 경로는 설정값으로 외부화한다.
- 예시 설정 키:
  - `reven.upload.photo-root`
  - `reven.upload.photo-base-url`

## 5. 서비스/컨트롤러 설계

### 5.1 신규 클래스

- `BDPhotoBoardController`
- `BDPhotoBoardService`
- `BDPhotoBoardMapper`
- `BDPhotoBoardSaveRequestDto`
- `BDPhotoBoardDetailResponseDto`
- `BDPhotoBoardListItemResponseDto`
- `BDPhotoBoardFileResponseDto`

### 5.2 관리자 컨트롤러 메서드

- `GET /admin/board/photo/list.do`
- `GET /admin/board/photo/write.do`
- `GET /admin/board/photo/detail.do`
- `POST /admin/board/photo/insert.do`
- `POST /admin/board/photo/update.do`
- `POST /admin/board/photo/delete.do`

### 5.3 서비스 책임

- 목록/상세 조회
- 등록/수정/삭제
- 파일 저장소 저장/삭제
- 첨부 검증
- 첨부 5개 제한 검증
- 게시여부 기반 노출 제어

## 6. AI News 재배치

### 6.1 관리자

- AI News 관리자 컨트롤러는 `/admin/board/ai-news/*` 아래로 이동한다.

### 6.2 공개

- 공개 목록/상세는 `/board/ai-news/*`로 이동한다.

### 6.3 프론트 링크

- 메인 화면, GNB, 상세/목록 링크를 새 canonical 경로로 수정한다.

## 7. 메뉴/네비게이션 재구성

- 관리자 GNB 1뎁스에 `게시판`을 추가한다.
- `게시판` 하위 2뎁스에 `포토게시판`, `AI News`를 둔다.
- 관리자 메뉴관리 데이터(`co_adm_menu_mst`)의 라벨/순서/활성 경로가 그대로 반영되도록 한다.
- `설문 운영`, `관리자 홈`, `사용자 화면`, `로그아웃` 구조는 유지한다.

## 8. 검증 계획

### 8.1 서비스 테스트

- 포토 게시판 목록/상세/등록/수정/삭제
- 첨부 5개 초과 거부
- 비허용 확장자/MIME 거부
- 수정 시 파일 교체 동작
- AI News 새/옛 경로 동시 동작

### 8.2 라우팅 테스트

- `/admin/board/photo/list.do`
- `/admin/board/ai-news/list.do`
- `/board/ai-news/list.do`

### 8.3 UI 확인

- 관리자 GNB에서 `게시판`이 1뎁스로 보이는지 확인
- 사진 게시판 상세에서 첨부 5개 제한이 표시되는지 확인
- AI News 링크가 새 경로로 바뀌었는지 확인

## 9. 구현 전제

- 포토 게시판은 우선 관리자 전용으로 구현한다.
- 공개 프론트에는 포토 게시판을 노출하지 않는다.
- 첨부파일은 DB가 아니라 파일 저장소에 둔다.
- 파일 저장소 실패는 게시글 저장 실패로 처리한다.
- canonical 경로만 사용한다.
