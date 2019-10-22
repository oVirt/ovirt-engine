/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
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
        } catch(DecoderException exception) {
            throw notValidHex(hex, exception);
        }
    }
}
