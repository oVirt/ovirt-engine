update vdc_options set option_value = replace (option_value , '/4/6', '')
where option_name = 'VdcBootStrapUrl';
