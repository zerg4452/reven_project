# 포토게시판 첨부 선택(유지/추가) 설계

## 배경

현재 포토게시판 수정 저장은 **새 첨부 업로드 시 기존 첨부 전체를 교체**한다.  
운영 의도는 **기존 첨부 유지 + 신규 첨부 추가**이며, 최종 저장 대상 합계는 **최대 5개**다.

기존 설계서 `docs/photo-board-board-restructure-spec.md` §3.4, §4.2의 “교체” 정책은 본 설계로 **대체**한다.

## 목표

- 등록/수정 화면 모두에서, 저장 전에 첨부를 **카드형 목록(체크박스 + 썸네일)** 으로 확인한다.
- 체크박스는 **기본 checked(포함)** 이다.
- 카드형 목록 상단에 빨간색 글자로 체크 해제시 저장에서 제외됩니다 문구 추가.
- unchecked 항목은 **이번 저장에서 제외**된다.
  - 기존 첨부: soft delete + 물리 파일 정리(커밋 후)
  - 신규 첨부: 업로드 대상에서 제외
- **유지할 기존 + 저장할 신규 ≤ 5** 를 반드시 지킨다.
- 5개 초과 저장 시도:
  - **프론트:** `alert('최대 업로드 갯수는 5개입니다.')` 후 저장 중단, **현재 페이지 유지**
  - **백엔드:** 동일 메시지로 `IllegalArgumentException` 발생(우회 요청 대비)

## 선택한 방향

**C안: 클라이언트 선차단 + 서버 재검증**

| 계층 | 역할 |
|---|---|
| JS (edit 화면) | 선택/해제 반영, 5개 초과 시 alert + submit 차단, 신규 파일 미리보기 |
| Controller | flash redirect 유지, 검증 실패 시 edit/write로 복귀 |
| Service | 유지/삭제/추가 순서 처리, 합계·최소 1개 검증 |

## 화면 설계

### 공통 UX

- 첨부 카드 1개 = `체크박스 + 썸네일 + 파일명`
- 체크박스 기본값: **checked**
- 상단 또는 목록 근처에 `현재 N/5` 카운터 표시
- 5개 초과 상태에서 submit 시 alert 후 중단

### 등록 화면 (`admin/photo/edit.html`, photoSeq 없음)

- 파일 input 선택 직후, 하단에 **신규 첨부 미리보기 카드 목록** 생성
- 이미지: `URL.createObjectURL` 썸네일
- mp4: `<video>` 소형 미리보기 또는 아이콘 + 파일명
- submit 시 checked 신규 파일만 실제 multipart에 포함(DataTransfer로 file input 재구성)
- checked 신규 0개면 저장 불가(alert: 첨부 최소 1개 필요 — 기존 서버 메시지와 동일 문구 유지)

### 수정 화면 (`photoSeq` 있음)

- **기존 첨부** 영역을 카드 + 체크박스로 변경
  - `name="keepPhotoFileSeqs"`, `value="{photoFileSeq}"`, 기본 checked
  - HTML 특성상 unchecked는 전송되지 않으므로, **미전송 = 제외(삭제)** 로 처리
- **추가 첨부** input 선택 시, 그 아래 **신규 미리보기 카드** 추가(등록과 동일)
- submit 시:
  - `checked 기존 수 + checked 신규 수 ≤ 5` 검증
  - checked 신규만 multipart 전송

### 상세 화면

- 기존 상세 컨텐츠 크기가 너무 커서 등록/수정에서 쓰일 카드형태의 ui를 재활용하여 여기서에도 카드형태로 보여주면 좋겠음(당연히 수정기능은 없어야함) 대신 영상의 경우 기존 사이즈보다는 다소 크게 해야하고 이미지의 경우는 확대기능이 있으니 기존 사이즈보다는 작아도 괜찮음

## 서버 설계

### 요청 파라미터

| 파라미터 | 용도 |
|---|---|
| `title`, `publishYn` | 기존과 동일 |
| `uploadFiles` | 이번 저장에 포함할 **신규** multipart 목록 |
| `keepPhotoFileSeqs` | 수정 시 **유지할** 기존 `photo_file_seq` 목록 (checkbox) |

### 저장 로직 (`BDPhotoBoardService.savePhotoBoard`)

