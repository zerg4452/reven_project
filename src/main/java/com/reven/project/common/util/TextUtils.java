// 여러 문자열 중 비어 있지 않은 첫 값을 고르는 문자열 유틸
package com.reven.project.common.util;

public final class TextUtils {

    private TextUtils() {
    }

    public static String firstText(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
