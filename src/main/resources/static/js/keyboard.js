(function () {
    const keyboard = document.getElementById('geo-keyboard');
    if (!keyboard) return;

    // ── Color keys based on existing board state ──
    function buildKeyStatuses() {
        const statuses = {};
        const priority = { g: 3, y: 2, x: 1 };

        document.querySelectorAll('#wordle-board .wordle-row').forEach(function (row) {
            row.querySelectorAll('.t').forEach(function (tile) {
                const letter = tile.textContent.trim().toLowerCase();
                if (!letter) return;

                const status = tile.classList.contains('g') ? 'g'
                             : tile.classList.contains('y') ? 'y'
                             : tile.classList.contains('x') ? 'x'
                             : null;

                if (!status) return;

                if (!statuses[letter] || priority[status] > priority[statuses[letter]]) {
                    statuses[letter] = status;
                }
            });
        });

        return statuses;
    }

    function colorKeys() {
        const statuses = buildKeyStatuses();
        keyboard.querySelectorAll('.geo-key[data-key]').forEach(function (key) {
            const letter = key.dataset.key;
            const status = statuses[letter];
            key.classList.remove('g', 'y', 'x', 'guessed');
            if (status) {
                key.classList.add(status);
            }
        });
    }

    colorKeys();

    // ── Click handlers ──
    keyboard.addEventListener('click', function (e) {
        const key = e.target.closest('.geo-key');
        if (!key) return;
        if (!window.wordleInput) return;

        const action = key.dataset.action;
        const letter = key.dataset.key;

        if (action === 'backspace') {
            window.wordleInput.backspace();
        } else if (action === 'submit') {
            window.wordleInput.submitGuess();
        } else if (letter) {
            window.wordleInput.typeLetter(letter);
        }
    });
})();