# User Main News Survey Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the public user main page, public AI news accordion, and improved public survey cards.

**Architecture:** Add public controllers and focused service methods on top of the existing `bd_ai_news_mst` and `sa_survey_mst` data. Reuse Thymeleaf fragments by adding a public GNB fragment while keeping admin navigation unchanged.

**Tech Stack:** Spring Boot MVC, Thymeleaf, MyBatis, JUnit/Spring MVC tests, static CSS and generated PNG asset.

---

### Task 1: Public Routes And Service Queries

**Files:**
- Modify: `src/main/java/com/reven/project/client/sa/SAPublicSurveyController.java`
- Create: `src/main/java/com/reven/project/client/main/COMainController.java`
- Create: `src/main/java/com/reven/project/client/bd/BDAiNewsPublicController.java`
- Modify: `src/main/java/com/reven/project/service/sa/SASurveyService.java`
- Modify: `src/main/java/com/reven/project/service/sa/mapper/SASurveyMapper.java`
- Modify: `src/main/resources/mapper/sa/SASurveyMapper.xml`
- Modify: `src/main/java/com/reven/project/service/bd/BDAiNewsService.java`
- Modify: `src/main/java/com/reven/project/service/bd/mapper/BDAiNewsMapper.java`
- Modify: `src/main/resources/mapper/bd/BDAiNewsMapper.xml`

- [ ] Add a main controller that maps `/`, `/main.do`, and `/index.do` to `client/main/index`.
- [ ] Add service methods for public survey summaries, all public surveys including closed items, and published AI news.
- [ ] Add a public AI news controller at `/news/ai/list.do` and `/news/ai/detail.do`.
- [ ] Change public survey list to show all non-deleted surveys so closed cards can be dimmed.

### Task 2: Public Templates And Navigation

**Files:**
- Modify: `src/main/resources/templates/fragments/layout.html`
- Create: `src/main/resources/templates/client/main/index.html`
- Create: `src/main/resources/templates/client/news/ai-list.html`
- Create: `src/main/resources/templates/client/news/ai-detail.html`
- Modify: `src/main/resources/templates/client/survey/list.html`
- Modify: `src/main/resources/templates/client/survey/form.html`
- Modify: `src/main/resources/templates/client/survey/thanks.html`
- Modify: `src/main/resources/static/common/css/app.css`

- [ ] Add a `publicGnb(active)` fragment with hover dropdown menus for `뉴스 > AI` and `설문 > 진행중인 설문`.
- [ ] Replace public pages that currently use the admin GNB with the public GNB.
- [ ] Build the main page B layout: hero image, emphasized survey cards, compact AI news cards, and bottom placeholder image section.
- [ ] Build the public AI news accordion list with title/content search.
- [ ] Dim closed survey cards and prevent their primary action from linking to participation.

### Task 3: Asset, Tests, And Worklog

**Files:**
- Create: `src/main/resources/static/common/images/main-it-placeholder.png`
- Create or modify tests under `src/test/java`
- Modify: `docs/worklog.md`
- Modify: `.gitignore`

- [ ] Generate the IT-style bottom image and store it as a project static asset.
- [ ] Add focused MVC/service tests for the new public routes and query filters where the current test setup supports it.
- [ ] Add `.superpowers/` to `.gitignore` so visual brainstorming artifacts are not committed.
- [ ] Update `docs/worklog.md` because navigation, layout, and public behavior materially change.
- [ ] Run the verification command and inspect the output before reporting completion.
