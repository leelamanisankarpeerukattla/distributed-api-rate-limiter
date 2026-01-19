-- Token Bucket Rate Limiter (Redis Lua)
-- KEYS[1] = bucket key (hash)
-- ARGV[1] = nowEpochMs
-- ARGV[2] = tokensRequested
-- ARGV[3] = capacity
-- ARGV[4] = refillTokens
-- ARGV[5] = refillPeriodMs
-- ARGV[6] = ttlMs
-- Returns: [allowed(1/0), remaining, resetEpochMs, limit, retryAfterMs]

local key = KEYS[1]
local now = tonumber(ARGV[1])
local tokensRequested = tonumber(ARGV[2])
local capacity = tonumber(ARGV[3])
local refillTokens = tonumber(ARGV[4])
local refillPeriod = tonumber(ARGV[5])
local ttlMs = tonumber(ARGV[6])

local tokens = redis.call('HGET', key, 'tokens')
local last = redis.call('HGET', key, 'ts')

if (tokens == false) then
  tokens = capacity
else
  tokens = tonumber(tokens)
end

if (last == false) then
  last = now
else
  last = tonumber(last)
end

-- Refill based on whole periods elapsed
local elapsed = now - last
if (elapsed < 0) then
  elapsed = 0
end

local periods = math.floor(elapsed / refillPeriod)
if (periods > 0) then
  local added = periods * refillTokens
  tokens = math.min(capacity, tokens + added)
  last = last + (periods * refillPeriod)
end

local allowed = 0
local retryAfter = 0

if (tokens >= tokensRequested) then
  allowed = 1
  tokens = tokens - tokensRequested
else
  allowed = 0
end

local reset = last + refillPeriod
if (allowed == 0) then
  retryAfter = reset - now
  if (retryAfter < 0) then
    retryAfter = 0
  end
end

redis.call('HSET', key, 'tokens', tokens, 'ts', last)
redis.call('PEXPIRE', key, ttlMs)

return { allowed, tokens, reset, capacity, retryAfter }
