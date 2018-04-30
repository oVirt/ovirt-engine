package org.ovirt.engine.core.bll.network.vm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dao.network.NetworkDao;

@ExtendWith(MockitoExtension.class)
public class VnicProfileHelperTest {
    private static final AuditLogType AUDIT_LOG_TYPE = AuditLogType.UNASSIGNED;
    private static final Guid CLUSTER_ID = Guid.newGuid();
    private static final Guid DATA_CENTER_ID = Guid.newGuid();
    private static final String ENTITY_NAME = "entity name";
    private static final String NETWORK_NAME = "network name";
    private static final String VNIC_NAME = "vnic name";

    @Mock
    private AuditLogDirector auditLogDirector;
    @Mock
    private NetworkDao networkDao;

    @Captor
    private ArgumentCaptor<AuditLogable> auditLogableCaptor;

    private VnicProfileHelper underTest;

    @BeforeEach
    public void setUp() {
        underTest = spy(new VnicProfileHelper(CLUSTER_ID, DATA_CENTER_ID, AUDIT_LOG_TYPE));
        doReturn(auditLogDirector).when(underTest).createAuditLogDirector();
        doReturn(networkDao).when(underTest).getNetworkDao();
    }

    private VmNetworkInterface createVnic(String vnicName, String networkName) {
        final VmNetworkInterface vmInterface = new VmNetworkInterface();
        vmInterface.setName(vnicName);
        vmInterface.setNetworkName(networkName);
        return vmInterface;
    }

    @Test
    public void testAuditInvalidInterfaces() {
        final List<String> vnicNames = new ArrayList<>();
        final Set<String> networkNames = new HashSet<>();
        for (int i = 1; i < 3; i++) {
            final String vnicName = VNIC_NAME + i;
            final String networkName = NETWORK_NAME;
            underTest.updateNicWithVnicProfileForUser(createVnic(vnicName, networkName), null);
            vnicNames.add(vnicName);
            networkNames.add(networkName);
        }

        underTest.auditInvalidInterfaces(ENTITY_NAME);

        verify(auditLogDirector).log(auditLogableCaptor.capture(), eq(AUDIT_LOG_TYPE));
        final Map<String, String> capturedCustomValues = auditLogableCaptor.getValue().getCustomValues();
        assertThat(capturedCustomValues, allOf(
                hasEntry("entityname", ENTITY_NAME),
                hasEntry("interfaces", vnicNames.stream().collect(Collectors.joining(","))),
                hasEntry("networks", networkNames.stream().collect(Collectors.joining(",")))));
    }
}
