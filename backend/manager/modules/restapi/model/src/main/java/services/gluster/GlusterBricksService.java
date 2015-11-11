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

package services.gluster;

import org.ovirt.api.metamodel.annotations.In;
import org.ovirt.api.metamodel.annotations.Out;
import org.ovirt.api.metamodel.annotations.Service;
import types.GlusterBrick;

@Service
public interface GlusterBricksService {
    interface Activate {
        @In GlusterBrick[] bricks();
    }

    /**
     * Adds given list of bricks to the volume, and updates the database accordingly. The properties `serverId` and
     * `brickDir`are required.
     */
    interface Add {
        /**
         * The list of bricks to be added to the volume
         */
        @In @Out GlusterBrick[] bricks();
    }

    interface List {
        @Out GlusterBrick[] bricks();
    }

    interface Migrate {
        @In GlusterBrick[] bricks();
    }

    /**
     * Removes the given list of bricks brick from the volume and deletes them from the database.
     */
    interface Remove {
        /**
         * The list of bricks to be removed
         */
        @In GlusterBrick[] bricks();
    }

    interface StopMigrate {
        @In GlusterBrick[] bricks();
    }

    @Service GlusterBrick brick(String id);
}
