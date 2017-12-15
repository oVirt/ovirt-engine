package org.ovirt.engine.core.bll.network.host;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.LldpInfo;
import org.ovirt.engine.core.common.businessentities.network.Nic;
import org.ovirt.engine.core.common.businessentities.network.Tlv;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.utils.MockConfigRule;

public class GetTlvsByHostNicIdQueryTest extends AbstractQueryTest<IdQueryParameters,
        GetTlvsByHostNicIdQuery<? extends IdQueryParameters>> {


    private static final String NIC_NAME = "eth0";

    @Mock
    private InterfaceDao interfaceDaoMocked;

    @Mock
    private VDSBrokerFrontend vdsBrokerFrontendMock;

    @Mock
    private VdsDao vdsDao;

    private enum ExpectedError {
        NIC_ID_NOT_NULL,
        NIC_ID_NETWORK_INTERFACE,
        NIC_ID_NIC,
        LLDP_ENABLED,
        SUCCESS,
        NOT_SUPPORTED
    }

    public void setup(ExpectedError expectedError) {
        Guid validVdsGuid = Guid.newGuid();
        VDS validVds = new VDS();
        validVds.setId(validVdsGuid);

        if (expectedError == ExpectedError.NOT_SUPPORTED) {
            validVds.setClusterCompatibilityVersion(Version.v4_1);
        } else {
            validVds.setClusterCompatibilityVersion(Version.v4_2);
        }

        final Guid NIC_ID = Guid.Empty;
        when(getQueryParameters().getId()).thenReturn(expectedError == ExpectedError.NIC_ID_NOT_NULL ? null : NIC_ID);
        when(interfaceDaoMocked.get(NIC_ID)).thenReturn(createNic(expectedError, validVdsGuid));
        when(vdsDao.get(eq(validVdsGuid))).thenReturn(validVds);

        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setReturnValue(creatLldpInfoMap(expectedError != ExpectedError.LLDP_ENABLED));
        when(vdsBrokerFrontendMock.runVdsCommand(eq(VDSCommandType.GetLldp), any())).thenReturn(returnValue);

    }

    private VdsNetworkInterface createNic(ExpectedError expectedError, Guid vdsId) {
         switch (expectedError) {
            case NIC_ID_NIC:
                VdsNetworkInterface vdsIface = new VdsNetworkInterface();
                vdsIface.setVdsId(vdsId);
                return vdsIface;
            case NIC_ID_NETWORK_INTERFACE:
                return null;
            default:
                Nic nic = new Nic();
                nic.setName(NIC_NAME);
                nic.setVdsId(vdsId);
                return nic;
        }
    }

    Map<String, LldpInfo> creatLldpInfoMap(boolean lldpEnabled) {
        LldpInfo lldpInfo = new LldpInfo();
        lldpInfo.setEnabled(lldpEnabled);
        lldpInfo.setTlvs(new ArrayList<Tlv>(Arrays.asList(new Tlv())));

        Map<String, LldpInfo> lldpInfoMap = new HashMap<>();
        lldpInfoMap.put(NIC_NAME, lldpInfo);

        return lldpInfoMap;
    }

    @Test
    public void testNoNicId() {
        setup(ExpectedError.NIC_ID_NOT_NULL);
        assertFalse(getQuery().validateInputs());
        assertEquals(EngineMessage.NIC_ID_IS_NULL.name(),
                getQuery().getQueryReturnValue().getExceptionString());
    }

    @Test
    public void testNoInterface() {
        setup(ExpectedError.NIC_ID_NETWORK_INTERFACE);
        assertFalse(getQuery().validateInputs());
        assertEquals(EngineMessage.NIC_ID_NOT_EXIST.name(),
                getQuery().getQueryReturnValue().getExceptionString());
    }

    @Test
    public void testNoNic() {
        setup(ExpectedError.NIC_ID_NIC);
        assertFalse(getQuery().validateInputs());
        assertEquals(EngineMessage.INTERFACE_TYPE_NOT_SUPPORT_LLDP.name(),
                getQuery().getQueryReturnValue().getExceptionString());
    }

    @Test
    public void testExecuteQueryCommandLldpDisabled() {
        setup(ExpectedError.LLDP_ENABLED);
        getQuery().executeQueryCommand();
        assertNull(getQuery().getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testExecuteQueryCommandSucess() {
        setup(ExpectedError.SUCCESS);
        getQuery().executeQueryCommand();
        List returnValue = getQuery().getQueryReturnValue().getReturnValue();
        assertNotNull(returnValue);
        assertTrue(returnValue.get(0) instanceof Tlv);
    }

    @Test
    public void testExecuteQueryNotSupported() {
        setup(ExpectedError.NOT_SUPPORTED);
        getQuery().executeQueryCommand();
        Map<String, LldpInfo> returnValue = getQuery().getQueryReturnValue().getReturnValue();
        assertTrue(returnValue == null);
    }

    @Override
    protected Set<MockConfigRule.MockConfigDescriptor<Object>> getExtraConfigDescriptors() {
        return new HashSet<>(Arrays.asList(
                mockConfig(ConfigValues.LldpInformationSupported, Version.v4_1, false),
                mockConfig(ConfigValues.LldpInformationSupported, Version.v4_2, true)
        ));
    }

}
