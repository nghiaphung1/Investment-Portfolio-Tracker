-- KEYS[1]: Key của User List (u_rts:123)
-- ARGV[1]: Token ID mới (rawRefreshToken)
-- ARGV[2]: Max Devices (Ví dụ: 2)
-- ARGV[3]: TTL của List (tính bằng milliseconds) -> NEW

-- 1. Đẩy token mới vào (LPUSH trả về độ dài list sau khi push)
redis.call('LPUSH', KEYS[1], ARGV[1])

-- 2. Thiết lập TTL ngay lập tức (Atomic) -> FIX RACE CONDITION
-- Dùng PEXPIRE để tính theo mili-giây
redis.call('PEXPIRE', KEYS[1], ARGV[3])

-- 3. Logic Trim (Cắt đuôi)
local list_len = redis.call('LLEN', KEYS[1])
local max_len = tonumber(ARGV[2])
local popped_tokens = {}

while list_len > max_len do
    local token = redis.call('RPOP', KEYS[1])
    if token then
        table.insert(popped_tokens, token)
    end
    list_len = list_len - 1
end

return popped_tokens