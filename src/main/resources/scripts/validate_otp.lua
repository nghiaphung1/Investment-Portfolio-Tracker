-- KEYS[1]: otpKey, KEYS[2]: failCountKey
-- ARGV[1]: hashedOtp, ARGV[2]: maxFailedAttempts, ARGV[3]: failTtl

local savedOtp = redis.call('GET', KEYS[1])
local failedAttempts = tonumber(redis.call('GET', KEYS[2]) or "0")

-- 1. Kiểm tra xem đã vượt quá số lần thử sai chưa
if failedAttempts >= tonumber(ARGV[2]) then
    return "BLOCKED"
end

-- 2. Kiểm tra mã OTP có tồn tại không (hết hạn)
if not savedOtp then
    return "EXPIRED"
end

-- 3. So sánh mã OTP
if savedOtp == ARGV[1] then
    -- ĐÚNG: Xóa OTP và Key đếm lỗi ngay lập tức
    redis.call('DEL', KEYS[1])
    redis.call('DEL', KEYS[2])
    return "SUCCESS"
else
    -- SAI: Tăng số lần thử sai và set thời gian hết hạn (ví dụ 10 phút)
    local currentFail = redis.call('INCR', KEYS[2])
    redis.call('EXPIRE', KEYS[2], tonumber(ARGV[3]))
    return "INVALID"
end