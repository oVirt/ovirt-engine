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
public interface StorageDomain extends Identified {
    StorageDomainType type();
    Status status();
    Status externalStatus();
    Boolean master();
    HostStorage storage();
    Integer available();
    Integer used();
    Integer committed();
    StorageFormat storageFormat();
    Boolean wipeAfterDelete();
    Boolean _import(); // TODO: Should be an action parameter.
    Integer warningLowSpaceIndicator();
    Integer criticalSpaceActionBlocker();

    /**
     * Host is only relevant at creation time.
     * @return
     */
    @Link Host host();
    /**
     * This is used to link to the data center that the storage domain is attached to. It is preserved for backwards
     * compatibility, as the storage domain may be attached to multiple data centers (if it is an ISO domain). Use
     * the `dataCenters` element instead.
     */
    @Link DataCenter dataCenter();
    /**
     * This is a set of links to the data centers that the storage domain is attached to.
     */
    @Link DataCenter[] dataCenters();
}
