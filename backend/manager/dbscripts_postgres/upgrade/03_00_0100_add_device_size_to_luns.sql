-- Add a new "device_size" column to the LUNs table in order to
-- rembember the size (in GB) of the device without needing to run
-- a new VDS command everytime we need to display the size. This
-- is required in order to fix #716964:

select fn_db_add_column('luns', 'device_size', 'integer default 0');
