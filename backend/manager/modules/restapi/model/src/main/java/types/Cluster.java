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
public interface Cluster extends Identified {
    Cpu cpu();
    MemoryPolicy memoryPolicy();
    SchedulingPolicy schedulingPolicy();
    Version version();
    Version[] supportedVersions();
    ErrorHandling errorHandling();
    Boolean virtService();
    Boolean glusterService();
    Boolean threadsAsCores();
    Boolean tunnelMigration();
    Boolean trustedService();
    Boolean haReservation();
    Boolean optionalReason();
    Boolean maintenanceReasonRequired();
    Boolean ballooningEnabled();
    Display display();
    Ksm ksm();
    SerialNumber serialNumber();
    RngSource[] requiredRngSources();
    FencingPolicy fencingPolicy();
    MigrationOptions migration();

    @Link DataCenter dataCenter();
    @Link Network managementNetwork();
}
