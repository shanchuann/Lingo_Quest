-- ============================================================
-- Lingo Quest - schema
-- Auto-loaded by docker-entrypoint when MySQL container boots
-- ============================================================
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `user` (
  id          BIGINT       PRIMARY KEY AUTO_INCREMENT,
  username    VARCHAR(50)  NOT NULL UNIQUE,
  password    VARCHAR(72)  NOT NULL COMMENT 'BCrypt hash (60 chars + safety)',
  email       VARCHAR(100) NOT NULL UNIQUE,
  avatar      VARCHAR(255) DEFAULT NULL,
  level       TINYINT      NOT NULL DEFAULT 1,
  experience  INT          NOT NULL DEFAULT 0,
  coins       INT          NOT NULL DEFAULT 0,
  deleted     TINYINT      NOT NULL DEFAULT 0,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_user_level (level),
  KEY idx_user_exp (experience)
) ENGINE=InnoDB CHARSET=utf8mb4 COMMENT='Players';

CREATE TABLE IF NOT EXISTS `game` (
  id               BIGINT      PRIMARY KEY AUTO_INCREMENT,
  user_id          BIGINT      NOT NULL,
  current_position TINYINT     NOT NULL DEFAULT 0,
  round_num        INT         NOT NULL DEFAULT 0,
  status           VARCHAR(20) NOT NULL COMMENT 'CREATED|IN_PROGRESS|PAUSED|FINISHED|ABANDONED',
  total_score      INT         NOT NULL DEFAULT 0,
  start_time       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  end_time         DATETIME    DEFAULT NULL,
  deleted          TINYINT     NOT NULL DEFAULT 0,
  KEY idx_game_user (user_id, status),
  CONSTRAINT fk_game_user FOREIGN KEY (user_id) REFERENCES `user`(id)
) ENGINE=InnoDB CHARSET=utf8mb4 COMMENT='Game sessions';

CREATE TABLE IF NOT EXISTS `tile` (
  id          INT          PRIMARY KEY AUTO_INCREMENT,
  position    TINYINT      NOT NULL UNIQUE COMMENT '0~29',
  tile_type   VARCHAR(20)  NOT NULL COMMENT 'START|END|VOCAB|GRAMMAR|SPEAKING|LUCKY|TRAP|CHALLENGE',
  tile_name   VARCHAR(100) NOT NULL,
  reward_exp  INT          NOT NULL DEFAULT 0,
  reward_coin INT          NOT NULL DEFAULT 0
) ENGINE=InnoDB CHARSET=utf8mb4 COMMENT='Board tiles';

CREATE TABLE IF NOT EXISTS `question` (
  id         BIGINT       PRIMARY KEY AUTO_INCREMENT,
  type       VARCHAR(20)  NOT NULL COMMENT 'VOCAB|GRAMMAR|SPELLING|READING|LISTENING',
  content    TEXT         NOT NULL,
  options    JSON         DEFAULT NULL COMMENT '["A...","B...","C...","D..."]',
  answer     VARCHAR(64)  NOT NULL,
  difficulty TINYINT      NOT NULL DEFAULT 1 COMMENT '1~5',
  tags       VARCHAR(255) DEFAULT NULL,
  deleted    TINYINT      NOT NULL DEFAULT 0,
  KEY idx_question_type_diff (type, difficulty)
) ENGINE=InnoDB CHARSET=utf8mb4 COMMENT='Question bank';

CREATE TABLE IF NOT EXISTS `learning_record` (
  id          BIGINT   PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT   NOT NULL,
  question_id BIGINT   NOT NULL,
  game_id     BIGINT   DEFAULT NULL,
  user_answer TEXT,
  is_correct  BOOLEAN  NOT NULL,
  score       INT      NOT NULL DEFAULT 0,
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_lr_user_time (user_id, created_at),
  CONSTRAINT fk_lr_user FOREIGN KEY (user_id) REFERENCES `user`(id),
  CONSTRAINT fk_lr_q    FOREIGN KEY (question_id) REFERENCES `question`(id)
) ENGINE=InnoDB CHARSET=utf8mb4 COMMENT='Answer history';

CREATE TABLE IF NOT EXISTS `achievement` (
  id          INT          PRIMARY KEY AUTO_INCREMENT,
  code        VARCHAR(50)  NOT NULL UNIQUE,
  title       VARCHAR(100) NOT NULL,
  description TEXT,
  icon        VARCHAR(50)  DEFAULT NULL COMMENT 'Lucide icon name',
  reward_exp  INT          NOT NULL DEFAULT 0,
  reward_coin INT          NOT NULL DEFAULT 0
) ENGINE=InnoDB CHARSET=utf8mb4 COMMENT='Achievement catalog';

CREATE TABLE IF NOT EXISTS `user_achievement` (
  user_id        BIGINT   NOT NULL,
  achievement_id INT      NOT NULL,
  unlocked_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, achievement_id),
  KEY idx_ua_user (user_id),
  CONSTRAINT fk_ua_user FOREIGN KEY (user_id) REFERENCES `user`(id),
  CONSTRAINT fk_ua_ach  FOREIGN KEY (achievement_id) REFERENCES `achievement`(id)
) ENGINE=InnoDB CHARSET=utf8mb4 COMMENT='Unlocked achievements';
