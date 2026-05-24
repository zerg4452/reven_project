(function () {
    function refreshRows(editor) {
        const rows = Array.from(editor.querySelectorAll('[data-field-row]'));

        rows.forEach(function (row, index) {
            const title = row.querySelector('.field-row-head strong');
            const removeButton = row.querySelector('[data-remove-field]');

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
        const editor = document.querySelector('[data-field-editor]');
        const addButton = document.querySelector('[data-add-field]');
        const template = document.querySelector('[data-field-template]');

        if (!editor || !addButton || !template) {
            return;
        }

        addButton.addEventListener('click', function () {
            const index = editor.querySelectorAll('[data-field-row]').length;
            const html = template.innerHTML.replaceAll('__INDEX__', String(index));
            editor.insertAdjacentHTML('beforeend', html);
            refreshRows(editor);
        });

        editor.addEventListener('click', function (event) {
            const button = event.target.closest('[data-remove-field]');
            if (!button || editor.querySelectorAll('[data-field-row]').length <= 1) {
                return;
            }

            button.closest('[data-field-row]').remove();
            refreshRows(editor);
        });

        refreshRows(editor);
    });
})();
