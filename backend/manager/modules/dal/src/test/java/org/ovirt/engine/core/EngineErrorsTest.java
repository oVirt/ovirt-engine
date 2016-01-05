package org.ovirt.engine.core;

import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.utils.AbstractPropertiesTestBase;

public class EngineErrorsTest extends AbstractPropertiesTestBase<EngineError> {

    public EngineErrorsTest() {
        super(EngineError.class, "src/main/resources/bundles/VdsmErrors.properties");
    }
}
