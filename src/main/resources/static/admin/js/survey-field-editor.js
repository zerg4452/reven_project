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
        var config = TYPE_CONFIG[surveyType] || TYPE_CONFIG['objective'];
        var fieldTypeSelect = row.querySelector('[name$=".fieldType"]');
        var optionsLabel = row.querySelector('[name$=".optionsText"]');
        if (!fieldTypeSelect) return;

        var currentValue = fieldTypeSelect.value;
        fieldTypeSelect.innerHTML = '';
        ALL_FIELD_TYPE_OPTIONS.forEach(function (opt) {
            if (config.fieldTypes.indexOf(opt.value) === -1) return;
            var el = document.createElement('option');
            el.value = opt.value;
            el.textContent = opt.label;
            if (opt.value === currentValue) el.selected = true;
            fieldTypeSelect.appendChild(el);
        });
        if (!fieldTypeSelect.value) {
            fieldTypeSelect.value = config.fieldTypes[0];
        }

        if (optionsLabel) {
            optionsLabel.closest('label').style.display = config.showOptions ? '' : 'none';
        }
    }

    function bindRowEvents(row) {
        var surveyTypeSelect = row.querySelector('[name$=".surveyType"]');
        if (!surveyTypeSelect) return;

        surveyTypeSelect.addEventListener('change', function () {
            applyTypeConfig(row, this.value);
        });
        applyTypeConfig(row, surveyTypeSelect.value);
    }

    function refreshRows(editor) {
        var rows = Array.from(editor.querySelectorAll('[data-field-row]'));

        rows.forEach(function (row, index) {
            var title = row.querySelector('.field-row-head strong');
            var removeButton = row.querySelector('[data-remove-field]');

            if (title) {
                title.textContent = '문항 ' + (index + 1);
            }
            if (removeButton) {
                removeButton.disabled = rows.length === 1;
            }

            row.querySelectorAll('[name]').forEach(function (field) {
                field.name = field.name.replace(/fields\[(?:\d+|__INDEX__)]/, 'fields[' + index + ']');
            });
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        var editor = document.querySelector('[data-field-editor]');
        var addButton = document.querySelector('[data-add-field]');
        var template = document.querySelector('[data-field-template]');

        if (!editor || !addButton || !template) {
            return;
        }

        editor.querySelectorAll('[data-field-row]').forEach(bindRowEvents);

        addButton.addEventListener('click', function () {
            var index = editor.querySelectorAll('[data-field-row]').length;
            var html = template.innerHTML.replaceAll('__INDEX__', String(index));
            editor.insertAdjacentHTML('beforeend', html);
            var newRow = editor.querySelector('[data-field-row]:last-child');
            bindRowEvents(newRow);
            refreshRows(editor);
        });

        editor.addEventListener('click', function (event) {
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
