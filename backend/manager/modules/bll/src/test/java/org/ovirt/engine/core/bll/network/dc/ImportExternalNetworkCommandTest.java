package org.ovirt.engine.core.bll.network.dc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.network.openstack.ExternalNetworkProviderProxy;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImportExternalNetworkParameters;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.provider.ProviderDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class ImportExternalNetworkCommandTest extends BaseCommandTest {
    private final Guid PROVIDER_ID = new Guid("000000000000-0000-0000-0000-00000001");
    private final String EXTERNAL_ID = "000000000000-0000-0000-0000-00000002";
    private final Guid DATACENTER_ID = new Guid("000000000000-0000-0000-0000-00000003");
    private final Guid NETWORK_ID = new Guid("000000000000-0000-0000-0000-00000005");

    @Mock
    private BackendInternal backend;

    @Mock
    private ProviderDao providerDao;

    @Mock
    private ProviderProxyFactory providerProxyFactory;

    @Mock
    private ExternalNetworkProviderProxy providerProxy;

    @Mock
    private NetworkHelper networkHelper;

    @InjectMocks
    private ImportExternalNetworkCommand<ImportExternalNetworkParameters> command =
            new ImportExternalNetworkCommand<>(new ImportExternalNetworkParameters(PROVIDER_ID, EXTERNAL_ID,
                    DATACENTER_ID, true, true), CommandContext.createContext("context"));

    private Provider provider = new Provider();

    @BeforeEach
    public void setUp() {
        provider.setType(ProviderType.EXTERNAL_NETWORK);
        when(providerDao.get(PROVIDER_ID)).thenReturn(provider);
        when(providerProxyFactory.create(provider)).thenReturn(providerProxy);
        when(providerProxy.get(EXTERNAL_ID)).thenReturn(getProviderNetwork());
        doNothing().when(networkHelper).mapPhysicalNetworkIdIfApplicable(any(), eq(DATACENTER_ID));

        ActionReturnValue returnValue = new ActionReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setActionReturnValue(NETWORK_ID);
        when(backend.runInternalAction(eq(ActionType.InternalImportExternalNetwork), any(), any())).thenReturn(returnValue);
    }

    @Test
    public void testImportSuccessfully() {
        ValidateTestUtils.runAndAssertValidateSuccess(command);
        command.executeCommand();
        assertTrue(command.getReturnValue().getSucceeded());
        assertEquals(NETWORK_ID, command.getReturnValue().getActionReturnValue());
        verify(backend).runInternalAction(eq(ActionType.InternalImportExternalNetwork), any(), any());
    }

    @Test
    public void testInvalidProvider() {
        provider.setType(ProviderType.OPENSTACK_IMAGE);
        when(providerProxy.get(any())).thenReturn(null);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_PROVIDER_NOT_NETWORK);
    }

    @Test
    public void testInvalidExternalId() {
        when(providerProxy.get(any())).thenReturn(null);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.NETWORK_HAVING_ID_NOT_EXISTS);
    }

    private Network getProviderNetwork() {
        ProviderNetwork providerNetwork = new ProviderNetwork();
        providerNetwork.setExternalId(EXTERNAL_ID);
        providerNetwork.setProviderId(PROVIDER_ID);
        Network network = new Network();
        network.setProvidedBy(providerNetwork);
        return network;
    }
}
