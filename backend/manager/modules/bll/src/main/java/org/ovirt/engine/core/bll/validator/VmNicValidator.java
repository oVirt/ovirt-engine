package org.ovirt.engine.core.bll.validator;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;

/**
 * A class that can validate a {@link VmNetworkInterface} is valid from certain aspects.
 */
public class VmNicValidator {

    protected static final String CLUSTER_VERSION_REPLACEMENT_FORMAT = "$clusterVersion %s";

    protected VmNetworkInterface nic;

    protected Version version;

    public VmNicValidator(VmNetworkInterface nic, Version version) {
        this.nic = nic;
        this.version = version;
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
    public ValidationResult networkNameValid() {
        return !FeatureSupported.networkLinking(version) && nic.getNetworkName() == null
                ? new ValidationResult(VdcBllMessages.NULL_NETWORK_IS_NOT_SUPPORTED, clusterVersion())
                : ValidationResult.VALID;
    }

    /**
     * @return An error if no network is set but port mirroring is set.
     */
    public ValidationResult networkProvidedForPortMirroring() {
        return nic.getNetworkName() == null && nic.isPortMirroring()
                ? new ValidationResult(VdcBllMessages.PORT_MIRRORING_REQUIRES_NETWORK)
                : ValidationResult.VALID;
    }

    protected String clusterVersion() {
        return String.format(CLUSTER_VERSION_REPLACEMENT_FORMAT, version.getValue());
    }
}
