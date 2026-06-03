// 관리자 설문 문항 동적 추가/삭제와 설문 유형별 인터랙션을 처리한다.
(function () {
    var TYPE_CONFIG = {
        objective: {
            fieldTypes: ['radio', 'checkbox', 'select'],
            showOptions: true
        },
        subjective: {
            fieldTypes: ['text', 'textarea', 'number', 'date', 'email'],
            showOptions: false
        }
    };

    var ALL_FIELD_TYPE_OPTIONS = [
        { value: 'text',     label: '단답형' },
        { value: 'textarea', label: '장문형' },
        { value: 'select',   label: '선택목록' },
        { value: 'radio',    label: '라디오' },
        { value: 'checkbox', label: '체크박스' },
        { value: 'date',     label: '날짜' },
        { value: 'number',   label: '숫자' },
        { value: 'email',    label: '이메일' }
    ];

    function applyTypeConfig(row, surveyType) {
        var config = TYPE_CONFIG[surveyType] || TYPE_CONFIG.objective;
        var fieldTypeSelect = row.querySelector('[name$=".fieldType"]');
        var optionsBlock = row.querySelector('[data-field-options]');
        if (!fieldTypeSelect) {
            return;
        }

        var currentValue = fieldTypeSelect.value;
        fieldTypeSelect.innerHTML = '';
        ALL_FIELD_TYPE_OPTIONS.forEach(function (opt) {
            if (config.fieldTypes.indexOf(opt.value) === -1) {
                return;
            }
            var el = document.createElement('option');
            el.value = opt.value;
            el.textContent = opt.label;
            if (opt.value === currentValue) {
                el.selected = true;
            }
            fieldTypeSelect.appendChild(el);
        });
        if (!fieldTypeSelect.value) {
            fieldTypeSelect.value = config.fieldTypes[0];
        }

        if (optionsBlock) {
            optionsBlock.style.display = config.showOptions ? '' : 'none';
            optionsBlock.querySelectorAll('input, button').forEach(function (control) {
                if (control.matches('[data-add-option], [data-remove-option]')) {
                    control.disabled = !config.showOptions;
                    return;
                }
                control.disabled = !config.showOptions;
            });
        }
    }

    function refreshOptionRows(fieldRow) {
        var optionList = fieldRow.querySelector('[data-option-list]');
        if (!optionList) {
            return;
        }

        var fieldIndexMatch = fieldRow.querySelector('[name^="fields["]');
        if (!fieldIndexMatch) {
            return;
        }
        var fieldIndex = fieldIndexMatch.name.match(/fields\[(\d+)]/);
        if (!fieldIndex) {
            return;
        }

        var optionRows = Array.from(optionList.querySelectorAll('[data-option-row]'));
        optionRows.forEach(function (optionRow, optionIndex) {
            var removeButton = optionRow.querySelector('[data-remove-option]');
            if (removeButton) {
                removeButton.disabled = optionRows.length <= 1;
            }
            optionRow.querySelectorAll('[name]').forEach(function (field) {
                field.name = field.name
                    .replace(/fields\[\d+]\.options\[\d+]/, 'fields[' + fieldIndex[1] + '].options[' + optionIndex + ']')
                    .replace(/fields\[\d+]\.options\[__OPT_INDEX__]/, 'fields[' + fieldIndex[1] + '].options[' + optionIndex + ']');
            });
        });
    }

    function refreshRows(editor) {
        var rows = Array.from(editor.querySelectorAll('[data-field-row]'));

        rows.forEach(function (row, index) {
            var title = row.querySelector('.field-row-head strong');
            var removeButton = row.querySelector('[data-remove-field]');
            var moveUpButton = row.querySelector('[data-move-field-up]');
            var moveDownButton = row.querySelector('[data-move-field-down]');

            if (title) {
                title.textContent = '문항 ' + (index + 1);
            }
            if (removeButton) {
                removeButton.disabled = rows.length === 1;
            }
            if (moveUpButton) {
                moveUpButton.disabled = index === 0;
            }
            if (moveDownButton) {
                moveDownButton.disabled = index === rows.length - 1;
            }

            row.querySelectorAll('[name]').forEach(function (field) {
                field.name = field.name.replace(/fields\[(?:\d+|__INDEX__)]/, 'fields[' + index + ']');
            });
            refreshOptionRows(row);
        });
    }

    function addOptionRow(fieldRow, optionTemplate) {
        var optionList = fieldRow.querySelector('[data-option-list]');
        if (!optionList || !optionTemplate) {
            return;
        }

        var fieldIndexMatch = fieldRow.querySelector('[name^="fields["]');
        var fieldIndex = fieldIndexMatch ? (fieldIndexMatch.name.match(/fields\[(\d+)]/) || [])[1] : '0';
        var optionIndex = optionList.querySelectorAll('[data-option-row]').length;
        var html = optionTemplate.innerHTML
            .replaceAll('__INDEX__', String(fieldIndex))
            .replaceAll('__OPT_INDEX__', String(optionIndex));
        optionList.insertAdjacentHTML('beforeend', html);
        refreshOptionRows(fieldRow);
    }

    function ensureDefaultOptionRows(fieldRow, optionTemplate) {
        var optionList = fieldRow.querySelector('[data-option-list]');
        if (!optionList || optionList.querySelectorAll('[data-option-row]').length > 0) {
            return;
        }
        addOptionRow(fieldRow, optionTemplate);
        addOptionRow(fieldRow, optionTemplate);
    }

    function bindRowEvents(row, optionTemplate) {
        var surveyTypeSelect = row.querySelector('[name$=".surveyType"]');
        if (!surveyTypeSelect) {
            return;
        }

        surveyTypeSelect.addEventListener('change', function () {
            applyTypeConfig(row, this.value);
        });
        applyTypeConfig(row, surveyTypeSelect.value);
        ensureDefaultOptionRows(row, optionTemplate);
    }

    function moveRow(row, direction) {
        var editor = row.parentElement;
        var rows = Array.from(editor.querySelectorAll('[data-field-row]'));
        var index = rows.indexOf(row);
        if (index === -1) {
            return;
        }

        if (direction === 'up' && index > 0) {
            rows[index - 1].insertAdjacentElement('beforebegin', row);
        } else if (direction === 'down' && index < rows.length - 1) {
            rows[index + 1].insertAdjacentElement('afterend', row);
        }

        refreshRows(editor);
    }

    document.addEventListener('DOMContentLoaded', function () {
        var editor = document.querySelector('[data-field-editor]');
        var addButton = document.querySelector('[data-add-field]');
        var template = document.querySelector('[data-field-template]');
        var optionTemplate = document.querySelector('[data-option-template]');

        if (!editor || !addButton || !template) {
            return;
        }

        editor.querySelectorAll('[data-field-row]').forEach(function (row) {
            bindRowEvents(row, optionTemplate);
        });

        addButton.addEventListener('click', function () {
            var index = editor.querySelectorAll('[data-field-row]').length;
            var html = template.innerHTML.replaceAll('__INDEX__', String(index));
            editor.insertAdjacentHTML('beforeend', html);
            var newRow = editor.querySelector('[data-field-row]:last-child');
            bindRowEvents(newRow, optionTemplate);
            refreshRows(editor);
        });

        editor.addEventListener('click', function (event) {
            var addOptionButton = event.target.closest('[data-add-option]');
            if (addOptionButton) {
                addOptionRow(addOptionButton.closest('[data-field-row]'), optionTemplate);
                return;
            }

            var removeOptionButton = event.target.closest('[data-remove-option]');
            if (removeOptionButton && !removeOptionButton.disabled) {
                var optionRow = removeOptionButton.closest('[data-option-row]');
                var fieldRow = removeOptionButton.closest('[data-field-row]');
                optionRow.remove();
                refreshOptionRows(fieldRow);
                return;
            }

            var moveUpButton = event.target.closest('[data-move-field-up]');
            if (moveUpButton && !moveUpButton.disabled) {
                moveRow(moveUpButton.closest('[data-field-row]'), 'up');
                return;
            }

            var moveDownButton = event.target.closest('[data-move-field-down]');
            if (moveDownButton && !moveDownButton.disabled) {
                moveRow(moveDownButton.closest('[data-field-row]'), 'down');
                return;
            }

            var button = event.target.closest('[data-remove-field]');
            if (!button || editor.querySelectorAll('[data-field-row]').length <= 1) {
                return;
            }

            button.closest('[data-field-row]').remove();
            refreshRows(editor);
        });

        refreshRows(editor);
    });
})();
