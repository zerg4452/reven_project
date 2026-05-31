// 공지사항 등록/수정 화면의 썸네일 미리보기와 첨부 파일 선택을 관리하는 스크립트
(function () {
    const form = document.querySelector('[data-notice-upload-form]');
    if (!form) {
        return;
    }

    const maxFiles = Number.parseInt(form.dataset.noticeMaxFiles || '10', 10);
    const fileInput = form.querySelector('[data-notice-upload-input]');
    const newList = form.querySelector('[data-notice-new-list]');
    const countEl = form.querySelector('[data-notice-selection-count]');
    const thumbInput = form.querySelector('[data-notice-thumb-input]');
    const thumbPreview = form.querySelector('[data-notice-thumb-preview]');
    const maxUploadMessage = '첨부 파일은 최대 ' + maxFiles + '개까지 등록할 수 있습니다.';

    let pendingFiles = [];
    let nextId = 0;

    function countExistingChecked() {
        return form.querySelectorAll('[data-notice-existing-checkbox]:checked').length;
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
        if (countEl) {
            countEl.textContent = '현재 ' + selectedTotal() + '/' + maxFiles;
        }
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
        card.className = 'notice-file-card';
        card.innerHTML =
            '<label class="notice-file-card-select">' +
                '<input type="checkbox" checked data-notice-new-checkbox data-notice-new-id="' + entry.id + '">' +
                '<span>저장 포함</span>' +
            '</label>' +
            '<strong class="notice-file-name">' + escapeHtml(entry.file.name) + '</strong>' +
            '<span class="notice-file-size">' + formatFileSizeKb(entry.file.size) + '</span>';
        newList.appendChild(card);
    }

    function rebuildPendingFiles(fileList) {
        pendingFiles = [];
        if (newList) {
            newList.innerHTML = '';
        }
        Array.from(fileList || []).forEach(function (file) {
            const entry = { id: nextId++, file: file, checked: true };
            pendingFiles.push(entry);
            renderNewCard(entry);
        });
        updateCount();
    }

    function renderThumbnailPreview(file) {
        if (!thumbPreview) {
            return;
        }
        thumbPreview.innerHTML = '';
        if (file && file.type && file.type.startsWith('image/')) {
            const objectUrl = URL.createObjectURL(file);
            const img = document.createElement('img');
            img.src = objectUrl;
            img.alt = file.name;
            img.className = 'notice-thumbnail-image';
            img.addEventListener('load', function () {
                URL.revokeObjectURL(objectUrl);
            });
            thumbPreview.appendChild(img);
        }
    }

    if (fileInput) {
        fileInput.addEventListener('change', function () {
            rebuildPendingFiles(fileInput.files);
        });
    }

    if (thumbInput) {
        thumbInput.addEventListener('change', function () {
            renderThumbnailPreview(thumbInput.files && thumbInput.files[0]);
        });
    }

    form.addEventListener('change', function (event) {
        const target = event.target;
        if (target.matches('[data-notice-new-checkbox]')) {
            const entryId = Number.parseInt(target.dataset.noticeNewId || '-1', 10);
            const entry = pendingFiles.find(function (item) {
                return item.id === entryId;
            });
            if (entry) {
                entry.checked = target.checked;
            }
        }
        if (target.matches('[data-notice-existing-checkbox], [data-notice-new-checkbox]')) {
            updateCount();
        }
    });

    form.addEventListener('submit', function (event) {
        const submitter = event.submitter;
        if (submitter && submitter.hasAttribute('data-notice-delete-button')) {
            return;
        }
        if (selectedTotal() > maxFiles) {
            event.preventDefault();
            alert(maxUploadMessage);
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
