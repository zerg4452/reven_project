// 공개 포토 게시판 첨부 미리보기 모달
(function () {
    document.addEventListener('DOMContentLoaded', function () {
        const modal = document.querySelector('[data-public-photo-modal]');
        if (!modal) {
            return;
        }

        const modalImage = modal.querySelector('[data-public-photo-modal-image]');
        const modalVideo = modal.querySelector('[data-public-photo-modal-video]');
        const closeControls = modal.querySelectorAll('[data-public-photo-modal-close]');
        const previewTriggers = document.querySelectorAll('[data-public-photo-preview]');

        function resetMedia() {
            if (modalImage) {
                modalImage.removeAttribute('src');
                modalImage.setAttribute('alt', '');
                modalImage.classList.remove('is-active');
            }

            if (modalVideo) {
                modalVideo.pause();
                modalVideo.removeAttribute('src');
                modalVideo.classList.remove('is-active');
                modalVideo.load();
            }
        }

        function closeModal() {
            modal.classList.remove('is-open', 'is-active');
            modal.setAttribute('aria-hidden', 'true');
            resetMedia();
            document.body.classList.remove('has-public-photo-modal');
        }

        function openModal(trigger) {
            const source = trigger.getAttribute('data-media-src');
            const mediaType = trigger.getAttribute('data-media-type') || '';
            const alt = trigger.getAttribute('data-media-alt') || '';
            const video = mediaType.indexOf('video/') === 0;

            if (!source) {
                return;
            }

            if (video && !modalVideo) {
                return;
            }

            if (!video && !modalImage) {
                return;
            }

            resetMedia();

            if (video) {
                modalVideo.src = source;
                modalVideo.classList.add('is-active');
            } else {
                modalImage.src = source;
                modalImage.alt = alt;
                modalImage.classList.add('is-active');
            }

            modal.classList.add('is-open', 'is-active');
            modal.setAttribute('aria-hidden', 'false');
            document.body.classList.add('has-public-photo-modal');
        }

        previewTriggers.forEach(function (trigger) {
            trigger.addEventListener('click', function () {
                openModal(trigger);
            });
        });

        closeControls.forEach(function (control) {
            control.addEventListener('click', closeModal);
        });

        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape') {
                closeModal();
            }
        });
    });
})();
