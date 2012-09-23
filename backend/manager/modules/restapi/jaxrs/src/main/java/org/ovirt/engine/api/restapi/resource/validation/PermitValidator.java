package org.ovirt.engine.api.restapi.resource.validation;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.api.model.PermitType;
import org.ovirt.engine.api.restapi.types.PermitMapper;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

@ValidatedClass(clazz = Permit.class)
public class PermitValidator implements Validator<Permit> {

    @Override
    public void validateEnums(Permit permit) {
        if (permit!=null) {
            if (permit.isSetName()) {
                validateEnum(PermitType.class, permit.getName(), true);
            }
            if (permit.isSetId()) {
                boolean valid = false;
                for (PermitType permitType : PermitType.values()) {
                    Permit mappedPermit = PermitMapper.map(permitType, (Permit)null);
                    if (mappedPermit != null && mappedPermit.getId().equals(permit.getId())) {
                        valid = true;
                        break;
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
