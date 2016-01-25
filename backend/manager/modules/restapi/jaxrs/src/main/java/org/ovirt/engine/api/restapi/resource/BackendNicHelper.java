/*
* Copyright (c) 2014 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
import org.ovirt.engine.core.common.queries.VdcQueryType;
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
            VdcQueryType.GetVmGuestAgentInterfacesByVmId,
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
