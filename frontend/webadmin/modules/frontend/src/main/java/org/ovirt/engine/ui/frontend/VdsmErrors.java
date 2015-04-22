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

    String HostIdMismatch();

    String ImageDeleteError();

    String ImageDoesNotExistInDomainError();

    String ImageIsNotEmpty();

    String ImageMissingFromVm();

    String ImagePathError();

    String ImagesActionError();

    String ImageValidationError();

    String ImportError();

    String ImportInfoError();

    String ImportUnknownType();

    String IncorrectFormat();

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

    String VDS_FENCING_OPERATION_FAILED();

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

    String VM_WITH_SAME_NAME_EXIST();

    @DefaultStringValue("Gluster General Exception occurred.")
    String GlusterGeneralException();

    @DefaultStringValue("Permission denied")
    String GlusterPermissionDeniedException();

    @DefaultStringValue("Command failed due to a Syntax error")
    String GlusterSyntaxErrorException();

    @DefaultStringValue("Command failed due to a missing argument")
    String GlusterMissingArgumentException();

    @DefaultStringValue("Command execution failed")
    String GlusterCmdExecFailedException();

    @DefaultStringValue("XML error")
    String GlusterXmlErrorException();

    @DefaultStringValue("Gluster Volume Delete Failed.")
    String GlusterVolumeDeleteFailed();

    @DefaultStringValue("Gluster Volume Remove Bricks Failed.")
    String GlusterVolumeRemoveBricksFailed();

    @DefaultStringValue("Failed to start removal of Bricks from Gluster Volume.")
    String GlusterVolumeRemoveBricksStartFailed();

    @DefaultStringValue("Failed to stop removal of Bricks from Gluster Volume.")
    String GlusterVolumeRemoveBricksStopFailed();

    @DefaultStringValue("Failed to commit removal of Bricks from Gluster Volume.")
    String GlusterVolumeRemoveBricksCommitFailed();

    @DefaultStringValue("Gluster Volume Add Brick Failed.")
    String AddBricksToGlusterVolumeFailed();

    @DefaultStringValue("Gluster Volume Rebalance Start Failed.")
    String GlusterVolumeRebalanceStartFailed();

    @DefaultStringValue("Failed to check if gluster volume is empty")
    String GlusterVolumeEmptyCheckFailed();

    @DefaultStringValue("Failed to create gluster host public key file.")
    String GlusterGeoRepPublicKeyFileCreateFailed();

    @DefaultStringValue("Error in reading gluster host public key")
    String GlusterGeoRepPublicKeyFileReadError();

    @DefaultStringValue("Geo-replication user does not exist.")
    String GlusterGeoRepUserNotFound();

    @DefaultStringValue("Failed to write gluster host public key")
    String GlusterGeoRepPublicKeyWriteFailed();

    @DefaultStringValue("Failed to create root geo-rep mount broker.")
    String GlusterMountBrokerRootCreateFailed();

    @DefaultStringValue("Failed to set geo rep mount broker option.")
    String GlusterGeoRepExecuteMountBrokerOptFailed();

    @DefaultStringValue("Failed to add geo rep mount broker user.")
    String GlusterGeoRepExecuteMountBrokerUserAddFailed();

    @DefaultStringValue("Failed to create geo-replication session.")
    String GlusterGeoRepSessionCreateFailed();

    @DefaultStringValue("Resume of geo-replication session failed on gluster volume")
    String GlusterVolumeGeoRepSessionResumeFailed();

    @DefaultStringValue("Gluster Geo-Replication Exception")
    String GlusterGeoRepException();

    @DefaultStringValue("Gluster Volume Rebalance Stop Failed.")
    String GlusterVolumeRebalanceStopFailed();

    @DefaultStringValue("Gluster Volume Replace Brick Start Failed.")
    String GlusterVolumeReplaceBrickStartFailed();

    @DefaultStringValue("Gluster Host Remove Failed.")
    String GlusterHostRemoveFailedException();

    @DefaultStringValue("Host is not part of the cluster")
    String GlusterHostIsNotPartOfCluster();

    @DefaultStringValue("Gluster Volume Profile Start Failed.")
    String GlusterVolumeProfileStartFailed();

    @DefaultStringValue("Gluster volume geo-replication start failed")
    String GlusterVolumeGeoRepSessionStartFailed();

    @DefaultStringValue("Gluster Volume Profile Stop Failed.")
    String GlusterVolumeProfileStopFailed();

    @DefaultStringValue("Failed to pause the geo-replication session.")
    String GlusterVolumeGeoRepSessionPauseFailed();

    String ACTIVATE_NIC_FAILED();

    String DEACTIVATE_NIC_FAILED();

    @DefaultStringValue("Failed to update VM Network Interface.")
    String UPDATE_VNIC_FAILED();

    @DefaultStringValue("Mac Address is in use.")
    String MAC_ADDRESS_IS_IN_USE();

    @DefaultStringValue("Gluster hook operation  failed")
    String GlusterHookFailed();

    @DefaultStringValue("Failed to enable gluster hook.")
    String GlusterHookEnableFailed();

    @DefaultStringValue("Failed to disable gluster hook.")
    String GlusterHookDisableFailed();

    @DefaultStringValue("Found conflicting hooks.")
    String GlusterHookConflict();

    @DefaultStringValue("Gluster hook not found")
    String GlusterHookNotFound();

    @DefaultStringValue("Failed to get gluster hook list")
    String GlusterHookListException();

    @DefaultStringValue("Gluster host UUID not found")
    String GlusterHostUUIDNotFound();

    @DefaultStringValue("Failed to fetch statuses of services.")
    String GlusterServicesListFailed();

    @DefaultStringValue("Failed to update gluster hook.")
    String GlusterHookUpdateFailed();

    @DefaultStringValue("Failed to add hook as hook already exists.")
    String GlusterHookAlreadyExists();

    @DefaultStringValue("Failed to update hook due to mismatch in checksum.")
    String GlusterHookChecksumMismatch();

    @DefaultStringValue("Failed to add hook")
    String GlusterHookAddFailed();

    @DefaultStringValue("Failed to remove hook")
    String GlusterHookRemoveFailed();

    @DefaultStringValue("Gluster service action failed")
    String GlusterServicesActionFailed();

    @DefaultStringValue("Gluster service action not supported")
    String GlusterServiceActionNotSupported();

    @DefaultStringValue("Failed to get gluster tasks list")
    String GlusterVolumeStatusAllFailedException();

    @DefaultStringValue("Failed to get gluster volume rebalance status")
    String GlusterVolumeRebalanceStatusFailedException();

    @DefaultStringValue("Failed to get status information of geo-replication session(s) on gluster volume")
    String GlusterVolumeGeoRepStatusFailed();

    @DefaultStringValue("Failed to modify geo-replication config")
    String GlusterGeoRepConfigFailed();

    @DefaultStringValue("Failed to get status of gluster volume remove bricks")
    String GlusterVolumeRemoveBrickStatusFailed();

    @DefaultStringValue("Command failed due to gluster libgfapi exception")
    String GlusterLibgfapiException();

    @DefaultStringValue("Failed to get gluster volume size info")
    String GlfsStatvfsException();

    @DefaultStringValue("Command failed while mounting gluster volume")
    String GlfsInitException();

    @DefaultStringValue("Command failed while unmounting gluster volume")
    String GlfsFiniException();

    @DefaultStringValue("Failed to delete geo-replication session")
    String GlusterGeoRepSessionDeleteFailedException();

    @DefaultStringValue("Failed to stop geo-replication session")
    String GlusterVolumeGeoRepSessionStopFailed();

    @DefaultStringValue("Reverting back to last known saved configuration.")
    String SETUP_NETWORKS_ROLLBACK();

    @DefaultStringValue("Failed to change the number of virtual functions.")
    String UPDATE_NUM_VFS_FAILURE();

    @DefaultStringValue("Cannot get list of images in ISO domain. " +
            "Please check that the storage domain status is Active")
    String GetIsoListError();

    @DefaultStringValue("Error in executing gluster snapshot command")
    String GlusterSnapshotException();

    @DefaultStringValue("Gluster snapshot info failed")
    String GlusterSnapshotInfoFailedException();

    @DefaultStringValue("Failed to delete gluster volume snapshot")
    String GlusterSnapshotDeleteFailedException();

    @DefaultStringValue("Failed to activate gluster volume snapshot")
    String GlusterSnapshotActivateFailedException();

    @DefaultStringValue("Failed to de-activate gluster volume snapshot")
    String GlusterSnapshotDeactivateFailedException();

    @DefaultStringValue("Failed to restore the gluster volume snapshot")
    String GlusterSnapshotRestoreFailedException();

    @DefaultStringValue("Failed to create snapshot for gluster volume")
    String GlusterSnapshotCreateFailedException();

    @DefaultStringValue("Failed to configure gluster volume snapshot")
    String GlusterSnapshotConfigFailedException();

    @DefaultStringValue("Failed to set the gluster volume snapshot configuration")
    String GlusterSnapshotConfigSetFailedException();

    @DefaultStringValue("Failed to get the gluster volume snapshot configuration")
    String GlusterSnapshotConfigGetFailedException();

    @DefaultStringValue("Storage device(s) not found")
    String GlusterHostStorageDeviceNotFoundException();

    @DefaultStringValue("Storage device(s) already in use")
    String GlusterHostStorageDeviceInUseException();

    @DefaultStringValue("Failed to mount the device")
    String GlusterHostStorageDeviceMountFailedException();

    @DefaultStringValue(" Failed to format the device")
    String GlusterHostStorageDeviceMkfsFailedException();

    @DefaultStringValue("FSTAB entry already exists for the device")
    String GlusterHostStorageDeviceFsTabFoundException();

    @DefaultStringValue("Failed to create LVM Physical Volume")
    String GlusterHostStorageDevicePVCreateFailedException();

    @DefaultStringValue("Failed to run lvconvert for device")
    String GlusterHostStorageDeviceLVConvertFailedException();

    @DefaultStringValue("Failed to run lvchange for the thin pool:")
    String GlusterHostStorageDeviceLVChangeFailedException();

    @DefaultStringValue("An error occurred on Cinder - '${cinderException}'")
    String CINDER_ERROR();
}
