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
import types.Vm;

@Service
public interface AffinityGroupVmsService {
    interface Add {
        @In @Out Vm vm();
    }

    interface List {
        @Out Vm[] vms();

        /**
         * Sets the maximum number of virtual machines to return. If not specified all the virtual machines are
         * returned.
         */
        @In Integer max();
    }

    @Service AffinityGroupVmService vm(String id);
}
