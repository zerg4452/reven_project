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
    });
})();
