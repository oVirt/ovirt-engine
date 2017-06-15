package org.ovirt.engine.ui.frontend;

import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.AbstractPropertiesTestBase;

public class ErrorMessagesTest extends AbstractPropertiesTestBase<EngineMessage> {

    public ErrorMessagesTest() {
        super(EngineMessage.class, "src/main/resources/org/ovirt/engine/ui/frontend/AppErrors.properties"); //$NON-NLS-1$
    }
}
