package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.utils.ObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.VDSCommandBase;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSNoMasterDomainException;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSNonOperationalException;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSUnicodeArgumentException;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsOperationFailedNoFailoverException;

public abstract class BrokerCommandBase<P extends VDSParametersBase> extends VDSCommandBase<P> {
    public BrokerCommandBase(P parameters) {
        super(parameters);
    }

    protected StatusOnlyReturn status;

    protected Status getReturnStatus() {
        return status.status;
    }

    protected void initializeVdsError(EngineError returnStatus) {
        VDSError tempVar = new VDSError();
        tempVar.setCode(returnStatus);
        tempVar.setMessage(getReturnStatus().message);
        getVDSReturnValue().setVdsError(tempVar);
    }

    protected void proceedProxyReturnValue() {
        EngineError returnStatus = getReturnValueFromStatus(getReturnStatus());
        VDSExceptionBase outEx;
        switch (returnStatus) {
        case Done:
            return;
        case recovery:
            outEx = new VDSRecoveringException(returnStatus, getReturnStatus().message);
            break;
        case SpmStatusError:
            outEx = new IRSNonOperationalException(getReturnStatus().message);
            break;
        case StoragePoolMasterNotFound:
        case StoragePoolTooManyMasters:
        case StoragePoolWrongMaster:
        case StoragePoolHasPotentialMaster:
        case StorageDomainMasterError:
            outEx = new IRSNoMasterDomainException(getReturnStatus().message);
            break;
        case UnicodeArgumentException:
            outEx = new IRSUnicodeArgumentException(getReturnStatus().message);
            break;
        case TooManyDomainsInStoragePoolError:
        case StorageDomainAlreadyAttached:
        case StorageDomainDescriptionTooLongError:
        case TooManyPVsInVG:
        case createIllegalVolumeSnapshotError:
        case prepareIllegalVolumeError:
        case createVolumeRollbackError:
        case InvalidParameterException:
        case InvalidDefaultExceptionException:
        case NotImplementedException:
        case OperationInProgress:
        case MiscDirCleanupFailure:
        case createVolumeSizeError:
        case IncorrectFormat:
        case VolumeIsBusy:
        case VolumeImageHasChildren:
        case VolumeUnlinkError:
        case OrphanVolumeError:
        case VolumeAlreadyExists:
        case VolumeNonWritable:
        case VolumeNonShareable:
        case VolumeCannotGetParent:
        case SharedVolumeNonWritable:
        case InternalVolumeNonWritable:
        case CannotDeleteSharedVolume:
        case NonLeafVolumeNotWritable:
        case ImagesActionError:
        case ImageValidationError:
        case ImageDeleteError:
        case ImageIsNotEmpty:
        case ImageIsNotLegalChain:
        case OverwriteImageError:
        case MoveTemplateImageError:
        case StoragePoolDisconnectionError:
        case StoragePoolAlreadyExists:
        case IsoCannotBeMasterDomain:
        case CannotConnectMultiplePools:
        case BackupCannotBeMasterDomain:
        case StoragePoolConnected:
        case StoragePoolDescriptionTooLongError:
        case StorageDomainNotInPool:
        case StorageDomainNotEmpty:
        case StorageDomainMetadataCreationError:
        case StorageDomainMetadataFileMissing:
        case StorageDomainMetadataNotFound:
        case StorageDomainAlreadyExists:
        case StorageDomainMasterUnmountError:
        case BlockStorageDomainMasterFSCKError:
        case StorageDomainLayoutError:
        case StorageDomainTypeError:
        case StorageDomainNotMemberOfPool:
        case StorageDomainStatusError:
        case StorageDomainCheckError:
        case StorageDomainTypeNotBackup:
        case StorageDomainStateTransitionIllegal:
        case StorageDomainActive:
        case CannotDetachMasterStorageDomain:
        case StorageDomainInsufficientPermissions:
        case StorageDomainClassError:
        case StorageDomainIsMadeFromTooManyPVs:
        case InvalidTask:
        case UnknownTask:
        case TaskClearError:
        case TaskNotFinished:
        case InvalidTaskType:
        case AddTaskError:
        case TaskInProgress:
        case TaskStateError:
        case TaskAborted:
        case TaskPersistError:
        case InvalidJob:
        case InvalidRecovery:
        case InvalidTaskMng:
        case TaskStateTransitionError:
        case TaskHasRefs:
        case VolumeGroupSizeError:
        case VolumeGroupAlreadyExistsError:
        case VolumeGroupUninitialized:
        case VolumeGroupHasDomainTag:
        case CannotRemoveLogicalVolume:
        case CannotDeactivateLogicalVolume:
        case CannotActivateLogicalVolume:
        case LogicalVolumePermissionsError:
        case LogicalVolumeAlreadyExists:
        case PartitionedPhysDev:
        case DomainAlreadyLocked:
        case DomainLockDoesNotExist:
        case MetaDataKeyError:
        case MetaDataSealIsBroken:
        case MetaDataValidationError:
        case MetaDataMappingError:
        case MetaDataParamError:
        case MetadataOverflowError:
        case ImportUnknownType:
        case ExportError:
        case MergeVolumeRollbackError:
        case ActionStopped:
        case FAILED_CHANGE_CD_IS_MOUNTED:
        case UnsupportedDomainVersion:
        case CurrentVersionTooAdvancedError:
        case iSCSILogoutError:
        case iSCSIDiscoveryError:
        case ISCSI_LOGIN_AUTH_ERROR:
        case PoolUpgradeInProgress:
        case MixedSDVersionError:
        case NoSpaceLeftOnDomain:
        case ImageDoesNotExistInDomainError:
        case NO_IMPLEMENTATION:
        case VOLUME_WAS_NOT_PREPARED_BEFORE_TEARDOWN:
        case IMAGES_NOT_SUPPORTED_ERROR:
        case GET_FILE_LIST_ERROR:
        case STORAGE_DOMAIN_REFRESH_ERROR:
        case VOLUME_GROUP_BLOCK_SIZE_ERROR:
        case MIGRATION_DEST_INVALID_HOSTNAME:
        case ResourceTimeout:
        case HOT_PLUG_UNPLUG_CPU_ERROR:
        case DEVICE_BLOCK_SIZE_NOT_SUPPORTED:
        case V2V_JOB_DOESNT_EXIST:
        case V2V_NO_SUCH_OVF:
        case V2V_JOB_NOT_DONE:
        case V2V_JOB_ALREADY_EXIST:
        case UnsupportedGlusterVolumeReplicaCountError:
        case MissingOvfFileFromVM:
        case ReplicationNotInProgress:
        case NoSuchVmLeaseOnDomain:
            if (this instanceof IrsBrokerCommand || this instanceof StorageJobVDSCommand) {
                outEx = new IrsOperationFailedNoFailoverException(getReturnStatus().message);
            } else {
                outEx = new VDSErrorException(String.format("Failed in vdscommand to %1$s, error = %2$s",
                        getCommandName(), getReturnStatus().message));
            }
            break;
        case ResourceExhausted:
        case VDS_NETWORK_ERROR:
        case ERR_BAD_ADDR:
            outEx = new VDSNetworkException(getReturnStatus().message);
            break;
        default:
            log.error("Failed in '{}' method", getCommandName());
            outEx = createException();
            break;
        }
        VDSError tempVar = new VDSError();
        tempVar.setCode(returnStatus);
        tempVar.setMessage(getReturnStatus().message);
        outEx.setVdsError(tempVar);

        logToAuditIfNeeded();

        throw outEx;
    }

