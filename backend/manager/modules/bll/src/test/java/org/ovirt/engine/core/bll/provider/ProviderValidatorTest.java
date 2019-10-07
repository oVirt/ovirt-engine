package org.ovirt.engine.core.bll.provider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.OpenStackImageProviderProperties;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.Provider.AdditionalProperties;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.InjectedMock;
import org.ovirt.engine.core.utils.InjectorExtension;
import org.ovirt.engine.core.utils.ReplacementUtils;

@ExtendWith({MockitoExtension.class, InjectorExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProviderValidatorTest {

    private final String INVALID_AUTH_URL = "http://invalid-auth.com";
    private final String VALID_AUTH_URL_IPV4 = "http://192.168.123.137:35357/v3";
    private final String VALID_AUTH_URL_IPV6 = "http://[2001:DB8::1]:35357/v3";
    private final String AUTH_URL_LOCALHOST = "http://localhost:35357/v2.0/";

    protected Provider<AdditionalProperties> provider = createProvider("provider");

    private ProviderValidator validator = new ProviderValidator(provider);

    @Mock
    @InjectedMock
    public ProviderDao providerDao;

    @Test
    public void providerIsSet() {
        assertThat(validator.providerIsSet(), isValid());
    }

    @Test
    public void providerIsNotSet() {
        validator = new ProviderValidator(null);
        assertThat(validator.providerIsSet(), failsWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_DOESNT_EXIST));
    }

    @Test
    public void nameAvailable() {
        assertThat(validator.nameAvailable(), isValid());
    }

    @Test
    public void authUrlNull() {
        assertThat(validator.validateAuthUrl(), isValid());
    }

    @Test
    public void authUrlInvalid() {
        when(provider.getAuthUrl()).thenReturn(INVALID_AUTH_URL);
        EngineMessage engineMessage = EngineMessage.ACTION_TYPE_FAILED_PROVIDER_INVALID_AUTH_URL;
        assertThat(validator.validateAuthUrl(), failsWith(engineMessage,
                ReplacementUtils.createSetVariableString(ProviderValidator.VAR_AUTH_URL, INVALID_AUTH_URL)));
    }

    @Test
    public void authUrlValidIpv4() {
        when(provider.getAuthUrl()).thenReturn(VALID_AUTH_URL_IPV4);
        assertThat(validator.validateAuthUrl(), isValid());
    }

    @Test
    public void authUrlValidIpv6() {
        when(provider.getAuthUrl()).thenReturn(VALID_AUTH_URL_IPV6);
        assertThat(validator.validateAuthUrl(), isValid());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void nameNotAvailable() {
        Provider<AdditionalProperties> otherProvider = createProvider(provider.getName());
        when((Provider<AdditionalProperties>) providerDao.getByName(provider.getName())).thenReturn(otherProvider);
        assertThat(validator.nameAvailable(), failsWith(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void openStackImageV2ConstraintsInvalid() {
        String url = "http://localhost:9292";
        String tenant = "admin";
        Provider<OpenStackImageProviderProperties> existingProvider = createOpenStackV2Provider("existing", url, tenant);
        Provider<OpenStackImageProviderProperties> newProvider = createOpenStackV2Provider("new", url, tenant);
        when(providerDao.getAllByTypes(ProviderType.OPENSTACK_IMAGE)).thenReturn(Collections.singletonList(existingProvider));
        ProviderValidator validator = new ProviderValidator(newProvider);
        assertThat(validator.validateOpenStackImageConstraints(), failsWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_URL_TENANT_COMBINATION_NOT_UNIQUE));
    }

    @Test
    public void testValidateReadOnlyActions() {

        String providerName = "providerName";
        OpenstackNetworkProviderProperties additionalProperties = new OpenstackNetworkProviderProperties();
        additionalProperties.setReadOnly(true);
        Provider<AdditionalProperties> provider = new Provider<> ();
        provider.setAdditionalProperties(additionalProperties);
        provider.setType(ProviderType.EXTERNAL_NETWORK);
        provider.setName(providerName);
        ProviderValidator validator = new ProviderValidator(provider);

        EngineMessage engineMessage = EngineMessage.ACTION_TYPE_FAILED_EXTERNAL_PROVIDER_IS_READ_ONLY;
        assertThat(validator.validateReadOnlyActions(), failsWith(engineMessage,
                ReplacementUtils.getVariableAssignmentString(engineMessage, providerName)));

        additionalProperties.setReadOnly(false);
        assertThat(validator.validateReadOnlyActions(), isValid());
        provider.setType(ProviderType.OPENSTACK_NETWORK);
        assertThat(validator.validateReadOnlyActions(), isValid());
    }

    @SuppressWarnings("unchecked")
    private Provider<AdditionalProperties> createProvider(String name) {
        Provider<AdditionalProperties> p = mock(Provider.class);
        when(p.getName()).thenReturn(name);
        return p;
    }

    @SuppressWarnings("unchecked")
    private Provider<OpenStackImageProviderProperties> createOpenStackV2Provider(String name, String url, String tenant) {
        Provider<OpenStackImageProviderProperties> result = mock(Provider.class);
        when(result.getName()).thenReturn(name);
        when(result.getType()).thenReturn(ProviderType.OPENSTACK_IMAGE);
        when(result.getAuthUrl()).thenReturn(AUTH_URL_LOCALHOST);
        OpenStackImageProviderProperties props = mock(OpenStackImageProviderProperties.class);
        when(props.getTenantName()).thenReturn(tenant);
        when(result.getAdditionalProperties()).thenReturn(props);
        when(result.getUrl()).thenReturn(url);
        return result;
    }
}
