package org.ovirt.engine.core.bll.provider;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.provider.ProviderDao;

public class ProviderValidator {

    protected Provider<?> provider;

    public ProviderValidator(Provider<?> provider) {
        this.provider = provider;
    }

    protected ProviderDao getProviderDao() {
        return DbFacade.getInstance().getProviderDao();
    }

    public ValidationResult nameAvailable() {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_NAME_ALREADY_USED)
                .when(getProviderDao().getByName(provider.getName()) != null);
    }

    public ValidationResult providerIsSet() {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_PROVIDER_DOESNT_EXIST)
                .when(provider == null);
    }

    /**
     * Specific validations that each sub-class can override and implement
     */
    public ValidationResult validateAddProvider() {
        return ValidationResult.VALID;
    }
}