    private void logToAuditIfNeeded(){
        if (shouldLogToAudit()) {
            logToAudit();
        }
    }

    protected boolean shouldLogToAudit() {
        // if error is in expected errors list, don't audit log it
        return !getParameters().getExpectedEngineErrors().contains(getReturnValueFromStatus(getReturnStatus()));
    }

    protected void logToAudit(){
    }

    private VDSExceptionBase createException() {
        final String errorMessage = String.format("Failed to %1$s, error = %2$s, code = %3$s", getCommandName(),
                getReturnStatus().message, getReturnStatus().code);
        return createDefaultConcreteException(errorMessage);
    }

    protected abstract VDSExceptionBase createDefaultConcreteException(String errorMessage);

    protected EngineError getReturnValueFromStatus(Status status) {
        try {
            EngineError bllErrors = EngineError.forValue(status.code);
            if (bllErrors == null) {
                log.warn("Unexpected return value: {}", status);
                bllErrors = EngineError.unexpected;
            }
            return bllErrors;
        } catch (Exception e) {
            return EngineError.unexpected;
        }
    }

    protected Object getReturnValueFromBroker() {
        return status;
    }

    protected boolean getIsPrintReturnValue() {
        return true;
    }

    protected String getAdditionalInformation() {
        return "";
    }

    protected void printReturnValue() {
        if (getReturnValueFromBroker() != null && getIsPrintReturnValue()) {
            StringBuilder builder = new StringBuilder();
            ObjectDescriptor.toStringBuilder(getReturnValueFromBroker(), builder);
            log.info("Command '{}' return value '{}'", getClass().getName(), builder.toString());
            if (!StringUtils.isEmpty(getAdditionalInformation())) {
                log.info(getAdditionalInformation());
            }
        }
    }
}
