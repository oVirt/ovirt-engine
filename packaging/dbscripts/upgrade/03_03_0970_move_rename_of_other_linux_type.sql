
-- remove "linux" and "windows" os types. they don't have a real usage. --
-- "linux" becomes "other_liunx"
update vm_static set os = 5 where os = 100;
-- ""windows" becomes "windows_xp"
update vm_static set os = 1 where os = 200;
