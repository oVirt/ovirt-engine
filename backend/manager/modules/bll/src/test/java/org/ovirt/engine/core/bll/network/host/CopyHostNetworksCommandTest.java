package org.ovirt.engine.core.bll.network.host;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.CopyHostNetworksParameters;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.network.Nic;
import org.ovirt.engine.core.common.businessentities.network.VdsInterfaceType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;

class CopyHostNetworksCommandTest extends BaseCommandTest {

    private Guid sourceHostId = Guid.newGuid();
    private Guid destinationHostId = Guid.newGuid();
    private Guid clusterId = Guid.newGuid();

    enum HostValid {
        VALID,
        INVALID;

        boolean isValid() {
            return this.equals(VALID);
        }
    }

    @Mock
    VdsStaticDao vdsStaticDao;

    @Mock
    InterfaceDao interfaceDao;

    @Mock
    NetworkAttachmentDao networkAttachmentDao;

    @Mock
    AuditLogDirector auditLogDirector;

    @InjectMocks
    CopyHostNetworksCommand<CopyHostNetworksParameters> command = new CopyHostNetworksCommand<>(
            new CopyHostNetworksParameters(sourceHostId, destinationHostId), null);

    @Test
    void invalidSourceHostId() {
        command.getParameters().setSourceHostId(null);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.HOST_ID_IS_NULL);
    }

    @Test
    void invalidDestinationHost() {
        setupHosts(HostValid.VALID, HostValid.INVALID);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_HOST_NOT_EXIST);
    }

    @Test
    void invalidSourceHost() {
        setupHosts(HostValid.INVALID, HostValid.VALID);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_HOST_NOT_EXIST);
    }

    @Test
    void invalidNicCount() {
        setupHosts(HostValid.VALID, HostValid.VALID);
        setupNics(VdsInterfaceType.NONE, VdsInterfaceType.MANAGEMENT);
        doNothing().when(auditLogDirector).log(any(), any());
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.INTERFACE_COUNT_DOES_NOT_MATCH);
    }

    @Test
    void validateSuccess() {
        setupHosts(HostValid.VALID, HostValid.VALID);
        setupNics(VdsInterfaceType.MANAGEMENT, VdsInterfaceType.NONE);
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    void setupHosts(HostValid sourceHostValid, HostValid destinationHostValid) {
        doReturn(sourceHostValid.isValid() ? createHost() : null).when(vdsStaticDao).get(sourceHostId);
        if (sourceHostValid.isValid()) {
            doReturn(destinationHostValid.isValid() ? createHost() : null).when(vdsStaticDao).get(destinationHostId);
        }
    }

    private VdsStatic createHost() {
        VdsStatic destinationHost = new VdsStatic();
        destinationHost.setClusterId(clusterId);
        return destinationHost;
    }

    void setupNics(VdsInterfaceType sourceInterfaceType, VdsInterfaceType destinationInterfaceType) {
        doReturn(Arrays.asList(createNic(sourceInterfaceType))).when(interfaceDao)
                .getAllInterfacesForVds(sourceHostId);
        doReturn(Arrays.asList(createNic(destinationInterfaceType))).when(interfaceDao)
                .getAllInterfacesForVds(destinationHostId);
    }

    private Nic createNic(VdsInterfaceType interfaceType) {
        Nic nic = new Nic();
        nic.setName("nic");
        nic.setId(Guid.newGuid());
        nic.setType(interfaceType.getValue());
        return nic;
    }
}
