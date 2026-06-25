document.addEventListener("DOMContentLoaded", () => {
    console.log("Frontend JS loaded successfully!");

       const geoUniCodes = /^[\u10D0-\u10FF]+$/;
       const input = document.querySelector(".wordle-form input[type='text']");
       const takeFromForm = document.querySelector(".wordle-form");
       const error = document.querySelector(".wordle-error");
       if(!input || !takeFromForm)return;

       input.addEventListener("input", () => {
        const value = input.value;
        input.value = value.split("").filter(ch => geoUniCodes.test(ch)).join("");

       });

       takeFromForm.addEventListener("submit", (event) => {
        const value = input.value.trim();
        if(!geoUniCodes.test(value)){
            event.preventDefault();
            if(error){
            error.textContent = "დაშვებულია მხოლოდ ქართული ასოები";
            error.style.display = "block";

            }else{
                const newErr = document.createElement("p");
                newErr.classList.add("wordle-error");
                newErr.textContent = "დაშვებულია მხოლოდ ქართული ასოები";
                form.insertAdjacentElement("beforebegin", newErr);

            }return;



        }
         if(error){
              error.textContent = "";
         }

       });
});
