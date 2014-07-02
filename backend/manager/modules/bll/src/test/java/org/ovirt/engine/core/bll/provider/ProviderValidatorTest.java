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
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.Provider.AdditionalProperties;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.dao.provider.ProviderDao;

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
        assertThat(validator.providerIsSet(), failsWith(VdcBllMessages.ACTION_TYPE_FAILED_PROVIDER_DOESNT_EXIST));
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
        assertThat(validator.nameAvailable(), failsWith(VdcBllMessages.ACTION_TYPE_FAILED_NAME_ALREADY_USED));
    }

    @SuppressWarnings("unchecked")
    private Provider<AdditionalProperties> createProvider(String name) {
        Provider<AdditionalProperties> p = mock(Provider.class);
        when(p.getName()).thenReturn(name);
        return p;
    }
}
