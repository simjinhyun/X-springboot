-- 1. 이름으로 나이를 조회하는 함수
CREATE OR REPLACE FUNCTION sf_get_age(p_name IN VARCHAR2) RETURN NUMBER IS
    v_age NUMBER;
BEGIN
    SELECT c_age INTO v_age FROM t_address_book WHERE c_name = p_name AND ROWNUM = 1;
    RETURN v_age;
EXCEPTION
    WHEN NO_DATA_FOUND THEN RETURN NULL;
END;


-- 2. 이름으로 전화번호를 조회하는 함수
CREATE OR REPLACE FUNCTION sf_get_phone(p_name IN VARCHAR2) RETURN VARCHAR2 IS
    v_phone VARCHAR2(20);
BEGIN
    SELECT c_phone INTO v_phone FROM t_address_book WHERE c_name = p_name AND ROWNUM = 1;
    RETURN v_phone;
EXCEPTION
    WHEN NO_DATA_FOUND THEN RETURN NULL;
END;


-- 3. 이름으로 주소를 조회하는 함수
CREATE OR REPLACE FUNCTION sf_get_address(p_name IN VARCHAR2) RETURN VARCHAR2 IS
    v_address VARCHAR2(100);
BEGIN
    SELECT c_address INTO v_address FROM t_address_book WHERE c_name = p_name AND ROWNUM = 1;
    RETURN v_address;
EXCEPTION
    WHEN NO_DATA_FOUND THEN RETURN NULL;
END;


CREATE OR REPLACE FUNCTION ADMIN.sf_mis_notice(av_pgm_sys_cd VARCHAR2)
    RETURN tbl_mis_info
IS
    v_result tbl_mis_info := tbl_mis_info();
BEGIN
    FOR rec IN (
        SELECT c_id, c_name, c_phone 
        FROM t_address_book
        ORDER BY c_id
    )
    LOOP
        v_result.EXTEND;
        v_result(v_result.COUNT) := mis_info_obj(rec.c_id, rec.c_name, rec.c_phone);
    END LOOP;

    RETURN v_result;
EXCEPTION
    WHEN OTHERS THEN
        RETURN tbl_mis_info();
END;