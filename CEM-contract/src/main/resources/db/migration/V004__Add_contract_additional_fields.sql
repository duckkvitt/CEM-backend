-- Migration to add additional fields for contract details
-- Điều 2: Thanh toán
ALTER TABLE contracts ADD COLUMN payment_method VARCHAR(255);
ALTER TABLE contracts ADD COLUMN payment_term VARCHAR(500);
ALTER TABLE contracts ADD COLUMN bank_account VARCHAR(500);

-- Điều 3: Thời gian, địa điểm, phương thức giao hàng
ALTER TABLE contracts ADD COLUMN delivery_time VARCHAR(500);
ALTER TABLE contracts ADD COLUMN delivery_location TEXT;
ALTER TABLE contracts ADD COLUMN delivery_method VARCHAR(255);

-- Điều 5: Bảo hành và hướng dẫn sử dụng hàng hóa
ALTER TABLE contracts ADD COLUMN warranty_product VARCHAR(500);
ALTER TABLE contracts ADD COLUMN warranty_period_months INTEGER;

-- Add comments for documentation
COMMENT ON COLUMN contracts.payment_method IS 'Hình thức thanh toán';
COMMENT ON COLUMN contracts.payment_term IS 'Thời hạn thanh toán';
COMMENT ON COLUMN contracts.bank_account IS 'Tài khoản ngân hàng';
COMMENT ON COLUMN contracts.delivery_time IS 'Thời gian giao hàng';
COMMENT ON COLUMN contracts.delivery_location IS 'Địa điểm giao hàng';
COMMENT ON COLUMN contracts.delivery_method IS 'Phương thức giao hàng';
COMMENT ON COLUMN contracts.warranty_product IS 'Loại hàng bảo hành';
COMMENT ON COLUMN contracts.warranty_period_months IS 'Thời gian bảo hành (tháng)'; 