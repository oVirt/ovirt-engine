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

import javax.xml.ws.Response;

import org.ovirt.api.metamodel.annotations.In;
import org.ovirt.api.metamodel.annotations.Out;
import org.ovirt.api.metamodel.annotations.Service;
import types.Permit;

@Service
public interface PermitsService {
    /**
     * Adds a permit to the set aggregated by parent role. The permit must be one retrieved from the capabilities
     * resource.
     */
    interface Add {
        /**
         * The permit to add.
         */
        @In @Out Permit permit();

    }

    interface List {
        @Out Permit[] permits();
    }

    /**
     * Sub-resource locator method, returns individual permit resource on which the remainder of the URI is dispatched.
     */
    @Service PermitService permit(String id);
}
