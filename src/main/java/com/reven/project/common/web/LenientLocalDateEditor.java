// 잘못되거나 빈 날짜 문자열을 예외 대신 null로 흡수하는 LocalDate 바인딩 editor
package com.reven.project.common.web;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class LenientLocalDateEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) {
        if (text == null || text.isBlank()) {
            setValue(null);
            return;
        }
        try {
            setValue(LocalDate.parse(text.trim()));
        } catch (DateTimeParseException ex) {
            setValue(null);
        }
    }
}
