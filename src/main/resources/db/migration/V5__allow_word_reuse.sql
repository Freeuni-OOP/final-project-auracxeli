-- Words are no longer globally unique: a word may be reused over time.
-- The admin add-word logic enforces a 60-day gap between uses of the same word.
-- scheduled_date stays UNIQUE (still one puzzle per day).
-- (The inline `word ... UNIQUE` in V2 created an index named `word`.)
ALTER TABLE wordle_words DROP INDEX word;
