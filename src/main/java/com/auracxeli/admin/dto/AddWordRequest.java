package com.auracxeli.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Form data for adding a Wordle word. The word must be exactly five Georgian
 * (Mkhedruli) letters; the scheduled date is optional.
 */
public record AddWordRequest(

        @NotBlank(message = "სიტყვა სავალდებულოა")
        @Pattern(regexp = "^[ა-ჰ]{5}$",
                message = "სიტყვა უნდა იყოს ზუსტად 5 ქართული ასო")
        String word,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate scheduledDate
) {
}
