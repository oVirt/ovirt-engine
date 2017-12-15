package org.ovirt.engine.core.bll.network.host;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.LldpInfo;
import org.ovirt.engine.core.common.businessentities.network.Tlv;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.MockConfigRule;

public class GetMultipleTlvsByHostIdTest extends AbstractQueryTest<IdQueryParameters,
        GetMultipleTlvsByHostIdQuery<? extends IdQueryParameters>> {

    private static final int RANGE = 3;

    @Mock
    private VDSBrokerFrontend vdsBrokerFrontendMocked;

    @Mock
    private VdsDao vdsDao;

    private enum ExpectedError {
        LLDP_ENABLE,
        SUCCESS,
        NOT_SUPPORTED
    }

    private void setup(ExpectedError expectedError) {
        Guid validGuid = Guid.newGuid();
        VDS validVds = new VDS();
        validVds.setId(validGuid);

        if (expectedError == ExpectedError.NOT_SUPPORTED) {
            validVds.setClusterCompatibilityVersion(Version.v4_1);
        } else {
            validVds.setClusterCompatibilityVersion(Version.v4_2);
        }


        when(getQueryParameters().getId()).thenReturn(validGuid);

        when(vdsDao.get(eq(validGuid))).thenReturn(validVds);



        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setReturnValue(creatLldpInfoMap(expectedError != ExpectedError.LLDP_ENABLE));

        when(vdsBrokerFrontendMocked.runVdsCommand(eq(VDSCommandType.GetLldp), any())).thenReturn(returnValue);
    }

    private Map<String, LldpInfo> creatLldpInfoMap(boolean lldpEnabled) {
        LldpInfo lldpInfo = new LldpInfo();
        lldpInfo.setEnabled(lldpEnabled);
        lldpInfo.setTlvs(new ArrayList<>(Arrays.asList(new Tlv())));

        Map<String, LldpInfo> lldpInfoMap = new HashMap<>();
        IntStream.range(0, RANGE).forEach(index -> lldpInfoMap.put("eth" + index, lldpInfo));

        return lldpInfoMap;
    }

    @Test
    public void testExecuteQueryCommandLldpDisabled() {
        setup(ExpectedError.LLDP_ENABLE);
        getQuery().executeQueryCommand();
        Map<String, LldpInfo> returnValue = getQuery().getQueryReturnValue().getReturnValue();
        assertTrue(returnValue.values().stream().allMatch(lldpInfo -> !lldpInfo.isEnabled()));
    }

    @Test
    public void testExecuteQueryCommandSuccess() {
        setup(ExpectedError.SUCCESS);
        getQuery().executeQueryCommand();
        Map<String, LldpInfo> returnValue = getQuery().getQueryReturnValue().getReturnValue();
        assertTrue(returnValue.values().stream().allMatch(Objects::nonNull));
    }

    @Test
    public void testExecuteQueryNotSupported() {
        setup(ExpectedError.NOT_SUPPORTED);
        getQuery().executeQueryCommand();
        Map<String, LldpInfo> returnValue = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals(returnValue, null);
    }

    @Override
    protected Set<MockConfigRule.MockConfigDescriptor<Object>> getExtraConfigDescriptors() {
        return new HashSet<>(Arrays.asList(
                mockConfig(ConfigValues.LldpInformationSupported, Version.v4_1, false),
                mockConfig(ConfigValues.LldpInformationSupported, Version.v4_2, true)
        ));
    }
}
