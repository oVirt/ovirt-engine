package org.ovirt.engine.ui.webadmin;

import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.utils.AbstractPropertiesTestBase;

public class EngineErrorsTest extends AbstractPropertiesTestBase<EngineError> {

    public EngineErrorsTest() {
        super(EngineError.class, "src/main/resources/org/ovirt/engine/ui/frontend/VdsmErrors.properties"); //$NON-NLS-1$
    }
}
