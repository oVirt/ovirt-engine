/*
Copyright (c) 2016 Red Hat, Inc.
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

package org.ovirt.engine.api.v3.helpers;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toSet;

import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.VnicProfile;
import org.ovirt.engine.api.resource.AssignedNetworksResource;
import org.ovirt.engine.api.resource.ClusterResource;
import org.ovirt.engine.api.resource.ClustersResource;
import org.ovirt.engine.api.resource.NetworkResource;
import org.ovirt.engine.api.resource.NetworksResource;
import org.ovirt.engine.api.resource.SystemResource;
import org.ovirt.engine.api.resource.VmResource;
import org.ovirt.engine.api.resource.VmsResource;
import org.ovirt.engine.api.resource.VnicProfileResource;
import org.ovirt.engine.api.resource.VnicProfilesResource;
import org.ovirt.engine.api.restapi.resource.BackendApiResource;
import org.ovirt.engine.api.v3.types.V3NIC;
import org.ovirt.engine.api.v3.types.V3Network;
import org.ovirt.engine.api.v3.types.V3Networks;
import org.ovirt.engine.api.v3.types.V3PortMirroring;

/**
 * This class contains a set of methods useful to handle backwards compatibility issues related to NICs.
 */
public class V3NICHelper {
    /**
     * Finds the VNIC profile that correspond to the given V3 NIC and assigns it to the given V4 NIC.
     *
     * @param vmId the identifier of the virtual machine that the NIC is going to be added to
     * @param v3Nic the V3 NIC where the information will be extracted from
     * @param v4Nic the V4 NIC that will be populated
     */
    public static void setVnicProfile(String vmId, V3NIC v3Nic, Nic v4Nic) {
        // Do nothing if the profile is already set:
        if (v4Nic.isSetVnicProfile()) {
            return;
        }

        // In version 4 of the API the "network" and "port_mirroring" properties of the NIC have been completely
        // removed, and instead of using them it is required to specify a VNIC profile. This means that if the user
        // isn't explicitly indicating the VNIC profile we need to find one that is compatible with the given network
        // and port mirroring configuration. The only VNIC profiles to consider are the ones corresponding to the
        // networks of the cluster where the VM resides, so we need to retrieve the VM, then the cluster, and then the
        // identifiers of the networks:
        SystemResource systemService = BackendApiResource.getInstance();
        VmsResource vmsService = systemService.getVmsResource();
        VmResource vmService = vmsService.getVmResource(vmId);
        Vm vm = vmService.get();
        ClustersResource clustersService = systemService.getClustersResource();
        ClusterResource clusterService = clustersService.getClusterResource(vm.getCluster().getId());
        AssignedNetworksResource networksService = clusterService.getNetworksResource();
        Set<String> validNetworkIds = networksService.list().getNetworks().stream()
            .map(Network::getId)
            .collect(toSet());

        // Find a VNIC profile that is in the set of valid networks and that is compatible with the NIC:
        VnicProfilesResource profilesService = systemService.getVnicProfilesResource();
        profilesService.list().getVnicProfiles().stream()
            .filter(profile -> validNetworkIds.contains(profile.getNetwork().getId()))
            .filter(profile -> isProfileCompatible(profile, v3Nic))
            .sorted(comparing(VnicProfile::getName))
            .map(VnicProfile::getId)
            .findFirst()
            .ifPresent(id -> {
                VnicProfile v4Profile = new VnicProfile();
                v4Profile.setId(id);
                v4Nic.setVnicProfile(v4Profile);
            });
    }

