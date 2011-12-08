/*
* Copyright (c) 2010 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*           http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.common.util;

/**
 * A utility class for converting sizes; i.e: bytes to mega-bytes, giga-bytes to bytes, etc.
 *
 * @author ori
 *
 */
public class SizeConverter {

    public static Long BYTES_IN_MEGA = 1024L * 1024L;
    public static Long MEGAS_IN_GIGA = 1024L;

    public static long megasToBytes(int megabytes) {
        return megabytes * BYTES_IN_MEGA;
    }

    public static long megasToBytes(long megabytes) {
        return megabytes * BYTES_IN_MEGA;
    }

    public static long gigasToBytes(int gigabytes) {
        return gigabytes * BYTES_IN_MEGA * MEGAS_IN_GIGA;
    }

    public static long gigasToBytes(long gigabytes) {
        return gigabytes * BYTES_IN_MEGA * MEGAS_IN_GIGA;
    }

    /**
     * Converts bytes to mega-bytes. Rounds down to the nearest mega-byte.
     * @param bytes number of bytes
     * @return number of megabytes.
     */
    public static long bytesToMegas(long bytes) {
        return bytes/BYTES_IN_MEGA;
    }

    /**
     * Converts bytes to giga-bytes. Rounds down to the nearest giga-byte.
     * @param bytes number of bytes
     * @return number of gigabytes.
     */
    public static long bytesToGigas(long bytes) {
        return bytes/(BYTES_IN_MEGA * MEGAS_IN_GIGA);
    }
}
