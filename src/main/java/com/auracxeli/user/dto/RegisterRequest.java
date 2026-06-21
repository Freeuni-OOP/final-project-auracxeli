package com.auracxeli.user.dto;


public record RegisterRequest (
    String username,
    String email,
    String password
){ }
