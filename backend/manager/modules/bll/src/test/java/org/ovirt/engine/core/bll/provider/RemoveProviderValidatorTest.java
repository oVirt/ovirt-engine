package org.ovirt.engine.core.bll.provider;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;
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
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

@RunWith(MockitoJUnitRunner.class)
public class RemoveProviderValidatorTest {

    @Mock
    private Provider<?> provider;

    private List<Network> networks = new ArrayList<>();

    private RemoveProviderValidator validator;

    @Mock
    private NetworkDao networkDao;

    @Mock
    private VmDao vmDao;

    /* --- Set up for tests --- */

    @Before
    public void setUp() throws Exception {
        validator = spy(new RemoveProviderValidator(vmDao, provider));

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

    private void networksUsedTest(boolean vmsNotUsingNetwork,
            boolean templatesNotUsingNetwork,
            Matcher<ValidationResult> matcher) {

        NetworkValidator networkValidator = mock(NetworkValidator.class);
        for (Network network : networks) {
            when(validator.getValidator(network)).thenReturn(networkValidator);
        }

        when(networkValidator.networkNotUsedByVms()).thenReturn(createValidationResult(vmsNotUsingNetwork));
        when(networkValidator.networkNotUsedByTemplates()).thenReturn(createValidationResult(templatesNotUsingNetwork));

        assertThat(validator.providerNetworksNotUsed(), matcher);
    }

    private ValidationResult createValidationResult(boolean valid) {
        return valid ? ValidationResult.VALID : new ValidationResult(EngineMessage.Unassigned);
    }

    @Test
    public void networksNotUsedByVmsNorTemplates() throws Exception {
        mockNetwork();
        networksUsedTest(true, true, isValid());
    }

    @Test
    public void networksUsedByAVm() throws Exception {
        Network net = mockNetwork();

        networksUsedTest(
                false,
                true,
                both(failsWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_NETWORKS_USED_ONCE))
                .and(replacements(hasItem(containsString(net.getName())))));
    }

    @Test
    public void networksUsedByAVmMultipleNetworks() throws Exception {
        Network net = mockNetwork();
        Network net2 = mockNetwork();

        networksUsedTest(
                false,
                true,
                both(failsWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_NETWORKS_USED_MULTIPLE_TIMES))
                .and(replacements(hasItem(containsString(net.getName()))))
                .and(replacements(hasItem(containsString(net2.getName())))));
    }

    @Test
    public void networksUsedByATemplate() throws Exception {
        Network net = mockNetwork();

        networksUsedTest(
                false,
                true,
                both(failsWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_NETWORKS_USED_ONCE))
                .and(replacements(hasItem(containsString(net.getName())))));
    }

    @Test
    public void networksUsedByATemplateMultipleNetworks() throws Exception {
        Network net = mockNetwork();
        Network net2 = mockNetwork();

        networksUsedTest(
                false,
                true,
                both(failsWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_NETWORKS_USED_MULTIPLE_TIMES))
                .and(replacements(hasItem(containsString(net.getName()))))
                        .and(replacements(hasItem(containsString(net2.getName())))));
    }
}
