package org.ovirt.engine.core.bll.provider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
    @SuppressWarnings("unchecked")
    public void nameNotAvailable() {
        Provider<AdditionalProperties> otherProvider = createProvider(provider.getName());
        when((Provider<AdditionalProperties>) providerDao.getByName(provider.getName())).thenReturn(otherProvider);
        assertThat(validator.nameAvailable(), failsWith(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED));
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
}
