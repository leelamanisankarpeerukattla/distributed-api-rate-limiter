-- Sliding Window Log Rate Limiter (Redis Lua)
-- KEYS[1] = zset key
-- KEYS[2] = seq key (for unique members)
-- ARGV[1] = nowEpochMs
-- ARGV[2] = tokensRequested
-- ARGV[3] = limit
-- ARGV[4] = windowMs
-- ARGV[5] = ttlMs
-- Returns: [allowed(1/0), remaining, resetEpochMs, limit, retryAfterMs]

local zkey = KEYS[1]
local seqKey = KEYS[2]
local now = tonumber(ARGV[1])
local tokensRequested = tonumber(ARGV[2])
local limit = tonumber(ARGV[3])
local window = tonumber(ARGV[4])
local ttlMs = tonumber(ARGV[5])

local windowStart = now - window

-- Remove old entries
redis.call('ZREMRANGEBYSCORE', zkey, 0, windowStart)

local current = redis.call('ZCARD', zkey)
local allowed = 0
local retryAfter = 0
local reset = now + window

-- Find oldest timestamp to compute reset time
local oldest = redis.call('ZRANGE', zkey, 0, 0, 'WITHSCORES')
if (oldest ~= nil and #oldest >= 2) then
  reset = tonumber(oldest[2]) + window
end

if (current + tokensRequested <= limit) then
  allowed = 1
  local seq = redis.call('INCR', seqKey)
  for i=1,tokensRequested do
    local member = tostring(now) .. ':' .. tostring(seq) .. ':' .. tostring(i)
    redis.call('ZADD', zkey, now, member)
  end
else
  allowed = 0
  retryAfter = reset - now
  if (retryAfter < 0) then
    retryAfter = 0
  end
end

redis.call('PEXPIRE', zkey, ttlMs)
redis.call('PEXPIRE', seqKey, ttlMs)

local newCount = redis.call('ZCARD', zkey)
local remaining = limit - newCount
if (remaining < 0) then
  remaining = 0
end

return { allowed, remaining, reset, limit, retryAfter }
