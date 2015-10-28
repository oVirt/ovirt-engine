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

package org.ovirt.engine.api.restapi.utils;

import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class HexUtils {

    public static String string2hex(String text) {
        return Hex.encodeHexString(text.getBytes(StandardCharsets.UTF_8));
    }

    private static RuntimeException notValidHex(String hex, Exception exception) {
        return new IllegalArgumentException("The string \"" + hex + "\" isn't a valid hexadecimal value", exception);
    }

    public static String hex2string(String hex) {
        try {
            return new String(Hex.decodeHex(hex.toCharArray()), StandardCharsets.UTF_8);
        }
        catch (DecoderException exception) {
            throw notValidHex(hex, exception);
        }
    }
}
