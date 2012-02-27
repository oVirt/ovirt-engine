-- This value indicates devices that although are given to us by VDSM
-- are still treated as managed devices
-- This should be a [device=<device> type=<type>[,]]* string
select fn_db_add_config_value('ManagedDevicesWhiteList','','general');
