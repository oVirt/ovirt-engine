/*
Copyright (c) 2015 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.api.metamodel.server;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains the set of MIME types supported by the model servlet.
 */
public class MimeTypes {
    // The log:
    private static final Logger log = LoggerFactory.getLogger(MimeTypes.class);

    // The supported MIME types:
    public static final MimeType APPLICATION_JSON = parseMimeType("application/json");
    public static final MimeType APPLICATION_OCTET_STREAM = parseMimeType("application/octet-stream");
    public static final MimeType APPLICATION_XML = parseMimeType("application/xml");

    /**
     * Converts the given text to a MIME type.
     *
     * @param text the text to convert
     * @return the converted MIME type or {@code null} if it can't be converted
     */
    public static MimeType parseMimeType(String text) {
        try {
            return new MimeType(text);
        }
        catch (MimeTypeParseException exception) {
            log.warn("The text \"{}\" isn't a valid mime type, will return null.", text, exception);
            return null;
        }
    }
}
