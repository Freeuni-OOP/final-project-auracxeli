document.addEventListener("DOMContentLoaded", () => {
    console.log("Frontend JS loaded successfully!");

       const geoUniCodes = /^[\u10D0-\u10FF]+$/;
       const input = document.querySelector(".wordle-form input[type='text']");
       const takeFromForm = document.querySelector(".wordle-form");
       const error = document.getElementBy("js-error");
       if(!input || !takeFromForm)return;

       input.addEventListener("input", () => {
        const value = input.value;
        input.value = value.split("").filter(ch => geoUniCodes.test(ch)).join("");

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
