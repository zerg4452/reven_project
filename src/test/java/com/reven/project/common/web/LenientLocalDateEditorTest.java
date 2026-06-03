package com.reven.project.common.web;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class LenientLocalDateEditorTest {

    @Test
    void parsesIsoDate() {
        LenientLocalDateEditor editor = new LenientLocalDateEditor();
        editor.setAsText("2026-06-03");
        assertThat(editor.getValue()).isEqualTo(LocalDate.of(2026, 6, 3));
    }

    @Test
    void returnsNullForBlank() {
        LenientLocalDateEditor editor = new LenientLocalDateEditor();
        editor.setAsText("   ");
        assertThat(editor.getValue()).isNull();
    }

    @Test
    void returnsNullForInvalidText() {
        LenientLocalDateEditor editor = new LenientLocalDateEditor();
        editor.setAsText("abc");
        assertThat(editor.getValue()).isNull();
    }
}
