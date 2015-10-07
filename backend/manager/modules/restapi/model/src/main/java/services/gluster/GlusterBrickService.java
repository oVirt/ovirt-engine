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

import org.ovirt.api.metamodel.annotations.Out;
import org.ovirt.api.metamodel.annotations.Service;
import services.MeasurableService;
import types.GlusterBrick;

@Service
public interface GlusterBrickService extends MeasurableService {
    interface Get {
        @Out GlusterBrick brick();
    }

    /**
     * Removes this brick from the volume and deletes it from the database.
     */
    interface Remove {
    }

    /**
     * Replaces this brick with a new one. The property `brick` is required.
     */
    @Deprecated
    interface Replace {
    }
}
