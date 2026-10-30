--[[
  Copyright (c) 2026 cardroute-engine contributors.

  Logical Purpose:
  Atomically checks and decrements the remaining monthly spend cap for a card and MCC category.
  Prevents race conditions and double-spending of accelerated rewards under concurrent transactions.

  Keys:
    KEYS[1] = Spend cap key: spend_cap:{cardId}:{billingCycle}:{mccGroup}

  Arguments:
    ARGV[1] = Requested transaction amount (decimal string, e.g., "5000.00")
    ARGV[2] = Total configured category cap limit (decimal string, e.g., "10000.00")
    ARGV[3] = TTL in seconds for key expiration (integer, e.g., 2678400 for 31 days)

  Returns:
    Table with 3 string values:
      [1] newRemainingCap: The remaining spend capacity after this transaction
      [2] allocatedAcceleratedSpend: The portion of transaction credited at accelerated rate
      [3] overflowSpend: The portion exceeding the cap, falling back to base rate
--]]

local key = KEYS[1]
local requestedAmount = tonumber(ARGV[1])
local totalCapLimit = tonumber(ARGV[2])
local ttlSeconds = tonumber(ARGV[3])

local currentRemaining = redis.call('GET', key)

if not currentRemaining then
    currentRemaining = totalCapLimit
else
    currentRemaining = tonumber(currentRemaining)
    if currentRemaining < 0 then
        currentRemaining = 0
    end
end

local acceleratedAmount = 0
local overflowAmount = requestedAmount

if currentRemaining > 0 then
    if currentRemaining >= requestedAmount then
        acceleratedAmount = requestedAmount
        overflowAmount = 0
        currentRemaining = currentRemaining - requestedAmount
    else
        acceleratedAmount = currentRemaining
        overflowAmount = requestedAmount - currentRemaining
        currentRemaining = 0
    end
else
    acceleratedAmount = 0
    overflowAmount = requestedAmount
    currentRemaining = 0
end

redis.call('SET', key, string.format("%.4f", currentRemaining))
redis.call('EXPIRE', key, ttlSeconds)

return {
    string.format("%.4f", currentRemaining),
    string.format("%.4f", acceleratedAmount),
    string.format("%.4f", overflowAmount)
}

