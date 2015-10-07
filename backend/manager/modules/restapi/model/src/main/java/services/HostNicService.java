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

@Service
public interface HostNicService extends MeasurableService {
    interface Attach {
    }

    interface Detach {
    }

    interface Get {
        @Out HostNic nic();
    }

    interface Update {
        @In @Out HostNic nic();
    }

    interface Remove {
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
