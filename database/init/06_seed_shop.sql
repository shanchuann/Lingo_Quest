-- ============================================================
-- Lingo Quest - shop catalog seed (12 items across 3 categories)
-- ============================================================
SET NAMES utf8mb4;

INSERT IGNORE INTO `shop_item`
  (code, name, description, category, effect, price, icon, payload, consumable, sort_order, enabled)
VALUES
  -- Functional consumables ─────────────────────────────────
  ('HINT_BASIC',  '提示卷',     '在答题时显示一条来自导师的提示。',          'FUNCTIONAL', 'HINT',       30,  'Lightbulb',     NULL,                       1, 10, 1),
  ('RETRY_DICE',  '重掷令',     '本次掷骰可以重投一次,取较好的结果。',       'FUNCTIONAL', 'RETRY',      40,  'Dices',         NULL,                       1, 20, 1),
  ('SKIP_TILE',   '越格符',     '跳过当前格的题目,不计入正确率统计。',       'FUNCTIONAL', 'SKIP',       60,  'SkipForward',   NULL,                       1, 30, 1),
  ('XP_DOUBLE',   '双倍经验',   '下一次答对获得双倍经验值。',                 'FUNCTIONAL', 'XP_DOUBLE',  80,  'Zap',           '{"multiplier": 2}',        1, 40, 1),

  -- Cosmetic permanents ────────────────────────────────────
  ('DICE_GOLD',   '镀金骰子',   '一颗温暖琥珀色的骰子。视觉装饰。',           'COSMETIC',   'DICE_SKIN',  120, 'Dice5',         '{"hue": "amber"}',         0, 110, 1),
  ('DICE_NEON',   '霓虹骰子',   '一颗会发光的霓虹骰子。视觉装饰。',           'COSMETIC',   'DICE_SKIN',  150, 'Dice5',         '{"hue": "violet"}',        0, 120, 1),
  ('BOARD_OCEAN', '深海主题',   '将棋盘背景换成深海蓝绿的色调。',             'COSMETIC',   'BOARD_THEME',180, 'Waves',         '{"hue": "ocean"}',         0, 130, 1),
  ('BOARD_SAKURA','樱花主题',   '将棋盘背景换成樱花粉的色调。',               'COSMETIC',   'BOARD_THEME',180, 'Flower',        '{"hue": "sakura"}',        0, 140, 1),
  ('FRAME_LAUREL','月桂头像框', '一圈金色月桂头像框,献给前三名玩家。',        'COSMETIC',   'AVATAR_FRAME',220,'Crown',         '{"style": "laurel"}',      0, 150, 1),

  -- Achievement accelerators ───────────────────────────────
  ('ACH_VOCAB',   '词海速记',   '直接解锁「VOCAB_MASTER · 词海征服者」徽章。','ACCELERATOR','ACH_UNLOCK', 300, 'Sparkles',      '{"code": "VOCAB_MASTER"}', 1, 210, 1),
  ('ACH_GRAMMAR', '语法速通',   '直接解锁「GRAMMAR_PRO · 语法练家」徽章。',  'ACCELERATOR','ACH_UNLOCK', 300, 'Sparkles',      '{"code": "GRAMMAR_PRO"}',  1, 220, 1),
  ('ACH_CENTURY', '百答速成',   '直接解锁「CENTURION · 百答百中」徽章。',    'ACCELERATOR','ACH_UNLOCK', 500, 'Trophy',        '{"code": "CENTURION"}',    1, 230, 1);
