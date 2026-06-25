document.addEventListener("DOMContentLoaded", () => {
    console.log("Frontend JS loaded successfully!");

    const georgianOnly = /^[\u10D0-\u10FF]+$/;
    const georgianChar = /[\u10D0-\u10FF]/;
    const input = document.querySelector(".wordle-form input[type='text']");
    const takeFromForm = document.querySelector(".wordle-form");
    const error = document.getElementById("js-error");

    if (!input || !takeFromForm) return;

    input.addEventListener("input", () => {
        const value = input.value;
        const filtered = value.split("").filter(ch => georgianChar.test(ch)).join("");
        input.value = filtered;

        if (value !== filtered) {
            error.textContent = "მხოლოდ ქართული ასოებია დაშვებული";
            error.style.display = "block";
        } else {
            error.textContent = "";
            error.style.display = "none";
        }
    });

    takeFromForm.addEventListener("submit", (event) => {
        const value = input.value.trim();

        if (!georgianOnly.test(value)) {
            event.preventDefault();
            error.textContent = "დაშვებულია მხოლოდ ქართული ასოები";
            error.style.display = "block";
            return;
        }

        error.textContent = "";
        error.style.display = "none";
    });
});