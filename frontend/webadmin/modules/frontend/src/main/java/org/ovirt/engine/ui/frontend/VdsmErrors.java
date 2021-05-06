package org.ovirt.engine.ui.frontend;

import com.google.gwt.i18n.client.ConstantsWithLookup;

public interface VdsmErrors extends ConstantsWithLookup {
    String BlockDeviceActionError();

    String BlockStorageDomainMasterFSCKError();

    String BlockStorageDomainMasterMountError();

    String CannotAccessLogicalVolume();

    String CannotCreateLogicalVolume();

    String CannotDeactivateLogicalVolume();

    String CannotRemoveLogicalVolume();

    String ExportError();

    String GeneralException();

    String GeneralHookError();

    String HostIdMismatch();

    String ImageDeamonError();

    String ImageDeamonUnsupported();

    String ImageDeleteError();

    String ImageDoesNotExistInDomainError();

    String ImageIsNotEmpty();

    String ImageMissingFromVm();

    String ImagePathError();

    String ImagesActionError();

    String ImageTicketsError();

    String ImageValidationError();

    String ImportError();

    String ImportInfoError();

    String ImportUnknownType();

    String IncorrectFormat();

    String NoSuchVmLeaseOnDomain();

    String InvalidParameterException();

    String InvalidTask();

    String LogicalVolumeExtendError();

    String MergeSnapshotsError();

    String MetaDataKeyError();

    String MetaDataKeyNotFoundError();

    String MetaDataSealIsBroken();

    String MetaDataValidationError();

    String MoveImageError();

    String NotImplementedException();

    String PhysDevInitializationError();

    String ReconstructMasterError();

    String ResourceException();

    String AcquireLockFailure();

    String SpmFenceError();

    String SpmParamsMismatch();

    String SpmStartError();

    String SpmStatusError();

    String SpmStopError();

    String StorageDomainActionError();

    String StorageDomainActivateError();

    String StorageDomainAlreadyExists();

    String StorageDomainAttachError();

    String StorageDomainCreationError();

    String StorageDomainDeactivateError();

    String StorageDomainDetachError();

    String StorageDomainDoesNotExist();

    String StorageDomainFormatError();

    String StorageDomainNotEmpty();

    String StorageDomainFSNotMounted();

    String StorageDomainLayoutError();

    String StorageDomainMasterCopyError();

    String StorageDomainMasterError();

    String StorageDomainMasterUnmountError();

    String StorageDomainMetadataCreationError();

    String StorageDomainMetadataFileMissing();

    String StorageDomainMetadataNotFound();

    String StorageDomainNotActive();

    String StorageDomainNotInPool();

    String StorageException();

    String StoragePoolActionError();

    String StoragePoolConnectionError();

    String StoragePoolCreationError();

    String StoragePoolDestroyingError();

    String StoragePoolDisconnectionError();

    String StoragePoolHasPotentialMaster();

    String StoragePoolInternalError();

    String StoragePoolMasterNotFound();

    String StoragePoolNotConnected();

    String StoragePoolTooManyMasters();

    String StoragePoolWrongMaster();

    String StoragePoolUnknown();

    String StorageServerActionError();

    String StorageServerConnectionError();

    String StorageServerDisconnectionError();

    String StorageServeriSCSIError();

    String StorageServerValidationError();

    String StorageUpdateVmError();

    String TaskClearError();

    String TemplateCreationError();

    String UnknownTask();

    String VolumeAccessError();

    String VolumeAlreadyExists();

    String VolumeCreationError();

    String VolumeDoesNotExist();

    String VolumeExtendingError();

    String VolumeGeneralException();

    String UnicodeArgumentException();

    String VolumeGroupActionError();

    String VolumeGroupAlreadyExistsError();

    String VolumeGroupCreateError();

    String VolumeGroupDoesNotExist();

    String VolumeGroupExtendError();

    String VolumeGroupPermissionsError();

    String VolumeGroupSizeError();

