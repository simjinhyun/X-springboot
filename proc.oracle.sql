-- 1. Address Book: INSERT
CREATE OR REPLACE PROCEDURE sp_address_book_i (
    p_name    IN VARCHAR2,
    p_age     IN NUMBER,
    p_phone   IN VARCHAR2,
    p_address IN VARCHAR2
) AS
BEGIN
    INSERT INTO t_address_book (c_name, c_age, c_phone, c_address)
    VALUES (p_name, p_age, p_phone, p_address);
    COMMIT;
END;
/

-- 2. Address Book: SELECT (오라클은 결과셋 반환 시 Cursor 필요)
CREATE OR REPLACE PROCEDURE sp_address_book_s (
    p_cursor OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN p_cursor FOR
    SELECT c_id, c_name, c_age, c_phone, c_address, c_created_at
    FROM t_address_book
    ORDER BY c_id;
END;
/

-- 3. Address Book: UPDATE
CREATE OR REPLACE PROCEDURE sp_address_book_u (
    p_id      IN NUMBER,
    p_name    IN VARCHAR2,
    p_age     IN NUMBER,
    p_phone   IN VARCHAR2,
    p_address IN VARCHAR2
) AS
BEGIN
    UPDATE t_address_book
    SET c_name    = COALESCE(p_name, c_name),
        c_age     = COALESCE(p_age, c_age),
        c_phone   = COALESCE(p_phone, c_phone),
        c_address = COALESCE(p_address, c_address)
    WHERE c_id = p_id;
    COMMIT;
END;
/

-- 4. Address Book: DELETE
CREATE OR REPLACE PROCEDURE sp_address_book_d (
    p_id IN NUMBER
) AS
BEGIN
    DELETE FROM t_address_book WHERE c_id = p_id;
    COMMIT;
END;
/

-- 5. User: INSERT
CREATE OR REPLACE PROCEDURE sp_user_i (
    p_id   IN VARCHAR2,
    p_pass IN VARCHAR2,
    p_role IN VARCHAR2
) AS
BEGIN
    INSERT INTO t_user (c_id, c_pass, c_role)
    VALUES (p_id, p_pass, p_role);
    COMMIT;
END;
/

-- 6. User: SELECT ONE
CREATE OR REPLACE PROCEDURE sp_user_s (
    p_id     IN  VARCHAR2,
    p_cursor OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN p_cursor FOR
    SELECT c_id, c_role, c_pass
    FROM t_user
    WHERE c_id = p_id;
END;
/

-- 7. Params: 프로시저 매개변수 정보 조회 (오라클 딕셔너리 사용)
CREATE OR REPLACE PROCEDURE sp_params (
    p_proc_name IN  VARCHAR2,
    p_cursor    OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN p_cursor FOR
    SELECT POSITION AS "ordinal",
           ARGUMENT_NAME AS "name",
           DATA_TYPE AS "type",
           DATA_LENGTH AS "length"
    FROM USER_ARGUMENTS
    WHERE OBJECT_NAME = UPPER(p_proc_name)
    ORDER BY POSITION;
END;
/