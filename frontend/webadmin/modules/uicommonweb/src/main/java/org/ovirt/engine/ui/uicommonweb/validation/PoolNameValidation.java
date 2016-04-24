package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class PoolNameValidation extends BaseI18NValidation {

    @Override
    protected String composeRegex() {
        return or(start() + oneOrMore(nonNumberMaskCharacter()) + numberMask() + zeroOrMore(nonNumberMaskCharacter()) + end(),
                start() + zeroOrMore(nonNumberMaskCharacter()) + numberMask() + oneOrMore(nonNumberMaskCharacter()) + end());
    }

    protected String numberMask() {
        return "[" + VmPool.MASK_CHARACTER + "]*"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    protected String oneOrMore(String exp) {
        return "[" + exp + "]+"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    protected String zeroOrMore(String exp) {
        return "[" + exp + "]*"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    protected String or(String exp1, String exp2) {
        return exp1 + "|" + exp2; //$NON-NLS-1$
    }

    protected String nonNumberMaskCharacter() {
        return letters() + numbers() + specialCharacters();
    }

    @Override
    protected String composeMessage() {
        return ConstantsManager.getInstance().getConstants().poolNameValidationMsg();
    }
}
