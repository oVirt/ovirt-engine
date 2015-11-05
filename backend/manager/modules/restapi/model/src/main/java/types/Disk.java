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
public interface Disk extends Device {
    String alias();
    String imageId();
    Integer provisionedSize();
    Integer actualSize();
    Status status();
    String _interface();
    DiskFormat format();
    Boolean sparse();
    Boolean bootable();
    Boolean shareable();
    Boolean wipeAfterDelete();
    Boolean propagateErrors();
    Statistic[] statistics();
    Boolean active();
    Boolean readOnly();
    HostStorage lunStorage();
    String sgio();
    Boolean usesScsiReservation();
    String storageType();
    String logicalName();

    @Link StorageDomain storageDomain();
    @Link StorageDomain[] storageDomains();
    @Link Quota quota();
    @Link DiskProfile diskProfile();
    @Link Snapshot snapshot();
    @Link OpenStackVolumeType openstackVolumeType();
}
