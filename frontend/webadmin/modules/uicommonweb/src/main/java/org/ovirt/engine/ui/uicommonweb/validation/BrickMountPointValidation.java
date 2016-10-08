package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;


public class BrickMountPointValidation extends BaseI18NValidation {

    public BrickMountPointValidation() {
        super(ConstantsManager.getInstance().getConstants().invalidMountPointMsg());
    }

    @Override
    public ValidationResult validate(Object value) {

        ValidationResult validationResult = super.validate(value);
        // Ensure that there is no space in the brick mount point. Though its allowed in a directory name, gluster
        // doesn't handle it properly so its better to avoid it.
        if (validationResult.getSuccess() && value instanceof String && ((String) value).contains(" ")) { //$NON-NLS-1$
            validationResult.setSuccess(false);
            validationResult.getReasons().add(ConstantsManager.getInstance().getConstants().invalidMountPointMsg());
        }

        return validationResult;

    }

    @Override
    protected String composeRegex() {
        return start() + mountPath() + end();
    }

    private String mountPath() {
        return "/(.*?/)?([^\\./|^\\.\\\\]+)(?:\\.([^\\\\]*)|)"; //$NON-NLS-1$
    }
}
