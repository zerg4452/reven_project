(function () {
    const form = document.querySelector('[data-photo-upload-form]');
    if (!form) {
        return;
    }

    const maxFiles = Number.parseInt(form.dataset.photoMaxFiles || '5', 10);
    const fileInput = form.querySelector('[data-photo-upload-input]');
    const newList = form.querySelector('[data-photo-new-list]');
    const countEl = form.querySelector('[data-photo-selection-count]');
    const maxUploadMessage = '최대 업로드 갯수는 5개입니다.';
    const minUploadMessage = '첨부 파일을 최소 1개 이상 업로드해 주세요.';

    let pendingFiles = [];
    let nextId = 0;

    function countExistingChecked() {
        return form.querySelectorAll('[data-photo-existing-checkbox]:checked').length;
    }

    function countNewChecked() {
        return pendingFiles.filter(function (entry) {
            return entry.checked;
        }).length;
    }

    function selectedTotal() {
        return countExistingChecked() + countNewChecked();
    }

    function updateCount() {
        if (!countEl) {
            return;
        }
        countEl.textContent = '현재 ' + selectedTotal() + '/' + maxFiles;
    }

    function revokeEntryUrl(entry) {
        if (entry && entry.objectUrl) {
            URL.revokeObjectURL(entry.objectUrl);
            entry.objectUrl = null;
        }
    }

    function clearPendingFiles() {
        pendingFiles.forEach(revokeEntryUrl);
        pendingFiles = [];
        if (newList) {
            newList.innerHTML = '';
        }
    }

    function createPreviewMarkup(file, objectUrl) {
        if (file.type && file.type.startsWith('image/') && objectUrl) {
            return '<img src="' + objectUrl + '" alt="' + escapeHtml(file.name) + '">';
        }
        if (file.type === 'video/mp4' && objectUrl) {
            return '<video src="' + objectUrl + '" controls muted></video>';
        }
        return '<span class="photo-file-fallback">' + escapeHtml(file.name) + '</span>';
    }

    function escapeHtml(value) {
        return String(value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function formatFileSizeKb(size) {
        const bytes = Number(size) || 0;
        const kb = Math.max(1, Math.floor((bytes + 1023) / 1024));
        return kb + ' KB';
    }

    function renderNewCard(entry) {
        if (!newList) {
            return;
        }

        const card = document.createElement('article');
        card.className = 'photo-file-card photo-select-card';
        card.innerHTML =
            '<div class="photo-file-card-body">' +
                '<div class="photo-file-card-media">' +
                    '<div class="photo-file-preview photo-file-preview--select">' +
                        createPreviewMarkup(entry.file, entry.objectUrl) +
                    '</div>' +
                '</div>' +
                '<aside class="photo-file-card-meta">' +
                    '<label class="photo-file-card-select">' +
                        '<input type="checkbox" checked data-photo-new-checkbox data-photo-new-id="' + entry.id + '">' +
                        '<span>저장 포함</span>' +
                    '</label>' +
                    '<strong class="photo-file-name">' + escapeHtml(entry.file.name) + '</strong>' +
                    '<span class="photo-file-size">' + formatFileSizeKb(entry.file.size) + '</span>' +
                '</aside>' +
            '</div>';
        newList.appendChild(card);
    }

    function rebuildPendingFiles(fileList) {
        clearPendingFiles();
        Array.from(fileList || []).forEach(function (file) {
            const entry = {
                id: nextId++,
                file: file,
                checked: true,
                objectUrl: null
            };
            if (file.type && (file.type.startsWith('image/') || file.type === 'video/mp4')) {
                entry.objectUrl = URL.createObjectURL(file);
            }
            pendingFiles.push(entry);
            renderNewCard(entry);
        });
        updateCount();
    }

    if (fileInput) {
        fileInput.addEventListener('change', function () {
            rebuildPendingFiles(fileInput.files);
        });
    }

    form.addEventListener('change', function (event) {
        const target = event.target;
        if (target.matches('[data-photo-new-checkbox]')) {
            const entryId = Number.parseInt(target.dataset.photoNewId || '-1', 10);
            const entry = pendingFiles.find(function (item) {
                return item.id === entryId;
            });
            if (entry) {
                entry.checked = target.checked;
            }
        }
        if (target.matches('[data-photo-existing-checkbox], [data-photo-new-checkbox]')) {
            updateCount();
        }
    });

    form.addEventListener('submit', function (event) {
        const submitter = event.submitter;
        if (submitter && submitter.hasAttribute('data-photo-delete-button')) {
            return;
        }

        const total = selectedTotal();
        if (total > maxFiles) {
            event.preventDefault();
            alert(maxUploadMessage);
            return;
        }
        if (total < 1) {
            event.preventDefault();
            alert(minUploadMessage);
            return;
        }

        if (!fileInput) {
            return;
        }

        const transfer = new DataTransfer();
        pendingFiles.forEach(function (entry) {
            if (entry.checked) {
                transfer.items.add(entry.file);
            }
        });
        fileInput.files = transfer.files;
    });

    updateCount();
})();
