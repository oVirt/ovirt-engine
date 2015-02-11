package org.ovirt.engine.core.bll;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.SetVmTicketParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.console.ConsoleOptions;
import org.ovirt.engine.core.common.queries.ConfigureConsoleOptionsParams;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ConfigureConsoleOptionsQueryTest {

    @Mock
    BackendInternal backend;

    @Test
    public void shouldFailtWhenNoId() {
        ConsoleOptions options = new ConsoleOptions(GraphicsType.SPICE);
        ConfigureConsoleOptionsQuery query =
                new ConfigureConsoleOptionsQuery(new ConfigureConsoleOptionsParams(options, false));
        assertFalse(query.validateInputs());
    }

    @Test
    public void shouldFailtWhenNoGraphicsType() {
        ConsoleOptions options = new ConsoleOptions();
        ConfigureConsoleOptionsQuery query =
                new ConfigureConsoleOptionsQuery(new ConfigureConsoleOptionsParams(options, false));
        assertFalse(query.validateInputs());
    }

    @Test
    public void testInputDataOk() {
        ConfigureConsoleOptionsQuery query = spy(new ConfigureConsoleOptionsQuery(new ConfigureConsoleOptionsParams(getValidOptions(GraphicsType.SPICE), false)));
        doReturn(mockVm(GraphicsType.SPICE)).when(query).getCachedVm();
        assertTrue(query.validateInputs());
    }

    @Test
    public void failOnStoppedVm() {
        ConfigureConsoleOptionsParams params = new ConfigureConsoleOptionsParams(getValidOptions(GraphicsType.SPICE), false);
        ConfigureConsoleOptionsQuery query = spy(new ConfigureConsoleOptionsQuery(params));
        VM mockVm = mockVm(GraphicsType.SPICE);
        mockVm.setStatus(VMStatus.Down);
        doReturn(mockVm).when(query).getCachedVm();

        query.validateInputs();
        assertFalse(query.getQueryReturnValue().getSucceeded());
    }

    @Test
    public void failGetSpiceOnVncVm() {
        ConfigureConsoleOptionsParams params = new ConfigureConsoleOptionsParams(getValidOptions(GraphicsType.SPICE), false);
        ConfigureConsoleOptionsQuery query = spy(new ConfigureConsoleOptionsQuery(params));
        VM mockVm = mockVm(GraphicsType.VNC);
        doReturn(mockVm).when(query).getCachedVm();
        query.validateInputs();
        assertFalse(query.getQueryReturnValue().getSucceeded());
    }

    private ConsoleOptions getValidOptions(GraphicsType graphicsType) {
        ConsoleOptions options = new ConsoleOptions(graphicsType);
        options.setVmId(Guid.Empty);
        return options;
    }

    @Test
    public void shouldCallSetTicket() {
        ConfigureConsoleOptionsParams params = new ConfigureConsoleOptionsParams(getValidOptions(GraphicsType.VNC), true);
        ConfigureConsoleOptionsQuery query = spy(new ConfigureConsoleOptionsQuery(params));
        doReturn(mockVm(GraphicsType.VNC)).when(query).getCachedVm();
        doReturn(null).when(query).getConfigValue(any(ConfigValues.class));
        doReturn(true).when(query).getConfigValue(ConfigValues.RemapCtrlAltDelDefault);

        VdcReturnValueBase result = new VdcReturnValueBase();
        result.setSucceeded(true);
        result.setActionReturnValue("nbusr123");
        doReturn(result).when(backend).runInternalAction(eq(VdcActionType.SetVmTicket), any(SetVmTicketParameters.class));
        doReturn(backend).when(query).getBackend();

        query.getQueryReturnValue().setSucceeded(true);
        query.executeQueryCommand();
        verify(backend, times(1)).runInternalAction(eq(VdcActionType.SetVmTicket), any(SetVmTicketParameters.class));
    }

    @Test
    public void failWhenCertEnforcedAndCANotFound() {
        ConfigureConsoleOptionsParams params = new ConfigureConsoleOptionsParams(getValidOptions(GraphicsType.SPICE), false);
        ConfigureConsoleOptionsQuery query = spy(new ConfigureConsoleOptionsQuery(params));
        doReturn(mockVm(GraphicsType.SPICE)).when(query).getCachedVm();

        mockSpiceRelatedConfig(query);
        doReturn(true).when(query).getConfigValue(ConfigValues.EnableSpiceRootCertificateValidation);
        doReturn(true).when(query).getConfigValue(ConfigValues.RemapCtrlAltDelDefault);

        VdcQueryReturnValue caResult = new VdcQueryReturnValue();
        caResult.setSucceeded(false);
        doReturn(caResult).when(backend).runInternalQuery(eq(VdcQueryType.GetCACertificate), any(VdcQueryParametersBase.class));
        doReturn(backend).when(query).getBackend();

        query.getQueryReturnValue().setSucceeded(true);
        query.executeQueryCommand();
        assertFalse(query.getQueryReturnValue().getSucceeded());
    }

    @Test
    public void passWhenCertNotEnforcedAndCANotFound() {
        ConfigureConsoleOptionsParams params = new ConfigureConsoleOptionsParams(getValidOptions(GraphicsType.SPICE), false);
        ConfigureConsoleOptionsQuery query = spy(new ConfigureConsoleOptionsQuery(params));
        doReturn(mockVm(GraphicsType.SPICE)).when(query).getCachedVm();

        mockSpiceRelatedConfig(query);
        doReturn(false).when(query).getConfigValue(ConfigValues.EnableSpiceRootCertificateValidation);
        doReturn(true).when(query).getConfigValue(ConfigValues.RemapCtrlAltDelDefault);

        doReturn(null).when(backend).runInternalQuery(eq(VdcQueryType.GetCACertificate), any(VdcQueryParametersBase.class));
        doReturn(backend).when(query).getBackend();

        query.getQueryReturnValue().setSucceeded(true);
        query.executeQueryCommand();
        assertTrue(query.getQueryReturnValue().getSucceeded());
    }

    private void mockSpiceRelatedConfig(ConfigureConsoleOptionsQuery query) {
        doReturn(null).when(query).getConfigValue(any(ConfigValues.class));
        doReturn(false).when(query).getConfigValue(ConfigValues.SSLEnabled);
        doReturn(false).when(query).getConfigValue(ConfigValues.EnableUSBAsDefault);
    }

    private VM mockVm(GraphicsType graphicsType) {
        VM vm = new VM();
        vm.setId(Guid.Empty);
        vm.getGraphicsInfos().put(graphicsType, new GraphicsInfo().setIp("host").setPort(5901));
        vm.setStatus(VMStatus.Up);
        return vm;
    }

}
