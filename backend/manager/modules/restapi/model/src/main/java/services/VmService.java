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
import services.externalhostproviders.KatelloErrataService;
import types.Cluster;
import types.Disk;
import types.Host;
import types.Snapshot;
import types.StorageDomain;
import types.Vm;

@Service
public interface VmService extends MeasurableService {
    interface CancelMigration {
    }

    interface CommitSnapshot {
    }

    interface Clone {
        @In Vm vm();
    }

    interface Detach {
    }

    interface Export {
        @In Boolean discardSnapshots();
        @In Boolean exclusive();
        @In StorageDomain storageDomain();
    }

    interface FreezeFilesystems {
    }

    interface Get {
        @Out Vm vm();
    }

    interface Logon {
    }

    interface Maintenance {
        @In Boolean maintenanceEnabled();
    }

    interface Migrate {
        @In Cluster cluster();
        @In Boolean force();
        @In Host host();
    }

    /**
     * This action is deprecated, use the `move` operation of the disks instead.
     */
    @Deprecated
    interface Move {
        @In StorageDomain storageDomain();
    }

    interface PreviewSnapshot {
        @In Disk[] disks();
        @In Boolean restoreMemory();
        @In Snapshot snapshot();
        @In Vm vm();
    }

    interface Update {
        @In @Out Vm vm();
    }

    interface Reboot {
    }

    interface Remove {
    }

    interface ReorderMacAddresses {
    }

    interface Shutdown {
    }

    interface Start {
        @In Boolean pause();
        @In Vm vm();
        @In Boolean useCloudInit();
        @In Boolean useSysprep();
    }

    interface Stop {
    }

    interface Suspend {
    }

    interface ThawFilesystems {
    }

    interface Ticket {
        @In @Out Ticket ticket();
    }

    interface UndoSnapshot {
    }

    @Service AssignedPermissionsService permissions();
    @Service AssignedTagsService tags();
    @Service GraphicsConsolesService graphicsConsoles();
    @Service KatelloErrataService katelloErrata();
    @Service SnapshotsService snapshots();
    @Service VmApplicationsService applications();
    @Service VmCdromsService cdroms();
    @Service VmDisksService disks();
    @Service VmHostDevicesService hostDevices();
    @Service VmNicsService nics();
    @Service VmNumaNodesService virtualNumaNodes();
    @Service VmReportedDevicesService reportedDevices();
    @Service VmSessionsService sessions();
    @Service VmWatchdogsService watchdogs();
}
