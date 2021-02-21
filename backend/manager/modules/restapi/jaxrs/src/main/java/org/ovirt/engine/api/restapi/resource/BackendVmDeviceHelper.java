/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Console;
import org.ovirt.engine.api.model.Payload;
import org.ovirt.engine.api.model.Payloads;
import org.ovirt.engine.api.model.VirtioScsi;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.restapi.types.RngDeviceMapper;
import org.ovirt.engine.api.restapi.util.DisplayHelper;
import org.ovirt.engine.api.restapi.util.VmHelper;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmDeviceHelper {
    public static void setPayload(BackendResource resouce, Vm vm) {
        try {
            VmPayload payload = resouce.getEntity(VmPayload.class,
                    QueryType.GetVmPayload,
                    new IdQueryParameters(new Guid(vm.getId())),
                    null,
                    true);

            if (payload != null) {
                Payload p = resouce.getMappingLocator().getMapper(VmPayload.class, Payload.class).map(payload, null);
                Payloads payloads = new Payloads();
                payloads.getPayloads().add(p);
                vm.setPayloads(payloads);
            }
        } catch(WebApplicationException ex) {
            if (ex.getResponse().getStatus()== Response.Status.NOT_FOUND.getStatusCode()) {
                //It's legal to not receive a payload for this VM, so the exception is caught and ignored.
                //(TODO: 'getEntity()' should be refactored to make it the programmer's decision,
                //whether to throw an exception or not in case the entity is not found.) Then
                //this try-catch won't be necessary.
            } else{
                throw ex;
            }
        }
    }

    public static void setConsoleDevice(BackendResource resouce, Vm vm) {
        if (!vm.isSetConsole()) {
            vm.setConsole(new Console());
        }
        vm.getConsole().setEnabled(!getConsoleDevicesForEntity(resouce, new Guid(vm.getId())).isEmpty());
    }

    private static List<String> getConsoleDevicesForEntity(BackendResource resouce, Guid id) {
        return resouce.getEntity(List.class,
                QueryType.GetConsoleDevices,
                new IdQueryParameters(id),
                "GetConsoleDevices", true);
    }

    public static void setVirtioScsiController(BackendResource resouce, Vm vm) {
        if (!vm.isSetVirtioScsi()) {
            vm.setVirtioScsi(new VirtioScsi());
        }
        vm.getVirtioScsi().setEnabled(!VmHelper.getVirtioScsiControllersForEntity(resouce, new Guid(vm.getId())).isEmpty());
    }

    public static void setSoundcard(BackendResource resouce, Vm vm) {
        vm.setSoundcardEnabled(!VmHelper.getSoundDevicesForEntity(resouce, new Guid(vm.getId())).isEmpty());
    }

    public static void setCertificateInfo(BackendResource resource, Vm vm) {
        DisplayHelper.addDisplayCertificate(resource, vm);
    }

    public static void setRngDevice(BackendResource resouce, Vm vm) {
        List<VmRngDevice> rngDevices = resouce.getEntity(List.class,
                QueryType.GetRngDevice,
                new IdQueryParameters(Guid.createGuidFromString(vm.getId())),
                "GetRngDevice", true);

        if (rngDevices != null && !rngDevices.isEmpty()) {
            vm.setRngDevice(RngDeviceMapper.map(rngDevices.get(0), null));
        }
    }

    public static void setTpmDevice(BackendResource resource, Vm vm) {
        vm.setTpmEnabled(!VmHelper.getTpmDevicesForEntity(resource, new Guid(vm.getId())).isEmpty());
    }
}
