// docs/worklog.md를 파싱해 월별·일별 타임라인 데이터로 변환하는 서비스
package com.reven.project.service.co;

import com.reven.project.service.co.dto.COWorklogDayResponseDto;
import com.reven.project.service.co.dto.COWorklogMonthResponseDto;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class COWorklogService {

    private static final Path WORKLOG_PATH = Paths.get("docs/worklog.md");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy년 M월");
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN);

    public List<COWorklogMonthResponseDto> getWorklogMonths() {
        List<String> lines;
        try {
            lines = Files.readAllLines(WORKLOG_PATH);
        } catch (IOException e) {
            return List.of();
        }

        // 날짜별 항목 수집 (내림차순 정렬을 위해 TreeMap 역순 사용)
        TreeMap<LocalDate, List<String>> byDay = new TreeMap<>(Comparator.reverseOrder());
        LocalDate currentDate = null;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("## ")) {
                String dateStr = trimmed.substring(3).trim();
                try {
                    currentDate = LocalDate.parse(dateStr, DATE_FMT);
                    byDay.putIfAbsent(currentDate, new ArrayList<>());
                } catch (Exception ignored) {
                    currentDate = null;
                }
            } else if (trimmed.startsWith("- ") && currentDate != null) {
                String entry = trimmed.substring(2).trim();
                // "2026-05-24: 내용" 형태의 인라인 날짜를 실제 날짜로 재분류
                java.util.regex.Matcher m = java.util.regex.Pattern
                        .compile("^(\\d{4}-\\d{2}-\\d{2}):\\s*(.+)$", java.util.regex.Pattern.DOTALL)
                        .matcher(entry);
                if (m.matches()) {
                    try {
                        LocalDate inlineDate = LocalDate.parse(m.group(1), DATE_FMT);
                        byDay.computeIfAbsent(inlineDate, k -> new ArrayList<>()).add(m.group(2).trim());
                    } catch (Exception ignored) {
                        byDay.get(currentDate).add(entry);
                    }
                } else {
                    byDay.get(currentDate).add(entry);
                }
            }
        }

        // 월별 그룹핑 (역순이므로 최신 월이 먼저)
        LinkedHashMap<YearMonth, List<COWorklogDayResponseDto>> byMonth = new LinkedHashMap<>();
        for (Map.Entry<LocalDate, List<String>> entry : byDay.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            YearMonth ym = YearMonth.from(entry.getKey());
            byMonth.computeIfAbsent(ym, k -> new ArrayList<>())
                    .add(new COWorklogDayResponseDto(
                            entry.getKey().format(DAY_FMT),
                            entry.getValue()
                    ));
        }

        return byMonth.entrySet().stream()
                .map(e -> new COWorklogMonthResponseDto(
                        e.getKey().format(MONTH_FMT),
                        e.getValue()
                ))
                .toList();
    }
}
