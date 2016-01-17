package org.ovirt.engine.core.bll.storage.connection;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.NfsMountPointConstraint;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStringUtils;

@InternalCommandAttribute
public class ConnectStorageToVdsCommand<T extends StorageServerConnectionParametersBase> extends
        StorageServerConnectionCommandBase<T> {
    public ConnectStorageToVdsCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
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
            if (!validate(validator.canVDSConnectToGlusterfs(getVds()))) {
                return false;
            }
        }

        if (checkIsConnectionFieldEmpty(conn)) {
            return false;
        }

        return true;
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
        Map<String, String> optionsMap = XmlRpcStringUtils.string2Map(mountOptions);

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
