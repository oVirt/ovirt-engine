UPDATE cluster
SET emulated_machine = 'pc-i440fx-rhel7.6.0;pc-q35-rhel8.1.0'
WHERE emulated_machine IN ('pc-q35-rhel8.1.0', 'pc-i440fx-rhel8.1.0');
UPDATE cluster
SET emulated_machine = 'pc-i440fx-2.12;pc-q35-4.1'
WHERE emulated_machine IN ('pc-q35-4.1', 'pc-i440fx-4.1');
