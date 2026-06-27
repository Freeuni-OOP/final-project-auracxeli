package com.auracxeli.user.dto;

/**
 * One bar of the Wordle guess-distribution chart: how many games the user won
 * in {@code guesses} attempts ({@code count}), and that bar's width as a
 * percentage of the tallest bar ({@code percent}), so the longest bar is 100%.
 */
public record GuessBucket(int guesses, int count, int percent) { }
