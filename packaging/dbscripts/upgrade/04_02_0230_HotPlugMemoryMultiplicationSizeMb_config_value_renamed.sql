UPDATE vdc_options
SET option_name = 'HotPlugMemoryBlockSizeMb'
WHERE option_name ILIKE 'HotPlugMemoryMultiplicationSizeMb';
