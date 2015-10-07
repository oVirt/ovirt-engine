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
import types.StorageDomain;

@Service
public interface StorageDomainService {
    interface Get {
        @Out StorageDomain storageDomain();
    }

    interface IsAttached {
    }

    interface Update {
        @In @Out StorageDomain storageDomain();
    }

    interface RefreshLuns {
    }

    interface Remove {
    }

    @Service AssignedDiskProfilesService diskProfiles();
    @Service AssignedPermissionsService permissions();
    @Service DiskSnapshotsService diskSnapshots();
    @Service DisksService disks();
    @Service FilesService files();
    @Service ImagesService images();
    @Service StorageDomainServerConnectionsService storageConnections();
    @Service StorageDomainTemplatesService templates();
    @Service StorageDomainVmsService vms();
}
