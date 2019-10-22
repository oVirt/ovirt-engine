/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.Bonding;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostNic;
import org.ovirt.engine.api.model.HostNics;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Permission;
import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.api.model.Roles;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.VmStatus;

public class CompletenessAssertorTest {

    @Test
    public void testMissingParameter() {
        Vm vm = new Vm();
        vm.setDescription("incomplete");
        try {
            CompletenessAssertor.validateParameters(vm, "status");
            fail("expected WebApplicationException on incomplete model");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Vm", "status");
        }
    }

    @Test
    public void testMissingParameterSpecificReason() {
        Vm vm = new Vm();
        vm.setDescription("incomplete");
        try {
            CompletenessAssertor.validateParameters("Missing stuff", "{0} {1} required for {2}", vm, "status");
            fail("expected WebApplicationException on incomplete model");
        } catch (WebApplicationException wae) {
            verifyIncompleteException("Missing stuff", wae, "Vm", "status");
        }
    }

    @Test
    public void testOffsetMissingParameter() {
        Vm vm = new Vm();
        vm.setDescription("incomplete");
        try {
            offsetValidateParameters(vm);
            fail("expected WebApplicationException on incomplete model");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Vm", "status");
        }
    }

    private void offsetValidateParameters(Vm vm) {
        CompletenessAssertor.validateParameters(vm, 2, "status");
    }

    @Test
    public void testMissingParameters() {
        Vm vm = new Vm();
        vm.setDescription("incomplete");
        try {
            CompletenessAssertor.validateParameters(vm, "name", "host", "status");
            fail("expected WebApplicationException on incomplete model");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Vm", "name", "host", "status");
        }
    }

    @Test
    public void testMissingParameterAlteratives() {
        Vm vm = new Vm();
        vm.setDescription("incomplete");
        try {
            CompletenessAssertor.validateParameters(vm, "status|host|name");
            fail("expected WebApplicationException on incomplete model");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Vm", "status|host|name");
        }
    }

    @Test
    public void testCompleteParameters() {
        Vm vm = new Vm();
        vm.setName("foo");
        vm.setStatus(VmStatus.UP);
        vm.setHost(new Host());
        CompletenessAssertor.validateParameters(vm, "name", "host", "status");
    }

    @Test
    public void testCompleteParametersAlternativesFirst() {
        Vm vm = new Vm();
        vm.setName("foo");
        vm.setHost(new Host());
        CompletenessAssertor.validateParameters(vm, "name", "host|status");
    }

    @Test
    public void testCompleteParametersAlternativesSecond() {
        Vm vm = new Vm();
        vm.setName("foo");
        vm.setStatus(VmStatus.UP);
        CompletenessAssertor.validateParameters(vm, "name", "host|status");
    }

    @Test
    public void testMissingSuperField() {
        Vm vm = new Vm();
        vm.setDescription("incomplete");
        try {
            CompletenessAssertor.validateParameters(vm, "host.id");
            fail("expected WebApplicationException on incomplete model");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Vm", "host.id");
        }
    }

    @Test
    public void testMissingSubField() {
        Vm vm = new Vm();
        vm.setDescription("incomplete");
        vm.setHost(new Host());
        try {
            CompletenessAssertor.validateParameters(vm, "host.id");
            fail("expected WebApplicationException on incomplete model");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Vm", "host.id");
        }
    }

    @Test
    public void testMissingSubFieldAlternatives() {
        Vm vm = new Vm();
        vm.setDescription("incomplete");
        vm.setHost(new Host());
        try {
            CompletenessAssertor.validateParameters(vm, "host.id|name");
            fail("expected WebApplicationException on incomplete model");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Vm", "host.id|name");
        }
    }

    @Test
    public void testMissingSuperFieldAlternatives() {
        Permission permission = new Permission();
        permission.setVm(new Vm());
        try {
            CompletenessAssertor.validateParameters(permission, "user|vm.name");
            fail("expected WebApplicationException on incomplete model");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Permission", "user|vm.name");
        }
    }

    @Test
    public void testMissingBothAlternatives() {
        Permission permission = new Permission();
        permission.setVm(new Vm());
        try {
            CompletenessAssertor.validateParameters(permission, "user|vm.name|id");
            fail("expected WebApplicationException on incomplete model");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Permission", "user|vm.name|id");
        }
    }

    @Test
    public void testCompleteSubField() {
        Vm vm = new Vm();
        vm.setHost(new Host());
        vm.getHost().setId("0");
        CompletenessAssertor.validateParameters(vm, "host.id");
    }

