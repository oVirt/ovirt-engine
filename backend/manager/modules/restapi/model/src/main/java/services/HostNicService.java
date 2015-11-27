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
import types.HostNic;
import types.HostNicVirtualFunctionsConfiguration;
import types.Network;

@Service
public interface HostNicService extends MeasurableService {
    interface Attach {
        @In Network network();

        /**
         * Indicates if the attach should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Detach {
        /**
         * Indicates if the detach should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Get {
        @Out HostNic nic();
    }

    interface Update {
        @In @Out HostNic nic();

        /**
         * Indicates if the update should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Remove {
        /**
         * Indicates if the remove should be performed asynchronously.
         */
        @In Boolean async();
    }

    /**
     * The action updates virtual function configuration in case the current resource represents an SR-IOV enabled NIC.
     * The input should be consisted of at least one of the following properties:
     *
     * - `allNetworksAllowed`
     * - `numberOfVirtualFunctions`
     *
     * Please see the `HostNicVirtualFunctionsConfiguration` type for the meaning of the properties.
     */
    interface UpdateVirtualFunctionsConfiguration {
        @In HostNicVirtualFunctionsConfiguration virtualFunctionsConfiguration();

        /**
         * Indicates if the update should be performed asynchronously.
         */
        @In Boolean async();
    }

    @Service LabelsService labels();
    @Service NetworkAttachmentsService networkAttachments();

    /**
     * Retrieves sub-collection resource of network labels that are allowed on an the virtual functions
     * in case that the current resource represents an SR-IOV physical function NIC.
     */
    @Service LabelsService virtualFunctionAllowedLabels();

    /**
     * Retrieves sub-collection resource of networks that are allowed on an the virtual functions
     * in case that the current resource represents an SR-IOV physical function NIC.
     */
    @Service VirtualFunctionAllowedNetworksService virtualFunctionAllowedNetworks();
}
