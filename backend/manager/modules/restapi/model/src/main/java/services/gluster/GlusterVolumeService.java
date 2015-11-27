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
import services.MeasurableService;
import types.GlusterVolume;
import types.GlusterVolumeProfileDetails;
import types.Option;

@Service
public interface GlusterVolumeService extends MeasurableService {
    interface Get {
        @Out GlusterVolume volume();
    }

    // TODO: Should also support "application/pdf"
    interface GetProfileStatistics {
        @Out GlusterVolumeProfileDetails details();
    }

    interface Rebalance {
        @In Boolean fixLayout();
        @In Boolean force();

        /**
         * Indicates if the rebalance should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Remove {
        /**
         * Indicates if the remove should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface ResetAllOptions {
        /**
         * Indicates if the reset should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface ResetOption {
        @In Boolean force();
        @In Option option();

        /**
         * Indicates if the reset should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface SetOption {
        @In Option option();

        /**
         * Indicates if the action should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Start {
        @In Boolean force();

        /**
         * Indicates if the action should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface StartProfile {
        /**
         * Indicates if the action should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface Stop {
        @In Boolean force();

        /**
         * Indicates if the action should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface StopProfile {
        /**
         * Indicates if the action should be performed asynchronously.
         */
        @In Boolean async();
    }

    interface StopRebalance {
        /**
         * Indicates if the action should be performed asynchronously.
         */
        @In Boolean async();
    }

    @Service GlusterBricksService glusterBricks();
}
