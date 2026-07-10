-- The ALL_ROUNDER achievement was removed from the catalog. Any row granted while
-- it existed can no longer be mapped back to the enum, which breaks profile loading.
-- Delete those orphaned rows so existing databases keep working.
DELETE FROM user_achievements WHERE achievement_key = 'ALL_ROUNDER';
