-- KEYS[1]: otpKey, KEYS[2]: lockKey
-- ARGV[1]: hashedOtp, ARGV[2]: otpTtl, ARGV[3]: lockValue, ARGV[4]: lockTtl

redis.call('SET', KEYS[1], ARGV[1], 'EX', ARGV[2])
redis.call('SET', KEYS[2], ARGV[3], 'EX', ARGV[4])
return 'OK'