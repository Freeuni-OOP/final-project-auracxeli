package com.auracxeli.achievement;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The catalog of achievements. Each carries its display text (Georgian) and a
 * rule over an {@link AchievementContext}. Kept in code (not a DB table) so each
 * rule is a plain, unit-testable predicate.
 */
public enum Achievement {

    FIRST_WIN("პირველი გამარჯვება", "ნებისმიერი თამაშის მოგება", "🥇",
            ctx -> ctx.wordleWins() > 0 || ctx.connectionsWins() > 0),

    HAT_TRICK("ჰეთ-ტრიკი", "3 მოგება ზედიზედ", "🎩",
            ctx -> ctx.wordleMaxStreak() >= 3 || ctx.connectionsMaxStreak() >= 3),

    ON_FIRE("სტაბილური", "7-დღიანი მოგებების სერია", "🔥",
            ctx -> ctx.wordleMaxStreak() >= 7 || ctx.connectionsMaxStreak() >= 7),

    FLAWLESS("უშეცდომო", "Connections-ის მოგება შეცდომის გარეშე", "✨",
            AchievementContext::flawlessConnectionsWin),

    SHARPSHOOTER("ტელეპათი", "Wordle-ის მოგება 2 ან ნაკლებ ცდაში", "🎯",
            AchievementContext::wonWordleInTwoOrFewer),

    REGULAR("ვეტერანი", "25 დასრულებული თამაში", "📅",
            ctx -> ctx.totalGamesPlayed() >= 25);

    private final String title;
    private final String description;
    private final String icon;
    private final Predicate<AchievementContext> earnedWhen;

    Achievement(String title, String description, String icon, Predicate<AchievementContext> earnedWhen) {
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.earnedWhen = earnedWhen;
    }

    public boolean isEarnedBy(AchievementContext context) {
        return earnedWhen.test(context);
    }

    /** Every achievement whose rule the given context satisfies. */
    public static Set<Achievement> earnedFor(AchievementContext context) {
        return Arrays.stream(values())
                .filter(achievement -> achievement.isEarnedBy(context))
                .collect(Collectors.toUnmodifiableSet());
    }

    public String getTitle()       { return title; }
    public String getDescription() { return description; }
    public String getIcon()        { return icon; }
}