    String OrphanVolumeError();

    String VolumeImageHasChildren();

    String VolumeIsBusy();

    String VolumeMetadataReadError();

    String VolumeMetadataWriteError();

    String VolumeUnlinkError();

    String copyerr();

    String createErr();

    String down();

    String exist();

    String imageErr();

    String migrateErr();

    String MissParam();

    String nfsErr();

    String noConPeer();

    String nonresp();

    String noVM();

    String HOT_PLUG_MEM();

    String noVmType();

    String outOfMem();

    String recovery();

    String sparse();

    String ticketErr();

    String unexpected();

    String unsupFormat();

    String AddiSCSINodeError();

    String AddiSCSIPortalError();

    String AddTaskError();

    String CannotActivateLogicalVolume();

    String CannotCloneVolume();

    String CannotShareVolume();

    String CopyImageError();

    String DestImageActionError();

    String GetAllLogicalVolumeTagsError();

    String GetFloppyListError();

    String GetiSCSISessionListError();

    String GetLogicalVolumeDevError();

    String GetLogicalVolumesByTagError();

    String GetLogicalVolumeTagError();

    String GetStorageDomainListError();

    String GetVolumeGroupListError();

    String ImageIsEmpty();

    String InternalVolumeNonWritable();

    String InvalidTaskType();

    String iSCSILoginError();

    String ISCSI_LOGIN_AUTH_ERROR();

    String ProblemWhileTryingToMountTarget();

    String iSCSISetupError();

    String IsoCannotBeMasterDomain();

    String IsSpm();

    String LogicalVolumeAddTagError();

    String LogicalVolumePermissionsError();

    String LogicalVolumeRefreshError();

    String LogicalVolumeRemoveTagError();

    String LogicalVolumeScanError();

    String LogicalVolumesListError();

    String MetaDataMappingError();

    String MiscBlockReadException();

    String MiscBlockWriteException();

    String MiscFileReadException();

    String MiscFileWriteException();

    String MultipathRestartError();

    String OperationInProgress();

    String RemoveiSCSINodeError();

    String RemoveiSCSIPortalError();

    String SetiSCSIAuthError();

    String SetiSCSIPasswdError();

    String SetiSCSIUsernameError();

    String SharedVolumeNonWritable();

    String SourceImageActionError();

    String StorageDomainTypeError();

    String StoragePoolAlreadyExists();

    String TaskInProgress();

    String TaskNotFinished();

    String VolumeCannotGetParent();

    String VolumeGroupAddTagError();

    String VolumeGroupHasDomainTag();

    String VolumeGroupReadTagError();

    String VolumeGroupRemoveTagError();

    String VolumeGroupScanError();

    String VolumeGroupUninitialized();

    String VolumeNonShareable();

    String VolumeNonWritable();

    String VolumeOwnershipError();

    String VolumesZeroingError();

    String StorageDomainNotMemberOfPool();

    String StorageDomainAccessError();

    String ERR_BAD_PARAMS();

    String ERR_BAD_ADDR();

    String ERR_BAD_NIC();

    String ERR_USED_NIC();

    String ERR_BAD_BONDING();

    String ERR_BAD_VLAN();

    String ERR_BAD_BRIDGE();

    String ERR_USED_BRIDGE();

    String ERR_FAILED_IFUP();

    String ENGINE();

    String IRS_IMAGE_STATUS_ILLEGAL();

    String IRS_REPOSITORY_NOT_FOUND();

    String MAC_POOL_INITIALIZATION_FAILED();

    String VDS_NETWORK_ERROR();

    String DeviceNotFound();

    String CannotModifyVolumeTime();

    String CannotDeleteVolume();

    String CannotDeleteSharedVolume();

    String NonLeafVolumeNotWritable();

    String VolumeCopyError();

    String ImageIsNotLegalChain();

    String CouldNotValideTemplateOnTargetDomain();

    String StoragePoolCheckError();

