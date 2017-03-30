/*
Copyright (c) 2017 Red Hat, Inc.

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

import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.restapi.resource.BackendApiResource;
import org.ovirt.engine.api.v3.types.V3Host;

/**
 * This class is a collection of static methods useful when working with hosts.
 */
public class V3HostHelper {
    /**
     * Retrieves host statistics from version 4 of the API and adds them to a version 4 host.
     *
     * @param host the host where the statistics should be added, should contain at least the 'id' attribute
     */
    public static void addStatistics(V3Host host) {
        Statistics statistics = BackendApiResource.getInstance()
            .getHostsResource()
            .getHostResource(host.getId())
            .getStatisticsResource()
            .list();
        host.setStatistics(adaptOut(statistics));
    }
}
