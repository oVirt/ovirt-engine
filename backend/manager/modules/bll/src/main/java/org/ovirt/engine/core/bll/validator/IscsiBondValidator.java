package org.ovirt.engine.core.bll.validator;

import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class IscsiBondValidator {

    public ValidationResult iscsiBondWithTheSameNameExistInDataCenter(IscsiBond iscsiBond) {
        List<IscsiBond> iscsiBonds = DbFacade.getInstance().getIscsiBondDao().getAllByStoragePoolId(iscsiBond.getStoragePoolId());

        for (IscsiBond bond : iscsiBonds) {
            if (bond.getName().equals(iscsiBond.getName()) && !bond.getId().equals(iscsiBond.getId())) {
                return new ValidationResult(VdcBllMessages.ISCSI_BOND_WITH_SAME_NAME_EXIST_IN_DATA_CENTER);
            }
        }

        return ValidationResult.VALID;
    }

    public ValidationResult isIscsiBondExist(IscsiBond iscsiBond) {
        return (iscsiBond == null) ?
                new ValidationResult(VdcBllMessages.ISCSI_BOND_NOT_EXIST) : ValidationResult.VALID;
    }
}
