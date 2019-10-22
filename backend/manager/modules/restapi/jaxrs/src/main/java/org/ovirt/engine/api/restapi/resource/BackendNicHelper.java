/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.ReportedDevice;
import org.ovirt.engine.api.model.ReportedDevices;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.restapi.types.ReportedDeviceMapper;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

/**
 * A collection of functions useful for dealing with network interface cards.
 */
public class BackendNicHelper {
    public static void addReportedDevices(BackendResource resource, Nic model, VmNetworkInterface entity) {
        List<ReportedDevice> devices = getDevices(resource, entity.getVmId(), entity.getMacAddress());
        if (!devices.isEmpty()) {
            ReportedDevices reportedDevices = new ReportedDevices();
            reportedDevices.getReportedDevices().addAll(devices);
            model.setReportedDevices(reportedDevices);
        }
    }

    private static List<ReportedDevice> getDevices(BackendResource resource, Guid vmId, String mac) {
        List<ReportedDevice> devices = new ArrayList<>();
        for (VmGuestAgentInterface iface : getDevicesCollection(resource, vmId)) {
            if (StringUtils.equals(iface.getMacAddress(), mac)) {
                ReportedDevice device = LinkHelper.addLinks(ReportedDeviceMapper.map(iface, new ReportedDevice()));
                devices.add(device);
            }
        }
        return devices;
    }

    private static List<VmGuestAgentInterface> getDevicesCollection(BackendResource resource, Guid vmId) {
        return resource.getBackendCollection(
            VmGuestAgentInterface.class,
            QueryType.GetVmGuestAgentInterfacesByVmId,
            new IdQueryParameters(vmId)
        );
    }

    public static void addStatistics(Nic model, VmNetworkInterface entity) {
        model.setStatistics(new Statistics());
        NicStatisticalQuery query = new NicStatisticalQuery(model);
        List<Statistic> statistics = query.getStatistics(entity);
        for (Statistic statistic : statistics) {
            LinkHelper.addLinks(statistic, query.getParentType());
        }
        model.getStatistics().getStatistics().addAll(statistics);
    }
}
