package com.auracxeli.admin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

// here we handle the date and the groups for the connections games
public record CreateConnectionsPuzzleRequest(

        @NotNull(message = "თარიღი სავალდებულოა")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate puzzleDate,

        @Valid
        @Size(min = 4, max = 4, message = "პაზლს უნდა ჰქონდეს ზუსტად 4 ჯგუფი")
        List<ConnectionsGroupRequest> groups
) {


}