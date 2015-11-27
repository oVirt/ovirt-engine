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
        /**
         * Indicates if the migration should cancelled asynchronously.
         */
        @In Boolean async();
    }

    interface CommitSnapshot {
        /**
         * Indicates if the snapshots should be committed asynchronously.
         */
        @In Boolean async();
    }

    interface Clone {
        @In Vm vm();

        /**
         * Indicates if the clone should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Detach {
        /**
         * Indicates if the detach should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Export {
        @In Boolean discardSnapshots();
        @In Boolean exclusive();
        @In StorageDomain storageDomain();

        /**
         * Indicates if the export should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface FreezeFilesystems {
        /**
         * Indicates if the freeze should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Get {
        @Out Vm vm();
    }

    interface Logon {
        /**
         * Indicates if the logon should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Maintenance {
        @In Boolean maintenanceEnabled();

        /**
         * Indicates if the action should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Migrate {
        @In Cluster cluster();
        @In Boolean force();
        @In Host host();

        /**
         * Indicates if the migration should be performed asynchronously.
         */
        @In Boolean async();
    }

    /**
     * This action is deprecated, use the `move` operation of the disks instead.
     */
    @Deprecated
    interface Move {
        @In StorageDomain storageDomain();

        /**
         * Indicates if the move should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface PreviewSnapshot {
        @In Disk[] disks();
        @In Boolean restoreMemory();
        @In Snapshot snapshot();
        @In Vm vm();

        /**
         * Indicates if the preview should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Update {
        @In @Out Vm vm();

        /**
         * Indicates if the update should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Reboot {
        /**
         * Indicates if the reboot should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Remove {
        /**
         * Indicates if the remove should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface ReorderMacAddresses {
        /**
         * Indicates if the action should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Shutdown {
        /**
         * Indicates if the shutdown should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Start {
        @In Boolean pause();
        @In Vm vm();
        @In Boolean useCloudInit();
        @In Boolean useSysprep();

        /**
         * Indicates if the action should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Stop {
        /**
         * Indicates if the action should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Suspend {
        /**
         * Indicates if the action should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface ThawFilesystems {
        /**
         * Indicates if the action should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Ticket {
        @In @Out Ticket ticket();

        /**
         * Indicates if the generation of the ticket should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface UndoSnapshot {
        /**
         * Indicates if the action should be performed asynchronously.
         */
        @In Boolean async();
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
