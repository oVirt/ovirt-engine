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
public interface HostNic {
    Mac mac();
    Ip ip();
    String baseInterface();
    Vlan vlan();
    Bonding bonding();
    String bootProtocol();
    Statistic[] statistics();
    Boolean checkConnectivity();
    Integer speed();
    Status status();
    Integer mtu();
    Boolean bridged();
    Boolean customConfiguration();
    Boolean overrideConfiguration();
    Label[] labels();
    Property[] properties();
    /**
     * For a SR-IOV physical function NIC describes its virtual functions configuration.
     */
    HostNicVirtualFunctionsConfiguration virtualFunctionsConfiguration();

    @Link Host host();
    @Link Network network();
    @Link Qos qos();

    /**
     * For a SR-IOV virtual function NIC references to its physical function NIC.
     */
    @Link HostNic physicalFunction();
}
