-- ---
-- Table 'Players'
--
-- ---

CREATE TABLE [Players] (
  id INTEGER PRIMARY KEY,
  username VARCHAR,
  [password] VARCHAR,
  score INTEGER
);

-- ---
-- Table 'ScoreBoard'
--
-- ---

CREATE TABLE [ScoreBoard] (
    id INTEGER PRIMARY KEY,
    proposername VARCHAR
    guessername VARCHAR
    score INTEGER
    imagename VARCHAR
);