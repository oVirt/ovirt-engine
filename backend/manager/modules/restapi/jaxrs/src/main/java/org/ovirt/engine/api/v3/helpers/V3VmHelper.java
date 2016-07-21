/*
Copyright (c) 2016 Red Hat, Inc.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.v3.helpers;

import java.util.List;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.v3.types.V3Actions;
import org.ovirt.engine.api.v3.types.V3Disk;
import org.ovirt.engine.api.v3.types.V3Link;
import org.ovirt.engine.api.v3.types.V3VM;

public class V3VmHelper {
    public static void addDisksLink(Response response) {
        Object entity = response.getEntity();
        if (entity instanceof V3VM) {
            addDisksLink((V3VM) entity);
        }
    }

    public static void addDisksLink(V3VM vm) {
        if (vm != null) {
            V3LinkHelper.addLink(vm.getLinks(), "disks", "vms", vm.getId(), "disks");
        }
    }

    /**
     * Version 4 of the API can't reliably build the links for the "disks" collection because it has been removed,
     * so we need to remove all the links and re-add them explicitly.
     */
    public static void fixDiskLinks(String vmId, V3Disk disk) {
        // Fix the link of the disk itself:
        disk.setHref(V3LinkHelper.linkHref("vms", vmId, "disks", disk.getId()));

        // Remove all the action links and add them again:
        V3Actions actions = disk.getActions();
        if (actions != null) {
            List<V3Link> links = actions.getLinks();
            links.clear();
            V3LinkHelper.addLink(links, "activate", "vms", vmId, "disks", disk.getId(), "acivate");
            V3LinkHelper.addLink(links, "deactivate", "vms", vmId, "disks", disk.getId(), "deactivate");
            V3LinkHelper.addLink(links, "export", "vms", vmId, "disks", disk.getId(), "export");
            V3LinkHelper.addLink(links, "move", "vms", vmId, "disks", disk.getId(), "move");
        }

        // Remove all the links and add them again:
        List<V3Link> links = disk.getLinks();
        links.clear();
        V3LinkHelper.addLink(links, "permissions", "vms", vmId, "disks", disk.getId(), "permissions");
        V3LinkHelper.addLink(links, "statistics", "vms", vmId, "disks", disk.getId(), "statistics");
    }
}
