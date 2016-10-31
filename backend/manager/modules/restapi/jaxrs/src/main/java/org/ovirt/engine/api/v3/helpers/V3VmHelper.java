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

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.model.Ip;
import org.ovirt.engine.api.model.Ips;
import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.ReportedDevice;
import org.ovirt.engine.api.model.ReportedDevices;
import org.ovirt.engine.api.resource.DiskAttachmentResource;
import org.ovirt.engine.api.resource.DiskAttachmentsResource;
import org.ovirt.engine.api.resource.SystemResource;
import org.ovirt.engine.api.resource.VmNicsResource;
import org.ovirt.engine.api.resource.VmResource;
import org.ovirt.engine.api.resource.VmsResource;
import org.ovirt.engine.api.restapi.resource.BackendApiResource;
import org.ovirt.engine.api.v3.servers.V3VmServer;
import org.ovirt.engine.api.v3.types.V3Actions;
import org.ovirt.engine.api.v3.types.V3Disk;
import org.ovirt.engine.api.v3.types.V3GuestInfo;
import org.ovirt.engine.api.v3.types.V3IPs;
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
            V3LinkHelper.addLink(links, "activate", "vms", vmId, "disks", disk.getId(), "activate");
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

    /**
     * In version 3 of the API the user can include a {@code detail} parameter that indicates if additional details
     * should be added to virtual machines. This has been removed in version 4 of the API, but needs to be preserved
     * in version 3 of the API for backwards compatibility.
     */
    public static void addInlineDetails(V3VM vm, V3VmServer server, Set<String> details) {
        if (details.contains("disks")) {
            vm.setDisks(server.getDisksResource().list());
        }
        if (details.contains("nics")) {
            vm.setNics(server.getNicsResource().list());
        }
        if (details.contains("tags")) {
            vm.setTags(server.getTagsResource().list());
        }
    }

    /**
     * If the V4 virtual machine has IP addresses reported, then add them to the V3 {@code guest_info} element.
     */
    public static void addGuestIp(V3VM vm) {
        String vmId = vm.getId();
        if (vmId != null) {
            SystemResource systemResource = BackendApiResource.getInstance();
            VmsResource vmsResource = systemResource.getVmsResource();
            VmResource vmResource = vmsResource.getVmResource(vmId);
            VmNicsResource nicsResource = vmResource.getNicsResource();
            try {
                Nics fromNics = nicsResource.list();
                List<Ip> fromIps = new ArrayList<>();
                for (Nic fromNic : fromNics.getNics()) {
                    ReportedDevices fromDevices = fromNic.getReportedDevices();
                    if (fromDevices != null) {
                        for (ReportedDevice fromDevice : fromDevices.getReportedDevices()) {
                            Ips deviceIps = fromDevice.getIps();
                            if (deviceIps != null) {
                                fromIps.addAll(deviceIps.getIps());
                            }
                        }
                    }
                }
                if (!fromIps.isEmpty()) {
                    V3GuestInfo guestInfo = vm.getGuestInfo();
                    if (guestInfo == null) {
                        guestInfo = new V3GuestInfo();
                        vm.setGuestInfo(guestInfo);
                    }
                    V3IPs ips = guestInfo.getIps();
                    if (ips == null) {
                        ips = new V3IPs();
                        guestInfo.setIps(ips);
                    }
                    ips.getIPs().addAll(adaptOut(fromIps));
                }
            }
            catch (WebApplicationException exception) {
                // If an application exception is generated while retrieving the details of the NICs is safe to ignore
                // it, as it may be that the user just doesn't have permission to see the NICs, but she may still have
                // permissions to see the other details of the virtual machine.
            }
        }
    }

    /**
     * In version 4 of the API the interface, some attributes have been moved from the disk to the disk attachment, as
     * they are specific of the relationship between a particular VM and the disk. But in version 3 of the API we need
     * to continue supporting them. To do so we need to find the disk attachment and copy these attributes to the disk.
     */
    public static void addDiskAttachmentDetails(String vmId, List<V3Disk> disks) {
        if (vmId != null) {
            SystemResource systemResource = BackendApiResource.getInstance();
            VmsResource vmsResource = systemResource.getVmsResource();
            VmResource vmResource = vmsResource.getVmResource(vmId);
            DiskAttachmentsResource attachmentsResource = vmResource.getDiskAttachmentsResource();
            for (V3Disk disk : disks) {
                String diskId = disk.getId();
                if (diskId != null) {
                    DiskAttachmentResource attachmentResource = attachmentsResource.getAttachmentResource(diskId);
                    try {
                        DiskAttachment attachment = attachmentResource.get();
                        if (attachment.isSetBootable()) {
                            disk.setBootable(attachment.isBootable());
                        }
                        if (attachment.isSetInterface()) {
                            disk.setInterface(attachment.getInterface().toString().toLowerCase());
                        }
                        if (attachment.isSetLogicalName()) {
                            disk.setLogicalName(attachment.getLogicalName());
                        }
                        if (attachment.isSetActive()) {
                            disk.setActive(attachment.isActive());
                        }
                        if (attachment.isSetUsesScsiReservation()) {
                            disk.setUsesScsiReservation(attachment.isUsesScsiReservation());
                        }
                    }
                    catch (WebApplicationException exception) {
                        // If an application exception is generated while retrieving the details of the disk attachment
                        // it is safe to ignore it, as it may be that the user just doesn't have permission to see
                        // attachment, but she may still have permissions to see the other details of the disk.
                    }
                }
            }
        }
    }
}
