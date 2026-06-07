package com.reven.project.service.co;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reven.project.service.co.dto.COAdminMenuResponseDto;
import com.reven.project.service.co.dto.COAdminMenuSaveRequestDto;
import com.reven.project.service.co.dto.COAdminMenuTreeItemDto;
import com.reven.project.service.co.dto.COAdminNavigationItemDto;
import com.reven.project.service.co.dto.COAdminNavigationResponseDto;
import com.reven.project.service.co.mapper.COAdminMenuMapper;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.reven.project.common.util.TextUtils.firstText;

@Service
public class COAdminMenuService {

    private static final Set<String> MENU_TYPES = Set.of("group", "page", "board");

    private final COAdminMenuMapper adminMenuMapper;
    private final ObjectMapper objectMapper;

    public COAdminMenuService(COAdminMenuMapper adminMenuMapper, ObjectMapper objectMapper) {
        this.adminMenuMapper = adminMenuMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 관리자 메뉴 트리 표시용 목록을 조회한다.
     */
    public List<COAdminMenuTreeItemDto> menuTree(Long selectedSeq) {
        List<COAdminMenuResponseDto> menus = adminMenuMapper.selectAdminMenus();
        Map<String, Long> seqByCode = menus.stream().collect(Collectors.toMap(
                COAdminMenuResponseDto::menuCode,
                menu -> menu.adminMenuSeq() == null ? 0L : menu.adminMenuSeq(),
                (left, right) -> left
        ));
        Map<String, List<COAdminMenuResponseDto>> childrenByParent = menus.stream()
                .collect(Collectors.groupingBy(menu -> firstText(menu.parentMenuCode())));
        Comparator<COAdminMenuResponseDto> menuOrder = Comparator
                .comparing((COAdminMenuResponseDto menu) -> menu.sortOrder() == null ? 0 : menu.sortOrder())
                .thenComparing(menu -> menu.adminMenuSeq() == null ? 0L : menu.adminMenuSeq());
        childrenByParent.replaceAll((parentCode, children) -> dedupeMenus(children, menuOrder));

        List<COAdminMenuTreeItemDto> tree = new ArrayList<>();
        List<COAdminMenuResponseDto> roots = dedupeMenus(childrenByParent.getOrDefault("", List.of()), menuOrder);
        for (COAdminMenuResponseDto root : roots) {
            appendTreeItem(tree, root, childrenByParent, seqByCode, selectedSeq);
        }
        return tree;
    }

    /**
     * jsTree 초기화에 사용할 노드 JSON을 생성한다.
     */
    public String menuTreeNodesJson(Long selectedSeq) {
        List<Map<String, Object>> nodes = menuTree(selectedSeq).stream()
                .map(item -> {
                    Map<String, Object> state = new LinkedHashMap<>();
                    state.put("opened", true);
                    state.put("disabled", "N".equals(item.useYn()));

                    Map<String, Object> node = new LinkedHashMap<>();
                    node.put("id", String.valueOf(item.adminMenuSeq()));
                    node.put("parent", item.parentAdminMenuSeq() == null || item.parentAdminMenuSeq() <= 0 ? "#" : String.valueOf(item.parentAdminMenuSeq()));
                    node.put("text", item.menuName());
                    node.put("state", state);
                    node.put("icon", false);
                    return node;
                })
                .toList();
        try {
            return objectMapper.writeValueAsString(nodes);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("메뉴 트리 JSON을 생성할 수 없습니다.", e);
        }
    }

    /**
     * 메뉴 관리에 등록된 사용 메뉴 기준으로 관리자 GNB/LNB를 만든다.
     */
    public COAdminNavigationResponseDto adminNavigation(String requestUri) {
        List<COAdminMenuResponseDto> menus = adminMenuMapper.selectAdminMenus().stream()
                .filter(menu -> "Y".equalsIgnoreCase(menu.useYn()))
                .toList();
        Map<String, List<COAdminMenuResponseDto>> childrenByParent = menus.stream()
                .collect(Collectors.groupingBy(menu -> firstText(menu.parentMenuCode())));
        Comparator<COAdminMenuResponseDto> menuOrder = Comparator
                .comparing((COAdminMenuResponseDto menu) -> menu.sortOrder() == null ? 0 : menu.sortOrder())
                .thenComparing(menu -> menu.adminMenuSeq() == null ? 0L : menu.adminMenuSeq());
        childrenByParent.replaceAll((parentCode, children) -> dedupeMenus(children, menuOrder));

        COAdminMenuResponseDto activeMenu = activeMenu(menus, requestUri);
        COAdminMenuResponseDto activeRoot = activeRoot(activeMenu, menus);
        List<COAdminNavigationItemDto> gnbItems = dedupeMenus(childrenByParent.getOrDefault("", List.of()), menuOrder).stream()
                .map(menu -> navigationItem(menu, childrenByParent, activeRoot, activeMenu))
                .toList();
        List<COAdminNavigationItemDto> lnbItems = activeRoot == null
                ? List.of()
                : dedupeMenus(childrenByParent.getOrDefault(activeRoot.menuCode(), List.of()), menuOrder).stream()
                        .map(menu -> navigationItem(menu, childrenByParent, activeRoot, activeMenu))
                        .toList();

        return new COAdminNavigationResponseDto(
                gnbItems,
                activeRoot == null ? null : navigationItem(activeRoot, childrenByParent, activeRoot, activeMenu),
                lnbItems
        );
    }

    /**
     * 선택한 메뉴 또는 신규 등록용 빈 메뉴를 반환한다.
     */
    public COAdminMenuResponseDto menuForm(Long selectedSeq) {
        COAdminMenuResponseDto menu = selectedSeq == null ? null : adminMenuMapper.selectAdminMenuBySeq(selectedSeq);
        if (menu == null) {
            return emptyMenu();
        }
        return withMatchUrlsText(menu);
    }

    /**
     * 상위 메뉴 select box에 노출할 수 있는 메뉴 목록을 반환한다.
     */
    public List<COAdminMenuResponseDto> parentOptions(Long editingSeq) {
        List<COAdminMenuResponseDto> menus = adminMenuMapper.selectAdminMenus();
        Set<String> excludedCodes = descendantCodes(editingSeq, menus);
        COAdminMenuResponseDto editingMenu = editingSeq == null ? null : adminMenuMapper.selectAdminMenuBySeq(editingSeq);
        if (editingMenu != null) {
            excludedCodes.add(editingMenu.menuCode());
        }
        return menus.stream()
                .filter(menu -> menu.depthNo() != null && menu.depthNo() < 3)
                .filter(menu -> !excludedCodes.contains(menu.menuCode()))
                .toList();
    }

    /**
     * 관리자 메뉴를 등록하거나 수정한다.
     */
    @Transactional
    public Long saveMenu(COAdminMenuSaveRequestDto requestDto) {
        COAdminMenuSaveRequestDto normalized = normalize(requestDto);
        COAdminMenuResponseDto existingBySeq = normalized.adminMenuSeq() == null
                ? null
                : adminMenuMapper.selectAdminMenuBySeq(normalized.adminMenuSeq());
        if (existingBySeq != null && !existingBySeq.menuCode().equals(normalized.menuCode())) {
            throw new IllegalArgumentException("메뉴 코드는 수정할 수 없습니다.");
        }
        COAdminMenuResponseDto existingByCode = adminMenuMapper.selectAdminMenuByCode(normalized.menuCode());
        if (existingByCode != null && !existingByCode.adminMenuSeq().equals(normalized.adminMenuSeq())) {
            throw new IllegalArgumentException("이미 사용 중인 메뉴 코드입니다.");
        }
        if (normalized.adminMenuSeq() != null && normalized.menuCode().equals(normalized.parentMenuCode())) {
            throw new IllegalArgumentException("자기 자신을 상위 메뉴로 선택할 수 없습니다.");
        }
        validateParent(normalized);

        int depthNo = depthNo(normalized.parentMenuCode());
        String matchUrlsJson = matchUrlsJson(normalized.matchUrlsText());
        if (normalized.adminMenuSeq() == null) {
            adminMenuMapper.insertAdminMenu(normalized, depthNo, matchUrlsJson);
            COAdminMenuResponseDto saved = adminMenuMapper.selectAdminMenuByCode(normalized.menuCode());
            return saved == null ? null : saved.adminMenuSeq();
        }
        adminMenuMapper.updateAdminMenu(normalized, depthNo, matchUrlsJson);
        return normalized.adminMenuSeq();
    }

    /**
     * 하위 메뉴가 없는 메뉴만 soft delete 처리한다.
     */
    @Transactional
    public void deleteMenu(Long adminMenuSeq, String actorId) {
        COAdminMenuResponseDto menu = adminMenuMapper.selectAdminMenuBySeq(adminMenuSeq);
        if (menu == null) {
            throw new IllegalArgumentException("삭제할 메뉴를 찾을 수 없습니다.");
        }
        if (adminMenuMapper.countChildren(menu.menuCode()) > 0) {
            throw new IllegalArgumentException("하위 메뉴가 있거나 삭제할 수 없는 메뉴입니다.");
        }
        adminMenuMapper.deleteAdminMenu(adminMenuSeq, firstText(actorId, "system"));
    }

    private COAdminMenuSaveRequestDto normalize(COAdminMenuSaveRequestDto requestDto) {
        String menuCode = firstText(requestDto.menuCode()).trim();
        String menuName = firstText(requestDto.menuName()).trim();
        if (menuCode.isBlank() || menuName.isBlank()) {
            throw new IllegalArgumentException("메뉴 코드와 메뉴명을 입력해 주세요.");
        }
        String menuType = MENU_TYPES.contains(firstText(requestDto.menuType())) ? requestDto.menuType() : "page";
        String boardKey = "board".equals(menuType) ? firstText(requestDto.boardKey()).trim() : "";
        return new COAdminMenuSaveRequestDto(
                requestDto.adminMenuSeq(),
                menuCode,
                firstText(requestDto.parentMenuCode()).trim(),
                menuName,
                firstText(requestDto.menuUrl()).trim(),
                firstText(requestDto.matchUrlsText()).trim(),
                menuType,
                boardKey,
                "N".equalsIgnoreCase(requestDto.useYn()) ? "N" : "Y",
                requestDto.sortOrder() == null ? 0 : requestDto.sortOrder(),
                firstText(requestDto.actorId(), "system")
        );
    }

    private void validateParent(COAdminMenuSaveRequestDto menu) {
        if (menu.parentMenuCode().isBlank()) {
            return;
        }
        COAdminMenuResponseDto parent = adminMenuMapper.selectAdminMenuByCode(menu.parentMenuCode());
        if (parent == null) {
            throw new IllegalArgumentException("상위 메뉴를 찾을 수 없습니다.");
        }
        if (parent.depthNo() != null && parent.depthNo() >= 3) {
            throw new IllegalArgumentException("3-depth 메뉴 아래에는 하위 메뉴를 만들 수 없습니다.");
        }
        List<COAdminMenuResponseDto> menus = adminMenuMapper.selectAdminMenus();
        Set<String> descendants = descendantCodes(menu.adminMenuSeq(), menus);
        if (descendants.contains(menu.parentMenuCode())) {
            throw new IllegalArgumentException("하위 메뉴를 상위 메뉴로 선택할 수 없습니다.");
        }
    }

    private int depthNo(String parentMenuCode) {
        if (parentMenuCode == null || parentMenuCode.isBlank()) {
            return 1;
        }
        COAdminMenuResponseDto parent = adminMenuMapper.selectAdminMenuByCode(parentMenuCode);
        return Math.min(3, (parent == null || parent.depthNo() == null ? 1 : parent.depthNo()) + 1);
    }

    private Set<String> descendantCodes(Long menuSeq, List<COAdminMenuResponseDto> menus) {
        if (menuSeq == null) {
            return new HashSet<>();
        }
        COAdminMenuResponseDto root = menus.stream()
                .filter(menu -> menuSeq.equals(menu.adminMenuSeq()))
                .findFirst()
                .orElse(null);
        if (root == null) {
            return new HashSet<>();
        }
        Map<String, List<COAdminMenuResponseDto>> childrenByParent = menus.stream()
                .collect(Collectors.groupingBy(menu -> firstText(menu.parentMenuCode())));
        Set<String> descendants = new HashSet<>();
        ArrayDeque<String> stack = new ArrayDeque<>();
        stack.add(root.menuCode());
        while (!stack.isEmpty()) {
            String parentCode = stack.pop();
            for (COAdminMenuResponseDto child : childrenByParent.getOrDefault(parentCode, List.of())) {
                descendants.add(child.menuCode());
                stack.push(child.menuCode());
            }
        }
        return descendants;
    }

    private void appendTreeItem(
            List<COAdminMenuTreeItemDto> tree,
            COAdminMenuResponseDto menu,
            Map<String, List<COAdminMenuResponseDto>> childrenByParent,
            Map<String, Long> seqByCode,
            Long selectedSeq
    ) {
        tree.add(new COAdminMenuTreeItemDto(
                menu.adminMenuSeq(),
                seqByCode.get(menu.parentMenuCode()),
                menu.menuCode(),
                menu.parentMenuCode(),
                menu.depthNo(),
                menu.menuName(),
                menu.menuUrl(),
                menu.menuType(),
                menu.useYn(),
                menu.sortOrder(),
                selectedSeq != null && selectedSeq.equals(menu.adminMenuSeq())
        ));
        for (COAdminMenuResponseDto child : childrenByParent.getOrDefault(menu.menuCode(), List.of())) {
            appendTreeItem(tree, child, childrenByParent, seqByCode, selectedSeq);
        }
    }

    private COAdminMenuResponseDto activeMenu(List<COAdminMenuResponseDto> menus, String requestUri) {
        String currentUri = firstText(requestUri, "");
        return menus.stream()
                .filter(menu -> bestMatchLength(menu, currentUri) > 0)
                .max(Comparator
                        .comparingInt((COAdminMenuResponseDto menu) -> bestMatchLength(menu, currentUri))
                        .thenComparing(menu -> menu.depthNo() == null ? 0 : menu.depthNo()))
                .orElse(null);
    }

    private COAdminMenuResponseDto activeRoot(COAdminMenuResponseDto activeMenu, List<COAdminMenuResponseDto> menus) {
        if (activeMenu == null) {
            return null;
        }
        COAdminMenuResponseDto current = activeMenu;
        while (current.parentMenuCode() != null && !current.parentMenuCode().isBlank()) {
            String parentCode = current.parentMenuCode();
            COAdminMenuResponseDto parent = menus.stream()
                    .filter(menu -> parentCode.equals(menu.menuCode()))
                    .findFirst()
                    .orElse(null);
            if (parent == null) {
                break;
            }
            current = parent;
        }
        return current;
    }

    private COAdminNavigationItemDto navigationItem(
            COAdminMenuResponseDto menu,
            Map<String, List<COAdminMenuResponseDto>> childrenByParent,
            COAdminMenuResponseDto activeRoot,
            COAdminMenuResponseDto activeMenu
    ) {
        List<COAdminNavigationItemDto> children = childrenByParent.getOrDefault(menu.menuCode(), List.of()).stream()
                .map(child -> navigationItem(child, childrenByParent, activeRoot, activeMenu))
                .toList();
        boolean active = (activeRoot != null && activeRoot.menuCode().equals(menu.menuCode()))
                || (activeMenu != null && activeMenu.menuCode().equals(menu.menuCode()));
        return new COAdminNavigationItemDto(
                menu.menuCode(),
                menu.parentMenuCode(),
                menu.depthNo(),
                menu.menuName(),
                navigationHref(menu.menuUrl()),
                active,
                children
        );
    }

    private int bestMatchLength(COAdminMenuResponseDto menu, String requestUri) {
        List<String> candidates = new ArrayList<>();
        candidates.add(menu.menuUrl());
        candidates.addAll(matchUrls(menu.matchUrlsJson()));
        return candidates.stream()
                .map(this::normalizePath)
                .filter(path -> !path.isBlank())
                .filter(path -> pathMatches(path, requestUri))
                .mapToInt(String::length)
                .max()
                .orElse(0);
    }

    private boolean pathMatches(String path, String requestUri) {
        String currentUri = normalizePath(requestUri);
        if (path.isBlank() || currentUri.isBlank()) {
            return false;
        }
        return currentUri.equals(path)
                || currentUri.startsWith(path + "/")
                || currentUri.startsWith(path + "?");
    }

    private List<String> matchUrls(String matchUrlsJson) {
        if (matchUrlsJson == null || matchUrlsJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(matchUrlsJson, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private String navigationHref(String menuUrl) {
        String normalized = normalizePath(menuUrl);
        if (normalized.equals("/admin")) {
            return "/admin/home.do";
        }
        if (normalized.startsWith("/admin/") && !normalized.endsWith(".do")) {
            return normalized + "/list.do";
        }
        return normalized.isBlank() ? "#" : normalized;
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        String normalized = path.trim();
        int queryIndex = normalized.indexOf('?');
        if (queryIndex >= 0) {
            normalized = normalized.substring(0, queryIndex);
        }
        while (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private COAdminMenuResponseDto withMatchUrlsText(COAdminMenuResponseDto menu) {
        return new COAdminMenuResponseDto(
                menu.adminMenuSeq(),
                menu.menuCode(),
                menu.parentMenuCode(),
                menu.depthNo(),
                menu.menuName(),
                menu.menuUrl(),
                menu.matchUrlsJson(),
                matchUrlsText(menu.matchUrlsJson()),
                menu.menuType(),
                menu.boardKey(),
                menu.useYn(),
                menu.deleteFlg(),
                menu.sortOrder(),
                menu.registeredDate(),
                menu.updatedDate(),
                menu.registeredAt(),
                menu.registeredBy(),
                menu.modifiedAt(),
                menu.modifiedBy()
        );
    }

    private COAdminMenuResponseDto emptyMenu() {
        return new COAdminMenuResponseDto(
                null, "", "", 1, "", "", "[]", "", "page", "", "Y", "N", 0,
                null, null, null, "", null, ""
        );
    }

    private String matchUrlsJson(String matchUrlsText) {
        List<String> matchUrls = matchUrlsText == null || matchUrlsText.isBlank()
                ? List.of()
                : matchUrlsText.lines()
                        .map(String::trim)
                        .filter(value -> !value.isBlank())
                        .distinct()
                        .toList();
        try {
            return objectMapper.writeValueAsString(matchUrls);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("활성 경로를 저장할 수 없습니다.", e);
        }
    }

    private String matchUrlsText(String matchUrlsJson) {
        if (matchUrlsJson == null || matchUrlsJson.isBlank()) {
            return "";
        }
        try {
            return String.join("\n", objectMapper.readValue(matchUrlsJson, new TypeReference<List<String>>() {
            }));
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    private List<COAdminMenuResponseDto> dedupeMenus(List<COAdminMenuResponseDto> menus, Comparator<COAdminMenuResponseDto> menuOrder) {
        return menus.stream()
                .sorted(menuOrder)
                .collect(Collectors.toMap(
                        this::menuIdentityKey,
                        menu -> menu,
                        (left, right) -> left,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();
    }

    private String menuIdentityKey(COAdminMenuResponseDto menu) {
        String normalizedUrl = normalizePath(menu.menuUrl());
        return normalizedUrl.isBlank() ? firstText(menu.menuCode()) : normalizedUrl;
    }
}
