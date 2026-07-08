package com.auracxeli.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
// here we have all the handlings for the edgecases from the ADmin
public record ConnectionsGroupRequest (
        @NotBlank(message = "კატეგორია სავალდებულოა")
        String category,

        @Min(value = 1, message = "სირთულე უნდა იყოს 1-დან 4-მდე")
        @Max(value = 4, message = "სირთულე უნდა იყოს 1-დან 4-მდე")
        int difficulty,

        @Size(min = 4, max = 4, message = "ჯგუფს უნდა ჰქონდეს ზუსტად 4 სიტყვა")
        List<@NotBlank(message = "სიტყვა არ უნდა იყოს ცარიელი") String> words
){


}
