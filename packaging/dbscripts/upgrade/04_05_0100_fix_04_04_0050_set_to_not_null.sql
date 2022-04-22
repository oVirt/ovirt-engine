-- Make sure that forgotten options from ancient releases have default value
UPDATE vdc_options
  SET default_value=option_value
  WHERE
    default_value IS NULL
    AND option_value IS NOT NULL;

-- If there are still some crappy options, let's set default value to empty string
UPDATE vdc_options
  SET default_value=''
  WHERE
    default_value IS NULL
    AND option_value IS NULL;

-- We shouldn't have any options with NULL values by now, but let's make sure
UPDATE vdc_options
  SET option_value = ''
  WHERE option_value IS NULL;

SELECT fn_db_change_column_null('vdc_options', 'default_value', false);
SELECT fn_db_change_column_null('vdc_options', 'option_value', false);
