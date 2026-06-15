-- ============================================================
-- Board: 30 tiles (position 0..29)
-- ============================================================
SET NAMES utf8mb4;

INSERT INTO tile (position, tile_type, tile_name, reward_exp, reward_coin) VALUES
( 0, 'START',     'Quest begins',          0,  0),
( 1, 'VOCAB',     'Word of the day',      10,  5),
( 2, 'GRAMMAR',   'Tense trial',          10,  5),
( 3, 'LUCKY',     'Lucky shrine',         15, 10),
( 4, 'VOCAB',     'Lexicon bridge',       10,  5),
( 5, 'SPEAKING',  'Cafe chatter',         20, 10),
( 6, 'TRAP',      'Mistake swamp',        -5,  0),
( 7, 'GRAMMAR',   'Article alley',        10,  5),
( 8, 'VOCAB',     'Synonym garden',       10,  5),
( 9, 'CHALLENGE', 'Boss riddle',          30, 15),
(10, 'LUCKY',     'Coin fountain',         5, 20),
(11, 'VOCAB',     'Phrase forge',         10,  5),
(12, 'GRAMMAR',   'Preposition pass',     10,  5),
(13, 'SPEAKING',  'Airport check-in',     20, 10),
(14, 'VOCAB',     'Idiom isle',           12,  6),
(15, 'TRAP',      'Spelling pit',         -5,  0),
(16, 'GRAMMAR',   'Conditional cliff',    12,  6),
(17, 'VOCAB',     'Verb meadow',          10,  5),
(18, 'CHALLENGE', 'Pop quiz',             25, 12),
(19, 'LUCKY',     'Time skip',            10, 10),
(20, 'VOCAB',     'Antonym arc',          10,  5),
(21, 'GRAMMAR',   'Subjunctive shore',    15,  8),
(22, 'SPEAKING',  'Doctor visit',         20, 10),
(23, 'VOCAB',     'Etymology echo',       12,  6),
(24, 'TRAP',      'Filler word bog',      -5,  0),
(25, 'GRAMMAR',   'Passive voice peak',   15,  8),
(26, 'VOCAB',     'Collocation cove',     12,  6),
(27, 'CHALLENGE', 'Final challenge',      35, 18),
(28, 'LUCKY',     'Treasure tile',        20, 25),
(29, 'END',       'Master throne',        50, 50);
