/**
 * Learning2019CompAnalysisDB.sql is the schema that we are using to store
 * computational tradespace data for parallel computing in a clould space.
 *
 * Author: Prakash Manandhar
 * Date: 2020-Mar-21
 */

DROP TABLE IF EXISTS ComputeVector;

CREATE TABLE ComputeVector (
  `id` CHAR(27) PRIMARY KEY,
  compute_status BIT(2) NOT NULL, /* 0 == No, 1 == computing, 2 == computed */
  cost FLOAT,
  team_mix FLOAT,
  interaction_score FLOAT,
  walking_time FLOAT
);