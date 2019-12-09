/**
 * Learning2019DB.sql is the schema that we are using to store
 * experiment data in a MySQL or similar data.
 *
 * Author: Prakash Manandhar
 * Date: 2019-Dec-07
 */

DROP TABLE UIEvent;
DROP TABLE Participants;
DROP TABLE Teams;
DROP TABLE Experiments;

CREATE TABLE `Experiments` (
  `id` INTEGER PRIMARY KEY AUTO_INCREMENT,
  `description` VARCHAR(2000)
);

CREATE TABLE `Teams` (
  `id` INTEGER PRIMARY KEY AUTO_INCREMENT,
  `experiment_id` INTEGER, 
  FOREIGN KEY (experiment_id) REFERENCES Experiments(id),
  `day` CHAR(1),
  `session` CHAR(2),
  `room` CHAR(2),
  `station` CHAR(1),
  `condition` CHAR(1)
);

CREATE TABLE `Participants` (
  `id` INTEGER PRIMARY KEY AUTO_INCREMENT,
  `team_id` INTEGER,
   FOREIGN KEY (team_id) REFERENCES Teams(id),
  `chair` CHAR(3),
  `pre_survey` CHAR(5),
  `post_survey` CHAR(8)
);

CREATE TABLE `UIEvent` (
  `id` INTEGER PRIMARY KEY AUTO_INCREMENT,
  `team_id` INTEGER,
   FOREIGN KEY (team_id) REFERENCES Teams(id),
  `timestamp` DATETIME,
  `time_from_start` TIME,
  `type` VARCHAR(256),

  b_1 BIT(1),
  f_1 TINYINT,
  tA_1 FLOAT,
  tB_1 FLOAT,
  tC_1 FLOAT,
  
  b_2 BIT(1),
  f_2 TINYINT,
  tA_2 FLOAT,
  tB_2 FLOAT,
  tC_2 FLOAT,
  
  b_3 BIT(1),
  f_3 TINYINT,
  tA_3 FLOAT,
  tB_3 FLOAT,
  tC_3 FLOAT,

  b_4 BIT(1),
  f_4 TINYINT,
  tA_4 FLOAT,
  tB_4 FLOAT,
  tC_4 FLOAT,
  
  b_5 BIT(1),
  f_5 TINYINT,
  tA_5 FLOAT,
  tB_5 FLOAT,
  tC_5 FLOAT,
  
  b_6 BIT(1),
  f_6 TINYINT,
  tA_6 FLOAT,
  tB_6 FLOAT,
  tC_6 FLOAT,

  SimulationID INT,
  Summary VARCHAR(1024),
  Description VARCHAR(4096),
  TradeSpace_X_AXIS VARCHAR(64),
  TradeSpace_Y_AXIS VARCHAR(64), 
  Cost FLOAT, 
  Walking FLOAT, 
  Team_Mix FLOAT, 
  Interaction FLOAT, 
  Nfloors TINYINT
);