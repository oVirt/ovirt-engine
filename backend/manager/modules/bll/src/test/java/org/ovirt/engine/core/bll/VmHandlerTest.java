package org.ovirt.engine.core.bll;

/**
 * TODO:
 * Commented out test most of the class in order to cancel dependency on PowerMock
 * This should be revisited.
 */

//package org.ovirt.engine.core.bll;
//
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertTrue;
//import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//import static org.powermock.api.mockito.PowerMockito.mockStatic;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//
//import junit.framework.Assert;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.MockitoAnnotations;
//import org.ovirt.engine.core.bll.context.CompensationContext;
//import org.ovirt.engine.core.common.businessentities.UsbPolicy;
//import org.ovirt.engine.core.common.businessentities.VDSGroup;
//import org.ovirt.engine.core.common.businessentities.VM;
//import org.ovirt.engine.core.common.businessentities.VMStatus;
//import org.ovirt.engine.core.common.businessentities.VmDynamic;
//import org.ovirt.engine.core.common.businessentities.VmOsType;
//import org.ovirt.engine.core.common.config.Config;
//import org.ovirt.engine.core.common.config.ConfigValues;
//import org.ovirt.engine.core.common.errors.VdcBLLException;
//import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
//import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
//import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
//import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
//import org.ovirt.engine.core.compat.Guid;
//import org.ovirt.engine.core.compat.Version;
//import org.ovirt.engine.core.dal.dbbroker.DbFacade;
//import org.ovirt.engine.core.dao.VmDynamicDAO;
//import org.ovirt.engine.core.utils.transaction.TransactionSupport;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//



import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.utils.RandomUtils;

/** A test case for the {@link VmHandler} class. */
public class VmHandlerTest {

    @Before
    public void setUp() {
        VmHandler.Init();
    }

    @Test
    public void testUpdateFieldsNameInStatusUp() {
        VmStatic src = new VmStatic();
        src.setvm_name(RandomUtils.instance().nextString(10));
        src.setInterfaces(new ArrayList<VmNetworkInterface>(2));

        VmStatic dest = new VmStatic();
        dest.setvm_name(RandomUtils.instance().nextString(10));

        System.out.println(VmHandler.mUpdateVmsStatic.IsUpdateValid(src, dest, VMStatus.Up));

        assertTrue("Update should be valid for different names",
                VmHandler.mUpdateVmsStatic.IsUpdateValid(src, dest));
    }

}

