-- Delete versions no longer supported
select fn_db_delete_config_for_version('3.0');
select fn_db_delete_config_for_version('3.1');
select fn_db_delete_config_for_version('3.2');
select fn_db_delete_config_for_version('3.3');
select fn_db_delete_config_for_version('3.4');

-- Delete "feature supported" keys no longer needed
select fn_db_delete_config_value_all_versions('GlusterAsyncTasksSupport');
select fn_db_delete_config_value_all_versions('VirtIoScsiEnabled');
select fn_db_delete_config_value_all_versions('NormalizedMgmtNetworkEnabled');
select fn_db_delete_config_value_all_versions('AbortMigrationOnError');
select fn_db_delete_config_value_all_versions('CpuPinningEnabled');
select fn_db_delete_config_value_all_versions('DirectLUNDiskEnabled');
select fn_db_delete_config_value_all_versions('FilteringLUNsEnabled');
select fn_db_delete_config_value_all_versions('VirtIoRngDeviceSupported');
select fn_db_delete_config_value_all_versions('GlusterHooksEnabled');
select fn_db_delete_config_value_all_versions('GlusterHostUUIDSupport');
select fn_db_delete_config_value_all_versions('GlusterRefreshHeavyWeight');
select fn_db_delete_config_value_all_versions('GlusterSupport');
select fn_db_delete_config_value_all_versions('GlusterSupportForceCreateVolume');
select fn_db_delete_config_value_all_versions('GlusterStopServicesSupported');
select fn_db_delete_config_value_all_versions('HardwareInfoEnabled');
select fn_db_delete_config_value_all_versions('HotPlugEnabled');
select fn_db_delete_config_value_all_versions('MigrationSupportForNativeUsb');
select fn_db_delete_config_value_all_versions('NetworkLinkingSupported');
select fn_db_delete_config_value_all_versions('TunnelMigrationEnabled');
select fn_db_delete_config_value_all_versions('MigrationNetworkEnabled');
select fn_db_delete_config_value_all_versions('MultipleGatewaysSupported');
select fn_db_delete_config_value_all_versions('MemorySnapshotSupported');
select fn_db_delete_config_value_all_versions('NetworkQosSupported');
select fn_db_delete_config_value_all_versions('StorageQosSupported');
select fn_db_delete_config_value_all_versions('CpuQosSupported');
select fn_db_delete_config_value_all_versions('CloudInitSupported');
select fn_db_delete_config_value_all_versions('ImportGlanceImageAsTemplate');
select fn_db_delete_config_value_all_versions('HotPlugDiskSnapshotSupported');
select fn_db_delete_config_value_all_versions('GetFileStats');
select fn_db_delete_config_value_all_versions('DefaultRouteSupported');
select fn_db_delete_config_value_all_versions('SerialNumberPolicySupported');
select fn_db_delete_config_value_all_versions('ReportWhetherDomainMonitoringResultIsActual');
select fn_db_delete_config_value_all_versions('OvfStoreOnAnyDomain');
select fn_db_delete_config_value_all_versions('IscsiMultipathingSupported');
select fn_db_delete_config_value_all_versions('ImportDataStorageDomain');
select fn_db_delete_config_value_all_versions('MixedDomainTypesInDataCenter');
select fn_db_delete_config_value_all_versions('BootMenuSupported');
select fn_db_delete_config_value_all_versions('SpiceCopyPasteToggleSupported');
select fn_db_delete_config_value_all_versions('StoragePoolMemoryBackend');
select fn_db_delete_config_value_all_versions('JsonProtocolSupported');
select fn_db_delete_config_value_all_versions('VmSlaPolicySupported');
select fn_db_delete_config_value_all_versions('ReportedDisksLogicalNames');
select fn_db_delete_config_value_all_versions('LiveMergeSupported');
select fn_db_delete_config_value_all_versions('LiveSnapshotEnabled');
select fn_db_delete_config_value_all_versions('NativeUSBEnabled');
select fn_db_delete_config_value_all_versions('NonVmNetworkSupported');
select fn_db_delete_config_value_all_versions('SupportBridgesReportByVDSM');
select fn_db_delete_config_value_all_versions('MacAntiSpoofingFilterRulesSupported');
select fn_db_delete_config_value_all_versions('GlusterFsStorageEnabled');
select fn_db_delete_config_value_all_versions('MTUOverrideSupported');
select fn_db_delete_config_value_all_versions('PortMirroringSupported');
select fn_db_delete_config_value_all_versions('PosixStorageEnabled');
select fn_db_delete_config_value_all_versions('SendVmTicketUID');
select fn_db_delete_config_value_all_versions('ShareableDiskEnabled');
select fn_db_delete_config_value_all_versions('SupportForceCreateVG');
select fn_db_delete_config_value_all_versions('SupportForceExtendVG');
select fn_db_delete_config_value_all_versions('SupportCustomDeviceProperties');
select fn_db_delete_config_value_all_versions('NetworkCustomPropertiesSupported');
select fn_db_delete_config_value_all_versions('SingleQxlPciEnabled');
select fn_db_delete_config_value_all_versions('SkipFencingIfSDActiveSupported');

-- Delete action_version_map values no longer needed
DELETE
FROM   action_version_map
WHERE  cluster_minimal_version IN ('3.0', '3.1', '3.2', '3.3', '3.4') OR
       storage_pool_minimal_version IN ('3.0', '3.1', '3.2', '3.3', '3.4')
