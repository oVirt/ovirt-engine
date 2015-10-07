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

package types;

import org.ovirt.api.metamodel.annotations.Link;
import org.ovirt.api.metamodel.annotations.Type;

@Type
public interface Statistic extends Identified {
    Value[] values();
    StatisticKind kind();
    ValueType type();
    StatisticUnit unit();

    @Link Disk disk();
    @Link Host host();
    @Link HostNic hostNic();
    @Link NumaNode hostNumaNode();
    @Link Nic nic();
    @Link Vm vm();
    @Link GlusterBrick brick();
    @Link Step step();
    @Link GlusterVolume glusterVolume();
}
