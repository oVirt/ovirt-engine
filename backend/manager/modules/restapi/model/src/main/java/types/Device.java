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

import org.ovirt.api.metamodel.annotations.Link;
import org.ovirt.api.metamodel.annotations.Type;

@Type
public interface Device extends Identified {
    // Links to potential parents
    @Link Template template();
    @Link InstanceType instanceType();

    /**
     * Rerefences to the virtual machines that are using this device (a device may be used by several virtual machines,
     * for example a shared disk my be used by two or more virtual machines simultaneously).
     */
    @Link Vm[] vms();

    /**
     * Don't use this element, use `vms` instead.
     */
    @Deprecated
    @Link Vm vm();
}
