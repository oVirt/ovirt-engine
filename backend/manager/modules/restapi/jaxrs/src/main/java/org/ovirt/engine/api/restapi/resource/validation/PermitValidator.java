package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.api.model.PermitType;
import org.ovirt.engine.api.restapi.types.PermitMapper;

@ValidatedClass(clazz = Permit.class)
public class PermitValidator implements Validator<Permit> {

    @Override
    public void validateEnums(Permit permit) {
        if (permit!=null) {
            if (permit.isSetName()) {
                // VM_BASIC_OPERATIONS is deprecated in ActionGroup
                // We are keeping its id for backward compatibility.
                if (!permit.getName().toLowerCase().equals(PermitType.VM_BASIC_OPERATIONS.toString().toLowerCase())) {
                    validateEnum(PermitType.class, permit.getName(), true);
                }
            }
            if (permit.isSetId()) {
                boolean valid = false;
                // VM_BASIC_OPERATIONS is deprecated in ActionGroup
                // We are keeping its id for backward compatibility.
                if (permit.getId().equals(PermitType.getVmBasicOperationsId())) {
                    valid = true;
                } else {
                    for (PermitType permitType : PermitType.values()) {
                        Permit mappedPermit = PermitMapper.map(permitType, (Permit)null);
                        if (mappedPermit != null && mappedPermit.getId().equals(permit.getId())) {
                            valid = true;
                            break;
                        }
                    }
                }
                if (!valid) {
                    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                            .entity(permit.getId() + " is not a valid permit ID.")
                            .build());
                }
            }
        }
    }

}
