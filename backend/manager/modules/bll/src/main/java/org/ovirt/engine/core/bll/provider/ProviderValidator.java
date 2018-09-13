package org.ovirt.engine.core.bll.provider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.ReplacementUtils;

public class ProviderValidator<P extends Provider.AdditionalProperties> {

    protected Provider<P> provider;

    private static final Pattern pattern = Pattern.compile("^http(s)?://[^/]*:[\\d]+/(v3|v2\\.0)/?$");
    static final String VAR_AUTH_URL = "AuthUrl";

    public ProviderValidator(Provider<P> provider) {
        this.provider = provider;
    }

    public ValidationResult nameAvailable() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED)
                .when(Injector.get(ProviderDao.class).getByName(provider.getName()) != null);
    }

    public ValidationResult providerIsSet() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_DOESNT_EXIST)
                .when(provider == null);
    }

    public ValidationResult validateAuthUrl() {
        String authUrl = provider.getAuthUrl();
        if (authUrl != null) {
            Matcher matcher = pattern.matcher(authUrl);
            return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_INVALID_AUTH_URL,
                    ReplacementUtils.createSetVariableString(VAR_AUTH_URL, authUrl))
                    .when(!matcher.matches());
        }
        return ValidationResult.VALID;
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
