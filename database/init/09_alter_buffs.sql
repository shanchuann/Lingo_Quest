-- ============================================================
-- Lingo Quest - alters for functional shop item buffs.
-- Applied idempotently so re-running on a populated DB is safe.
-- ============================================================
SET NAMES utf8mb4;

-- user.xp_double_pending — set to 1 when player "uses" an XP_DOUBLE item,
-- consumed by GameService.answer() on the next correct answer.
SET @col := (
  SELECT COUNT(*) FROM information_schema.columns
   WHERE table_schema = DATABASE() AND table_name = 'user' AND column_name = 'xp_double_pending'
);
SET @sql := IF(@col = 0,
  'ALTER TABLE `user` ADD COLUMN xp_double_pending TINYINT NOT NULL DEFAULT 0 COMMENT ''pending XP×2 buff'' AFTER current_book_id',
  'SELECT ''xp_double_pending already exists'' AS note');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- game.previous_position — pre-roll snapshot used to revert when RETRY is used.
SET @col2 := (
  SELECT COUNT(*) FROM information_schema.columns
   WHERE table_schema = DATABASE() AND table_name = 'game' AND column_name = 'previous_position'
);
SET @sql2 := IF(@col2 = 0,
  'ALTER TABLE `game` ADD COLUMN previous_position TINYINT NULL COMMENT ''snapshot before last roll'' AFTER current_position',
  'SELECT ''previous_position already exists'' AS note');
PREPARE stmt2 FROM @sql2; EXECUTE stmt2; DEALLOCATE PREPARE stmt2;
