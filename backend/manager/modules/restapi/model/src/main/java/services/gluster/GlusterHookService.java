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
import types.GlusterHook;
import types.Host;

@Service
public interface GlusterHookService {
    /**
     * Resolves status conflict of hook among servers in cluster by disabling Gluster hook in all servers of the
     * cluster. This updates the hook status to `DISABLED` in database.
     */
    interface Disable {
        /**
         * Indicates if the action should be performed asynchronously.
         */
        @In Boolean async();
    }

    /**
     * Resolves status conflict of hook among servers in cluster by disabling Gluster hook in all servers of the
     * cluster. This updates the hook status to `DISABLED` in database.
     */
    interface Enable {
        /**
         * Indicates if the action should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Get {
        @Out GlusterHook hook();
    }

    /**
     * Removes the this Gluster hook from all servers in cluster and deletes it from the database.
     */
    interface Remove {
        /**
         * Indicates if the remove should be performed asynchronously.
         */
        @In Boolean async();
    }

    /**
     * Resolves missing hook conflict depending on the resolution type.
     *
     * For `ADD` resolves by copying hook stored in engine database to all servers where the hook is missing. The
     * engine maintains a list of all servers where hook is missing.
     *
     * For `COPY` resolves conflict in hook content by copying hook stored in engine database to all servers where
     * the hook is missing. The engine maintains a list of all servers where the content is conflicting. If a host
     * id is passed as parameter, the hook content from the server is used as the master to copy to other servers
     * in cluster.
     */
    interface Resolve {
        @In Host host();
        @In String resolutionType();

        /**
         * Indicates if the action should be performed asynchronously.
         */
        @In Boolean async();
    }
}
