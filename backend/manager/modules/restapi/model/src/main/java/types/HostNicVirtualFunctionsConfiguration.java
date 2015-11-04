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

package types;

import org.ovirt.api.metamodel.annotations.Type;

/**
 * Describes virtual functions configuration for an SR-IOV enabled physical function NIC.
 */
@Type
public interface HostNicVirtualFunctionsConfiguration {
    /**
     * Maximum number of virtual functions the NIC supports. Read-only property.
     */
    Integer maxNumberOfVirtualFunctions();

    /**
     * Number of curently defined virtual functions. User-defined value between 0 and `maxNumberOfVirtualFunctions`.
     */
    Integer numberOfVirtualFunctions();

    /**
     * Defines whether all networks are allowed to be defined on the related virtual functions or specified ones only.
     */
    Boolean allNetworksAllowed();
}
