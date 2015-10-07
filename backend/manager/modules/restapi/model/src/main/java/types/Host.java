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
public interface Host extends Identified {
    String address();
    Status status();
    Certificate certificate();
    Status externalStatus();
    Integer port();
    String type();
    Spm spm();
    Version version();
    HardwareInformation hardwareInformation();
    PowerManagement powerManagement();
    Ksm ksm();
    TransparentHugePages transparentHugePages();
    IscsiDetails iscsi();
    /**
     * When creating a new host, a root password is required, but this is not
     * subsequently included in the representation.
     */
    String rootPassword();
    Ssh ssh();
    Statistic[] statistics();
    Cpu cpu();
    Integer memory();
    Integer maxSchedulingMemory();
    VmSummary summary();
    Boolean overrideIptables();
    String protocol();
    OperatingSystem os();
    Version libvirtVersion();
    /**
     * Optionally specify the display address of this host explicitly.
     */
    Display display();
    HostedEngine hostedEngine();
    String kdumpStatus();
    SeLinux selinux();
    String autoNumaStatus();
    Boolean numaSupported();
    Boolean liveSnapshotSupport();
    KatelloErratum[] katelloErrata();
    Boolean updateAvailable();
    HostDevicePassthrough devicePassthrough();

    @Link Cluster cluster();
    @Link Hook hooks();
    @Link ExternalHostProvider externalHostProvider();
    @Link StorageConnectionExtension[] storageConnectionExtensions();
}
