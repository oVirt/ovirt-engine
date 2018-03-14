package org.ovirt.engine.core.bll.provider.vms;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.provider.ProviderProxy;
import org.ovirt.engine.core.bll.provider.ProviderValidator;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmProviderProperties;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.GetVmsFromExternalProviderQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;

public abstract class AbstractVmProviderProxy<P extends VmProviderProperties> implements ProviderProxy {
    protected Provider<P> provider;

    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private BackendInternal backend;

    protected AbstractVmProviderProxy(Provider<P> provider) {
        this.provider = provider;
    }

    @Override
    public void testConnection() {
        chooseDcForCheckingIfGetNamesFromExternalProviderSupported();

        QueryReturnValue retVal = backend.runInternalQuery(
                QueryType.GetVmsFromExternalProvider,
                buildGetVmsFromExternalProviderQueryParameters());
        if (!retVal.getSucceeded()) {
            throw new EngineException(EngineError.PROVIDER_FAILURE, retVal.getExceptionString());
        }
    }

    protected abstract GetVmsFromExternalProviderQueryParameters buildGetVmsFromExternalProviderQueryParameters();

    public ProviderValidator getProviderValidator() {
        return new ProviderValidator<P>(provider) {
            @Override
            public ValidationResult validateAddProvider() {
                P properties = provider.getAdditionalProperties();
                Guid proxyHostId = properties.getProxyHostId();
                if (proxyHostId != null) {
                    VDS proxyHost = vdsDao.get(proxyHostId);
                    if (proxyHost == null) {
                        return new ValidationResult(EngineMessage.VDS_DOES_NOT_EXIST);
                    }

                    if (!proxyHost.getStoragePoolId().equals(properties.getStoragePoolId())) {
                        return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VDS_NOT_IN_DEST_STORAGE_POOL);
                    }
                }
                return ValidationResult.VALID;
            }
        };
    }

    private void chooseDcForCheckingIfGetNamesFromExternalProviderSupported() {
        Version chosenDataCenterVersion = null;
        Guid chosenDataCenterId = provider.getAdditionalProperties().getStoragePoolId();

        if (chosenDataCenterId == null) {
            // find data center with highest version
            for (StoragePool sp : storagePoolDao.getAllByStatus(StoragePoolStatus.Up)) {
                if (chosenDataCenterVersion == null || chosenDataCenterVersion.less(sp.getCompatibilityVersion())) {
                    chosenDataCenterVersion = sp.getCompatibilityVersion();
                    chosenDataCenterId = sp.getId();
                }
            }
            provider.getAdditionalProperties().setStoragePoolId(chosenDataCenterId);
        }
    }
}
