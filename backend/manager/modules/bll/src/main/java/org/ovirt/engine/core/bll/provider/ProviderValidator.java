package org.ovirt.engine.core.bll.provider;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.ReplacementUtils;

public class ProviderValidator {

    protected Provider<?> provider;

    public ProviderValidator(Provider<?> provider) {
        this.provider = provider;
    }

    protected ProviderDao getProviderDao() {
        return DbFacade.getInstance().getProviderDao();
    }

    public ValidationResult nameAvailable() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED)
                .when(getProviderDao().getByName(provider.getName()) != null);
    }

    public ValidationResult providerIsSet() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_DOESNT_EXIST)
                .when(provider == null);
    }

    /**
     * Specific validations that each sub-class can override and implement
     */
    public ValidationResult validateAddProvider() {
        return ValidationResult.VALID;
    }

    /**
     * Specific validations that each sub-class can override and implement
     */
    public ValidationResult validateRemoveProvider() {
        return ValidationResult.VALID;
    }

    /**
     * Validate that this action can be performed for read only providers
     */
    public ValidationResult validateReadOnlyActions() {
        if (provider.getType() != ProviderType.EXTERNAL_NETWORK){
            return ValidationResult.VALID;
        }
        boolean isReadOnly = ((OpenstackNetworkProviderProperties) provider.getAdditionalProperties()).getReadOnly();
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_EXTERNAL_PROVIDER_IS_READ_ONLY,
                getProviderNameReplacement()).when(isReadOnly);
    }

    private String getProviderNameReplacement() {
        return ReplacementUtils.getVariableAssignmentString(EngineMessage.ACTION_TYPE_FAILED_EXTERNAL_PROVIDER_IS_READ_ONLY, provider.getName());
    }
}