    String BackupCannotBeMasterDomain();

    String MissingOvfFileFromVM();

    String ImageNotOnTargetDomain();

    String VMPathNotExists();

    String CannotConnectMultiplePools();

    String StorageDomainStatusError();

    String StorageDomainCheckError();

    String StorageDomainTypeNotBackup();

    String LogicalVolumeRenameError();

    String CannotWriteAccessLogialVolume();

    String CannotSetRWLogicalVolume();

    String LVMSetupError();

    String CouldNotRetrievePhysicalVolumeList();

    String LogicalVolumeAlreadyExists();

    String StorageDomainAlreadyAttached();

    String MultipathSetupError();

    String LogicalVolumesScanError();

    String CannotActivateLogicalVolumes();

    String GetLogicalVolumeDataError();

    String CouldNotRetrieveLogicalVolumesList();

    String MetaDataParamError();

    String MiscBlockWriteIncomplete();

    String NO_FREE_VM_IN_POOL();

    String CannotDetachMasterStorageDomain();

    String createIllegalVolumeSnapshotError();

    String createVolumeRollbackError();

    String createVolumeSizeError();

    String DomainAlreadyLocked();

    String DomainLockDoesNotExist();

    String CannotRetrieveSpmStatus();

    String FileStorageDomainStaleNFSHandle();

    String InvalidJob();

    String ReachedMaxNumberOfHostsInDC();

    String InaccessiblePhysDev();

    String PartitionedPhysDev();

    String InvalidRecovery();

    String InvalidTaskMng();

    String MoveTemplateImageError();

    String MultipleMoveImageError();

    String OverwriteImageError();

    String prepareIllegalVolumeError();

    String StorageDomainActive();

    String StorageDomainClassError();

    String StorageDomainInsufficientPermissions();

    String StorageDomainStateTransitionIllegal();

    String StoragePoolConnected();

    String StoragePoolHigherVersionMasterFound();

    String StorageTypeError();

    String StorageServerAccessPermissionError();

    String MountTypeError();

    String MountParsingError();

    String InvalidIpAddress();

    String iSCSIifaceError();

    String iSCSILogoutError();

    String iSCSIDiscoveryError();

    String TaskAborted();

    String TaskDirError();

    String TaskHasRefs();

    String ActionStopped();

    String TaskMetaDataLoadError();

    String TaskMetaDataSaveError();

    String TaskPersistError();

    String TaskStateError();

    String TaskStateTransitionError();

    String UnmanagedTask();

    String VolumeGroupRemoveError();

    String VolumeGroupRenameError();

    String InvalidDefaultExceptionException();

    String MiscDirCleanupFailure();

    String ResourceNamespaceNotEmpty();

    String ResourceTimeout();

    String StoragePoolDescriptionTooLongError();

    String TooManyDomainsInStoragePoolError();

    String StorageDomainDescriptionTooLongError();

    String StorageDomainIsMadeFromTooManyPVs();

    String TooManyPVsInVG();

    String MetadataOverflowError();

    String MergeVolumeRollbackError();

    String ResourceReferenceInvalid();

    String ResourceAcqusitionFailed();

    String ENGINE_ERROR_CREATING_STORAGE_POOL();

    String unavail();

    String FAILED_CHANGE_CD_IS_MOUNTED();

    String destroyErr();

    String fenceAgent();

    String StorageDomainIllegalRemotePath();

    String CannotFormatAttachedStorageDomain();

    String CannotFormatStorageDomainInConnectedPool();

    String UnsupportedDomainVersion();

    String CurrentVersionTooAdvancedError();

    String PoolUpgradeInProgress();

    String NoSpaceLeftOnDomain();

    String MixedSDVersionError();

    String StorageDomainTargetUnsupported();

    String VolumeGroupReplaceTagError();

    String LogicalVolumeReplaceTagError();

    String MkfsError();

    String MissingTagOnLogicalVolume();

    String LogicalVolumeDoesNotExistError();

