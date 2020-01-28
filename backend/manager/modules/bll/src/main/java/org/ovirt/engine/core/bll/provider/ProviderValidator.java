package org.ovirt.engine.core.bll.provider;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.provider.network.openstack.OpenStackTokenProviderFactory;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.OpenStackImageProviderProperties;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.ReplacementUtils;

public class ProviderValidator<P extends Provider.AdditionalProperties> {

    protected Provider<P> provider;

    public static final Pattern URL_PATTERN = Pattern.compile("^http(s)?://[^/]*:[\\d]*/?$");
    public static final Pattern AUTH_URL_PATTERN = Pattern.compile("^http(s)?://[^/]*:[\\d]+/(v3|v2\\.0)/?$");
    static final String VAR_AUTH_URL = "AuthUrl";

    public ProviderValidator(Provider<P> provider) {
        this.provider = provider;
    }

    public ValidationResult nameAvailable() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED)
                .when(Injector.get(ProviderDao.class).getByName(provider.getName()) != null);
    }

    public ValidationResult validateOpenStackImageConstraints() {
        if (ProviderType.OPENSTACK_IMAGE != provider.getType()) {
            return ValidationResult.VALID;
        }
        List<Provider<?>> existingProviders = Injector.get(ProviderDao.class).getAllByTypes(ProviderType.OPENSTACK_IMAGE);
        return OpenStackTokenProviderFactory.isApiV3(provider) ?
                openStackImageV3UrlAndNameExists(existingProviders) :
                openStackImageV2UrlAndTenantExists(existingProviders);
    }

    private ValidationResult openStackImageV3UrlAndNameExists(List<Provider<?>> existingProviders) {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_URL_PROJECT_NAME_AND_DOMAIN_NAME_COMBINATION_NOT_UNIQUE)
                .when(existingProviders.stream().anyMatch(this::matchesProviderUrlNameDomainNameCombination));
    }

    private boolean matchesProviderUrlNameDomainNameCombination(Provider provider) {
        return Objects.equals(provider.getUrl(), this.provider.getUrl()) &&
                Objects.equals(getProjectName(provider), getProjectName(this.provider)) &&
                Objects.equals(getProjectDomainName(provider), getProjectDomainName(this.provider));
    }

    private static String getProjectName(Provider provider) {
        return ((OpenStackImageProviderProperties) provider.getAdditionalProperties()).getProjectName();
    }

    private static String getProjectDomainName(Provider provider) {
        return ((OpenStackImageProviderProperties) provider.getAdditionalProperties()).getProjectDomainName();
    }

    private ValidationResult openStackImageV2UrlAndTenantExists(List<Provider<?>> existingProviders) {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_URL_TENANT_COMBINATION_NOT_UNIQUE)
                .when(existingProviders.stream().anyMatch(this::matchesProviderUrlTenantCombination));
    }

    private boolean matchesProviderUrlTenantCombination(Provider provider) {
        return Objects.equals(provider.getUrl(), this.provider.getUrl()) &&
                Objects.equals(getTenantName(provider), getTenantName(this.provider));
    }

    private static String getTenantName(Provider provider) {
        return ((OpenStackImageProviderProperties) provider.getAdditionalProperties()).getTenantName();
    }

    public ValidationResult providerIsSet() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_DOESNT_EXIST)
                .when(provider == null);
    }

    public ValidationResult validateAuthUrl() {
        String authUrl = provider.getAuthUrl();
        if (authUrl != null) {
            Matcher matcher = AUTH_URL_PATTERN.matcher(authUrl);
            return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_INVALID_AUTH_URL,
                    ReplacementUtils.createSetVariableString(VAR_AUTH_URL, authUrl))
                    .when(!matcher.matches());
        }
        return ValidationResult.VALID;
    }

    public ValidationResult validatePassword() {
        String password = provider.getPassword();
        if (password != null) {
            return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_PASSWORD_TOO_LONG)
                    .when(password.length() > BusinessEntitiesDefinitions.PROVIDER_PASSWORD_MAX_SIZE);
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
     * Specific validation that each sub-class can override and implement
     */
    public ValidationResult validateUpdateProvider() {
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
