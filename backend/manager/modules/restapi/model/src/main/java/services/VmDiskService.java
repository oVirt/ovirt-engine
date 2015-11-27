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
import types.Disk;

@Service
public interface VmDiskService extends MeasurableService {
    interface Activate {
        /**
         * Indicates if the activation should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Deactivate {
        /**
         * Indicates if the deactivation should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Export {
        /**
         * Indicates if the export should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Get {
        @Out Disk disk();
    }

    interface Move {
        /**
         * Indicates if the move should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Update {
        @In @Out Disk disk();

        /**
         * Indicates if the update should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Remove {
        /**
         * Indicates if the remove should be performed asynchronously.
         */
        @In Boolean async();
    }

    @Service AssignedPermissionsService permissions();
}
