package org.ovirt.engine.core.bll.validator;

import java.util.Set;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.compat.Version;

/**
 * A class that can validate a {@link VmWatchdog} is valid from certain aspects.
 */
public class VmWatchdogValidator {

    private int osId;
    private Version version;
    private VmWatchdog vmWatchdog;

    public VmWatchdogValidator(int osId, VmWatchdog vmWatchdog, Version version) {
        this.osId = osId;
        this.vmWatchdog = vmWatchdog;
        this.version = version;
    }

    /**
     * Check if the watchdog model is supported (as per the configuration), taking into account the
     * OS type.
     *
     * @return An error if the watchdog model is not compatible with the selected operating system,
     * otherwise it's OK.
     */
    public ValidationResult isModelCompatibleWithOs() {
        Set<VmWatchdogType> vmWatchdogTypes = getOsRepository().getVmWatchdogTypes(osId, version);

        return (!vmWatchdogTypes.contains(vmWatchdog.getModel()))
                ? new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_ILLEGAL_WATCHDOG_MODEL_IS_NOT_SUPPORTED_BY_OS)
                : ValidationResult.VALID;
    }

    public OsRepository getOsRepository() {
        return SimpleDependecyInjector.getInstance().get(OsRepository.class);
    }

}