    String LogicalVolumeCachingError();

    String LogicalVolumeWrongTagError();

    String MetaDataGeneralError();

    String ResourceDoesNotExist();

    String InvalidResourceName();

    String CANT_RECONSTRUCT_WHEN_A_DOMAIN_IN_POOL_IS_LOCKED();

    String NO_IMPLEMENTATION();

    String FailedToPlugDisk();

    String FailedToUnPlugDisk();

    String VOLUME_WAS_NOT_PREPARED_BEFORE_TEARDOWN();

    String IMAGES_NOT_SUPPORTED_ERROR();

    String GET_FILE_LIST_ERROR();

    String STORAGE_DOMAIN_REFRESH_ERROR();

    String VOLUME_GROUP_BLOCK_SIZE_ERROR();

    String DEVICE_BLOCK_SIZE_NOT_SUPPORTED();

    String HOST_ALREADY_EXISTS();

    String NO_ACTIVE_ISO_DOMAIN_IN_DATA_CENTER();

    String PROVIDER_FAILURE();

    String PROVIDER_SSL_FAILURE();

    String PROVIDER_AUTHENTICATION_FAILURE();

    String FAILED_UPDATE_RUNNING_VM();

    String PROVIDER_IMPORT_CERTIFICATE_ERROR();

    String VM_NOT_QUALIFIED_FOR_SNAPSHOT_MERGE();

    String VM_INVALID_SERVER_CLUSTER_ID();

    String MIGRATION_DEST_INVALID_HOSTNAME();

    String MIGRATION_CANCEL_ERROR();

    String SNAPSHOT_FAILED();

    String DB();

    String MAC_POOL_NO_MACS_LEFT();

    String MAC_POOL_NOT_INITIALIZED();

    String RESOURCE_MANAGER_CANT_ALLOC_VDS_MIGRATION();

    String RESOURCE_MANAGER_MIGRATION_FAILED_AT_DST();

    String RESOURCE_MANAGER_VDS_NOT_FOUND();

    String VM_TEMPLATE_CANT_LOCATE_DISKS_IN_DB();

    String GlusterGeneralException();

    String GlusterPermissionDeniedException();

    String GlusterSyntaxErrorException();

    String GlusterMissingArgumentException();

    String GlusterCmdExecFailedException();

    String GlusterXmlErrorException();

    String GlusterVolumeDeleteFailed();

    String GlusterVolumeRemoveBricksFailed();

    String GlusterVolumeRemoveBricksStartFailed();

    String GlusterVolumeRemoveBricksStopFailed();

    String GlusterVolumeRemoveBricksCommitFailed();

    String AddBricksToGlusterVolumeFailed();

    String GlusterVolumeRebalanceStartFailed();

    String GlusterVolumeEmptyCheckFailed();

    String GlusterGeoRepPublicKeyFileCreateFailed();

    String GlusterGeoRepPublicKeyFileReadError();

    String GlusterGeoRepUserNotFound();

    String GlusterGeoRepPublicKeyWriteFailed();

    String GlusterMountBrokerRootCreateFailed();

    String GlusterGeoRepExecuteMountBrokerOptFailed();

    String GlusterGeoRepExecuteMountBrokerUserAddFailed();

    String GlusterGeoRepSessionCreateFailed();

    String GlusterVolumeGeoRepSessionResumeFailed();

    String GlusterGeoRepException();

    String GlusterVolumeRebalanceStopFailed();

    String GlusterVolumeReplaceBrickStartFailed();

    String GlusterHostRemoveFailedException();

    String GlusterHostIsNotPartOfCluster();

    String GlusterVolumeProfileStartFailed();

    String GlusterVolumeGeoRepSessionStartFailed();

    String GlusterVolumeProfileStopFailed();

    String GlusterVolumeGeoRepSessionPauseFailed();

    String ACTIVATE_NIC_FAILED();

    String DEACTIVATE_NIC_FAILED();

