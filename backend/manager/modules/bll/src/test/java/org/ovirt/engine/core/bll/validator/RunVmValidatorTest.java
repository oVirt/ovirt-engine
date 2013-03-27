package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.exceptions.InitializationException;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils;

@RunWith(MockitoJUnitRunner.class)
public class RunVmValidatorTest {

    @Spy
    private RunVmValidator runVmValidator = new RunVmValidator();

    @Before
    public void setup() {
        mockVmPropertiesUtils();
    }

    @Test
    public void testValidEmptyCustomProerties() {
        VM vm = new VM();
        vm.setCustomProperties("");
        vm.setVdsGroupCompatibilityVersion(Version.v3_3);
        List<String> messages = new ArrayList<String>();
        assertTrue(runVmValidator.validateVmProperties(vm, messages));
        assertTrue(messages.isEmpty());
    }

    @Test
    public void testWrongFormatCustomProerties() {
        VM vm = new VM();
        vm.setCustomProperties("sap_agent;"); // missing '= true'
        vm.setVdsGroupCompatibilityVersion(Version.v3_3);
        List<String> messages = new ArrayList<String>();
        assertFalse(runVmValidator.validateVmProperties(vm, messages));
        assertFalse(messages.isEmpty());
    }

    @Test
    public void testNotValidCustomProerties() {
        VM vm = new VM();
        vm.setCustomProperties("property=value;");
        vm.setVdsGroupCompatibilityVersion(Version.v3_3);
        List<String> messages = new ArrayList<String>();
        assertFalse(runVmValidator.validateVmProperties(vm, messages));
        assertFalse(messages.isEmpty());
    }

    @Test
    public void testValidCustomProerties() {
        VM vm = new VM();
        vm.setCustomProperties("sap_agent=true;");
        vm.setVdsGroupCompatibilityVersion(Version.v3_3);
        List<String> messages = new ArrayList<String>();
        assertTrue(runVmValidator.validateVmProperties(vm, messages));
        assertTrue(messages.isEmpty());
    }

    private VmPropertiesUtils mockVmPropertiesUtils() {
        VmPropertiesUtils utils = spy(new VmPropertiesUtils());
        doReturn("sap_agent=^(true|false)$;sndbuf=^[0-9]+$;" +
                "vhost=^(([a-zA-Z0-9_]*):(true|false))(,(([a-zA-Z0-9_]*):(true|false)))*$;" +
                "viodiskcache=^(none|writeback|writethrough)$").
                when(utils)
                .getPredefinedVMProperties(any(Version.class));
        doReturn("").
                when(utils)
                .getUserdefinedVMProperties(any(Version.class));
        doReturn(new HashSet<Version>(Arrays.asList(Version.v3_2, Version.v3_3))).
                when(utils)
                .getSupportedClusterLevels();
        doReturn(utils).when(runVmValidator).getVmPropertiesUtils();
        try {
            utils.init();
        } catch (InitializationException e) {
            e.printStackTrace();
        }
        return utils;
    }

}
