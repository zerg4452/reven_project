package com.reven.project.service.sa;

import com.reven.project.service.sa.dto.SASurveyDto;
import com.reven.project.service.sa.mapper.SASurveySubmitMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class SASurveyCsvService {
    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private final SASurveySubmitMapper submitMapper;

    public SASurveyCsvService(SASurveySubmitMapper submitMapper) {
        this.submitMapper = submitMapper;
    }

    /**
     * 설문 이력 검색 결과를 UTF-8 BOM이 포함된 CSV 바이트 배열로 만든다.
     */
    public byte[] createSubmissionCsv(SASurveyDto.SubmissionSearchRequest request) {
        List<SASurveyDto.CsvRow> rows = submitMapper.selectCsvRows(request);
        try {
            StringWriter writer = new StringWriter();
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader("제출UID", "설문명", "제출자명", "연락처", "이메일", "상태", "제출일", "문항", "답변", "답변JSON")
                    .build();
            try (CSVPrinter printer = new CSVPrinter(writer, format)) {
                for (SASurveyDto.CsvRow row : rows) {
                    printer.printRecord(row.submitUid, row.surveyTitle, row.submitterName, row.phone, row.email,
                            row.status, row.submittedDate, row.fieldLabel, row.answerValue, row.answerJson);
                }
            }
            byte[] body = writer.toString().getBytes(StandardCharsets.UTF_8);
            // Excel에서 한글 CSV를 열 때 깨지지 않도록 UTF-8 BOM을 앞에 붙인다.
            byte[] csv = new byte[UTF8_BOM.length + body.length];
            System.arraycopy(UTF8_BOM, 0, csv, 0, UTF8_BOM.length);
            System.arraycopy(body, 0, csv, UTF8_BOM.length, body.length);
            return csv;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to create survey submission CSV.", ex);
        }
    }
}
