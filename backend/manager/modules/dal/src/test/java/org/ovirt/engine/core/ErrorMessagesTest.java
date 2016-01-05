package org.ovirt.engine.core;

import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.AbstractPropertiesTestBase;

public class ErrorMessagesTest extends AbstractPropertiesTestBase<EngineMessage> {

    public ErrorMessagesTest() {
        super(EngineMessage.class, "src/main/resources/bundles/AppErrors.properties");
    }
}
