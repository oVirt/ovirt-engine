package org.ovirt.engine.core.bll.provider;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.provider.ProviderDao;

public class ProviderValidator {

    private Provider provider;

    public ProviderValidator(Provider provider) {
        this.provider = provider;
    }

    protected ProviderDao getProviderDao() {
        return DbFacade.getInstance().getProviderDao();
    }

    public ValidationResult nameAvailable() {
        return getProviderDao().getByName(provider.getName()) == null
                ? ValidationResult.VALID
                : new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
    }

    public ValidationResult providerIsSet() {
        return provider == null ? new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_PROVIDER_DOESNT_EXIST)
                : ValidationResult.VALID;
    }
}