    @Test
    public void testCompleteSubFieldAlternatives() {
        Vm vm = new Vm();
        vm.setHost(new Host());
        vm.getHost().setName("zog");
        CompletenessAssertor.validateParameters(vm, "host.id|name");
    }

    @Test
    public void testCompleteSuperFieldAlternatives() {
        Permission permission = new Permission();
        permission.setUser(new User());
        permission.getUser().setName("joe");
        CompletenessAssertor.validateParameters(permission, "vm|user.name");
    }

    @Test
    public void testCompleteBothAlternatives() {
        Permission permission = new Permission();
        permission.setUser(new User());
        permission.getUser().setName("joe");
        CompletenessAssertor.validateParameters(permission, "vm|user.name|id");
    }

    @Test
    public void testCompleteListSubField() {
        User user = new User();
        user.setRoles(new Roles());
        user.getRoles().getRoles().add(new Role());
        user.getRoles().getRoles().get(0).setId("0");
        user.getRoles().getRoles().add(new Role());
        user.getRoles().getRoles().get(1).setId("0");
        CompletenessAssertor.validateParameters(user, "roles.id");
    }

    @Test
    public void testCompleteListSubFields() {
        HostNic bond = new HostNic();
        bond.setName("joe");
        bond.setNetwork(new Network());
        bond.getNetwork().setId("0");
        bond.setBonding(new Bonding());
        bond.getBonding().setSlaves(new HostNics());

        HostNic slave = new HostNic();
        slave.setId("0");
        bond.getBonding().getSlaves().getHostNics().add(slave);

        slave = new HostNic();
        slave.setId("0");
        bond.getBonding().getSlaves().getHostNics().add(slave);


        CompletenessAssertor.validateParameters(bond, "name", "network.id|name", "bonding.slaves.id|name");
    }

    @Test
    public void testCompleteListSubFieldAlternatives() {
        User user = new User();
        user.setRoles(new Roles());
        user.getRoles().getRoles().add(new Role());
        user.getRoles().getRoles().get(0).setId("0");
        user.getRoles().getRoles().add(new Role());
        user.getRoles().getRoles().get(1).setName("0");
        CompletenessAssertor.validateParameters(user, "roles.id|name");
    }

    @Test
    public void testMissingListSubField() {
        User user = new User();
        user.setRoles(new Roles());
        user.getRoles().getRoles().add(new Role());
        user.getRoles().getRoles().get(0).setId("0");
        user.getRoles().getRoles().add(new Role());
        user.getRoles().getRoles().get(1).setName("0");
        try {
            CompletenessAssertor.validateParameters(user, "roles.id");
            fail("expected WebApplicationException on incomplete model");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "User", "roles.id");
        }
    }

    @Test
    public void testMissingListSubFields() {
        HostNic bond = new HostNic();

        try {
            CompletenessAssertor.validateParameters(bond, "name", "network.id|name", "bonding.slaves.id|name");
            fail("expected WebApplicationException on incomplete model");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "HostNic", "name, network.id|name, bonding.slaves.id|name");
        }
    }

    @Test
    public void testMissingListSubFieldAlternatives() {
        User user = new User();
        user.setRoles(new Roles());
        user.getRoles().getRoles().add(new Role());
        user.getRoles().getRoles().get(0).setId("0");
        user.getRoles().getRoles().add(new Role());
        user.getRoles().getRoles().get(1).setDescription("0");
        try {
            CompletenessAssertor.validateParameters(user, "roles.id|name");
            fail("expected WebApplicationException on incomplete model");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "User", "roles.id|name");
        }
    }

    private void verifyIncompleteException(WebApplicationException wae, String type,  String... fields) {
        assertEquals(400, wae.getResponse().getStatus());
        Fault fault = (Fault)wae.getResponse().getEntity();
        assertNotNull(fault);
        assertEquals("Incomplete parameters", fault.getReason());
        String method = new Throwable().getStackTrace()[1].getMethodName();
        assertEquals(type + " " +  Arrays.asList(fields) + " required for " + method, fault.getDetail());
    }

    private void verifyIncompleteException(String reason, WebApplicationException wae, String type,  String... fields) {
        assertEquals(400, wae.getResponse().getStatus());
        Fault fault = (Fault)wae.getResponse().getEntity();
        assertNotNull(fault);
        assertEquals(reason, fault.getReason());
        String method = new Throwable().getStackTrace()[1].getMethodName();
        assertEquals(type + " " +  Arrays.asList(fields) + " required for " + method, fault.getDetail());
    }
}
