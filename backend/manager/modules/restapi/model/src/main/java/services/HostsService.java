/*
Copyright (c) 2015 Red Hat, Inc.

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

package services;

import org.ovirt.api.metamodel.annotations.In;
import org.ovirt.api.metamodel.annotations.Out;
import org.ovirt.api.metamodel.annotations.Service;
import types.Host;

@Service
public interface HostsService {
    /**
     * Creates a new host and adds it to the database. The host is created based on the properties of the `host`
     * parameter. The `name`, `address` `rootPassword` properties are required.
     */
    interface Add {
        /**
         * The host definition from which to create the new host is passed as parameter, and the newly created host
         * is returned.
         */
        @In @Out Host host();
    }

    interface List {
        @Out Host[] hosts();

        /**
         * Sets the maximum number of hosts to return. If not specified all the hosts are returned.
         */
        @In Integer max();
    }

    @Service HostService host(String id);
}
