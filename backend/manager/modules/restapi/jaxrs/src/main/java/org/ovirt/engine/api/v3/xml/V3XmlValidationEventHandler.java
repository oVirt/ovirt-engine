/*
* Copyright (c) 2014 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.v3.xml;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

/**
 * This validation event handler considers all errors as fatal, and reports them so that the JAXB context will abort the
 * operation and throw an exception. If this isn't used, type errors (integer overflows, for example) are silently
 * ignored.
 */
public class V3XmlValidationEventHandler implements ValidationEventHandler {
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
