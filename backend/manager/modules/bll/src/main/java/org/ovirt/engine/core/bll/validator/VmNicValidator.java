package org.ovirt.engine.core.bll.validator;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.network.SwitchType;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.di.Injector;

/**
 * A class that can validate a {@link VmNic} is valid from certain aspects.
 */
public class VmNicValidator {

    protected VmNic nic;

    protected Version version;

    protected int osId;

    private VnicProfile vnicProfile = null;

    private Network network = null;

    public VmNicValidator(VmNic nic, Version version) {
        this.nic = nic;
        this.version = version;
    }

    public VmNicValidator(VmNic nic, Version version, int osId) {
        this.nic = nic;
        this.version = version;
        this.osId = osId;
    }

    /**
     * @return An error if the interface is passthrough and unlinked, otherwise it's OK.
     */
    public ValidationResult passthroughIsLinked() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_UNLINKING_OF_PASSTHROUGH_VNIC_IS_NOT_SUPPORTED)
                .when(nic.isPassthrough() && !nic.isLinked());
    }

    /**
     * @return <ul>
     *         <li>{@code EngineMessage.ACTION_TYPE_FAILED_VNIC_PROFILE_NOT_EXISTS} if the profile doesn't exist.</li>
     *         <li>{@code EngineMessage.NETWORK_NOT_EXISTS_IN_CURRENT_CLUSTER} if the network is not in the current
     *         cluster.</li>
     *         <li>{@code ValidationResult.VALID} otherwise.</li>
     *         </ul>
     */
    public ValidationResult profileValid(Guid clusterId) {
        if (nic.getVnicProfileId() != null) {
            // Check that the profile exists
            VnicProfile vnicProfile = getVnicProfile();
            if (vnicProfile == null) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VNIC_PROFILE_NOT_EXISTS);
            }

            // Check that the network exists in current cluster
            Network network = getNetworkByVnicProfile(vnicProfile);
            if (network == null || !isNetworkInCluster(network, clusterId)) {
                return new ValidationResult(EngineMessage.NETWORK_NOT_EXISTS_IN_CURRENT_CLUSTER);
            }
        }

        return ValidationResult.VALID;
    }

    /**
     * @return An error if the network interface type is not compatible with the selected operating
     *         system.
     */
    public ValidationResult isCompatibleWithOs() {
        List<String> networkDevices = Injector.get(OsRepository.class).getNetworkDevices(osId, version);
        List<VmInterfaceType> interfaceTypes = new ArrayList<>();

        for (String networkDevice : networkDevices) {
            interfaceTypes.add(VmInterfaceType.valueOf(networkDevice));
        }

        return !interfaceTypes.contains(VmInterfaceType.forValue(nic.getType()))
                ? new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_INTERFACE_TYPE_IS_NOT_SUPPORTED_BY_OS)
                : ValidationResult.VALID;

    }

    public ValidationResult typeMatchesProfile() {
        boolean profilePassthrough = getVnicProfile() != null && getVnicProfile().isPassthrough();
        boolean typePassthrough = VmInterfaceType.pciPassthrough == VmInterfaceType.forValue(nic.getType());
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_VM_INTERFACE_TYPE_NOT_MATCH_PROFILE)
                .when(profilePassthrough ^ typePassthrough);
    }

    protected Network getNetworkByVnicProfile(VnicProfile vnicProfile) {
        return getNetworkHelper().getNetworkByVnicProfile(vnicProfile);
    }

    protected boolean isNetworkInCluster(Network network, Guid clusterId) {
        return getNetworkHelper().isNetworkInCluster(network, clusterId);
    }

    protected VnicProfile getVnicProfile() {
        if (vnicProfile == null) {
            vnicProfile = loadVnicProfile(nic.getVnicProfileId());
        }

        return vnicProfile;
    }

    VnicProfile loadVnicProfile(Guid vnicProfileId) {
        return getNetworkHelper().getVnicProfile(vnicProfileId);
    }

    private NetworkHelper getNetworkHelper() {
        return Injector.get(NetworkHelper.class);
    }

    protected Network getNetwork() {
        if (network == null) {
            network = getNetworkByVnicProfile(getVnicProfile());
        }

        return network;
    }

    public ValidationResult validateProfileNotEmptyForHostedEngineVm(VM vm) {
        return ValidationResult.failWith(EngineMessage.HOSTED_ENGINE_VM_CANNOT_HAVE_NIC_WITH_EMPTY_PROFILE)
                .when(vm.isHostedEngine() && nic.getVnicProfileId() == null);
    }

    private boolean isVnicEmptyOrAttachedToExternalNetwork() {
        final Network network = getNetwork();
        return network == null || network.isExternal();
    }

    public ValidationResult isNetworkSupportedByClusterSwitchType(Cluster cluster) {
        return ValidationResult
                .failWith(EngineMessage.ACTION_TYPE_FAILED_ONLY_EXTERNAL_NETWORK_IS_SUPPORTED_IN_OVS_SWITCH_TYPE)
                .when(cluster.getRequiredSwitchTypeForCluster().equals(SwitchType.OVS)
                        && !isVnicEmptyOrAttachedToExternalNetwork());
    }

    public ValidationResult isFailoverInSupportedClusterVersion() {
        if (getVnicProfile() == null || getVnicProfile().getFailoverVnicProfileId() == null) {
            return ValidationResult.VALID;
        }

        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_VM_INTERFACE_WITH_FAILOVER_IS_SUPPORTED_ONLY_IN_CLUSTER_4_6_AND_ABOVE)
                .when(version.less(Version.v4_6));
    }
}
