/*
* Copyright (c) 2010 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*           http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.common.util;

import java.util.Arrays;
import javax.ws.rs.WebApplicationException;

import org.junit.Assert;
import org.junit.Test;
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

public class CompletenessAssertorTest extends Assert {

    @Test
    public void testMissingParameter() throws Exception {
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
    public void testMissingParameterSpecificReason() throws Exception {
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
    public void testOffsetMissingParameter() throws Exception {
        Vm vm = new Vm();
        vm.setDescription("incomplete");
        try {
            offsetValidateParameters(vm);
            fail("expected WebApplicationException on incomplete model");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Vm", "status");
        }
    }

    private void offsetValidateParameters(Vm vm) throws Exception {
        CompletenessAssertor.validateParameters(vm, 2, "status");
    }

    @Test
    public void testMissingParameters() throws Exception {
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
    public void testMissingParameterAlteratives() throws Exception {
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
    public void testCompleteParameters() throws Exception {
        Vm vm = new Vm();
        vm.setName("foo");
        vm.setStatus(VmStatus.UP);
        vm.setHost(new Host());
        CompletenessAssertor.validateParameters(vm, "name", "host", "status");
    }

    @Test
    public void testCompleteParametersAlternativesFirst() throws Exception {
        Vm vm = new Vm();
        vm.setName("foo");
        vm.setHost(new Host());
        CompletenessAssertor.validateParameters(vm, "name", "host|status");
    }

    @Test
    public void testCompleteParametersAlternativesSecond() throws Exception {
        Vm vm = new Vm();
        vm.setName("foo");
        vm.setStatus(VmStatus.UP);
        CompletenessAssertor.validateParameters(vm, "name", "host|status");
    }

    @Test
    public void testMissingSuperField() throws Exception {
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
    public void testMissingSubField() throws Exception {
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
    public void testMissingSubFieldAlternatives() throws Exception {
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
    public void testMissingSuperFieldAlternatives() throws Exception {
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
    public void testMissingBothAlternatives() throws Exception {
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
    public void testCompleteSubField() throws Exception {
        Vm vm = new Vm();
        vm.setHost(new Host());
        vm.getHost().setId("0");
        CompletenessAssertor.validateParameters(vm, "host.id");
    }

    @Test
    public void testCompleteSubFieldAlternatives() throws Exception {
        Vm vm = new Vm();
        vm.setHost(new Host());
        vm.getHost().setName("zog");
        CompletenessAssertor.validateParameters(vm, "host.id|name");
    }

    @Test
    public void testCompleteSuperFieldAlternatives() throws Exception {
        Permission permission = new Permission();
        permission.setUser(new User());
        permission.getUser().setName("joe");
        CompletenessAssertor.validateParameters(permission, "vm|user.name");
    }

    @Test
    public void testCompleteBothAlternatives() throws Exception {
        Permission permission = new Permission();
        permission.setUser(new User());
        permission.getUser().setName("joe");
        CompletenessAssertor.validateParameters(permission, "vm|user.name|id");
    }

    @Test
    public void testCompleteListSubField() throws Exception {
        User user = new User();
        user.setRoles(new Roles());
        user.getRoles().getRoles().add(new Role());
        user.getRoles().getRoles().get(0).setId("0");
        user.getRoles().getRoles().add(new Role());
        user.getRoles().getRoles().get(1).setId("0");
        CompletenessAssertor.validateParameters(user, "roles.id");
    }

    @Test
    public void testCompleteListSubFields() throws Exception {
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
    public void testCompleteListSubFieldAlternatives() throws Exception {
        User user = new User();
        user.setRoles(new Roles());
        user.getRoles().getRoles().add(new Role());
        user.getRoles().getRoles().get(0).setId("0");
        user.getRoles().getRoles().add(new Role());
        user.getRoles().getRoles().get(1).setName("0");
        CompletenessAssertor.validateParameters(user, "roles.id|name");
    }

    @Test
    public void testMissingListSubField() throws Exception {
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
    public void testMissingListSubFields() throws Exception {
        HostNic bond = new HostNic();

        try {
            CompletenessAssertor.validateParameters(bond, "name", "network.id|name", "bonding.slaves.id|name");
            fail("expected WebApplicationException on incomplete model");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "HostNic", "name, network.id|name, bonding.slaves.id|name");
        }
    }

    @Test
    public void testMissingListSubFieldAlternatives() throws Exception {
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
