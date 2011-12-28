-- Increase AsyncTaskZombieTaskLifeInMinutes to 50 hours if it's the default 5 hours.

UPDATE vdc_options
SET    option_value = '3000'
WHERE  option_name = 'AsyncTaskZombieTaskLifeInMinutes'
AND    option_value = '300';

