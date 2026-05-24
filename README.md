# Reven Project

Spring Boot + Thymeleaf 기반으로 설문 접수/관리 사이트를 재구축하기 위한 레포지토리입니다.

현재 `legacy-php-source/`에는 기존 PHP 구현을 참고용으로 복사해 두었습니다. 이 폴더는 기능 동등성 확인, 화면 비교, 데이터 구조 분석용이며 신규 구현의 기준 명세 역할을 합니다.

## 참고 문서

- [이관 개요](docs/migration-overview.md)
- [기존 PHP 소스 맵](docs/legacy-source-map.md)
- [설문 DB 모델 초안](docs/survey-db-model-draft.md)
- [작업 로그](docs/worklog.md)

## 권장 진행 순서

1. Spring Boot 프로젝트 골격 생성
2. 공통 레이아웃, GNB/LNB, breadcrumb 이식
3. 설문 도메인 DB 모델 확정
4. 설문 관리/상세/작성/이력/CSV 기능 1차 이식
5. 사용자 메인, 설문화면 고도화, 뉴스탭 확장
