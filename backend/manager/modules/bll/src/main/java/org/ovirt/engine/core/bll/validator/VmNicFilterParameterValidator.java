package org.ovirt.engine.core.bll.validator;

import java.util.Objects;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.dao.network.VmNicFilterParameterDao;
import org.ovirt.engine.core.utils.ReplacementUtils;

/**
 * A class that can validate a {@link VmNicFilterParameter} is valid from certain aspects.
 */
public class VmNicFilterParameterValidator {
    static final String VAR_INTERFACE_ID = "INTERFACE_ID";
    static final String VAR_VM_ID = "VM_ID";
    static final String VAR_FILTER_ID = "FILTER_ID";

    private final VmNicFilterParameterDao vmNicFilterParameterDao;

    private final VmNicDao vmNicDao;

    @Inject
    VmNicFilterParameterValidator(VmNicFilterParameterDao vmNicFilterParameterDao, VmNicDao vmNicDao) {
        this.vmNicFilterParameterDao = vmNicFilterParameterDao;
        this.vmNicDao = vmNicDao;
    }


    /**
     * @return An error if the VmNicFilterParameter's id is not used.
     */
    public ValidationResult parameterHavingIdExists(Guid id) {
        if (vmNicFilterParameterDao.get(id) == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_NIC_FILTER_PARAMETER_ID_NOT_EXISTS,
                    ReplacementUtils.createSetVariableString(VAR_FILTER_ID, id));
        }
        return ValidationResult.VALID;
    }

    /**
     * @return An error if the VmNicFilterParameter's vmInterfaceId does not exists.
     */
    public ValidationResult vmInterfaceHavingIdExists(Guid vmInterfaceId) {
        if (vmNicDao.get(vmInterfaceId) == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_INVALID_NIC_FILTER_PARAMETER_INTERFACE,
                    ReplacementUtils.createSetVariableString(VAR_INTERFACE_ID, vmInterfaceId));
        }
        return ValidationResult.VALID;
    }

    /**
     * @return An error if the interface does not exist on vm.
     */
    public ValidationResult vmInterfaceHavingIdExistsOnVmHavingId(Guid vmInterfaceId, Guid vmId) {
        VmNic vmNic = Objects.requireNonNull(vmNicDao.get(vmInterfaceId));
        if (!vmNic.getVmId().equals(vmId)) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_INVALID_NIC_FILTER_PARAMETER_INTERFACE_VM,
                    ReplacementUtils.createSetVariableString(VAR_INTERFACE_ID, vmInterfaceId),
                    ReplacementUtils.createSetVariableString(VAR_VM_ID, vmId));
        }
        return ValidationResult.VALID;
    }
}
