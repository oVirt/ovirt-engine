package org.ovirt.engine.core.bll.validator;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * A class that can validate a {@link vmNic} is valid from certain aspects.
 */
public class VmNicValidator {

    protected static final String CLUSTER_VERSION_REPLACEMENT_FORMAT = "$clusterVersion %s";

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
     * @return An error if unlinking is not supported and the interface is unlinked, otherwise it's OK.
     */
    public ValidationResult linkedCorrectly() {
        return !FeatureSupported.networkLinking(version) && !nic.isLinked()
                ? new ValidationResult(VdcBllMessages.UNLINKING_IS_NOT_SUPPORTED, clusterVersion())
                : ValidationResult.VALID;
    }

    /**
     * @return An error if unlinking is not supported and the network is not set, otherwise it's OK.
     */
    public ValidationResult emptyNetworkValid() {
        return !FeatureSupported.networkLinking(version) && nic.getVnicProfileId() == null
                ? new ValidationResult(VdcBllMessages.NULL_NETWORK_IS_NOT_SUPPORTED, clusterVersion())
                : ValidationResult.VALID;
    }

    /**
     * @return <ul>
     *         <li>{@code VdcBllMessages.ACTION_TYPE_FAILED_VNIC_PROFILE_NOT_EXISTS} if the profile doesn't exist.</li>
     *         <li>{@code VdcBllMessages.NETWORK_NOT_EXISTS_IN_CURRENT_CLUSTER} if the network is not in the current
     *         cluster.</li>
     *         <li>{@code ValidationResult.VALID} otherwise.</li>
     *         </ul>
     */
    public ValidationResult profileValid(Guid clusterId) {
        if (nic.getVnicProfileId() != null) {
            // Check that the profile exists
            VnicProfile vnicProfile = getVnicProfile();
            if (vnicProfile == null) {
                return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VNIC_PROFILE_NOT_EXISTS);
            }

            // Check that the network exists in current cluster
            Network network = getNetworkByVnicProfile(vnicProfile);
            if (network == null || !isNetworkInCluster(network, clusterId)) {
                return new ValidationResult(VdcBllMessages.NETWORK_NOT_EXISTS_IN_CURRENT_CLUSTER);
            }
        }

        return ValidationResult.VALID;
    }

    protected String clusterVersion() {
        return String.format(CLUSTER_VERSION_REPLACEMENT_FORMAT, version.getValue());
    }

    /**
     * @return An error if the network interface type is not compatible with the selected operating
     *         system.
     */
    public ValidationResult isCompatibleWithOs() {
        List<String> networkDevices = getOsRepository().getNetworkDevices(osId, version);
        List<VmInterfaceType> interfaceTypes = new ArrayList<VmInterfaceType>();

        for (String networkDevice : networkDevices) {
            interfaceTypes.add(VmInterfaceType.valueOf(networkDevice));
        }

        return (!interfaceTypes.contains(VmInterfaceType.forValue(nic.getType())))
                ? new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_INTERFACE_TYPE_IS_NOT_SUPPORTED_BY_OS)
                : ValidationResult.VALID;

    }

    public OsRepository getOsRepository() {
        return SimpleDependecyInjector.getInstance().get(OsRepository.class);
    }

    protected Network getNetworkByVnicProfile(VnicProfile vnicProfile) {
        return NetworkHelper.getNetworkByVnicProfile(vnicProfile);
    }

    protected boolean isNetworkInCluster(Network network, Guid clusterId) {
        return NetworkHelper.isNetworkInCluster(network, clusterId);
    }

    protected DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    protected VnicProfile getVnicProfile() {
        if (vnicProfile == null) {
            vnicProfile = loadVnicProfile(nic.getVnicProfileId());
        }

        return vnicProfile;
    }

    VnicProfile loadVnicProfile(Guid vnicProfileId) {
        return NetworkHelper.getVnicProfile(vnicProfileId);
    }

    protected Network getNetwork() {
        if (network == null) {
            network = getNetworkByVnicProfile(getVnicProfile());
        }

        return network;
    }
}
