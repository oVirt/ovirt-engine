package org.ovirt.engine.core.bll.provider;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.both;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.replacements;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.provider.RemoveProviderCommand.RemoveProviderValidator;
import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkDao;

@RunWith(MockitoJUnitRunner.class)
public class RemoveProviderValidatorTest {

    @Mock
    private Provider<?> provider;

    private List<Network> networks = new ArrayList<Network>();

    private RemoveProviderValidator validator;

    @Mock
    private NetworkDao networkDao;

    /* --- Set up for tests --- */

    @Before
    public void setUp() throws Exception {
        validator = spy(new RemoveProviderValidator(provider));

        doReturn(networkDao).when(validator).getNetworkDao();
        when(networkDao.getAllForProvider(any(Guid.class))).thenReturn(networks);
    }

    @Test
    public void networksNotUsedWhenNoNetworks() throws Exception {
        assertThat(validator.providerNetworksNotUsed(), isValid());
    }

    private Network mockNetwork() {
        Network net = mock(Network.class);
        when(net.getName()).thenReturn("net");
        networks.add(net);
        return net;
    }

    private void networksUsedTest(Network net,
            boolean vmsNotUsingNetwork,
            boolean templatesNotUsingNetwork,
            Matcher<ValidationResult> matcher) {

        NetworkValidator networkValidator = mock(NetworkValidator.class);
        when(validator.getValidator(net)).thenReturn(networkValidator);

        when(networkValidator.networkNotUsedByVms()).thenReturn(createValidationResult(vmsNotUsingNetwork));
        when(networkValidator.networkNotUsedByTemplates()).thenReturn(createValidationResult(templatesNotUsingNetwork));

        assertThat(validator.providerNetworksNotUsed(),
                matcher);
    }

    private ValidationResult createValidationResult(boolean valid) {
        return valid ? ValidationResult.VALID : new ValidationResult(VdcBllMessages.Unassigned);
    }

    @Test
    public void networksNotUsedByVmsNorTemplates() throws Exception {
        Network net = mockNetwork();

        networksUsedTest(net, true, true, isValid());
    }

    @Test
    public void networksUsedByAVm() throws Exception {
        Network net = mockNetwork();

        networksUsedTest(net,
                false,
                true,
                both(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_PROVIDER_NETWORKS_USED))
                .and(replacements(hasItem(containsString(net.getName())))));
    }

    @Test
    public void networksUsedByATemplate() throws Exception {
        Network net = mockNetwork();

        networksUsedTest(net,
                false,
                true,
                both(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_PROVIDER_NETWORKS_USED))
                .and(replacements(hasItem(containsString(net.getName())))));
    }
}
