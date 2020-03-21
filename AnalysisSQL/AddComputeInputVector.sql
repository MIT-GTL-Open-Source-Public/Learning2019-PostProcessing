/**
 * AddComputeInputVector.sql adds a single compute vector to the database with given 
 * wave and compute status set to not computed. If the input already exits, does nothing.
 *
 * Author: Prakash Manandhar
 * Date: 2020-Mar-21
 */

USE Learning2019;
DROP PROCEDURE IF EXISTS AddComputeInputVector;
DELIMITER $$
 
CREATE PROCEDURE AddComputeInputVector(
    IN  input_vector CHAR(27),
    IN  input_wave   INTEGER,
    OUT already_exists BOOLEAN
) 
BEGIN
    DECLARE input_count INTEGER;
    SELECT count(input_vector) INTO input_count FROM ComputeVector
        WHERE input_vector = input_vector;
    IF input_count = 0 THEN 
        SET already_exists = FALSE;
        INSERT INTO ComputeVector(input_vector, compute_status, input_wave) 
            VALUES (input_vector, 0, input_wave);
    ELSE
        SET already_exists = TRUE;
    END IF;
END $$
 
DELIMITER ;