//@PrepareForTest({ DbFacade.class, TransactionSupport.class, Config.class, Backend.class })
//@RunWith(PowerMockRunner.class)
//public class VmHandlerTest {
//
//    @Mock
//    DbFacade dbFacade;
//
//    @Mock
//    VmDynamicDAO vmDynamicDAO;
//
//    public VmHandlerTest() {
//        mockStatic(Backend.class);
//        mockStatic(DbFacade.class);
//        mockStatic(TransactionSupport.class);
//        MockitoAnnotations.initMocks(this);
//    }
//
//    @Test(expected = VdcBLLException.class)
//    public void lockLockedVM() {
//        mockVmDynamicDAOWithLockedVm();
//        executeCheckAndLock();
//    }
//
//    @Test
//    public void lockUnlockedVm() {
//        mockVmDynamicDAOWithUnlockedVm();
//        executeCheckAndLock();
//    }
//
//    @Before
//    public void mockDbFacade() {
//        when(DbFacade.getInstance()).thenReturn(dbFacade);
//        when(dbFacade.getVmDynamicDAO()).thenReturn(vmDynamicDAO);
//    }
//
//    private void mockVmDynamicDAOWithLockedVm() {
//        when(vmDynamicDAO.get(any(Guid.class))).thenReturn(createVmDynamic(VMStatus.ImageLocked));
//    }
//
//    private void mockVmDynamicDAOWithUnlockedVm() {
//        when(vmDynamicDAO.get(any(Guid.class))).thenReturn(createVmDynamic(VMStatus.Down));
//    }
//
//    private static VmDynamic createVmDynamic(VMStatus status) {
//        VmDynamic dynamic = new VmDynamic();
//        dynamic.setstatus(status);
//        return dynamic;
//    }
//
//    private static void executeCheckAndLock() {
//        VmHandler.checkStatusAndLockVm(Guid.NewGuid(), mock(CompensationContext.class));
//    }
//
//    @Test
//    public void UpdateVmGuestAgentVersionWithNullAppList() {
//        VM vm = new VM();
//        vm.setapp_list(null);
//        VmHandler.UpdateVmGuestAgentVersion(vm);
//        Assert.assertNull(vm.getGuestAgentVersion());
//        Assert.assertNull(vm.getSpiceDriverVersion());
//    }
//
//    @Test
//    public void UpdateVmGuestAgentVersionWithAppList() {
//        PowerMockito.mockStatic(Config.class);
//        Mockito.when(Config.GetValue(ConfigValues.AgentAppName)).thenReturn("oVirt-Agent");
//        HashMap<String, String> drivers = new HashMap<String, String>();
//        drivers.put("linux", "xorg-x11-drv-qxl");
//        Mockito.when(Config.GetValue(ConfigValues.SpiceDriverNameInGuest)).thenReturn(drivers);
//
//        VM vm = new VM();
//        vm.getStaticData().setos(VmOsType.OtherLinux);
//        vm.setapp_list("kernel-3.0,ovirt-agent-4.5.6,xorg-x11-drv-qxl-0.0.21-3.fc15.i686");
//        VmHandler.UpdateVmGuestAgentVersion(vm);
//        Assert.assertNotNull(vm.getGuestAgentVersion());
//        Assert.assertNotNull(vm.getSpiceDriverVersion());
//    }
//
//    public void BaseUsbPolicyTest(UsbPolicy usbPolicy, Version version, VmOsType osType, boolean result) {
//        VDSGroup vdsGroup = new VDSGroup();
//        vdsGroup.setcompatibility_version(version);
//
//        PowerMockito.mockStatic(Config.class);
//        Mockito.when(Config.<Boolean> GetValue(ConfigValues.NativeUSBEnabled, vdsGroup.getcompatibility_version()
//                .getValue())).thenReturn(result);
//
//        List<String> messages = new ArrayList<String>();
//        if (result) {
//            assertTrue(VmHandler.isUsbPolicyLegal(usbPolicy, osType, vdsGroup, messages));
//            assertTrue(messages.isEmpty());
//        } else {
//            assertFalse(VmHandler.isUsbPolicyLegal(usbPolicy, osType, vdsGroup, messages));
//            assertFalse(messages.isEmpty());
//        }
//    }
//
//    @Test
//    public void TestUsbPolicyLegalNative31() {
//        BaseUsbPolicyTest(UsbPolicy.ENABLED_NATIVE, Version.v3_1, VmOsType.Windows2003, true);
//    }
//
//    @Test
//    public void TestUsbPolicyNotLegalNative30() {
//        BaseUsbPolicyTest(UsbPolicy.ENABLED_NATIVE, Version.v3_0, VmOsType.Windows2003, false);
//    }
//
//    @Test
//    public void TestUsbPolicyLegalLegacy30() {
//        BaseUsbPolicyTest(UsbPolicy.ENABLED_LEGACY, Version.v3_0, VmOsType.Windows2003, true);
//    }
//
//    @Test
//    public void TestUsbPolicyLegalLegacy31() {
//        BaseUsbPolicyTest(UsbPolicy.ENABLED_LEGACY, Version.v3_1, VmOsType.Windows2003, true);
//    }
//
//    @Test
//    public void TestUsbPolicyLegalDisabled30() {
//        BaseUsbPolicyTest(UsbPolicy.DISABLED, Version.v3_0, VmOsType.Windows2003, true);
//    }
//
//    @Test
//    public void TestUsbPolicyLegalDisabled31() {
//        BaseUsbPolicyTest(UsbPolicy.DISABLED, Version.v3_1, VmOsType.Windows2003, true);
//    }
//
//    @Test
//    public void TestUsbPolicyNotLegalLegacy31Linux() {
//        BaseUsbPolicyTest(UsbPolicy.ENABLED_LEGACY, Version.v3_1, VmOsType.OtherLinux, false);
//    }
//
//    @Test
//    public void TestUsbPolicyLegalNative31Linux() {
//        BaseUsbPolicyTest(UsbPolicy.ENABLED_NATIVE, Version.v3_1, VmOsType.OtherLinux, true);
//    }
//
//    @Test
//    public void VerifyAddVmTest() {
//        VDSReturnValue returnValue = new VDSReturnValue();
//        returnValue.setReturnValue(new Boolean(false));
//        Backend backendMock = mock(Backend.class);
//        VDSBrokerFrontend resourceManagerMock = mock(VDSBrokerFrontend.class);
//        when(Backend.getInstance()).thenReturn(backendMock);
//        when(backendMock.getResourceManager()).thenReturn(resourceManagerMock);
//        when(resourceManagerMock.RunVdsCommand(any(VDSCommandType.class), any(IrsBaseVDSCommandParameters.class))).thenReturn(returnValue);
//
//        assertFalse(VmHandler.VerifyAddVm(new ArrayList<String>(), 0, null, Guid.NewGuid(), 0));
//    }
//}
