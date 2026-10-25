
CREATE DATABASE IF NOT EXISTS homestay
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE homestay;

CREATE TABLE IF NOT EXISTS tai_khoan_quan_ly (
    ma_quan_ly BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    ten_dang_nhap VARCHAR(50) NOT NULL,
    mat_khau_da_bam VARCHAR(60) NOT NULL,
    ho_ten VARCHAR(120) NOT NULL,
    dang_hoat_dong BOOLEAN NOT NULL DEFAULT TRUE,
    thoi_diem_mo_khoa DATETIME NULL,
    tao_luc DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cap_nhat_luc DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (ma_quan_ly),
    CONSTRAINT uq_admin_ten_dang_nhap UNIQUE (ten_dang_nhap)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ho_so_khach_hang (
    ma_khach_hang BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    ho_ten VARCHAR(120) NOT NULL,
    so_dien_thoai VARCHAR(20) NOT NULL,
    thu_dien_tu VARCHAR(254) NULL,
    so_giay_to VARCHAR(30) NULL,
    ghi_chu VARCHAR(1000) NULL,
    dang_hoat_dong BOOLEAN NOT NULL DEFAULT TRUE,
    tao_luc DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cap_nhat_luc DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (ma_khach_hang),
    CONSTRAINT uq_ho_so_khach_hang_thu_dien_tu UNIQUE (thu_dien_tu),
    INDEX idx_customer_so_dien_thoai (so_dien_thoai),
    INDEX idx_customer_so_giay_to (so_giay_to),
    INDEX idx_customer_name (ho_ten)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tai_khoan_khach_hang (
    ma_tai_khoan_khach_hang BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    ma_khach_hang BIGINT UNSIGNED NOT NULL,
    thu_dien_tu VARCHAR(254) NOT NULL,
    mat_khau_da_bam VARCHAR(60) NOT NULL,
    dang_hoat_dong BOOLEAN NOT NULL DEFAULT TRUE,
    thoi_diem_mo_khoa DATETIME NULL,
    tao_luc DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cap_nhat_luc DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (ma_tai_khoan_khach_hang),
    CONSTRAINT uq_tai_khoan_khach_hang_thu_dien_tu UNIQUE (thu_dien_tu),
    CONSTRAINT uq_account_profile UNIQUE (ma_khach_hang),
    CONSTRAINT fk_account_profile FOREIGN KEY (ma_khach_hang)
        REFERENCES ho_so_khach_hang (ma_khach_hang),
    INDEX idx_tai_khoan_khach_hang_lock (thoi_diem_mo_khoa)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS loai_phong (
    ma_loai_phong BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    ten VARCHAR(80) NOT NULL,
    suc_chua SMALLINT UNSIGNED NOT NULL,
    mo_ta TEXT NULL,
    gia_ngay_thuong DECIMAL(12,0) NOT NULL,
    gia_cuoi_tuan DECIMAL(12,0) NOT NULL,
    dang_hoat_dong BOOLEAN NOT NULL DEFAULT TRUE,
    tao_luc DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cap_nhat_luc DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (ma_loai_phong),
    CONSTRAINT uq_loai_phong_name UNIQUE (ten),
    CONSTRAINT chk_loai_phong_suc_chua CHECK (suc_chua > 0),
    CONSTRAINT chk_loai_phong_gia_ngay_thuong CHECK (gia_ngay_thuong > 0),
    CONSTRAINT chk_loai_phong_gia_cuoi_tuan CHECK (gia_cuoi_tuan > 0),
    INDEX idx_loai_phong_active (dang_hoat_dong)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS phong (
    ma_phong BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    so_phong VARCHAR(20) NOT NULL,
    tang SMALLINT NOT NULL,
    ma_loai_phong BIGINT UNSIGNED NOT NULL,
    mo_ta VARCHAR(1000) NULL,
    trang_thai ENUM('TRONG','DA_DAT','DANG_O','DANG_DON','BAO_TRI') NOT NULL DEFAULT 'TRONG',
    dang_hoat_dong BOOLEAN NOT NULL DEFAULT TRUE,
    tao_luc DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cap_nhat_luc DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (ma_phong),
    CONSTRAINT uq_so_phong UNIQUE (so_phong),
    CONSTRAINT fk_loai_phong FOREIGN KEY (ma_loai_phong) REFERENCES loai_phong (ma_loai_phong),
    INDEX idx_loai_phong_floor (ma_loai_phong, tang),
    INDEX idx_phong_trang_thai_active (trang_thai, dang_hoat_dong),
    INDEX idx_phong_floor (tang)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tien_nghi (
    ma_tien_nghi BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    ten VARCHAR(80) NOT NULL,
    dang_hoat_dong BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (ma_tien_nghi),
    CONSTRAINT uq_tien_nghi_name UNIQUE (ten)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS phong_tien_nghi (
    ma_phong BIGINT UNSIGNED NOT NULL,
    ma_tien_nghi BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (ma_phong, ma_tien_nghi),
    CONSTRAINT fk_phong_tien_nghi_phong FOREIGN KEY (ma_phong) REFERENCES phong (ma_phong),
    CONSTRAINT fk_phong_tien_nghi_tien_nghi FOREIGN KEY (ma_tien_nghi) REFERENCES tien_nghi (ma_tien_nghi)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS anh_phong (
    ma_anh_phong BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    ma_phong BIGINT UNSIGNED NOT NULL,
    duong_dan_anh VARCHAR(1000) NOT NULL,
    chu_thich VARCHAR(255) NULL,
    thu_tu_hien_thi SMALLINT UNSIGNED NOT NULL DEFAULT 0,
    dang_hoat_dong BOOLEAN NOT NULL DEFAULT TRUE,
    tao_luc DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (ma_anh_phong),
    CONSTRAINT fk_anh_phong_phong FOREIGN KEY (ma_phong) REFERENCES phong (ma_phong),
    INDEX idx_anh_phong_order (ma_phong, dang_hoat_dong, thu_tu_hien_thi)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS anh_loai_phong (
    ma_anh_loai_phong BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    ma_loai_phong BIGINT UNSIGNED NOT NULL,
    duong_dan_anh VARCHAR(1000) NOT NULL,
    chu_thich VARCHAR(255) NULL,
    thu_tu_hien_thi SMALLINT UNSIGNED NOT NULL DEFAULT 0,
    dang_hoat_dong BOOLEAN NOT NULL DEFAULT TRUE,
    tao_luc DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (ma_anh_loai_phong),
    CONSTRAINT fk_anh_loai_phong_loai FOREIGN KEY (ma_loai_phong) REFERENCES loai_phong (ma_loai_phong),
    INDEX idx_anh_loai_phong_order (ma_loai_phong, dang_hoat_dong, thu_tu_hien_thi)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS don_dat_phong (
    ma_don_dat_phong BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    ma_tra_cuu_don VARCHAR(24) NOT NULL,
    ma_khach_hang BIGINT UNSIGNED NOT NULL,
    ma_tai_khoan_khach_hang BIGINT UNSIGNED NULL,
    ma_quan_ly_tao BIGINT UNSIGNED NULL,
    nguon_tao ENUM('TRUC_TUYEN','QUAN_LY') NOT NULL,
    so_khach SMALLINT UNSIGNED NOT NULL DEFAULT 1,
    trang_thai ENUM('CHO_COC','HET_HAN','DA_DAT','DANG_O','HOAN_TAT','DA_HUY') NOT NULL DEFAULT 'DA_DAT',
    tien_coc_da_nhan DECIMAL(12,0) NOT NULL DEFAULT 0,
    tien_coc_yeu_cau DECIMAL(12,0) NOT NULL DEFAULT 0,
    han_giu_cho DATETIME(3) NULL,
    khach_bao_da_chuyen_luc DATETIME(3) NULL,
    ghi_chu VARCHAR(1000) NULL,
    tao_luc DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cap_nhat_luc DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (ma_don_dat_phong),
    CONSTRAINT uq_ma_tra_cuu_don UNIQUE (ma_tra_cuu_don),
    CONSTRAINT fk_don_dat_phong_profile FOREIGN KEY (ma_khach_hang)
        REFERENCES ho_so_khach_hang (ma_khach_hang),
    CONSTRAINT fk_don_dat_phong_account FOREIGN KEY (ma_tai_khoan_khach_hang)
        REFERENCES tai_khoan_khach_hang (ma_tai_khoan_khach_hang),
    CONSTRAINT fk_don_dat_phong_admin FOREIGN KEY (ma_quan_ly_tao)
        REFERENCES tai_khoan_quan_ly (ma_quan_ly),
    CONSTRAINT chk_don_dat_phong_deposit CHECK (tien_coc_da_nhan >= 0),
    CONSTRAINT chk_don_dat_phong_deposit_required CHECK (tien_coc_yeu_cau >= 0),
    CONSTRAINT chk_don_dat_phong_nguon_tao_actor CHECK (
        (nguon_tao = 'TRUC_TUYEN' AND ma_tai_khoan_khach_hang IS NOT NULL AND ma_quan_ly_tao IS NULL)
        OR (nguon_tao = 'QUAN_LY' AND ma_tai_khoan_khach_hang IS NULL AND ma_quan_ly_tao IS NOT NULL)
    ),
    CONSTRAINT chk_don_dat_phong_so_khach CHECK (so_khach > 0),
    INDEX idx_don_dat_phong_trang_thai_created (trang_thai, tao_luc),
    INDEX idx_don_dat_phong_ho_so_khach_hang (ma_khach_hang, tao_luc),
    INDEX idx_don_dat_phong_tai_khoan_khach_hang (ma_tai_khoan_khach_hang, trang_thai),
    INDEX idx_don_dat_phong_created_admin (ma_quan_ly_tao)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS chi_tiet_dat_phong (
    ma_chi_tiet_dat_phong BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    ma_don_dat_phong BIGINT UNSIGNED NOT NULL,
    ma_phong BIGINT UNSIGNED NOT NULL,
    ngay_nhan DATE NOT NULL,
    ngay_tra DATE NOT NULL,
    gia_ngay_thuong_luc_dat DECIMAL(12,0) NOT NULL,
    gia_cuoi_tuan_luc_dat DECIMAL(12,0) NOT NULL,
    tien_phong DECIMAL(12,0) NOT NULL,
    trang_thai ENUM('DA_DAT','DANG_O','DA_TRA','DA_HUY') NOT NULL DEFAULT 'DA_DAT',
    thoi_diem_nhan_thuc_te DATETIME NULL,
    thoi_diem_tra_thuc_te DATETIME NULL,
    huy_luc DATETIME NULL,
    ly_do_huy VARCHAR(1000) NULL,
    tao_luc DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cap_nhat_luc DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (ma_chi_tiet_dat_phong),
    CONSTRAINT fk_detail_don_dat_phong FOREIGN KEY (ma_don_dat_phong) REFERENCES don_dat_phong (ma_don_dat_phong),
    CONSTRAINT fk_detail_phong FOREIGN KEY (ma_phong) REFERENCES phong (ma_phong),
    CONSTRAINT chk_detail_dates CHECK (ngay_tra > ngay_nhan),
    CONSTRAINT chk_detail_weekday_rate CHECK (gia_ngay_thuong_luc_dat > 0),
    CONSTRAINT chk_detail_weekend_rate CHECK (gia_cuoi_tuan_luc_dat > 0),
    CONSTRAINT chk_detail_tien_phong CHECK (tien_phong >= 0),
    INDEX idx_detail_phong_dates_trang_thai (ma_phong, ngay_nhan, ngay_tra, trang_thai),
    INDEX idx_detail_don_dat_phong_trang_thai (ma_don_dat_phong, trang_thai),
    INDEX idx_detail_dates (ngay_nhan, ngay_tra)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS gia_tung_dem_dat_phong (
    ma_gia_tung_dem BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    ma_chi_tiet_dat_phong BIGINT UNSIGNED NOT NULL,
    ngay_luu_tru DATE NOT NULL,
    gia_dem DECIMAL(12,0) NOT NULL,
    PRIMARY KEY (ma_gia_tung_dem),
    CONSTRAINT uq_gia_tung_dem_dat_phong UNIQUE (ma_chi_tiet_dat_phong, ngay_luu_tru),
    CONSTRAINT fk_gia_tung_dem_dat_phong_detail FOREIGN KEY (ma_chi_tiet_dat_phong)
        REFERENCES chi_tiet_dat_phong (ma_chi_tiet_dat_phong),
    CONSTRAINT chk_gia_tung_dem_dat_phong_rate CHECK (gia_dem > 0),
    INDEX idx_gia_tung_dem_dat_phong_date (ngay_luu_tru)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS hoa_don (
    ma_hoa_don BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    ma_don_dat_phong BIGINT UNSIGNED NOT NULL,
    so_hoa_don VARCHAR(30) NOT NULL,
    lap_luc DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tien_phong DECIMAL(12,0) NOT NULL,
    tien_coc_duoc_tru DECIMAL(12,0) NOT NULL DEFAULT 0,
    so_tien_con_phai_thu DECIMAL(12,0) NOT NULL,
    trang_thai ENUM('CHUA_THANH_TOAN','DA_THANH_TOAN','DA_HUY') NOT NULL DEFAULT 'CHUA_THANH_TOAN',
    chot_luc DATETIME NULL,
    PRIMARY KEY (ma_hoa_don),
    CONSTRAINT uq_hoa_don_don_dat_phong UNIQUE (ma_don_dat_phong),
    CONSTRAINT uq_so_hoa_don UNIQUE (so_hoa_don),
    CONSTRAINT fk_hoa_don_don_dat_phong FOREIGN KEY (ma_don_dat_phong) REFERENCES don_dat_phong (ma_don_dat_phong),
    CONSTRAINT chk_hoa_don_tien_phong CHECK (tien_phong >= 0),
    CONSTRAINT chk_hoa_don_deposit CHECK (tien_coc_duoc_tru >= 0),
    CONSTRAINT chk_hoa_don_due CHECK (so_tien_con_phai_thu >= 0),
    INDEX idx_hoa_don_issued_trang_thai (lap_luc, trang_thai)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS thanh_toan (
    ma_thanh_toan BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    ma_don_dat_phong BIGINT UNSIGNED NOT NULL,
    ma_hoa_don BIGINT UNSIGNED NULL,
    loai_thanh_toan ENUM('TIEN_COC','THANH_TOAN','HOAN_COC') NOT NULL,
    phuong_thuc ENUM('TIEN_MAT','CHUYEN_KHOAN','VI_DIEN_TU') NOT NULL,
    so_tien DECIMAL(12,0) NOT NULL,
    ma_giao_dich VARCHAR(100) NULL,
    ghi_chu VARCHAR(500) NULL,
    ma_quan_ly_ghi_nhan BIGINT UNSIGNED NULL,
    trang_thai_hoan_coc ENUM('CHO_XU_LY','DA_XAC_NHAN') NULL,
    so_tien_thuc_hoan DECIMAL(12,0) NULL,
    phuong_thuc_hoan ENUM('TIEN_MAT','CHUYEN_KHOAN','VI_DIEN_TU') NULL,
    tao_luc DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (ma_thanh_toan),
    CONSTRAINT fk_thanh_toan_don_dat_phong FOREIGN KEY (ma_don_dat_phong) REFERENCES don_dat_phong (ma_don_dat_phong),
    CONSTRAINT fk_thanh_toan_hoa_don FOREIGN KEY (ma_hoa_don) REFERENCES hoa_don (ma_hoa_don),
    CONSTRAINT fk_thanh_toan_admin FOREIGN KEY (ma_quan_ly_ghi_nhan) REFERENCES tai_khoan_quan_ly (ma_quan_ly),
    CONSTRAINT chk_thanh_toan_so_tien CHECK (so_tien > 0),
    INDEX idx_thanh_toan_don_dat_phong_type (ma_don_dat_phong, loai_thanh_toan, tao_luc),
    INDEX idx_thanh_toan_hoa_don (ma_hoa_don, tao_luc),
    INDEX idx_thanh_toan_hoan_coc (loai_thanh_toan, trang_thai_hoan_coc, tao_luc)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS chinh_sach_hoan_huy (
    ma_chinh_sach_hoan_huy BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    ten VARCHAR(120) NOT NULL,
    so_ngay_truoc_toi_thieu SMALLINT UNSIGNED NOT NULL,
    so_ngay_truoc_toi_da SMALLINT UNSIGNED NULL,
    phan_tram_hoan DECIMAL(5,2) NOT NULL,
    dang_hoat_dong BOOLEAN NOT NULL DEFAULT TRUE,
    ma_quan_ly_tao BIGINT UNSIGNED NULL,
    tao_luc DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (ma_chinh_sach_hoan_huy),
    CONSTRAINT fk_policy_admin FOREIGN KEY (ma_quan_ly_tao) REFERENCES tai_khoan_quan_ly (ma_quan_ly),
    CONSTRAINT chk_policy_refund CHECK (phan_tram_hoan >= 0 AND phan_tram_hoan <= 100),
    INDEX idx_policy_active (dang_hoat_dong, so_ngay_truoc_toi_thieu)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS nhat_ky_thao_tac (
    ma_nhat_ky_thao_tac BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    loai_nguoi_thao_tac ENUM('QUAN_LY','KHACH_HANG','HE_THONG') NOT NULL,
    ma_quan_ly BIGINT UNSIGNED NULL,
    ma_tai_khoan_khach_hang BIGINT UNSIGNED NULL,
    hanh_dong VARCHAR(80) NOT NULL,
    ma_don_dat_phong BIGINT UNSIGNED NULL,
    mo_ta VARCHAR(2000) NULL,
    tao_luc DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (ma_nhat_ky_thao_tac),
    CONSTRAINT fk_audit_admin FOREIGN KEY (ma_quan_ly) REFERENCES tai_khoan_quan_ly (ma_quan_ly),
    CONSTRAINT fk_audit_customer FOREIGN KEY (ma_tai_khoan_khach_hang) REFERENCES tai_khoan_khach_hang (ma_tai_khoan_khach_hang),
    CONSTRAINT fk_audit_don_dat_phong FOREIGN KEY (ma_don_dat_phong) REFERENCES don_dat_phong (ma_don_dat_phong),
    INDEX idx_audit_don_dat_phong_time (ma_don_dat_phong, tao_luc),
    INDEX idx_audit_actor_time (loai_nguoi_thao_tac, tao_luc),
    INDEX idx_audit_hanh_dong_time (hanh_dong, tao_luc)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Migration lặp an toàn cho cơ sở dữ liệu đã được tạo trước khi có luồng đặt cọc.
SET @lenh_coc = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='don_dat_phong' AND column_name='tien_coc_yeu_cau')=0,
    'ALTER TABLE don_dat_phong ADD COLUMN tien_coc_yeu_cau DECIMAL(12,0) NOT NULL DEFAULT 0 AFTER tien_coc_da_nhan', 'SELECT 1');
PREPARE lenh_coc FROM @lenh_coc; EXECUTE lenh_coc; DEALLOCATE PREPARE lenh_coc;
SET @lenh_coc = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='don_dat_phong' AND column_name='han_giu_cho')=0,
    'ALTER TABLE don_dat_phong ADD COLUMN han_giu_cho DATETIME(3) NULL AFTER tien_coc_yeu_cau', 'SELECT 1');
PREPARE lenh_coc FROM @lenh_coc; EXECUTE lenh_coc; DEALLOCATE PREPARE lenh_coc;
SET @lenh_coc = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='don_dat_phong' AND column_name='khach_bao_da_chuyen_luc')=0,
    'ALTER TABLE don_dat_phong ADD COLUMN khach_bao_da_chuyen_luc DATETIME(3) NULL AFTER han_giu_cho', 'SELECT 1');
PREPARE lenh_coc FROM @lenh_coc; EXECUTE lenh_coc; DEALLOCATE PREPARE lenh_coc;
ALTER TABLE don_dat_phong MODIFY COLUMN trang_thai ENUM('CHO_COC','HET_HAN','DA_DAT','DANG_O','HOAN_TAT','DA_HUY') NOT NULL DEFAULT 'DA_DAT';

CREATE TABLE IF NOT EXISTS lan_dang_nhap (
    ma_lan_dang_nhap BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    loai_tai_khoan ENUM('QUAN_LY','KHACH_HANG') NOT NULL,
    ma_quan_ly BIGINT UNSIGNED NULL,
    ma_tai_khoan_khach_hang BIGINT UNSIGNED NULL,
    dinh_danh_da_nhap VARCHAR(254) NOT NULL,
    dang_nhap_thanh_cong BOOLEAN NOT NULL DEFAULT FALSE,
    dia_chi_ip VARCHAR(45) NULL,
    thoi_diem_thu DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (ma_lan_dang_nhap),
    CONSTRAINT fk_attempt_admin FOREIGN KEY (ma_quan_ly) REFERENCES tai_khoan_quan_ly (ma_quan_ly),
    CONSTRAINT fk_attempt_customer FOREIGN KEY (ma_tai_khoan_khach_hang) REFERENCES tai_khoan_khach_hang (ma_tai_khoan_khach_hang),
    INDEX idx_attempt_identifier_time (loai_tai_khoan, dinh_danh_da_nhap, thoi_diem_thu),
    INDEX idx_attempt_admin_time (ma_quan_ly, thoi_diem_thu),
    INDEX idx_attempt_customer_time (ma_tai_khoan_khach_hang, thoi_diem_thu)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


SET @lenh_tao_index = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = DATABASE() AND table_name = 'hoa_don'
       AND index_name = 'idx_hoa_don_ngay_lap_trang_thai_don') = 0,
    'CREATE INDEX idx_hoa_don_ngay_lap_trang_thai_don ON hoa_don (lap_luc, trang_thai, ma_don_dat_phong)',
    'SELECT 1'
);
PREPARE lenh_index FROM @lenh_tao_index;
EXECUTE lenh_index;
DEALLOCATE PREPARE lenh_index;

SET @lenh_tao_index = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = DATABASE() AND table_name = 'chi_tiet_dat_phong'
       AND index_name = 'idx_chi_tiet_phong_ngay_trang_thai_don') = 0,
    'CREATE INDEX idx_chi_tiet_phong_ngay_trang_thai_don ON chi_tiet_dat_phong (ma_phong, ngay_nhan, ngay_tra, trang_thai, ma_don_dat_phong)',
    'SELECT 1'
);
PREPARE lenh_index FROM @lenh_tao_index;
EXECUTE lenh_index;
DEALLOCATE PREPARE lenh_index;

SET @lenh_tao_index = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = DATABASE() AND table_name = 'chi_tiet_dat_phong'
       AND index_name = 'idx_chi_tiet_trang_thai_ngay_don') = 0,
    'CREATE INDEX idx_chi_tiet_trang_thai_ngay_don ON chi_tiet_dat_phong (trang_thai, ngay_nhan, ngay_tra, ma_don_dat_phong)',
    'SELECT 1'
);
PREPARE lenh_index FROM @lenh_tao_index;
EXECUTE lenh_index;
DEALLOCATE PREPARE lenh_index;

SET @lenh_tao_index = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = DATABASE() AND table_name = 'don_dat_phong'
       AND index_name = 'idx_don_trang_thai_khach') = 0,
    'CREATE INDEX idx_don_trang_thai_khach ON don_dat_phong (trang_thai, ma_khach_hang, ma_don_dat_phong)',
    'SELECT 1'
);
PREPARE lenh_index FROM @lenh_tao_index;
EXECUTE lenh_index;
DEALLOCATE PREPARE lenh_index;
INSERT IGNORE INTO tai_khoan_quan_ly (ma_quan_ly, ten_dang_nhap, mat_khau_da_bam, ho_ten)
VALUES (1, 'admin', '$2a$10$ObVCwOxP8OhkXqE95cB6j.H5YdiFR623UIh6I4x5R7/uIDoKsOSl.', 'Quản trị viên');

INSERT IGNORE INTO ho_so_khach_hang
    (ma_khach_hang, ho_ten, so_dien_thoai, thu_dien_tu, so_giay_to, ghi_chu)
VALUES
    (1, 'Nguyễn Minh An', '0901000001', 'an.nguyen@example.com', '079201000001', 'Tài khoản khách'),
    (2, 'Trần Gia Bình', '0901000002', 'binh.tran@example.com', '079201000002', 'Tài khoản khách'),
    (3, 'Lê Thu Hà', '0901000003', 'ha.le@example.com', '079201000003', 'Hồ sơ do quản lý tạo, chưa có tài khoản');

INSERT IGNORE INTO tai_khoan_khach_hang
    (ma_tai_khoan_khach_hang, ma_khach_hang, thu_dien_tu, mat_khau_da_bam)
VALUES
    (1, 1, 'an.nguyen@example.com', '$2a$10$cJ4CtUD2dHj5UfKeGlDQo.59HpTPJtOKavgtdIoC5yOZ/cVdz4pPC'),
    (2, 2, 'binh.tran@example.com', '$2a$10$cJ4CtUD2dHj5UfKeGlDQo.59HpTPJtOKavgtdIoC5yOZ/cVdz4pPC');

INSERT IGNORE INTO loai_phong
    (ma_loai_phong, ten, suc_chua, mo_ta, gia_ngay_thuong, gia_cuoi_tuan)
VALUES
    (1, 'Phòng đơn', 1, 'Phòng ấm cúng cho một khách.', 500000, 650000),
    (2, 'Phòng đôi', 2, 'Phòng có giường đôi, phù hợp hai khách.', 750000, 950000),
    (3, 'Phòng gia đình', 4, 'Phòng rộng cho gia đình hoặc nhóm nhỏ.', 1200000, 1500000),
    (4, 'Phòng dorm', 6, 'Phòng tập thể, sức chứa tối đa sáu khách.', 300000, 400000);

INSERT IGNORE INTO phong
    (ma_phong, so_phong, tang, ma_loai_phong, mo_ta, trang_thai)
VALUES
    (1, '101', 1, 1, 'Phòng đơn tầng 1.', 'TRONG'),
    (2, '102', 1, 2, 'Phòng đôi tầng 1.', 'TRONG'),
    (3, '103', 1, 2, 'Phòng đôi tầng 1, gần ban công.', 'TRONG'),
    (4, '104', 1, 3, 'Phòng gia đình tầng 1.', 'TRONG'),
    (5, '201', 2, 1, 'Phòng đơn tầng 2.', 'TRONG'),
    (6, '202', 2, 2, 'Phòng đôi tầng 2.', 'TRONG'),
    (7, '203', 2, 3, 'Phòng gia đình tầng 2.', 'TRONG'),
    (8, '204', 2, 4, 'Phòng dorm tầng 2.', 'TRONG'),
    (9, '301', 3, 2, 'Phòng đôi tầng 3.', 'TRONG'),
    (10, '302', 3, 3, 'Phòng gia đình tầng 3.', 'TRONG');

INSERT IGNORE INTO tien_nghi (ma_tien_nghi, ten)
VALUES (1, 'Wi-Fi'), (2, 'Điều hòa'), (3, 'Máy sấy tóc'), (4, 'Ấm đun nước');

INSERT IGNORE INTO phong_tien_nghi (ma_phong, ma_tien_nghi)
VALUES (1,1),(1,2),(1,3),(2,1),(2,2),(2,4),(4,1),(4,2),(4,3),(4,4);

INSERT IGNORE INTO anh_phong (ma_anh_phong, ma_phong, duong_dan_anh, chu_thich, thu_tu_hien_thi)
VALUES
    (1, 1, '/images/phong/101-main.jpg', 'Ảnh minh họa phòng 101', 1),
    (2, 2, '/images/phong/102-main.jpg', 'Ảnh minh họa phòng 102', 1),
    (3, 4, '/images/phong/104-main.jpg', 'Ảnh minh họa phòng 104', 1);

INSERT IGNORE INTO don_dat_phong
    (ma_don_dat_phong, ma_tra_cuu_don, ma_khach_hang, ma_tai_khoan_khach_hang,
     ma_quan_ly_tao, nguon_tao, so_khach, trang_thai, tien_coc_da_nhan, ghi_chu, tao_luc)
VALUES
    (1, 'HS-2026-0001', 1, 1, NULL, 'TRUC_TUYEN', 1, 'DA_DAT', 0,
     'Đơn mẫu do khách tự đặt.', '2026-10-10 10:00:00'),
    (2, 'HS-2026-0002', 3, NULL, 1, 'QUAN_LY', 2, 'HOAN_TAT', 200000,
     'Đơn mẫu do quản lý tạo cho khách chưa có tài khoản.', '2026-09-01 09:00:00');

INSERT IGNORE INTO chi_tiet_dat_phong
    (ma_chi_tiet_dat_phong, ma_don_dat_phong, ma_phong, ngay_nhan, ngay_tra,
     gia_ngay_thuong_luc_dat, gia_cuoi_tuan_luc_dat, tien_phong, trang_thai,
     thoi_diem_nhan_thuc_te, thoi_diem_tra_thuc_te)
VALUES
    (1, 1, 2, '2026-11-10', '2026-11-12', 750000, 950000, 1500000, 'DA_DAT', NULL, NULL),
    (2, 2, 4, '2026-09-08', '2026-09-10', 1200000, 1500000, 2400000,
     'DA_TRA', '2026-09-08 15:00:00', '2026-09-10 11:00:00');

INSERT IGNORE INTO gia_tung_dem_dat_phong (ma_gia_tung_dem, ma_chi_tiet_dat_phong, ngay_luu_tru, gia_dem)
VALUES
    (1, 1, '2026-11-10', 750000), (2, 1, '2026-11-11', 750000),
    (3, 2, '2026-09-08', 1200000), (4, 2, '2026-09-09', 1200000);

INSERT IGNORE INTO hoa_don
    (ma_hoa_don, ma_don_dat_phong, so_hoa_don, lap_luc, tien_phong,
     tien_coc_duoc_tru, so_tien_con_phai_thu, trang_thai, chot_luc)
VALUES
    (1, 2, 'INV-HS-2026-0002', '2026-09-10 11:00:00', 2400000,
     200000, 2200000, 'DA_THANH_TOAN', '2026-09-10 11:30:00');

INSERT IGNORE INTO thanh_toan
    (ma_thanh_toan, ma_don_dat_phong, ma_hoa_don, loai_thanh_toan, phuong_thuc, so_tien,
     ma_giao_dich, ghi_chu, ma_quan_ly_ghi_nhan, tao_luc)
VALUES
    (1, 2, NULL, 'TIEN_COC', 'TIEN_MAT', 200000, NULL, 'Tiền cọc mẫu', 1, '2026-09-01 09:15:00'),
    (2, 2, 1, 'THANH_TOAN', 'CHUYEN_KHOAN', 2200000, 'DEMO-TRANSFER-0002',
     'Thanh toán phần còn lại', 1, '2026-09-10 11:30:00');

-- Ba bậc chính sách đang hoạt động: hoàn 100% từ 3 ngày, 50% trước 1–2 ngày, còn lại 0%.
INSERT IGNORE INTO chinh_sach_hoan_huy
    (ma_chinh_sach_hoan_huy, ten, so_ngay_truoc_toi_thieu, so_ngay_truoc_toi_da, phan_tram_hoan, ma_quan_ly_tao)
VALUES
    (1, 'Hủy trước từ 3 ngày', 3, NULL, 100.00, 1),
    (2, 'Hủy trước 1 đến 2 ngày', 1, 2, 50.00, 1),
    (3, 'Hủy trong ngày hoặc quá hạn', 0, 0, 0.00, 1);

INSERT IGNORE INTO nhat_ky_thao_tac
    (ma_nhat_ky_thao_tac, loai_nguoi_thao_tac, ma_quan_ly, ma_tai_khoan_khach_hang, hanh_dong, ma_don_dat_phong, mo_ta, tao_luc)
VALUES
    (1, 'KHACH_HANG', NULL, 1, 'TAO_DON_DAT_PHONG', 1, 'Khách tạo đơn mẫu.', '2026-10-10 10:00:00'),
    (2, 'QUAN_LY', 1, NULL, 'TAO_DON_DAT_PHONG', 2, 'Quản lý tạo đơn cho hồ sơ chưa có tài khoản.', '2026-09-01 09:00:00');

INSERT IGNORE INTO ho_so_khach_hang
    (ma_khach_hang, ho_ten, so_dien_thoai, thu_dien_tu, so_giay_to, ghi_chu)
VALUES
    (4, 'Chi Le', '0900000004', 'chi.le@example.com', NULL, 'Tài khoản khách mẫu Giai đoạn 5'),
    (5, 'Dung Pham', '0900000005', 'dung.pham@example.com', NULL, 'Tài khoản khách mẫu Giai đoạn 5');

INSERT IGNORE INTO tai_khoan_khach_hang
    (ma_tai_khoan_khach_hang, ma_khach_hang, thu_dien_tu, mat_khau_da_bam)
VALUES
    (3, 4, 'chi.le@example.com', '$2a$10$cJ4CtUD2dHj5UfKeGlDQo.59HpTPJtOKavgtdIoC5yOZ/cVdz4pPC'),
    (4, 5, 'dung.pham@example.com', '$2a$10$cJ4CtUD2dHj5UfKeGlDQo.59HpTPJtOKavgtdIoC5yOZ/cVdz4pPC');

-- Mẫu đủ bốn trạng thái đơn. Ngày đơn đang ở dùng ngày hiện tại để có thể kiểm tra thao tác check-in/check-out.
INSERT IGNORE INTO don_dat_phong
    (ma_don_dat_phong, ma_tra_cuu_don, ma_khach_hang, ma_tai_khoan_khach_hang,
     nguon_tao, so_khach, trang_thai, tien_coc_da_nhan, ghi_chu, tao_luc)
VALUES
    (3, 'HS-GD5-DANG-O', 4, 3, 'TRUC_TUYEN', 1, 'DANG_O', 0, 'Mẫu trạng thái đang ở', NOW() - INTERVAL 2 DAY),
    (4, 'HS-GD5-DA-HUY', 5, 4, 'TRUC_TUYEN', 2, 'DA_HUY', 300000, 'Mẫu đã hủy có yêu cầu hoàn cọc chờ xử lý', NOW() - INTERVAL 20 DAY),
    (5, 'HS-GD5-DA-DAT', 5, 4, 'TRUC_TUYEN', 1, 'DA_DAT', 0, 'Mẫu trạng thái đã đặt', NOW()),
    (6, 'HS-GD5-HOAN-TAT', 4, 3, 'TRUC_TUYEN', 2, 'HOAN_TAT', 100000, 'Mẫu lịch sử hoàn tất', NOW() - INTERVAL 40 DAY);

INSERT IGNORE INTO chi_tiet_dat_phong
    (ma_chi_tiet_dat_phong, ma_don_dat_phong, ma_phong, ngay_nhan, ngay_tra,
     gia_ngay_thuong_luc_dat, gia_cuoi_tuan_luc_dat, tien_phong, trang_thai,
     thoi_diem_nhan_thuc_te)
VALUES
    (3, 3, 5, CURDATE() - INTERVAL 2 DAY, CURDATE() + INTERVAL 1 DAY,
     500000, 650000,
     IF(DAYOFWEEK(CURDATE() - INTERVAL 2 DAY) IN (6,7),650000,500000)
       + IF(DAYOFWEEK(CURDATE() - INTERVAL 1 DAY) IN (6,7),650000,500000)
       + IF(DAYOFWEEK(CURDATE()) IN (6,7),650000,500000),
     'DANG_O', NOW() - INTERVAL 2 DAY);

UPDATE phong SET trang_thai = 'DANG_O' WHERE ma_phong = 5;

INSERT IGNORE INTO chi_tiet_dat_phong
    (ma_chi_tiet_dat_phong, ma_don_dat_phong, ma_phong, ngay_nhan, ngay_tra,
     gia_ngay_thuong_luc_dat, gia_cuoi_tuan_luc_dat, tien_phong, trang_thai, ly_do_huy, huy_luc)
VALUES
    (4, 4, 6, CURDATE() + INTERVAL 3 DAY, CURDATE() + INTERVAL 5 DAY,
     750000, 950000, 1500000, 'DA_HUY', 'Đơn mẫu đã hủy', NOW());

INSERT IGNORE INTO chi_tiet_dat_phong
    (ma_chi_tiet_dat_phong, ma_don_dat_phong, ma_phong, ngay_nhan, ngay_tra,
     gia_ngay_thuong_luc_dat, gia_cuoi_tuan_luc_dat, tien_phong, trang_thai)
VALUES
    (5, 5, 7, CURDATE() + INTERVAL 20 DAY, CURDATE() + INTERVAL 22 DAY,
     1200000, 1500000, 2400000, 'DA_DAT'),
    (6, 6, 1, CURDATE() - INTERVAL 40 DAY, CURDATE() - INTERVAL 38 DAY,
     500000, 650000, 1000000, 'DA_TRA');

INSERT IGNORE INTO hoa_don
    (ma_hoa_don, ma_don_dat_phong, so_hoa_don, lap_luc, tien_phong,
     tien_coc_duoc_tru, so_tien_con_phai_thu, trang_thai, chot_luc)
VALUES
    (2, 6, 'INV-HS-GD5-HOAN-TAT', NOW() - INTERVAL 38 DAY,
     1000000, 100000, 0, 'DA_THANH_TOAN', NOW() - INTERVAL 38 DAY);

INSERT IGNORE INTO thanh_toan
    (ma_thanh_toan, ma_don_dat_phong, ma_hoa_don, loai_thanh_toan, phuong_thuc,
     so_tien, ghi_chu, ma_quan_ly_ghi_nhan, tao_luc)
VALUES
    (3, 6, NULL, 'TIEN_COC', 'TIEN_MAT', 100000, 'Tiền cọc mẫu Giai đoạn 5', NULL, NOW() - INTERVAL 40 DAY),
    (4, 6, 2, 'THANH_TOAN', 'TIEN_MAT', 900000, 'Thanh toán mẫu Giai đoạn 5', NULL, NOW() - INTERVAL 38 DAY),
    (5, 4, NULL, 'TIEN_COC', 'CHUYEN_KHOAN', 300000, 'Tiền cọc của đơn mẫu đã hủy', NULL, NOW() - INTERVAL 20 DAY);

INSERT IGNORE INTO thanh_toan
    (ma_thanh_toan, ma_don_dat_phong, ma_hoa_don, loai_thanh_toan, phuong_thuc,
     so_tien, ghi_chu, ma_quan_ly_ghi_nhan, trang_thai_hoan_coc, tao_luc)
VALUES
    (6, 4, NULL, 'HOAN_COC', 'CHUYEN_KHOAN', 300000,
     'Yêu cầu hoàn cọc mẫu, chờ Quản lý xác nhận', NULL, 'CHO_XU_LY', NOW());

-- Bộ đơn mẫu mở rộng: đặt mới, nhiều phòng/trả từng phòng, hóa đơn còn nợ,
-- đơn do Quản lý tạo cho hồ sơ không có tài khoản, hủy một phần và lịch nối ngày.
-- Các mã tra cứu cố định cùng NOT EXISTS giúp nạp lại script không nhân đôi đơn.
INSERT IGNORE INTO don_dat_phong
    (ma_tra_cuu_don, ma_khach_hang, ma_tai_khoan_khach_hang, ma_quan_ly_tao,
     nguon_tao, so_khach, trang_thai, tien_coc_da_nhan, ghi_chu, tao_luc)
SELECT s.ma_tra_cuu_don, k.ma_khach_hang, s.ma_tai_khoan, s.ma_quan_ly,
       s.nguon_tao, s.so_khach, s.trang_thai, s.tien_coc, s.ghi_chu, s.tao_luc
FROM (
    SELECT 'HS-MAU-NHIEU-PHONG' ma_tra_cuu_don, 4 ma_khach_hang, 3 ma_tai_khoan,
           NULL ma_quan_ly, 'TRUC_TUYEN' nguon_tao, 2 so_khach, 'DANG_O' trang_thai,
           100000 tien_coc, 'Đơn nhiều phòng: một phòng đã trả, một phòng đang ở.' ghi_chu,
           NOW() - INTERVAL 4 DAY tao_luc
    UNION ALL
    SELECT 'HS-MAU-HOA-DON-CHO-THU', 5, 4, NULL, 'TRUC_TUYEN', 3, 'DANG_O',
           200000, 'Phòng cuối đã tính tiền, hóa đơn còn khoản phải thu.', NOW() - INTERVAL 2 DAY
    UNION ALL
    SELECT 'HS-MAU-QL-CHUA-TK', 3, NULL, 1, 'QUAN_LY', 2, 'DA_DAT',
           100000, 'Đơn Quản lý tạo cho hồ sơ chưa có tài khoản đăng nhập.', NOW()
    UNION ALL
    SELECT 'HS-MAU-HUY-DA-HOAN-COC', 2, 2, NULL, 'TRUC_TUYEN', 1, 'DA_HUY',
           200000, 'Đơn đã hủy, khoản hoàn cọc đã được xác nhận.', NOW() - INTERVAL 8 DAY
    UNION ALL
    SELECT 'HS-MAU-HUY-KHONG-COC', 1, 1, NULL, 'TRUC_TUYEN', 1, 'DA_HUY',
           0, 'Đơn đã hủy không phát sinh hoàn cọc.', NOW() - INTERVAL 6 DAY
    UNION ALL
    SELECT 'HS-MAU-HUY-MOT-PHONG', 1, 1, NULL, 'TRUC_TUYEN', 2, 'DA_DAT',
           0, 'Đơn có một phòng đã hủy và một phòng vẫn được đặt.', NOW()
    UNION ALL
    SELECT 'HS-MAU-NOI-TIEP-1', 2, 2, NULL, 'TRUC_TUYEN', 2, 'DA_DAT',
           0, 'Phòng 202: đặt đến ngày trả.', NOW()
    UNION ALL
    SELECT 'HS-MAU-NOI-TIEP-2', 1, 1, NULL, 'TRUC_TUYEN', 1, 'DA_DAT',
           0, 'Phòng 202: nhận đúng ngày khách trước trả phòng.', NOW()
) s
JOIN ho_so_khach_hang k ON k.ma_khach_hang = s.ma_khach_hang
WHERE NOT EXISTS (
    SELECT 1 FROM don_dat_phong d WHERE d.ma_tra_cuu_don = s.ma_tra_cuu_don
);

-- Chi tiết đơn nhiều phòng: phòng 204 đã trả; phòng 301 vẫn đang ở.
INSERT IGNORE INTO chi_tiet_dat_phong
    (ma_don_dat_phong, ma_phong, ngay_nhan, ngay_tra,
     gia_ngay_thuong_luc_dat, gia_cuoi_tuan_luc_dat, tien_phong, trang_thai,
     thoi_diem_nhan_thuc_te, thoi_diem_tra_thuc_te)
SELECT d.ma_don_dat_phong, p.ma_phong, s.ngay_nhan, s.ngay_tra,
       s.gia_thuong, s.gia_cuoi_tuan, s.tien_phong, s.trang_thai,
       s.nhan_thuc_te, s.tra_thuc_te
FROM (
    SELECT 'HS-MAU-NHIEU-PHONG' ma_tra_cuu_don, '204' so_phong,
           CURDATE() - INTERVAL 4 DAY ngay_nhan, CURDATE() - INTERVAL 2 DAY ngay_tra,
           300000 gia_thuong, 400000 gia_cuoi_tuan,
           (IF(DAYOFWEEK(CURDATE() - INTERVAL 4 DAY) IN (6,7),400000,300000)
            + IF(DAYOFWEEK(CURDATE() - INTERVAL 3 DAY) IN (6,7),400000,300000)) tien_phong, 'DA_TRA' trang_thai,
           NOW() - INTERVAL 4 DAY nhan_thuc_te, NOW() - INTERVAL 2 DAY tra_thuc_te
    UNION ALL
    SELECT 'HS-MAU-NHIEU-PHONG', '301', CURDATE() - INTERVAL 4 DAY,
           CURDATE() + INTERVAL 1 DAY, 750000, 950000,
           (IF(DAYOFWEEK(CURDATE() - INTERVAL 4 DAY) IN (6,7),950000,750000)
            + IF(DAYOFWEEK(CURDATE() - INTERVAL 3 DAY) IN (6,7),950000,750000)
            + IF(DAYOFWEEK(CURDATE() - INTERVAL 2 DAY) IN (6,7),950000,750000)
            + IF(DAYOFWEEK(CURDATE() - INTERVAL 1 DAY) IN (6,7),950000,750000)
            + IF(DAYOFWEEK(CURDATE()) IN (6,7),950000,750000)), 'DANG_O',
           NOW() - INTERVAL 4 DAY, NULL
    UNION ALL
    SELECT 'HS-MAU-HOA-DON-CHO-THU', '302', CURDATE() - INTERVAL 1 DAY,
           CURDATE() + INTERVAL 1 DAY, 1200000, 1500000,
           (IF(DAYOFWEEK(CURDATE() - INTERVAL 1 DAY) IN (6,7),1500000,1200000)
            + IF(DAYOFWEEK(CURDATE()) IN (6,7),1500000,1200000)), 'DANG_O',
           NOW() - INTERVAL 1 DAY, NULL
    UNION ALL
    SELECT 'HS-MAU-QL-CHUA-TK', '103', CURDATE() + INTERVAL 10 DAY,
           CURDATE() + INTERVAL 12 DAY, 750000, 950000,
           (IF(DAYOFWEEK(CURDATE() + INTERVAL 10 DAY) IN (6,7),950000,750000)
            + IF(DAYOFWEEK(CURDATE() + INTERVAL 11 DAY) IN (6,7),950000,750000)), 'DA_DAT', NULL, NULL
    UNION ALL
    SELECT 'HS-MAU-HUY-DA-HOAN-COC', '101', CURDATE() + INTERVAL 8 DAY,
           CURDATE() + INTERVAL 10 DAY, 500000, 650000, 1000000, 'DA_HUY', NULL, NULL
    UNION ALL
    SELECT 'HS-MAU-HUY-KHONG-COC', '104', CURDATE() + INTERVAL 8 DAY,
           CURDATE() + INTERVAL 10 DAY, 1200000, 1500000, 2400000, 'DA_HUY', NULL, NULL
    UNION ALL
    SELECT 'HS-MAU-HUY-MOT-PHONG', '101', CURDATE() + INTERVAL 5 DAY,
           CURDATE() + INTERVAL 7 DAY, 500000, 650000, 1000000, 'DA_HUY', NULL, NULL
    UNION ALL
    SELECT 'HS-MAU-HUY-MOT-PHONG', '102', CURDATE() + INTERVAL 5 DAY,
           CURDATE() + INTERVAL 7 DAY, 750000, 950000, 1500000, 'DA_DAT', NULL, NULL
    UNION ALL
    SELECT 'HS-MAU-NOI-TIEP-1', '202', CURDATE() + INTERVAL 16 DAY,
           CURDATE() + INTERVAL 18 DAY, 750000, 950000, 1500000, 'DA_DAT', NULL, NULL
    UNION ALL
    SELECT 'HS-MAU-NOI-TIEP-2', '202', CURDATE() + INTERVAL 18 DAY,
           CURDATE() + INTERVAL 20 DAY, 750000, 950000, 1500000, 'DA_DAT', NULL, NULL
) s
JOIN don_dat_phong d ON d.ma_tra_cuu_don = s.ma_tra_cuu_don
JOIN phong p ON p.so_phong = s.so_phong
WHERE NOT EXISTS (
    SELECT 1 FROM chi_tiet_dat_phong x
    WHERE x.ma_don_dat_phong = d.ma_don_dat_phong AND x.ma_phong = p.ma_phong
      AND x.ngay_nhan = s.ngay_nhan AND x.ngay_tra = s.ngay_tra
);

-- Lưu tiền cọc thành giao dịch riêng, đúng với cách tính tiền cọc còn giữ.
INSERT IGNORE INTO thanh_toan
    (ma_don_dat_phong, loai_thanh_toan, phuong_thuc, so_tien, ghi_chu, ma_quan_ly_ghi_nhan, tao_luc)
SELECT d.ma_don_dat_phong, 'TIEN_COC', s.phuong_thuc, s.so_tien, s.ghi_chu, s.ma_quan_ly, s.tao_luc
FROM (
    SELECT 'HS-MAU-NHIEU-PHONG' ma_tra_cuu_don, 'CHUYEN_KHOAN' phuong_thuc,
           100000 so_tien, 'Tiền cọc mẫu đơn nhiều phòng' ghi_chu, 1 ma_quan_ly, NOW() - INTERVAL 4 DAY tao_luc
    UNION ALL
    SELECT 'HS-MAU-HOA-DON-CHO-THU', 'TIEN_MAT', 200000, 'Tiền cọc mẫu hóa đơn còn nợ', 1, NOW() - INTERVAL 2 DAY
    UNION ALL
    SELECT 'HS-MAU-QL-CHUA-TK', 'TIEN_MAT', 100000, 'Tiền cọc do Quản lý nhận', 1, NOW()
    UNION ALL
    SELECT 'HS-MAU-HUY-DA-HOAN-COC', 'CHUYEN_KHOAN', 200000, 'Tiền cọc mẫu đã hoàn', NULL, NOW() - INTERVAL 8 DAY
) s
JOIN don_dat_phong d ON d.ma_tra_cuu_don = s.ma_tra_cuu_don
WHERE NOT EXISTS (
    SELECT 1 FROM thanh_toan t
    WHERE t.ma_don_dat_phong = d.ma_don_dat_phong AND t.loai_thanh_toan = 'TIEN_COC'
);

-- Hóa đơn còn nợ để thử: đã trừ cọc 200.000 và đã thu trước 300.000.
INSERT IGNORE INTO hoa_don
    (ma_don_dat_phong, so_hoa_don, lap_luc, tien_phong, tien_coc_duoc_tru,
     so_tien_con_phai_thu, trang_thai, chot_luc)
SELECT d.ma_don_dat_phong, 'INV-HS-MAU-HOA-DON-CHO-THU', NOW(), x.tien_phong,
       LEAST(200000, x.tien_phong), GREATEST(x.tien_phong - LEAST(200000, x.tien_phong) - 300000, 0),
       'CHUA_THANH_TOAN', NULL
FROM don_dat_phong d
JOIN (
    SELECT ma_don_dat_phong, SUM(tien_phong) tien_phong
    FROM chi_tiet_dat_phong
    WHERE trang_thai <> 'DA_HUY'
    GROUP BY ma_don_dat_phong
) x ON x.ma_don_dat_phong = d.ma_don_dat_phong
WHERE d.ma_tra_cuu_don = 'HS-MAU-HOA-DON-CHO-THU'
  AND NOT EXISTS (SELECT 1 FROM hoa_don h WHERE h.ma_don_dat_phong = d.ma_don_dat_phong);

INSERT IGNORE INTO thanh_toan
    (ma_don_dat_phong, ma_hoa_don, loai_thanh_toan, phuong_thuc, so_tien,
     ghi_chu, ma_quan_ly_ghi_nhan, tao_luc)
SELECT d.ma_don_dat_phong, h.ma_hoa_don, 'THANH_TOAN', s.phuong_thuc, s.so_tien,
       s.ghi_chu, 1, s.tao_luc
FROM (
    SELECT 'HS-MAU-HOA-DON-CHO-THU' ma_tra_cuu_don, 'TIEN_MAT' phuong_thuc,
           100000 so_tien, 'Thanh toán lần 1 mẫu' ghi_chu, NOW() - INTERVAL 1 DAY tao_luc
    UNION ALL
    SELECT 'HS-MAU-HOA-DON-CHO-THU', 'CHUYEN_KHOAN', 200000,
           'Thanh toán lần 2 mẫu', NOW()
) s
JOIN don_dat_phong d ON d.ma_tra_cuu_don = s.ma_tra_cuu_don
JOIN hoa_don h ON h.ma_don_dat_phong = d.ma_don_dat_phong
WHERE NOT EXISTS (
    SELECT 1 FROM thanh_toan t
    WHERE t.ma_hoa_don = h.ma_hoa_don AND t.loai_thanh_toan = 'THANH_TOAN'
);

-- Khoản hoàn cọc mẫu đã xác nhận để đối chiếu với khoản CHO_XU_LY ở trên.
INSERT IGNORE INTO thanh_toan
    (ma_don_dat_phong, loai_thanh_toan, phuong_thuc, so_tien, ghi_chu,
     trang_thai_hoan_coc, so_tien_thuc_hoan, phuong_thuc_hoan, tao_luc)
SELECT d.ma_don_dat_phong, 'HOAN_COC', 'CHUYEN_KHOAN', 200000,
       'Khoản hoàn cọc mẫu đã được xác nhận.', 'DA_XAC_NHAN', 200000,
       'CHUYEN_KHOAN', NOW() - INTERVAL 7 DAY
FROM don_dat_phong d
WHERE d.ma_tra_cuu_don = 'HS-MAU-HUY-DA-HOAN-COC'
  AND NOT EXISTS (
      SELECT 1 FROM thanh_toan t
      WHERE t.ma_don_dat_phong = d.ma_don_dat_phong AND t.loai_thanh_toan = 'HOAN_COC'
  );

-- Phòng 204 đã dọn sau trả phòng; 301 và 302 đang ở; các phòng còn lại sẵn sàng.
UPDATE phong SET trang_thai = 'DANG_DON'
WHERE so_phong = '204' AND EXISTS (
    SELECT 1 FROM don_dat_phong d WHERE d.ma_tra_cuu_don = 'HS-MAU-NHIEU-PHONG'
);
UPDATE phong SET trang_thai = 'DANG_O'
WHERE so_phong IN ('301','302') AND EXISTS (
    SELECT 1 FROM chi_tiet_dat_phong c
    JOIN don_dat_phong d ON d.ma_don_dat_phong = c.ma_don_dat_phong
    WHERE d.ma_tra_cuu_don IN ('HS-MAU-NHIEU-PHONG','HS-MAU-HOA-DON-CHO-THU')
      AND c.ma_phong = phong.ma_phong AND c.trang_thai = 'DANG_O'
);

INSERT IGNORE INTO nhat_ky_thao_tac
    (loai_nguoi_thao_tac, ma_quan_ly, ma_tai_khoan_khach_hang,
     hanh_dong, ma_don_dat_phong, mo_ta, tao_luc)
SELECT s.loai_nguoi, s.ma_quan_ly, s.ma_tai_khoan, s.hanh_dong,
       d.ma_don_dat_phong, s.mo_ta, s.tao_luc
FROM (
    SELECT 'KHACH_HANG' loai_nguoi, NULL ma_quan_ly, 3 ma_tai_khoan,
           'TAO_DON_DAT_PHONG' hanh_dong, 'HS-MAU-NHIEU-PHONG' ma_tra_cuu_don,
           'Khách tạo đơn mẫu gồm hai phòng.' mo_ta, NOW() - INTERVAL 4 DAY tao_luc
    UNION ALL
    SELECT 'QUAN_LY', 1, NULL, 'TAO_DON_DAT_PHONG', 'HS-MAU-QL-CHUA-TK',
           'Quản lý tạo đơn cho khách chưa có tài khoản.', NOW()
    UNION ALL
    SELECT 'QUAN_LY', 1, NULL, 'LAP_HOA_DON_CHO_THU', 'HS-MAU-HOA-DON-CHO-THU',
           'Đã lập hóa đơn, còn khoản phải thu.', NOW()
) s
JOIN don_dat_phong d ON d.ma_tra_cuu_don = s.ma_tra_cuu_don
WHERE NOT EXISTS (
    SELECT 1 FROM nhat_ky_thao_tac n
    WHERE n.ma_don_dat_phong = d.ma_don_dat_phong AND n.hanh_dong = s.hanh_dong
      AND n.mo_ta = s.mo_ta
);

INSERT IGNORE INTO don_dat_phong
    (ma_tra_cuu_don, ma_khach_hang, ma_tai_khoan_khach_hang, nguon_tao, so_khach,
     trang_thai, tien_coc_da_nhan, ghi_chu, tao_luc)
SELECT s.ma_tra_cuu_don, s.ma_khach_hang, s.ma_tai_khoan, 'TRUC_TUYEN', 1,
       'HOAN_TAT', s.tien_coc, 'Dữ liệu minh họa báo cáo', s.tao_luc
FROM (
    SELECT 'HS-BC-2026-01' ma_tra_cuu_don, 1 ma_khach_hang, 1 ma_tai_khoan, 100000 tien_coc, '2026-01-05 09:00:00' tao_luc UNION ALL
    SELECT 'HS-BC-2026-02', 1, 1, 0, '2026-02-05 09:00:00' UNION ALL
    SELECT 'HS-BC-2026-03', 2, 2, 50000, '2026-03-09 09:00:00' UNION ALL
    SELECT 'HS-BC-2026-04', 1, 1, 100000, '2026-04-14 09:00:00' UNION ALL
    SELECT 'HS-BC-2026-05', 2, 2, 0, '2026-05-19 09:00:00' UNION ALL
    SELECT 'HS-BC-2026-06', 1, 1, 100000, '2026-06-11 09:00:00'
) s
WHERE NOT EXISTS (
    SELECT 1 FROM don_dat_phong d WHERE d.ma_tra_cuu_don = s.ma_tra_cuu_don
);

INSERT IGNORE INTO chi_tiet_dat_phong
    (ma_don_dat_phong, ma_phong, ngay_nhan, ngay_tra,
     gia_ngay_thuong_luc_dat, gia_cuoi_tuan_luc_dat, tien_phong, trang_thai,
     thoi_diem_nhan_thuc_te, thoi_diem_tra_thuc_te)
SELECT d.ma_don_dat_phong, p.ma_phong, s.ngay_nhan, s.ngay_tra,
       s.gia_thuong, s.gia_cuoi_tuan, s.tien_phong, 'DA_TRA',
       TIMESTAMP(s.ngay_nhan, '15:00:00'), TIMESTAMP(s.ngay_tra, '11:00:00')
FROM (
    SELECT 'HS-BC-2026-01' ma_tra_cuu_don, '101' so_phong, '2026-01-05' ngay_nhan, '2026-01-07' ngay_tra, 500000 gia_thuong, 650000 gia_cuoi_tuan, 1000000 tien_phong UNION ALL
    SELECT 'HS-BC-2026-02', '102', '2026-02-06', '2026-02-08', 750000, 950000, 1900000 UNION ALL
    SELECT 'HS-BC-2026-03', '103', '2026-03-10', '2026-03-11', 750000, 950000, 750000 UNION ALL
    SELECT 'HS-BC-2026-04', '104', '2026-04-15', '2026-04-17', 1200000, 1500000, 2400000 UNION ALL
    SELECT 'HS-BC-2026-05', '201', '2026-05-20', '2026-05-22', 500000, 650000, 1000000 UNION ALL
    SELECT 'HS-BC-2026-06', '202', '2026-06-12', '2026-06-14', 750000, 950000, 1900000
) s
JOIN don_dat_phong d ON d.ma_tra_cuu_don = s.ma_tra_cuu_don
JOIN phong p ON p.so_phong = s.so_phong
WHERE NOT EXISTS (
    SELECT 1 FROM chi_tiet_dat_phong x WHERE x.ma_don_dat_phong = d.ma_don_dat_phong
);

INSERT IGNORE INTO hoa_don
    (ma_don_dat_phong, so_hoa_don, lap_luc, tien_phong, tien_coc_duoc_tru,
     so_tien_con_phai_thu, trang_thai, chot_luc)
SELECT d.ma_don_dat_phong, CONCAT('INV-', d.ma_tra_cuu_don), s.lap_luc,
       s.tien_phong, s.tien_coc, s.tien_phong - s.tien_coc,
       'DA_THANH_TOAN', s.lap_luc
FROM don_dat_phong d
JOIN (
    SELECT 'HS-BC-2026-01' ma_tra_cuu_don, '2026-01-07 11:00:00' lap_luc, 1000000 tien_phong, 100000 tien_coc UNION ALL
    SELECT 'HS-BC-2026-02', '2026-02-08 11:00:00', 1900000, 0 UNION ALL
    SELECT 'HS-BC-2026-03', '2026-03-11 11:00:00', 750000, 50000 UNION ALL
    SELECT 'HS-BC-2026-04', '2026-04-17 11:00:00', 2400000, 100000 UNION ALL
    SELECT 'HS-BC-2026-05', '2026-05-22 11:00:00', 1000000, 0 UNION ALL
    SELECT 'HS-BC-2026-06', '2026-06-14 11:00:00', 1900000, 100000
) s ON s.ma_tra_cuu_don = d.ma_tra_cuu_don
WHERE NOT EXISTS (
    SELECT 1 FROM hoa_don h WHERE h.ma_don_dat_phong = d.ma_don_dat_phong
);

INSERT IGNORE INTO thanh_toan
    (ma_don_dat_phong, ma_hoa_don, loai_thanh_toan, phuong_thuc, so_tien, ghi_chu, tao_luc)
SELECT d.ma_don_dat_phong, h.ma_hoa_don, 'THANH_TOAN', 'CHUYEN_KHOAN',
       h.tien_phong - h.tien_coc_duoc_tru, 'Thanh toán minh họa báo cáo', h.lap_luc
FROM don_dat_phong d
JOIN hoa_don h ON h.ma_don_dat_phong = d.ma_don_dat_phong
WHERE d.ma_tra_cuu_don LIKE 'HS-BC-2026-%'
  AND NOT EXISTS (
      SELECT 1 FROM thanh_toan t
      WHERE t.ma_hoa_don = h.ma_hoa_don AND t.loai_thanh_toan = 'THANH_TOAN'
  );

INSERT INTO loai_phong (ten, suc_chua, mo_ta, gia_ngay_thuong, gia_cuoi_tuan, dang_hoat_dong)
VALUES ('Phòng gia đình', 4, 'Phòng rộng cho gia đình hoặc nhóm nhỏ.', 1200000, 1500000, TRUE)
ON DUPLICATE KEY UPDATE dang_hoat_dong = TRUE;