    /**
     * Populates the {@code network} and {@code port_mirroring} attributes used in V3.
     *
     * @param v4Nic the V4 NIC where the details of the NIC profile will be extracted from
     * @param v3Nic the V3 NIC object that will be populated
     */
    public static void setNetworkAndPortMirroring(Nic v4Nic, V3NIC v3Nic) {
        // Do nothing if the V4 NIC doesn't specify a profile:
        VnicProfile v4Profile = v4Nic.getVnicProfile();
        if (v4Profile == null) {
            return;
        }

        // Retrieve the complete representation of the profile, as we need it to compute the "network" and
        // "port_mirroring" attributes used in V3:
        BackendApiResource systemService = BackendApiResource.getInstance();
        VnicProfilesResource profilesService = systemService.getVnicProfilesResource();
        VnicProfileResource profileService = profilesService.getProfileResource(v4Profile.getId());
        v4Profile = profileService.get();

        // Populate the "network" and "port_mirroring" attributes of the V3 object:
        Network v4Network = v4Profile.getNetwork();
        V3Network v3Network = new V3Network();
        v3Network.setId(v4Network.getId());
        v4Network.setHref(v4Network.getHref());
        v3Nic.setNetwork(v3Network);
        if (v4Profile.isSetPortMirroring() && v4Profile.isPortMirroring()) {
            V3PortMirroring v3PortMirroring = new V3PortMirroring();
            V3Networks v3PortMirroringNetworks = new V3Networks();
            V3Network v3PortMirroringNetwork = new V3Network();
            v3PortMirroringNetwork.setId(v4Network.getId());
            v3PortMirroringNetwork.setHref(v4Network.getHref());
            v3PortMirroringNetworks.getNetworks().add(v3PortMirroringNetwork);
            v3PortMirroring.setNetworks(v3PortMirroringNetworks);
            v3Nic.setPortMirroring(v3PortMirroring);
        }
    }

    /**
     * Checks if the given VNIC profile is compatible with the given NIC.
     *
     * @param profile the VNIC profile to check
     * @param nic the NIC to check
     * @return {@code true} iff the profile is compatible with the network and port mirroring configuration of the NIC
     */
    private static boolean isProfileCompatible(VnicProfile profile, V3NIC nic) {
        // Retrieve the representation of the network corresponding to the profile, as we are going to need it in
        // order to check the name:
        SystemResource systemService = BackendApiResource.getInstance();
        NetworksResource networksService = systemService.getNetworksResource();
        NetworkResource networkService = networksService.getNetworkResource(profile.getNetwork().getId());
        Network profileNetwork = networkService.get();

        // If the NIC configuration explicitly specifies a network then the profile has to correspond to that same
        // network:
        V3Network nicNetwork = nic.getNetwork();
        if (nicNetwork != null) {
            if (nicNetwork.isSetId()) {
                if (!Objects.equals(profileNetwork.getId(), nicNetwork.getId())) {
                    return false;
                }
            }
            if (nicNetwork.isSetName()) {
                if (!Objects.equals(profileNetwork.getName(), nicNetwork.getName())) {
                    return false;
                }
            }
        }

        // If the NIC configuration explicitly specifies a port mirroring configuration then the profile must have
        // port mirroring enabled, and all the networks included in the port mirroring configuration must be the same
        // network used by the profile:
        V3PortMirroring nicPortMirroring = nic.getPortMirroring();
        if (nicPortMirroring != null) {
            if (!profile.isSetPortMirroring() || !profile.isPortMirroring()) {
                return false;
            }
            V3Networks nicPortMirroringNetworks = nicPortMirroring.getNetworks();
            if (nicPortMirroringNetworks != null) {
                for (V3Network nicPortMirroringNetwork : nicPortMirroringNetworks.getNetworks()) {
                    if (nicPortMirroringNetwork.isSetId()) {
                        if (!Objects.equals(profileNetwork.getId(), nicPortMirroringNetwork.getId())) {
                            return false;
                        }
                    }
                    if (nicPortMirroringNetwork.isSetName()) {
                        if (!Objects.equals(profileNetwork.getName(), nicPortMirroringNetwork.getName())) {
                            return false;
                        }
                    }
                }
            }
        }

        // All checks passed, so the profile is compatible:
        return true;
    }
}
