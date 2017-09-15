package org.ovirt.engine.core.bll.validator;

import java.util.Set;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.di.Injector;

/**
 * A class that can validate a {@link VmWatchdog} is valid from certain aspects.
 */
public class VmWatchdogValidator {

    private VmWatchdogClusterIndependentValidator clusterIndependentPart;

    private VmWatchdogClusterDependentValidator clusterDependentPart;

    public VmWatchdogValidator(int osId, VmWatchdog vmWatchdog, Version version) {
        this.clusterIndependentPart = new VmWatchdogClusterIndependentValidator(vmWatchdog);
        this.clusterDependentPart = new VmWatchdogClusterDependentValidator(osId, vmWatchdog, version);
    }

    public ValidationResult isValid() {
        ValidationResult properlyFilledResult = clusterIndependentPart.isValid();
        if (!properlyFilledResult.isValid()) {
            return properlyFilledResult;
        }

        return clusterDependentPart.isValid();
    }

    public static class VmWatchdogClusterDependentValidator {

        private int osId;
        private Version version;
        private VmWatchdog vmWatchdog;

        public VmWatchdogClusterDependentValidator(int osId, VmWatchdog vmWatchdog, Version version) {
            this.osId = osId;
            this.version = version;
            this.vmWatchdog = vmWatchdog;
        }

        /**
         * Check if the watchdog model is supported (as per the configuration), taking into account the
         * OS type.
         *
         * @return An error if the watchdog model is not compatible with the selected operating system,
         * otherwise it's OK.
         */
        public ValidationResult isValid() {
            Set<VmWatchdogType> vmWatchdogTypes = getOsRepository().getVmWatchdogTypes(osId, version);

            return !vmWatchdogTypes.contains(vmWatchdog.getModel())
                    ? new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_ILLEGAL_WATCHDOG_MODEL_IS_NOT_SUPPORTED_BY_OS)
                    : ValidationResult.VALID;
        }

        public OsRepository getOsRepository() {
            return Injector.get(OsRepository.class);
        }
    }

    public static class VmWatchdogClusterIndependentValidator {

        private VmWatchdog vmWatchdog;

        public VmWatchdogClusterIndependentValidator(VmWatchdog vmWatchdog) {
            this.vmWatchdog = vmWatchdog;
        }

        public ValidationResult isValid() {
            if (vmWatchdog.getAction() == null) {
                return new ValidationResult(EngineMessage.WATCHDOG_ACTION_REQUIRED);
            }
            if (vmWatchdog.getModel() == null) {
                return new ValidationResult(EngineMessage.WATCHDOG_MODEL_REQUIRED);
            }

            return ValidationResult.VALID;
        }
    }
}
