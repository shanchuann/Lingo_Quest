-- ============================================================
-- Achievements: 8 starter badges
-- ============================================================
SET NAMES utf8mb4;

INSERT INTO achievement (code, title, description, icon, reward_exp, reward_coin) VALUES
('FIRST_STEP',   'First step',         'Complete your very first answer.',                  'Footprints',  10,  5),
('STREAK_5',     'Hot streak',         'Answer 5 questions correctly in a row.',            'Flame',       30, 15),
('VOCAB_MASTER', 'Vocab master',       'Correctly answer 20 vocabulary questions.',         'BookOpen',    60, 30),
('GRAMMAR_PRO',  'Grammar pro',        'Correctly answer 20 grammar questions.',            'Pencil',      60, 30),
('SPEAK_UP',     'Speak up',           'Finish 3 AI conversation practices.',               'Mic',         50, 25),
('LEVEL_UP',     'Rising star',        'Reach Level 3.',                                    'TrendingUp',  80, 40),
('CENTURION',    'Centurion',          'Accumulate 100 correct answers.',                   'Award',      120, 60),
('CHAMPION',     'Quest champion',     'Reach the End tile in any game.',                   'Crown',      150, 80);
