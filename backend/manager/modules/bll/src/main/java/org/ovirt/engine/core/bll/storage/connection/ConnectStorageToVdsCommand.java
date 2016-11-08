package org.ovirt.engine.core.bll.storage.connection;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.storage.StorageConnectionValidator;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.NfsMountPointConstraint;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.StringMapUtils;

@InternalCommandAttribute
public class ConnectStorageToVdsCommand<T extends StorageServerConnectionParametersBase> extends
        StorageServerConnectionCommandBase<T> {
    private static final String KEY_VALUE_SEPARATOR = "=";

    public ConnectStorageToVdsCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public ConnectStorageToVdsCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        Pair<Boolean, Integer> result = connectHostToStorage();
        setSucceeded(result.getFirst());
        if (!result.getFirst()) {
            setErrorMessageAtReturn(result);
        }
    }

    private void setErrorMessageAtReturn(Pair<Boolean, Integer> result) {
        EngineFault fault = new EngineFault();
        fault.setError(result.getSecond());
        if (fault.getError() != null) {
            fault.setMessage(
                    Backend.getInstance()
                            .getVdsErrorsTranslator()
                            .translateErrorTextSingle(fault.getError().toString()));
        }
        getReturnValue().setFault(fault);
    }

    protected Pair<Boolean, Integer> connectHostToStorage() {
        List<StorageServerConnections> connections = Arrays.asList(getConnection());

        if (getConnection().getStorageType() == StorageType.ISCSI) {
            connections = ISCSIStorageHelper.updateIfaces(connections, getVds().getId());
        }

        Map<String, String> result = (HashMap<String, String>) runVdsCommand(
                VDSCommandType.ConnectStorageServer,
                new StorageServerConnectionManagementVDSParameters(getVds().getId(), Guid.Empty,
                        getConnection().getStorageType(), connections)).getReturnValue();

        return new Pair<>(StorageHelperDirector.getInstance()
                .getItem(getConnection().getStorageType())
                .isConnectSucceeded(result, connections),
                Integer.parseInt(result.values().iterator().next()));
    }

    protected boolean isValidStorageConnectionPort(String port) {
         return !StringUtils.isEmpty(port) && StringUtils.isNumeric(port) && Integer.parseInt(port) > 0;
    }

    protected boolean isValidConnection(StorageServerConnections conn) {
        StorageType storageType = conn.getStorageType();

        if (storageType == StorageType.NFS && !new NfsMountPointConstraint().isValid(conn.getConnection(), null)) {
            return failValidation(EngineMessage.VALIDATION_STORAGE_CONNECTION_INVALID);
        }

        if (storageType == StorageType.POSIXFS && StringUtils.isEmpty(conn.getVfsType())) {
            return failValidation(EngineMessage.VALIDATION_STORAGE_CONNECTION_EMPTY_VFSTYPE);
        }

        if ((storageType == StorageType.POSIXFS || storageType == StorageType.NFS) && !validate(validateMountOptions())) {
            return false;
        }

        if (storageType == StorageType.ISCSI) {
            if (StringUtils.isEmpty(conn.getIqn())) {
                return failValidation(EngineMessage.VALIDATION_STORAGE_CONNECTION_EMPTY_IQN);
            }
            if (!isValidStorageConnectionPort(conn.getPort())) {
                return failValidation(EngineMessage.VALIDATION_STORAGE_CONNECTION_INVALID_PORT);
            }
        }

        if (storageType == StorageType.GLUSTERFS) {
            StorageConnectionValidator validator = new StorageConnectionValidator(conn);
            if (!validate(validateVolumeIdAndUpdatePath(conn))
                    || !validate(validator.canVDSConnectToGlusterfs(getVds()))) {
                return false;
            }
        }

        if (checkIsConnectionFieldEmpty(conn)) {
            return false;
        }

        return true;
    }

    private ValidationResult validateVolumeIdAndUpdatePath(StorageServerConnections connection) {
        if (connection.getGlusterVolumeId() != null) {
            GlusterVolumeEntity glusterVolume = glusterVolumeDao.getById(connection.getGlusterVolumeId());
            if (glusterVolume == null || glusterVolume.getBricks().isEmpty()) {
                return new ValidationResult(EngineMessage.VALIDATION_STORAGE_CONNECTION_INVALID_GLUSTER_VOLUME);
            }
            Set<String> addressSet = new LinkedHashSet<>();
            glusterVolume.getBricks().stream().forEach(
                    brick -> addressSet.add(brick.getNetworkId() != null && !brick.getNetworkAddress().isEmpty()
                            ? brick.getNetworkAddress() : brick.getServerName()));
            String firstHost = (String) addressSet.toArray()[0];
            connection.setConnection(firstHost + StorageConstants.GLUSTER_VOL_SEPARATOR + glusterVolume.getName());
            String mountOptions = StorageConstants.GLUSTER_BACKUP_SERVERS_MNT_OPTION
                    + KEY_VALUE_SEPARATOR + StringUtils.join(addressSet.toArray(), ':');
            if (StringUtils.isBlank(connection.getMountOptions())) {
                connection.setMountOptions(mountOptions);
            } else if (!connection.getMountOptions().contains(StorageConstants.GLUSTER_BACKUP_SERVERS_MNT_OPTION)) {
                mountOptions = connection.getMountOptions().concat("," + mountOptions);
                connection.setMountOptions(mountOptions);
            }
        }
        return ValidationResult.VALID;
    }

    private static final List<String> NFS_MANAGED_OPTIONS = Arrays.asList("timeo", "retrans", "vfs_type", "protocol_version", "nfsvers", "vers", "minorversion", "addr", "clientaddr");
    private static final List<String> POSIX_MANAGED_OPTIONS = Arrays.asList("vfs_type", "addr", "clientaddr");

    protected ValidationResult validateMountOptions() {
        String mountOptions = getConnection().getMountOptions();
        if (StringUtils.isBlank(mountOptions)) {
            return ValidationResult.VALID;
        }

        List<String> disallowedOptions =
                getConnection().getStorageType() == StorageType.POSIXFS ? POSIX_MANAGED_OPTIONS : NFS_MANAGED_OPTIONS;
        Map<String, String> optionsMap = StringMapUtils.string2Map(mountOptions);

        Set<String> optionsKeys = new HashSet<>();
        for (String option : optionsMap.keySet()) {
            optionsKeys.add(option.toLowerCase());
        }

        optionsKeys.retainAll(disallowedOptions);

        if (!optionsKeys.isEmpty()) {
            addValidationMessageVariable("invalidOptions", StringUtils.join(optionsKeys, ", "));
            return new ValidationResult(EngineMessage.VALIDATION_STORAGE_CONNECTION_MOUNT_OPTIONS_CONTAINS_MANAGED_PROPERTY);
        }

        return ValidationResult.VALID;
    }
}
