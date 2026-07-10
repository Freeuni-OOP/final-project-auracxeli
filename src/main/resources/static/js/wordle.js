document.getElementById('share-button')?.addEventListener('click', getWordleEmoji);

function getWordleEmoji() {
    const rows = document.querySelectorAll('#wordle-board .wordle-row');
    const lines = [];

    rows.forEach(function (row) {
        const tiles = row.querySelectorAll('.t');
        let line = '';
        let hasLetter = false;

        tiles.forEach(function (tile) {
            if (tile.classList.contains('g')) {
                line += '🟩';
                hasLetter = true;
            } else if (tile.classList.contains('y')) {
                line += '🟨';
                hasLetter = true;
            } else if (tile.classList.contains('x')) {
                line += '⬜';
                hasLetter = true;
            }
        });

        if (hasLetter) {
            lines.push(line);
        }
    });

    const attemptsUsed = document.getElementById('attempts-used').textContent;
    const maxAttempts = document.getElementById('max-attempts').textContent;
    const header = 'ცდები ' + attemptsUsed + '/' + maxAttempts;

    const grid = header + '\n\n' + lines.join('\n');
    const feedback = document.getElementById('share-feedback');

    navigator.clipboard.writeText(grid)
        .then(function () {
            feedback.textContent = 'დაკოპირდა!';
        })
        .catch(function () {
            feedback.textContent = 'კოპირება ვერ მოხერხდა';
        });
}

(function () {
    const board = document.getElementById('wordle-board');
    if (!board) return;

    const guessForm = document.getElementById('guess-form');
    if (!guessForm) return;

    const activeRowIndex = parseInt(board.dataset.activeRow, 10);
    const activeRow = board.querySelector('[data-row-index="' + activeRowIndex + '"]');
    if (!activeRow) return;

    const tiles = Array.from(activeRow.querySelectorAll('.t'));
    const guessInput = document.getElementById('guess-input');

    activeRow.classList.add('active-row');

    let letters = [];
    const georgianLetterPattern = /^[ა-ჰ]$/;

    function render() {
        tiles.forEach(function (tile, i) {
            tile.textContent = letters[i] ? letters[i].toUpperCase() : '';
        });
    }

    function typeLetter(letter) {
        if (georgianLetterPattern.test(letter) && letters.length < tiles.length) {
            letters.push(letter);
            render();
        }
    }

    function backspace() {
        if (letters.length > 0) {
            letters.pop();
            render();
        }
    }

    function submitGuess() {
        if (letters.length === tiles.length) {
            guessInput.value = letters.join('');
            guessForm.submit();
        }
    }

    window.wordleInput = {
        typeLetter: typeLetter,
        backspace: backspace,
        submitGuess: submitGuess
    };

    document.addEventListener('keydown', function (e) {
        if (e.key === 'Backspace') {
            e.preventDefault();
            backspace();
        } else if (e.key === 'Enter') {
            e.preventDefault();
            submitGuess();
        } else if (e.key.length === 1) {
            typeLetter(e.key);
        }
    });
})();