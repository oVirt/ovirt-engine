package org.ovirt.engine.core.bll.validator;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Version;

/**
 * A class that can validate a {@link vmNic} is valid from certain aspects.
 */
public class VmNicValidator {

    protected static final String CLUSTER_VERSION_REPLACEMENT_FORMAT = "$clusterVersion %s";

    protected VmNic nic;

    protected Version version;

    public VmNicValidator(VmNic nic, Version version) {
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
    public ValidationResult emptyNetworkValid() {
        return !FeatureSupported.networkLinking(version) && nic.getVnicProfileId() == null
                ? new ValidationResult(VdcBllMessages.NULL_NETWORK_IS_NOT_SUPPORTED, clusterVersion())
                : ValidationResult.VALID;
    }

    protected String clusterVersion() {
        return String.format(CLUSTER_VERSION_REPLACEMENT_FORMAT, version.getValue());
    }
}
