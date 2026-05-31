// 게시판 조회수 쿠키 기반 중복 제거 공통 헬퍼
package com.reven.project.client.bd;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 같은 브라우저가 같은 글을 다시 열 때 조회수가 중복으로 증가하지 않도록 쿠키로 제어한다.
 * 쿠키 값에는 조회한 글 seq 목록을 CSV로 저장하고, 미포함일 때만 increaser를 실행한다.
 */
public final class BDBoardViewCountSupport {

    private static final int COOKIE_MAX_AGE_SECONDS = 60 * 60 * 24;
    private static final int MAX_TRACKED_SEQS = 200;

    private BDBoardViewCountSupport() {
    }

    /**
     * 쿠키에 해당 seq가 없을 때만 increaser를 실행하고 쿠키에 seq를 추가한다.
     */
    public static void countOnce(
            HttpServletRequest request,
            HttpServletResponse response,
            String cookieName,
            long seq,
            Runnable increaser
    ) {
        if (request == null || response == null) {
            increaser.run();
            return;
        }
        Set<String> viewed = readViewedSeqs(request, cookieName);
        String seqText = Long.toString(seq);
        if (viewed.contains(seqText)) {
            return;
        }
        increaser.run();
        viewed.add(seqText);
        writeCookie(response, cookieName, trimToLimit(viewed));
    }

    private static Set<String> readViewedSeqs(HttpServletRequest request, String cookieName) {
        Set<String> viewed = new LinkedHashSet<>();
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return viewed;
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                Arrays.stream(cookie.getValue().split(","))
                        .map(String::trim)
                        .filter(value -> !value.isEmpty())
                        .forEach(viewed::add);
            }
        }
        return viewed;
    }

    private static List<String> trimToLimit(Set<String> viewed) {
        List<String> values = new ArrayList<>(viewed);
        if (values.size() > MAX_TRACKED_SEQS) {
            return values.subList(values.size() - MAX_TRACKED_SEQS, values.size());
        }
        return values;
    }

    private static void writeCookie(HttpServletResponse response, String cookieName, List<String> values) {
        Cookie cookie = new Cookie(cookieName, String.join(",", values));
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(COOKIE_MAX_AGE_SECONDS);
        response.addCookie(cookie);
    }
}
