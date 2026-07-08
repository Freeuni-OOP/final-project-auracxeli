(function () {
    'use strict';

    var MAX_SELECTED = 4;
    var TOAST_DURATION_MS = 1800;

    document.addEventListener('DOMContentLoaded', function () {
        setupSelection();
        showLastGuessFeedback();
    });

    function setupSelection() {
        var form = document.getElementById('guess-form');
        if (!form) {
            return;
        }

        var checkboxes = Array.prototype.slice.call(
            form.querySelectorAll('.connections-tile-checkbox')
        );
        var submitButton = document.getElementById('submit-guess');

        function selectedCount() {
            return checkboxes.filter(function (cb) {
                return cb.checked;
            }).length;
        }

        function updateState() {
            var count = selectedCount();
            if (submitButton) {
                submitButton.disabled = count !== MAX_SELECTED;
            }
            checkboxes.forEach(function (cb) {
                if (!cb.checked) {
                    cb.disabled = count >= MAX_SELECTED;
                }
            });
        }

        checkboxes.forEach(function (cb) {
            cb.addEventListener('change', updateState);
        });

        updateState();
    }
    function showLastGuessFeedback() {
        var root = document.getElementById('connections-app');
        if (!root) {
            return;
        }

        var result = root.getAttribute('data-last-guess-result');
        if (!result) {
            return;
        }

        var wordsAttr = root.getAttribute('data-last-guess-words') || '';
        var words = wordsAttr ? wordsAttr.split(',') : [];

        if (result === 'wrong') {
            shakeTiles(words);
        } else if (result === 'one_away') {
            shakeTiles(words);
            showToast('ერთი სიტყვით ცდები!');
        }
    }

    function shakeTiles(words) {
        if (!words.length) {
            return;
        }
        var checkboxes = document.querySelectorAll('.connections-tile-checkbox');
        checkboxes.forEach(function (cb) {
            if (words.indexOf(cb.value) === -1) {
                return;
            }
            var tile = cb.closest('.connections-tile');
            if (!tile) {
                return;
            }
            tile.classList.remove('shake');
            // force reflow so the animation can restart if triggered again
            void tile.offsetWidth;
            tile.classList.add('shake');
        });
    }

    function showToast(message) {
        var toast = document.getElementById('connections-toast');
        if (!toast) {
            return;
        }
        toast.textContent = message;
        toast.classList.add('visible');
        window.setTimeout(function () {
            toast.classList.remove('visible');
        }, TOAST_DURATION_MS);
    }
})();