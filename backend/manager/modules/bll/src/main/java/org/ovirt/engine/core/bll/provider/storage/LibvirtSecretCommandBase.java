package org.ovirt.engine.core.bll.provider.storage;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.connection.CINDERStorageHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LibvirtSecretParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LibvirtSecretDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VdsDao;

public abstract class LibvirtSecretCommandBase extends CommandBase<LibvirtSecretParameters> {

    private StorageDomain storageDomain;

    @Inject
    protected LibvirtSecretDao libvirtSecretDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private CINDERStorageHelper cinderStorageHelper;

    public LibvirtSecretCommandBase(LibvirtSecretParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(Guid.SYSTEM,
                VdcObjectType.System,
                ActionGroup.CREATE_STORAGE_POOL));
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__TYPE__AUTHENTICATION_KEY);
    }

    @Override
    protected void executeCommand() {
        getReturnValue().setActionReturnValue(getParameters().getLibvirtSecret().getId());
    }

    public String getLibvirtSecretUUID() {
        return getParameters().getLibvirtSecret().getId().toString();
    }

    @Override
    public StorageDomain getStorageDomain() {
        if (storageDomain == null) {
            Guid providerId = getParameters().getLibvirtSecret().getProviderId();
            List<StorageDomain> storageDomains = storageDomainDao.getAllByConnectionId(providerId);
            storageDomain = storageDomains.get(0);
        }
        return storageDomain;
    }

    protected List<VDS> getAllRunningVdssInPool() {
        return vdsDao.getAllForStoragePoolAndStatus(getStoragePool().getId(), VDSStatus.Up);
    }

    protected void registerLibvirtSecret() {
        if (getStorageDomain().getStatus() == StorageDomainStatus.Active) {
            List<VDS> hostsInStatusUp = getAllRunningVdssInPool();
            for (VDS vds : hostsInStatusUp) {
                cinderStorageHelper.registerLibvirtSecrets(
                        getStorageDomain(), vds, Collections.singletonList(getParameters().getLibvirtSecret()));
            }
        } else {
            log.info("Libvirt secret '{}' hasn't been registered since storage domain '{}' is not Active",
                    getParameters().getLibvirtSecret().getId(), getStorageDomain().getName());
        }
    }

    protected void unregisterLibvirtSecret() {
        if (getStorageDomain().getStatus() == StorageDomainStatus.Active) {
            List<VDS> hostsInStatusUp = getAllRunningVdssInPool();
            for (VDS vds : hostsInStatusUp) {
                cinderStorageHelper.unregisterLibvirtSecrets(
                        getStorageDomain(), vds, Collections.singletonList(getParameters().getLibvirtSecret()));
            }
        } else {
            log.info("Libvirt secret '{}' hasn't been unregistered since storage domain '{}' is not Active",
                    getParameters().getLibvirtSecret().getId(), getStorageDomain().getName());
        }
    }
}
