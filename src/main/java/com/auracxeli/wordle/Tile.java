package com.auracxeli.wordle;

/** A single rendered Wordle tile: the (upper-cased) letter and its CSS class. */
public record Tile(String letter, String cssClass) {
}
