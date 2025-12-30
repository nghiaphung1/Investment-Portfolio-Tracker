-- KEYS[1]: lockKey (ví dụ: otp:lock:123)
-- ARGV[1]: lockTtl (thời gian giãn cách giữa 2 lần gửi, ví dụ 60s)

local isLocked = redis.call('EXISTS', KEYS[1])

if isLocked == 1 then
    return "TOO_FAST"
else
    -- Đánh dấu gửi mail, set lock với TTL mới
    redis.call('SET', KEYS[1], "LOCKED", 'EX', tonumber(ARGV[1]))
    return "PROCEED"
end