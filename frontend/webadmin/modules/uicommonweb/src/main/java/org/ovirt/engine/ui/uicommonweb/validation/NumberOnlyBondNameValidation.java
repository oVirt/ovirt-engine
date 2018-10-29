package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NumberOnlyBondNameValidation extends RegexValidation {

    public NumberOnlyBondNameValidation() {
        setExpression(BusinessEntitiesDefinitions.NUM_ONLY_BOND_NAME_PATTERN);
        setMessage(ConstantsManager.getInstance().getConstants().bondNameInvalid());
    }

}
