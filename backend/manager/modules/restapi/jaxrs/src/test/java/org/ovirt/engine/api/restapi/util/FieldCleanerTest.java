package org.ovirt.engine.api.restapi.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.core.compat.Guid;

public class FieldCleanerTest {

    private static final String HOST_ID = Guid.newGuid().toString();
    private static final String VM_ID = Guid.newGuid().toString();

    @Test
    void testRemoveAllLinksExcept() {
        Link link1 = new Link();
        link1.setRel("rel1");
        link1.setHref("href1");

        Link link2 = new Link();
        link2.setRel("rel2");
        link2.setHref("href2");

        Vm vm = createVm();
        vm.getLinks().addAll(List.of(link1, link2));

        FieldCleaner.removeAllLinksExcept(vm, "rel2", "rel3");

        List<Link> links = vm.getLinks();
        assertEquals(1, links.size());

        Link link = links.get(0);
        assertEquals("rel2", link.getRel());
        assertEquals("href2", link.getHref());
    }

    @Test
    void testNullifyAllFieldsExcept() {
        Vm vm = createVm();

        FieldCleaner.nullifyAllFieldsExcept(vm, "id", "name", "host");
        FieldCleaner.nullifyAllFieldsExcept(vm.getHost(), "id");

        assertEquals(VM_ID, vm.getId());
        assertNotNull(vm.getName());
        assertNull(vm.getDescription());

        assertNotNull(vm.getHost());
        assertEquals(HOST_ID, vm.getHost().getId());
        assertNull(vm.getHost().getName());
    }

    private Vm createVm() {
        Host host = new Host();
        host.setId(HOST_ID);
        host.setName("host name");

        Vm vm = new Vm();
        vm.setId(VM_ID);
        vm.setName("vm name");
        vm.setDescription("vm description");
        vm.setHost(host);

        return vm;
    }
}
