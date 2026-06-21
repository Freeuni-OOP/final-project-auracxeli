package com.auracxeli.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest (
        @NotBlank(message = "მომხმარებლის სახელი სავალდებულოა")
        @Size(min = 5, max = 50, message = "სახელის ზომა უნდა იყოს 5 დან 50 სიმბოლომდე")
        String username,
        @NotBlank(message = "ელფოსტა სავალდებულოა")
        @Email(message = "ელფოსტის ფორმატი არასწორია")
        @Size(max = 100, message = "ელფოსტა ძალიან გრძელია")
        String email,
        @NotBlank(message = "პაროლი სავალდებულოა")
        @Size(min = 8, message = "პაროლი მინიმუმ 8 სიმბოლო უნდა იყოს")
        String password
){ }
