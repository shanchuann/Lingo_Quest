-- ============================================================
-- Lingo Quest - shop schema (catalog + per-user inventory)
-- ============================================================
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `shop_item` (
  id           INT          PRIMARY KEY AUTO_INCREMENT,
  code         VARCHAR(50)  NOT NULL UNIQUE COMMENT 'Stable identifier (HINT_BASIC, DICE_GOLD, ...)',
  name         VARCHAR(100) NOT NULL,
  description  VARCHAR(255) NOT NULL,
  category     VARCHAR(20)  NOT NULL COMMENT 'FUNCTIONAL | COSMETIC | ACCELERATOR',
  effect       VARCHAR(30)  NOT NULL COMMENT 'HINT | RETRY | SKIP | XP_DOUBLE | DICE_SKIN | BOARD_THEME | AVATAR_FRAME | ACH_UNLOCK',
  price        INT          NOT NULL,
  icon         VARCHAR(50)  DEFAULT NULL COMMENT 'Lucide icon name',
  payload      JSON         DEFAULT NULL COMMENT 'effect-specific extras (multiplier, hue, etc.)',
  consumable   TINYINT      NOT NULL DEFAULT 1 COMMENT '1 = consumes on use; 0 = cosmetic / permanent',
  sort_order   INT          NOT NULL DEFAULT 0,
  enabled      TINYINT      NOT NULL DEFAULT 1,
  KEY idx_shop_category (category, enabled, sort_order)
) ENGINE=InnoDB CHARSET=utf8mb4 COMMENT='Shop catalog';

CREATE TABLE IF NOT EXISTS `user_inventory` (
  id           BIGINT   PRIMARY KEY AUTO_INCREMENT,
  user_id      BIGINT   NOT NULL,
  item_id      INT      NOT NULL,
  quantity     INT      NOT NULL DEFAULT 1 COMMENT 'consumable: count; cosmetic: always 1',
  equipped     TINYINT  NOT NULL DEFAULT 0 COMMENT 'only meaningful for cosmetics',
  acquired_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_item (user_id, item_id),
  KEY idx_inv_user (user_id),
  CONSTRAINT fk_inv_user FOREIGN KEY (user_id) REFERENCES `user`(id),
  CONSTRAINT fk_inv_item FOREIGN KEY (item_id) REFERENCES `shop_item`(id)
) ENGINE=InnoDB CHARSET=utf8mb4 COMMENT='Owned items per user';
