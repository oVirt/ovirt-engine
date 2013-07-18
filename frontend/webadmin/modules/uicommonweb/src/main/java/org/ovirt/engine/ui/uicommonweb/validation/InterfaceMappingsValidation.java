package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class InterfaceMappingsValidation extends RegexValidation {

    private static String ifMappingsPattern =
            "^(?:(?:[^:,\\s]{1,15}:[^:,\\s]{1,15})(?:,[^:,\\s]{1,15}:[^:,\\s]{1,15})*)?$"; //$NON-NLS-1$

    public InterfaceMappingsValidation() {
        setExpression(ifMappingsPattern);
        setMessage(ConstantsManager.getInstance().getConstants().interfaceMappingsInvalid());
    }

}
