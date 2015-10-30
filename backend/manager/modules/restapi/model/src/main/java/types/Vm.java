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

import java.util.Date;

@Type
public interface Vm extends VmBase {
    String stopReason();
    Date startTime();
    Date stopTime();
    Boolean runOnce();
    Payload[] payloads();
    Statistic[] statistics();
    Tag[] tags();
    VmPlacementPolicy placementPolicy();
    String fqdn();
    Boolean useLatestTemplateVersion();
    Boolean nextRunConfigurationExists();
    String numaTuneMode();
    TimeZone guestTimeZone();
    GuestOperatingSystem guestOperatingSystem();

    @Link Host host();
    @Link Template template();
    @Link InstanceType instanceType();
    @Link Disk[] disks();
    @Link Nic[] nics();
    @Link Snapshot[] snapshots();
    @Link Quota quota();
    @Link VmPool vmPool();
    @Link Cdrom[] cdroms();
    @Link Floppy[] floppies();
    @Link ReportedDevice[] reportedDevices();
    @Link Watchdog[] watchdogs();
    @Link Permission[] permissions();
    @Link ExternalHostProvider externalHostProvider();
    @Link KatelloErratum[] katelloErrata();
}
