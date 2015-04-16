-- Revert entries for AddVmHostDevicesCommand and RemoveVmHostDevicesCommand
delete from action_version_map where action_type in (2350,2351);
