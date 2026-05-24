# Project Agents Rules

This file captures project-specific UI, content, and workflow rules for the Spring Boot + Thymeleaf rebuild of the survey intake site.
Use these rules only for this repository.

## Product Terms

- Use `설문` as the primary term.
- `설문 운영` is the top-level admin area.
- `설문 관리` is the survey master management screen.
- `설문 상세` is the survey master detail/edit screen.
- `설문 이력 관리` is the submitted survey history screen.
- `설문 이력 상세` is the read-only submitted survey detail screen.

## Navigation

- GNB labels must use admin-oriented terms.
- LNB must show only the relevant second-level section for the current top-level area.
- The admin GNB hierarchy is:
  - `설문 운영`
  - `관리자 홈`
  - `사용자 화면`
  - `로그아웃`
- The admin LNB hierarchy under `설문 운영` is:
  - `설문 관리`
  - `설문 이력 관리`

## Layout Rules

- Main content background must be white.
- GNB should be the darkest blue layer.
- LNB should be a lighter blue layer than GNB.
- LNB depth hierarchy:
  - 2-depth group label is medium blue.
  - 3-depth menu items are the lightest blue.
- LNB items must span full width with no left/right padding gap on the clickable area.
- Page breadcrumb must appear at the top of every page.
- Breadcrumb format must follow `1-depth > 2-depth > 3-depth`.
- List titles must have the filled arrow marker `▶` before the title.

## Button Rules

- Register button: blue background with white text.
- Edit button: green background with white text.
- Delete button: red background with white text.
- Button text must always be white.
- Button corners should be slightly rounded more than cards and inputs.
- Keep buttons filled rather than outline-styled.
- Place navigation back buttons at the lower left of detail/edit screens.
- Place action buttons such as save/edit/delete at the lower right of detail/edit screens.

## Card and Border Rules

- Cards, inputs, tables, and general content containers should feel sharper and less rounded.
- Buttons may remain slightly softer than other surfaces.
- LNB and table header sections should feel visually segmented.
- Table header cells should use a subtle yellow/green-tinted background.
- List content sections should have no horizontal inner padding where tables need to reach the edges.

## Search Rules

- All date values and date display must use `yyyy-mm-dd`.
- Date inputs should default to:
  - start date: current date minus 60 days
  - end date: current date plus 1 day
- Survey management search:
  - date is based on registration date
  - search structure must match the survey history search layout
  - keyword type options: `전체`, `설문명`
- Survey history search:
  - date is based on submission date
  - keyword type options: `전체`, `설문명`, `작성자명`
  - `전체` means survey title OR submitter name

## List Rules

- Survey management list columns:
  - `순번`, `설문 제목`, `문항 수`, `사용여부`, `등록일`, `수정일`
- Survey history list columns:
  - `순번`, `설문명`, `제출자명`, `연락처`, `상태`, `제출일`
- Survey title in the survey management list should link to the detail screen.
- Survey title and submitter name in the survey history list should link to the detail screen.
- Remove separate detail/view buttons from list rows when the column text itself is the link.

## Date Handling

- Store and display dates in `yyyy-mm-dd` format where possible.
- Use the project timezone `Asia/Seoul`.

## Data Storage

- The new implementation should use DB storage for 설문 and 설문 이력.
- Legacy PHP runtime files under `legacy-php-source/data/*.json` are local reference data only and must not be committed.
- Preserve submitted survey snapshots so past 설문 이력 상세 screens do not change when a survey definition is edited later.

## HTML Notes

- Every page should include a short HTML comment describing:
  - the screen name
  - the feature purpose
  - the creation date in `yyyy-mm-dd`

## Current Implementation Notes

- This repository is the Spring Boot + Thymeleaf rebuild workspace.
- The previous PHP implementation is copied under `legacy-php-source/` for reference only.
- Use `docs/worklog.md` as the longer-running human-readable work log.
- When a change materially affects layout, navigation, data flow, storage, or overall behavior, update `docs/worklog.md` even if the user did not explicitly ask for a log entry.
- Before adding new user-facing features, first preserve functional parity for the core 설문 management and submission flow.
