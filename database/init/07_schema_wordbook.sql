-- ============================================================
-- Lingo Quest - word book module (catalog + words + progress)
-- ============================================================
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `word_book` (
  id           INT          PRIMARY KEY AUTO_INCREMENT,
  code         VARCHAR(50)  NOT NULL UNIQUE COMMENT 'PRIMARY_800 | CET4 | ...',
  name         VARCHAR(100) NOT NULL,
  description  VARCHAR(255) NOT NULL,
  level        VARCHAR(20)  NOT NULL COMMENT 'PRIMARY|MIDDLE|HIGH|CET4|CET6|POSTGRAD|IELTS|TOEFL',
  cover_color  VARCHAR(20)  NOT NULL DEFAULT 'green' COMMENT 'pixel-* color name for the cover',
  total_words  INT          NOT NULL DEFAULT 0 COMMENT 'denormalised total word count',
  sort_order   INT          NOT NULL DEFAULT 0,
  enabled      TINYINT      NOT NULL DEFAULT 1
) ENGINE=InnoDB CHARSET=utf8mb4 COMMENT='Word book catalog';

CREATE TABLE IF NOT EXISTS `word` (
  id              BIGINT       PRIMARY KEY AUTO_INCREMENT,
  book_id         INT          NOT NULL,
  word            VARCHAR(50)  NOT NULL,
  phonetic        VARCHAR(60)  DEFAULT NULL,
  part_of_speech  VARCHAR(20)  DEFAULT NULL,
  meaning_zh      VARCHAR(255) NOT NULL,
  meaning_en      VARCHAR(255) DEFAULT NULL,
  example_en      VARCHAR(255) DEFAULT NULL,
  example_zh      VARCHAR(255) DEFAULT NULL,
  difficulty      TINYINT      NOT NULL DEFAULT 1,
  sort_order      INT          NOT NULL DEFAULT 0,
  KEY idx_word_book (book_id),
  KEY idx_word_text (word),
  CONSTRAINT fk_word_book FOREIGN KEY (book_id) REFERENCES `word_book`(id)
) ENGINE=InnoDB CHARSET=utf8mb4 COMMENT='Words within a book';

CREATE TABLE IF NOT EXISTS `user_word_progress` (
  user_id          BIGINT       NOT NULL,
  word_id          BIGINT       NOT NULL,
  state            VARCHAR(10)  NOT NULL DEFAULT 'NEW' COMMENT 'NEW|LEARNING|MASTERED',
  reviewed_count   INT          NOT NULL DEFAULT 0,
  correct_count    INT          NOT NULL DEFAULT 0,
  next_review_at   DATETIME     DEFAULT NULL,
  updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, word_id),
  KEY idx_uwp_user_state (user_id, state),
  CONSTRAINT fk_uwp_user FOREIGN KEY (user_id) REFERENCES `user`(id),
  CONSTRAINT fk_uwp_word FOREIGN KEY (word_id) REFERENCES `word`(id)
) ENGINE=InnoDB CHARSET=utf8mb4 COMMENT='Per-user word learning state';

-- Add user.current_book_id for "selected word book" — applied idempotently.
SET @col := (
  SELECT COUNT(*) FROM information_schema.columns
   WHERE table_schema = DATABASE() AND table_name = 'user' AND column_name = 'current_book_id'
);
SET @sql := IF(@col = 0,
  'ALTER TABLE `user` ADD COLUMN current_book_id INT DEFAULT NULL COMMENT ''Selected word book'' AFTER coins',
  'SELECT ''current_book_id already exists'' AS note');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
