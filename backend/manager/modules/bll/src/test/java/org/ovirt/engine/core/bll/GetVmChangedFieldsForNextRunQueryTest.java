package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetVmChangedFieldsForNextRunParameters;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.InjectedMock;

public class GetVmChangedFieldsForNextRunQueryTest
        extends AbstractQueryTest<GetVmChangedFieldsForNextRunParameters, GetVmChangedFieldsForNextRunQuery<? extends GetVmChangedFieldsForNextRunParameters>> {

    @Mock
    @InjectedMock
    public CpuFlagsManagerHandler cpuFlagsManagerHandler;

    @Mock
    VmPropertiesUtils vmPropertiesUtils;

    @Spy
    @InjectMocks
    VmHandler vmHandler;

    @BeforeEach
    public void init() {
        doNothing().when(vmHandler).enableVmsToolVersionCheck();
        vmHandler.init();
        mockVmPropertiesUtils();
    }

    private void mockVmPropertiesUtils() {
        SimpleDependencyInjector.getInstance().bind(VmPropertiesUtils.class, vmPropertiesUtils);
    }

    private VM createEmptyVm() {
        VM vm = new VM();
        vm.setCustomProperties(StringUtils.EMPTY);
        vm.setPredefinedProperties(StringUtils.EMPTY);
        vm.setUserDefinedProperties(StringUtils.EMPTY);
        vm.setClusterCompatibilityVersion(Version.getLast());
        vm.setClusterArch(ArchitectureType.x86_64);
        vm.setClusterBiosType(BiosType.I440FX_SEA_BIOS);
        return vm;
    }

    @Test
    public void testEmptyVms() {
        VM srcVm = createEmptyVm();
        VM dstVm = createEmptyVm();

        when(getQueryParameters().getOriginal()).thenReturn(srcVm);
        when(getQueryParameters().getUpdated()).thenReturn(dstVm);

        getQuery().executeQueryCommand();

        assertTrue(((List<String>) getQuery().getQueryReturnValue().getReturnValue()).isEmpty());
    }

    @Test
    public void testDifferentCpuPerSocketOnly() {
        VM srcVm = createEmptyVm();
        VM dstVm = createEmptyVm();
        srcVm.setCpuPerSocket(4);
        dstVm.setCpuPerSocket(5);

        when(getQueryParameters().getOriginal()).thenReturn(srcVm);
        when(getQueryParameters().getUpdated()).thenReturn(dstVm);

        getQuery().executeQueryCommand();

        assertFalse(((List<String>) getQuery().getQueryReturnValue().getReturnValue()).isEmpty());
    }

    @Test
    public void testDifferentVms() {
        VM srcVm = createEmptyVm();
        VM dstVm = createEmptyVm();
        // field that should not count
        srcVm.setUseLatestVersion(false);
        dstVm.setUseLatestVersion(true);
        srcVm.setName("a");
        dstVm.setName("b");
        // some equal fields
        srcVm.setComment("my comment..");
        dstVm.setComment("my comment..");
        srcVm.setOriginalTemplateName("template4");
        dstVm.setOriginalTemplateName("template4");
        srcVm.setVmMemSizeMb(128);
        dstVm.setVmMemSizeMb(128);
        // changes for next run
        srcVm.setCustomProperties("prop=value");
        dstVm.setCustomProperties("prop=value2");
        srcVm.setUsbPolicy(UsbPolicy.DISABLED);
        dstVm.setUsbPolicy(UsbPolicy.ENABLED_NATIVE);
        srcVm.setStateless(true);
        dstVm.setStateless(false);
        srcVm.setUseHostCpuFlags(true);
        dstVm.setUseHostCpuFlags(false);

        when(getQueryParameters().getOriginal()).thenReturn(srcVm);
        when(getQueryParameters().getUpdated()).thenReturn(dstVm);

        getQuery().executeQueryCommand();

        assertFalse(((List<String>) getQuery().getQueryReturnValue().getReturnValue()).isEmpty());
    }

    @Test
    public void testSameVms() {
        VM srcVm = createEmptyVm();
        VM dstVm = createEmptyVm();
        // field that should not count
        srcVm.setUseLatestVersion(false);
        dstVm.setUseLatestVersion(true);
        // some equal fields
        srcVm.setName("a");
        dstVm.setName("a");
        srcVm.setComment("my comment..");
        dstVm.setComment("my comment..");
        srcVm.setOriginalTemplateName("template4");
        dstVm.setOriginalTemplateName("template4");
        srcVm.setVmMemSizeMb(128);
        dstVm.setVmMemSizeMb(128);
        // changes for next run
        srcVm.setCustomProperties("prop=value");
        dstVm.setCustomProperties("prop=value");
        srcVm.setUsbPolicy(UsbPolicy.ENABLED_NATIVE);
        dstVm.setUsbPolicy(UsbPolicy.ENABLED_NATIVE);
        srcVm.setStateless(true);
        dstVm.setStateless(true);
        srcVm.setUseHostCpuFlags(true);
        dstVm.setUseHostCpuFlags(true);

        when(getQueryParameters().getOriginal()).thenReturn(srcVm);
        when(getQueryParameters().getUpdated()).thenReturn(dstVm);

        getQuery().executeQueryCommand();

        assertTrue(((List<String>) getQuery().getQueryReturnValue().getReturnValue()).isEmpty());
    }
}
