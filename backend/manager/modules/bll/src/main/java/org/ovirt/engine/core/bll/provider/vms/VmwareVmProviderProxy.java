package org.ovirt.engine.core.bll.provider.vms;

import java.security.cert.Certificate;
import java.util.List;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.provider.ProviderProxy;
import org.ovirt.engine.core.bll.provider.ProviderValidator;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmwareVmProviderProperties;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.GetVmsFromExternalProviderQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsDao;

public class VmwareVmProviderProxy implements ProviderProxy {

    private Provider<VmwareVmProviderProperties> provider;

    public VmwareVmProviderProxy(Provider<VmwareVmProviderProperties> provider) {
        this.provider = provider;
    }

    @Override
    public void testConnection() {
        VdcQueryReturnValue retVal = Backend.getInstance().runInternalQuery(
                VdcQueryType.GetVmsFromExternalProvider,
                buildGetVmsFromExternalProviderQueryParameters());
        if (!retVal.getSucceeded()) {
            throw new EngineException(EngineError.PROVIDER_FAILURE, retVal.getExceptionString());
        }
    }

    private GetVmsFromExternalProviderQueryParameters buildGetVmsFromExternalProviderQueryParameters() {
        return new GetVmsFromExternalProviderQueryParameters(
                provider.getUrl(),
                provider.getUsername(),
                provider.getPassword(),
                OriginType.VMWARE,
                provider.getAdditionalProperties().getProxyHostId(),
                provider.getAdditionalProperties().getStoragePoolId()
                );
    }

    @Override
    public List<? extends Certificate> getCertificateChain() {
        return null;
    }

    @Override
    public void onAddition() {
    }

    @Override
    public void onModification() {
    }

    @Override
    public void onRemoval() {
    }

    @Override
    public ProviderValidator getProviderValidator() {
        return new ProviderValidator(provider) {
            @Override
            public ValidationResult validateAddProvider() {
                VmwareVmProviderProperties properties = VmwareVmProviderProxy.this.provider.getAdditionalProperties();
                Guid proxyHostId = properties.getProxyHostId();
                if (proxyHostId != null) {
                    VDS proxyHost = getVdsDao().get(proxyHostId);
                    if (proxyHost == null) {
                        return new ValidationResult(EngineMessage.VDS_DOES_NOT_EXIST);
                    }

                    if (!proxyHost.getStoragePoolId().equals(properties.getStoragePoolId())) {
                        return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VDS_NOT_IN_DEST_STORAGE_POOL);
                    }
                }
                return ValidationResult.VALID;
            }

            private VdsDao getVdsDao() {
                return DbFacade.getInstance().getVdsDao();
            }
        };
    }
}
