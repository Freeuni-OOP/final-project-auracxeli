document.addEventListener("DOMContentLoaded", () => {
    console.log("Frontend JS loaded successfully!");

       const geoUniCodes = /^[\u10D0-\u10FF]+$/;
       const input = document.querySelector(".wordle-form input[type='text']");
       const takeFromForm = document.querySelector(".wordle-form");
       const error = document.getElementByid("js-error");
       if(!input || !takeFromForm)return;

       input.addEventListener("input", () => {
        const value = input.value;
        const filtered = value.split("").filter(ch => geoUniCodes.test(ch)).join("");
        input.value = filtered;
           if (value !== filtered) {
            errorEl.textContent = "მხოლოდ ქართული ასოებია დაშვებული";
            errorEl.style.display = "block";
        } else {
            errorEl.textContent = "";
            errorEl.style.display = "none";
        }
       });

       takeFromForm.addEventListener("submit", (event) => {
        const value = input.value.trim();
        if(!geoUniCodes.test(value)){
            event.preventDefault();
            
            error.textContent = "დაშვებულია მხოლოდ ქართული ასოები";
            error.style.display = "block";
            return;
        }
         
             error.textContent = "";
            error.style.display = "none";
       });
});
