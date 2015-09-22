package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.hostedengine.HostedEngineHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.AttachStorageDomainToPoolParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.GetExistingStorageDomainListParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;

/**
 * <pre>
 * Try to import the hosted engine storage domain which is already connected to the host by the hosted engine broker.
 * We use 1) the Storage Domain name from the config values
 * <code>{@linkplain org.ovirt.engine.core.common.config.ConfigValues#HostedEngineStorageDomainName}</code>
 * 2) The connection details are fetched from the deviceList
 * {@link org.ovirt.engine.core.common.vdscommands.VDSCommandType#GetDeviceList} connected in vdsm
 * (as the domain already connected) and crossed the storage domain info.
 * With that in hand we are able to get the connection user/pass (in case of block device)
 * </pre>
 */
public class ImportHostedEngineStorageDomainCommand<T extends StorageDomainManagementParameter> extends CommandBase<T> {

    @Inject
    private HostedEngineHelper hostedEngineHelper;
    private StorageDomain heStorageDomain;
    static final StorageType[] SUPPORTED_DOMAIN_TYPES =
            { StorageType.NFS, StorageType.FCP, StorageType.GLUSTERFS, StorageType.ISCSI };

    public ImportHostedEngineStorageDomainCommand(T parameters) {
        super(parameters);
    }

    public ImportHostedEngineStorageDomainCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean canDoAction() {
        // if sd imported already, fail
        if (hostedEngineHelper.getStorageDomain() != null) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_ALREADY_EXIST);
        }

        // fetch info on storage domain from VDSM, sets #heStorageDomain
        if (!fetchStorageDomainInfo()) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
        }

        if (!Arrays.asList(SUPPORTED_DOMAIN_TYPES).contains(heStorageDomain.getStorageType())) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_UNSUPPORTED);
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        StorageDomainManagementParameter addSdParams =
                new StorageDomainManagementParameter(heStorageDomain.getStorageStaticData());
        setVdsId(getParameters().getVdsId());
        addSdParams.setVdsId(getParameters().getVdsId());
        addSdParams.setStoragePoolId(getVds().getStoragePoolId());

        VdcActionType actionType = null;
        switch (heStorageDomain.getStorageType()) {
        case NFS:
        case GLUSTERFS:
            actionType = VdcActionType.AddExistingFileStorageDomain;
            break;
        case ISCSI:
        case FCP:
            actionType = VdcActionType.AddExistingBlockStorageDomain;
            discoverBlockConnectionDetails();
            removeHostedEngineLunDisk();
            break;
        }

        setSucceeded(getBackend().runInternalAction(
                actionType,
                addSdParams).getSucceeded());

        if (getSucceeded()) {
            AttachStorageDomainToPoolParameters attachSdParams =
                    new AttachStorageDomainToPoolParameters(
                            addSdParams.getStorageDomainId(),
                            addSdParams.getStoragePoolId());
            setSucceeded(getBackend().runInternalAction(
                    VdcActionType.AttachStorageDomainToPool,
                    attachSdParams).getSucceeded());
        }

        setActionReturnValue(heStorageDomain);
    }

    private void discoverBlockConnectionDetails() {
        // get device list
        VDSReturnValue getDeviceList = runVdsCommand(
                VDSCommandType.GetDeviceList,
                new GetDeviceListVDSCommandParameters(
                        getParameters().getVdsId(),
                        heStorageDomain.getStorageType()));

        if (getDeviceList.getSucceeded() && getDeviceList.getReturnValue() != null
                && heStorageDomain.getStorageStaticData().getStorage() != null) {
            for (LUNs lun : (ArrayList<LUNs>) getDeviceList.getReturnValue()) {
                // match a lun vgid to the domain vgid.
                if (heStorageDomain.getStorage().equals(lun.getvolume_group_id())) {
                    // found a lun. Use its connection details
                    heStorageDomain.getStorageStaticData()
                            .setConnection(lun.getLunConnections().get(0));
                    break;
                }
            }
            log.error("There are no luns with VG that match the SD VG '{}'."
                    + " Connections details are missing.  completing this automatic import");
        }
    }

    private void removeHostedEngineLunDisk() {
        List<BaseDisk> disks =
                getDbFacade().getBaseDiskDao().getDisksByAlias(StorageConstants.HOSTED_ENGINE_LUN_DISK_ALIAS);
        if (disks != null && !disks.isEmpty()) {
            BaseDisk heDirectLun = disks.get(0);
            VdcReturnValueBase removeDisk = getBackend().runInternalAction(
                    VdcActionType.RemoveDisk,
                    new RemoveDiskParameters(heDirectLun.getId()));
            if (!removeDisk.getSucceeded()) {
                setSucceeded(false);
                log.error("Failed to remove the hosted engine direct lun disk");
            }
        }
    }

    /**
     * This command should run internal only. No permission is needed.
     * @return empty collection. The subjects shouldn't be checked for permission.
     */
    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    private boolean fetchStorageDomainInfo() {
        VdcQueryReturnValue allDomainsQuery = getBackend().runInternalQuery(
                VdcQueryType.GetExistingStorageDomainList,
                new GetExistingStorageDomainListParameters(
                        getParameters().getVdsId(),
                        null,
                        StorageDomainType.Data,
                        null));
        if (allDomainsQuery.getSucceeded()) {
            for (StorageDomain sd : (List<StorageDomain>) allDomainsQuery.getReturnValue()) {
                if (sd.getName().equals(Config.<String>getValue(ConfigValues.HostedEngineStorageDomainName))) {
                    heStorageDomain = sd;
                    return true;
                }
            }
        } else {
            log.error("Failed query for all Storage Domains."
                    + " The import command can not proceed without this info");
        }
        return false;
    }
}
