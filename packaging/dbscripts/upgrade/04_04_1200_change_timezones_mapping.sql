-- changes timezones mappings that existed before and valid only to non-windows, to mappings that is valid to both
-- non-windows and windows
UPDATE vm_static
SET time_zone = CASE
                    WHEN time_zone = 'America/Indianapolis' THEN 'America/New_York'
                    WHEN time_zone = 'US Eastern Standard Time (Indiana)' THEN 'Eastern Standard Time'
                    WHEN time_zone = 'Atlantic/Reykjavik' THEN 'Etc/GMT'
                    WHEN time_zone = 'Iceland Standard Time' THEN 'Greenwich Standard Time'
                    END
WHERE time_zone in ('America/Indianapolis',
                    'US Eastern Standard Time (Indiana)',
                    'Atlantic/Reykjavik',
                    'Iceland Standard Time');


UPDATE vm_init
SET time_zone = CASE
                    WHEN time_zone = 'America/Indianapolis' THEN 'America/New_York'
                    WHEN time_zone = 'US Eastern Standard Time (Indiana)' THEN 'Eastern Standard Time'
                    WHEN time_zone = 'Atlantic/Reykjavik' THEN 'Etc/GMT'
                    WHEN time_zone = 'Iceland Standard Time' THEN 'Greenwich Standard Time'
                    END
WHERE time_zone in ('America/Indianapolis',
                    'US Eastern Standard Time (Indiana)',
                    'Atlantic/Reykjavik',
                    'Iceland Standard Time');

