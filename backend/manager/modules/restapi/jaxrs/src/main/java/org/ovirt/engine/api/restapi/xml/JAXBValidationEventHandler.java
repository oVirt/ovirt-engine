/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.xml;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

/**
 * This validation event handler considers all errors as fatal, and reports them so that the JAXB context will abort the
 * operation and throw an exception. If this isn't used, type errors (integer overflows, for example) are silently
 * ignored.
 */
public class JAXBValidationEventHandler implements ValidationEventHandler {
    @Override
    public boolean handleEvent(ValidationEvent event) {
        switch (event.getSeverity()) {
        case ValidationEvent.ERROR:
        case ValidationEvent.FATAL_ERROR:
            return false;
        default:
            return true;
        }
    }
}
