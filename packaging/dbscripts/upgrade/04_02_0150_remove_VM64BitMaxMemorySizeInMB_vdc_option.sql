-- It removes unnecessary VMPpc64BitMaxMemorySizeInMB config option, the default value will be obtained
-- from ConfigValues class.

DELETE FROM vdc_options
WHERE option_name = 'VMPpc64BitMaxMemorySizeInMB'
    AND vdc_options.option_value = '1048576';
