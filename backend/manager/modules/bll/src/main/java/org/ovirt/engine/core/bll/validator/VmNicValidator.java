package org.ovirt.engine.core.bll.validator;

import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.customprop.DevicePropertiesUtils;
import org.ovirt.engine.core.utils.customprop.ValidationError;

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

    public boolean validateCustomProperties(List<String> messages) {
        // validate custom properties
        List<ValidationError> errors =
                DevicePropertiesUtils.getInstance().validateProperties(version,
                        VmDeviceGeneralType.INTERFACE,
                        nic.getCustomProperties());
        if (!errors.isEmpty()) {
            DevicePropertiesUtils.getInstance().handleCustomPropertiesError(errors, messages);
            return false;
        }
        return true;
    }

    public ValidationResult portMirroringNotSetIfExternalNetwork(Network network) {
        return !nic.isPortMirroring() || !network.isExternal()
                ? ValidationResult.VALID
                : new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_CANNOT_BE_PORT_MIRRORED);
    }
}
