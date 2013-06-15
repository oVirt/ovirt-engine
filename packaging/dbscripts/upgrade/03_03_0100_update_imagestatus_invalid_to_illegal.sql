-- Replace all INVALID statuses with ILLEGAL ones
UPDATE images SET imagestatus = 4 WHERE imagestatus = 3;
