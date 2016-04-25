package org.ovirt.engine.core.bll.provider;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.Provider.AdditionalProperties;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.ReplacementUtils;

@RunWith(MockitoJUnitRunner.class)
public class ProviderValidatorTest {

    protected Provider<AdditionalProperties> provider = createProvider("provider");

    @Mock
    private ProviderDao providerDao;

    @Spy
    private ProviderValidator validator = new ProviderValidator(provider);

    @Before
    public void setup() {
        doReturn(providerDao).when(validator).getProviderDao();
    }

    @Test
    public void providerIsSet() throws Exception {
        assertThat(validator.providerIsSet(), isValid());
    }

    @Test
    public void providerIsNotSet() throws Exception {
        validator = new ProviderValidator(null);
        assertThat(validator.providerIsSet(), failsWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_DOESNT_EXIST));
    }

    @Test
    public void nameAvailable() throws Exception {
        when(providerDao.getByName(provider.getName())).thenReturn(null);
        assertThat(validator.nameAvailable(), isValid());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void nameNotAvailable() throws Exception {
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
