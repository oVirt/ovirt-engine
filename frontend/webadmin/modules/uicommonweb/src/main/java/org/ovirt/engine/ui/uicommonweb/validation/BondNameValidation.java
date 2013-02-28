package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class BondNameValidation extends RegexValidation {

    public BondNameValidation() {
        setExpression(BusinessEntitiesDefinitions.BOND_NAME_PATTERN);
        setMessage(ConstantsManager.getInstance().getConstants().bondNameInvalid());
    }

}
