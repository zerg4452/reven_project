// P6Spy SQL 로그를 키워드 단위 줄바꿈으로 읽기 쉽게 포맷한다
package com.reven.project.common.config;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;

import java.util.Set;
import java.util.regex.Pattern;

public class P6spyPrettySqlFormatter implements MessageFormattingStrategy {

    private static final Set<String> SKIP_CATEGORIES = Set.of(
            "commit", "rollback", "info", "debug", "result", "resultset", "batch"
    );

    private static final Pattern KEYWORD_BREAK = Pattern.compile(
            "(?i)\\s+(SELECT|INSERT INTO|UPDATE|DELETE FROM|FROM|WHERE|"
                    + "LEFT JOIN|RIGHT JOIN|INNER JOIN|JOIN|GROUP BY|ORDER BY|HAVING|LIMIT|"
                    + "SET|VALUES|ON)\\s+"
    );

    @Override
    public String formatMessage(
            int connectionId,
            String now,
            long elapsed,
            String category,
            String prepared,
            String sql,
            String url
    ) {
        if (SKIP_CATEGORIES.contains(category.toLowerCase())) {
            return "";
        }

        String executableSql = resolveExecutableSql(prepared, sql);
        if (executableSql.isBlank()) {
            return "";
        }

        String formatted = formatSql(executableSql);
        return System.lineSeparator()
                + "/* SQL (" + elapsed + "ms) */"
                + System.lineSeparator()
                + formatted
                + System.lineSeparator();
    }

    private String resolveExecutableSql(String prepared, String sql) {
        if (prepared != null && !prepared.isBlank()) {
            return prepared.trim();
        }

        if (sql != null && !sql.isBlank()) {
            return sql.trim();
        }

        return "";
    }

    private String formatSql(String sql) {
        String singleLine = sql.replaceAll("\\s+", " ").trim();
        String withKeywordBreaks = KEYWORD_BREAK.matcher(singleLine).replaceAll(System.lineSeparator() + "$1 ");
        return withKeywordBreaks.replaceAll("(?i),\\s*", "," + System.lineSeparator() + "       ");
    }

}
