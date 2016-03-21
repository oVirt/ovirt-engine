package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetVmChangedFieldsForNextRunParameters;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.compat.Version;

@RunWith(MockitoJUnitRunner.class)
public class GetVmChangedFieldsForNextRunQueryTest
        extends AbstractQueryTest<GetVmChangedFieldsForNextRunParameters, GetVmChangedFieldsForNextRunQuery<? extends GetVmChangedFieldsForNextRunParameters>> {

    @Rule
    public InjectorRule injectorRule = new InjectorRule();

    @Mock
    CpuFlagsManagerHandler cpuFlagsManagerHandler;

    @Mock
    VmPropertiesUtils vmPropertiesUtils;

    @Before
    public void init() {
        mockCpuFlagsManagerHandler();
        VmHandler.init();
        mockVmPropertiesUtils();
        doNothing().when(vmPropertiesUtils).separateCustomPropertiesToUserAndPredefined(any(Version.class), any(VmStatic.class));
    }

    @Override
    protected Set<MockConfigRule.MockConfigDescriptor<Map>> getExtraConfigDescriptors() {
        Map<String, String> value = Collections.singletonMap(ArchitectureType.x86_64.toString(), Boolean.FALSE.toString());
        return Collections.singleton(mockConfig(ConfigValues.HotUnplugMemorySupported, Version.getLast(), value));
    }

    private void mockCpuFlagsManagerHandler() {
        injectorRule.bind(CpuFlagsManagerHandler.class, cpuFlagsManagerHandler);
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
        dstVm.setUsbPolicy(UsbPolicy.ENABLED_LEGACY);
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
        dstVm.setCustomProperties("prop=value");
        srcVm.setUsbPolicy(UsbPolicy.ENABLED_LEGACY);
        dstVm.setUsbPolicy(UsbPolicy.ENABLED_LEGACY);
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