1. 제목/게시여부 normalize (기존과 동일)
2. 신규 multipart normalize (빈 파일 제외)
3. **수정**인 경우:
   - DB에서 기존 활성 첨부 목록 조회
   - `keepPhotoFileSeqs`에 **없는** 기존 첨부 → `deletePhotoBoardFile` 단건 soft delete
   - 삭제 대상 물리 파일은 `afterCommit` cleanup
   - `keepPhotoFileSeqs`에 포함되었지만 다른 게시글/삭제된 seq면 무시(변조 방어: photoSeq 일치 검증)
4. **합계 검증**
   - `retainedCount + newCount > 5` → `IllegalArgumentException("최대 업로드 갯수는 5개입니다.")`
   - `retainedCount + newCount < 1` → `IllegalArgumentException("첨부 파일을 최소 1개 이상 업로드해 주세요.")`
5. 마스터 insert/update
6. 신규 파일만 `storePhotoFile` append
   - `sortOrder` = (유지된 기존 파일의 max sortOrder) + 1부터 순차 부여
7. 실패 시 writtenFiles rollback (기존과 동일)

### 삭제 정책 변경점

- ~~수정 + 신규 업로드 시 `deletePhotoBoardFiles(photoSeq)` 전체 삭제~~ **폐기**
- **체크 해제된 기존 파일만** `deletePhotoBoardFile(photoFileSeq)` 호출

### 컨트롤러

- `insert.do` / `update.do`에 `keepPhotoFileSeqs` optional 파라미터 추가
- 검증 실패 시 기존처럼 flash `error` + write 화면 redirect
- alert 문구와 서버 메시지를 동일하게 맞춘다

## 프론트 설계 (`static/common/js` 또는 edit 전용 script)

- edit 폼에 `data-photo-upload-form` 같은 hook 추가
- file input `change` → 미리보기 카드 렌더/갱신
- checkbox toggle → N/5 카운터 갱신
- form `submit`:
  - 합계 > 5 → alert, `preventDefault`
  - 등록/수정 공통: checked 신규만 DataTransfer로 file input 재구성
  - (선택) 6번째 신규 선택 시 file input 추가 자체를 막는 보조 UX

## 테스트 설계

### Service

| 케이스 | 기대 |
|---|---|
| 수정: 기존 1 유지 + 신규 2 추가 | 기존 1 + 신규 2 = 3 유지 |
| 수정: 기존 1 unchecked + 신규 1 | 기존 삭제, 신규 1만 남음 |
| 수정: 기존 3 유지 + 신규 3 | 예외 `최대 업로드 갯수는 5개입니다.` |
| 수정: 기존 전부 unchecked + 신규 0 | 예외 `첨부 파일을 최소 1개 이상...` |
| 등록: 신규 2 | 2개 저장, 전체 삭제 호출 없음 |

### Controller

- `keepPhotoFileSeqs` 전달 시 service 호출
- 5개 초과 서버 예외 → flash error redirect

## 변경 파일(예상)

- `BDPhotoBoardService.java` — append + 선택 삭제 + 합계 검증
- `BDPhotoBoardController.java` — `keepPhotoFileSeqs` 수신
- `admin/photo/edit.html` — 카드 UI, 카운터, script hook
- `admin/photo/detail.html` — 등록/수정과 동일 카드형 첨부 목록(체크/수정 없음), 이미지·영상 크기 조정
- `static/common/js/app.js` 또는 `static/admin/js/photo-edit.js` — 미리보기/submit 가드
- `static/common/css/app.css` — 선택 카드 스타일(필요 최소)
- `BDPhotoBoardServiceTest.java`, `BDPhotoBoardControllerTest.java` — 정책 변경 반영
- `docs/photo-board-board-restructure-spec.md` — 첨부 정책 문구 갱신(교체 → 유지/추가/선택 삭제)
- `docs/worklog.md` — 동작 변경 기록

## 비범위

- 공개 프론트 포토게시판 노출
- 이미지 확대 modal **동작** 변경(상세 첨부 목록은 카드형 UI로 재구성)
- AI News 경로/메뉴 재작업

## 검증

- Gradle 단위 테스트 통과
- Docker 재빌드 후 수동 시나리오:
  1. A 1장 등록
  2. 수정에서 A 유지 + B,C 추가 → A,B,C 3장
  3. 기존 1장 uncheck + 신규 2장 → 2장
  4. 5장 상태에서 1장 추가 시도 → alert, 페이지 유지