    String UPDATE_VNIC_FAILED();

    String MAC_ADDRESS_IS_IN_USE();

    String GlusterHookFailed();

    String GlusterHookEnableFailed();

    String GlusterHookDisableFailed();

    String GlusterHookConflict();

    String GlusterHookNotFound();

    String GlusterHookListException();

    String GlusterHostUUIDNotFound();

    String GlusterHookUpdateFailed();

    String GlusterHookAlreadyExists();

    String GlusterHookChecksumMismatch();

    String GlusterHookAddFailed();

    String GlusterHookRemoveFailed();

    String GlusterServicesActionFailed();

    String GlusterServiceActionNotSupported();

    String GlusterVolumeStatusAllFailedException();

    String GlusterVolumeRebalanceStatusFailedException();

    String GlusterVolumeGeoRepStatusFailed();

    String GlusterGeoRepConfigFailed();

    String GlusterVolumeRemoveBrickStatusFailed();

    String GlusterLibgfapiException();

    String GlfsStatvfsException();

    String GlfsInitException();

    String GlfsFiniException();

    String GlusterGeoRepSessionDeleteFailedException();

    String GlusterVolumeGeoRepSessionStopFailed();

    String SETUP_NETWORKS_ROLLBACK();

    String LABELED_NETWORK_INTERFACE_NOT_FOUND();

    String NETWORK_LABEL_CONFLICT();

    String UPDATE_NUM_VFS_FAILURE();

    String GetIsoListError();

    String GlusterSnapshotException();

    String GlusterSnapshotInfoFailedException();

    String GlusterSnapshotDeleteFailedException();

    String GlusterSnapshotActivateFailedException();

    String GlusterSnapshotDeactivateFailedException();

    String GlusterSnapshotRestoreFailedException();

    String GlusterSnapshotCreateFailedException();

    String GlusterSnapshotConfigFailedException();

    String GlusterSnapshotConfigSetFailedException();

    String GlusterSnapshotConfigGetFailedException();

    String GlusterSnapshotScheduleFlagUpdateFailedException();

    String GlusterDisableSnapshotScheduleFailedException();

    String GlusterHostStorageDeviceNotFoundException();

    String GlusterHostStorageDeviceInUseException();

    String GlusterHostStorageDeviceMountFailedException();

    String GlusterHostStorageDeviceFsTabFoundException();

    String GlusterHostStorageDevicePVCreateFailedException();

    String GlusterHostStorageDeviceLVConvertFailedException();

    String GlusterHostStorageDeviceLVChangeFailedException();

    String GlusterHostStorageDeviceMakeDirsFailedException();

    String GlusterHostStorageMountPointInUseException();

    String GlusterHostStorageDeviceVGCreateFailedException();

    String GlusterHostStorageDeviceVGScanFailedException();

    String UnsupportedGlusterVolumeReplicaCountError();

    String GlusterHostFailedToSetSelinuxContext();

    String GlusterHostFailedToRunRestorecon();

    String GlusterEventException();

    String GlusterWebhookAddException();

    String GlusterWebhookUpdateException();

    String GlusterWebhookSyncException();

    String GlusterWebhookDeleteException();

    String CINDER_ERROR();

    String V2V_JOB_DOESNT_EXIST();

    String V2V_NO_SUCH_OVF();

    String V2V_JOB_NOT_DONE();

    String V2V_JOB_ALREADY_EXIST();

    String UnsupportedOperationErr();

    String freezeErr();

    String thawErr();

    String VMCantBeObtained();

    String GraphicsConsoleCantBeObtained();

    String FailedToCreateWebsocketProxyTicket();

    String DefaultIconPairNotFound();

    String HOST_DEVICES_TAKEN_BY_OTHER_VM();

    String ResourceExhausted();

    String FailedToCreateLease();

    String GlusterVolumeResetBrickStartFailed();

    String INVALID_HA_VM_LEASE();

    String InvalidParameter();
}

