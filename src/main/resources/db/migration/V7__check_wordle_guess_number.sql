-- Wordle allows at most 6 guesses per session, so guess_number must be 1..6.
-- The application already enforces this (WordleSessionService caps guesses at 6
-- and blocks further input once a session is WON/LOST), but nothing stopped a
-- stray 7th row at the database level. This makes the invariant explicit.
-- See issue #34.
ALTER TABLE wordle_guesses
    ADD CONSTRAINT chk_guess_number CHECK (guess_number BETWEEN 1 AND 6);
