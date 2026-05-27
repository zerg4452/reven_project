(function () {
    function formatKstDate(date) {
        return new Intl.DateTimeFormat('en-CA', {
            timeZone: 'Asia/Seoul',
            year: 'numeric',
            month: '2-digit',
            day: '2-digit'
        }).format(date);
    }

    function shiftedDate(days) {
        const date = new Date();
        date.setDate(date.getDate() + days);
        return date;
    }

    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('[data-date-default="start"]').forEach(function (input) {
            if (!input.value) {
                input.value = formatKstDate(shiftedDate(-60));
            }
        });

        document.querySelectorAll('[data-date-default="end"]').forEach(function (input) {
            if (!input.value) {
                input.value = formatKstDate(shiftedDate(1));
            }
        });

        document.querySelectorAll('[data-nav-toggle]').forEach(function (button) {
            button.addEventListener('click', function () {
                const target = document.getElementById(button.getAttribute('aria-controls'));
                const expanded = button.getAttribute('aria-expanded') === 'true';
                button.setAttribute('aria-expanded', String(!expanded));
                if (target) {
                    target.classList.toggle('is-open', !expanded);
                }
            });
        });

        const modal = document.querySelector('[data-photo-modal]');
        const modalImage = modal ? modal.querySelector('[data-photo-modal-image]') : null;
        const closeSelectors = modal ? modal.querySelectorAll('[data-photo-modal-close]') : [];

        function closePhotoModal() {
            if (!modal || !modalImage) {
                return;
            }
            modal.classList.remove('is-open');
            modal.setAttribute('aria-hidden', 'true');
            modalImage.removeAttribute('src');
            modalImage.setAttribute('alt', '');
            document.body.classList.remove('has-photo-modal');
        }

        function openPhotoModal(src, alt) {
            if (!modal || !modalImage) {
                return;
            }
            modalImage.src = src;
            modalImage.alt = alt || '';
            modal.classList.add('is-open');
            modal.setAttribute('aria-hidden', 'false');
            document.body.classList.add('has-photo-modal');
        }

        document.querySelectorAll('[data-photo-preview]').forEach(function (trigger) {
            trigger.addEventListener('click', function () {
                openPhotoModal(trigger.getAttribute('data-photo-src'), trigger.getAttribute('data-photo-alt'));
            });
        });

        closeSelectors.forEach(function (control) {
            control.addEventListener('click', closePhotoModal);
        });

        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape') {
                closePhotoModal();
            }
        });
    });
})();
