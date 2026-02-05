-- CREATE (Insert)
DROP PROCEDURE IF EXISTS sp_address_book_i;
DELIMITER //
CREATE PROCEDURE sp_address_book_i (
    p_name    VARCHAR(50),
    p_age     INT,
    p_phone   VARCHAR(20),
    p_address VARCHAR(100)
)
BEGIN
    INSERT INTO t_address_book (c_name, c_age, c_phone, c_address)
    VALUES (p_name, p_age, p_phone, p_address);
END //
DELIMITER ;

-- READ (Select All)
DROP PROCEDURE IF EXISTS sp_address_book_s;
DELIMITER //
CREATE PROCEDURE sp_address_book_s ()
BEGIN
    SELECT c_id, c_name, c_age, c_phone, c_address, c_created_at
    FROM t_address_book
    ORDER BY c_id;
END //
DELIMITER ;

-- UPDATE
DROP PROCEDURE IF EXISTS sp_address_book_u;
DELIMITER //
CREATE PROCEDURE sp_address_book_u (
    p_id       INT,
    p_name     VARCHAR(50),   -- 변경하지 않으려면 NULL 전달
    p_age      INT,           -- 변경하지 않으려면 NULL 전달
    p_phone    VARCHAR(20),   -- 변경하지 않으려면 NULL 전달
    p_address  VARCHAR(100)   -- 변경하지 않으려면 NULL 전달
)
BEGIN
    UPDATE t_address_book
    SET c_name    = COALESCE(p_name, c_name),
        c_age     = COALESCE(p_age, c_age),
        c_phone   = COALESCE(p_phone, c_phone),
        c_address = COALESCE(p_address, c_address)
    WHERE c_id = p_id;
END //
DELIMITER ;

-- DELETE
DROP PROCEDURE IF EXISTS sp_address_book_d;
DELIMITER //
CREATE PROCEDURE sp_address_book_d (
    p_id INT
)
BEGIN
    DELETE FROM t_address_book
    WHERE c_id = p_id;
END //
DELIMITER ;

-- CREATE (Insert)
DROP PROCEDURE IF EXISTS sp_user_i;
DELIMITER //
CREATE PROCEDURE sp_user_i (
    p_id    VARCHAR(50),
    p_pass  VARCHAR(255),
    p_role  VARCHAR(50)
)
BEGIN
    INSERT INTO t_user (c_id, c_pass, c_role)
    VALUES (p_id, p_pass, p_role);
END //
DELIMITER ;

-- READ (Select One)
DROP PROCEDURE IF EXISTS sp_user_s;
DELIMITER //
CREATE PROCEDURE sp_user_s (
    p_id VARCHAR(50)
)
BEGIN
    SELECT c_id, c_role, c_pass
    FROM t_user
    WHERE c_id = p_id;
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS params;
DELIMITER //
CREATE PROCEDURE params(IN p_proc_name VARCHAR(64))
BEGIN
    SELECT ORDINAL_POSITION AS ordinal,
           PARAMETER_NAME AS name,
           DATA_TYPE AS type,
           CHARACTER_MAXIMUM_LENGTH AS length
    FROM INFORMATION_SCHEMA.PARAMETERS
    WHERE SPECIFIC_NAME = p_proc_name
    ORDER BY ORDINAL_POSITION;
END // 
DELIMITER ;
