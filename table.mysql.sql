-- CREATE DATABASE testdb 
-- DEFAULT CHARACTER SET utf8mb4 
-- COLLATE utf8mb4_0900_ai_ci;

USE testdb;

-- 주소록 테이블이 있으면 삭제 (테이블명 규칙 t_ 적용)
DROP TABLE IF EXISTS t_address_book;
CREATE TABLE t_address_book (
    c_id INT AUTO_INCREMENT PRIMARY KEY,
    c_name VARCHAR(50) NOT NULL,
    c_age INT NOT NULL,
    c_phone VARCHAR(20),
    c_address VARCHAR(100),
    c_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 샘플 데이터 삽입 (컬럼명 규칙 c_ 적용)
INSERT INTO t_address_book (c_name, c_age, c_phone, c_address) VALUES
('김철수', 32, '010-1234-5678', '서울특별시 강남구 역삼동'),
('이영희', 28, '010-2345-6789', '부산광역시 해운대구 우동'),
('박민수', 45, '010-3456-7890', '대구광역시 수성구 범어동'),
('최지현', 37, '010-4567-8901', '인천광역시 남동구 구월동'),
('정우성', 29, '010-5678-9012', '광주광역시 서구 치평동'),
('한지민', 41, '010-6789-0123', '대전광역시 유성구 봉명동'),
('오세훈', 35, '010-7890-1234', '울산광역시 남구 삼산동'),
('윤아름', 26, '010-8901-2345', '경기도 성남시 분당구 정자동'),
('강호동', 50, '010-9012-3456', '경상남도 창원시 의창구 팔용동'),
('배수지', 31, '010-0123-4567', '강원특별자치도 원주시 무실동');

-- 사용자 테이블이 있으면 삭제 (테이블명 규칙 t_ 적용)
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    c_id VARCHAR(50) PRIMARY KEY,
    c_pass VARCHAR(255) NOT NULL,
    c_role VARCHAR(255)
);

INSERT INTO t_user (c_id, c_pass, c_role)
VALUES (
    'admin',
    'Bs0RhQ8X3l72YJuIQeZpXA==:Zbw4+TU7JShKOWq14s1sZgEIVm7U7zy4DhQVTj75kA4=',
    'admin, manager'
);
