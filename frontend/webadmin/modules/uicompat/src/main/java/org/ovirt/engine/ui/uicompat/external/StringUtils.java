package org.ovirt.engine.ui.uicompat.external;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.math.BigDecimal;
import java.util.Iterator;

public class StringUtils {

    /**
     * <p>
     * Checks if a String is empty ("") or null.
     * </p>
     *
     * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = false
     * StringUtils.isEmpty("bob")     = false
     * StringUtils.isEmpty("  bob  ") = false
     * </pre>
     *
     * <p>
     * NOTE: This method changed in Lang version 2.0. It no longer trims the String. That functionality is available in
     * isBlank().
     * </p>
     *
     * @param str
     *            the String to check, may be null
     * @return <code>true</code> if the String is empty or null
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * <p>
     * Checks if a String is not empty ("") and not null.
     * </p>
     *
     * <pre>
     * StringUtils.isNotEmpty(null)      = false
     * StringUtils.isNotEmpty("")        = false
     * StringUtils.isNotEmpty(" ")       = true
     * StringUtils.isNotEmpty("bob")     = true
     * StringUtils.isNotEmpty("  bob  ") = true
     * </pre>
     *
     * @param str
     *            the String to check, may be null
     * @return <code>true</code> if the String is not empty and not null
     */
    public static boolean isNotEmpty(String str) {
        return !StringUtils.isEmpty(str);
    }

    /**
     * Joins the Strings of the provided array into a single String.
     *
     * @param array the array of Strings to join together
     * @param delimiter the separator string to use
     */
    public static String join(Iterable<String> list, String delimiter) {
        if (list == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        Iterator<String> iter = list.iterator();
        while (iter.hasNext()) {
            builder.append(iter.next());
            if (!iter.hasNext()) {
                break;
            }
            builder.append(delimiter);
        }
        return builder.toString();
    }

    /**
     * Returns the String form of an object.
     *
     * @param obj
     *            The object to turn into a String.
     * @return null if the object is null, obj.toString() otherwise.
     */
    public static String render(Object obj) {
        return obj == null ? null : obj.toString();
    }

    /**
     * Returns the equivalent BigDecimal representation of a String, if possible.
     *
     * @param str
     *            The String to try to parse.
     * @return null if the String is null or empty, its BigDecimal value otherwise.
     * @throws NumberFormatException
     *             if the String cannot be parsed as an BigDecimal.
     */
    public static BigDecimal parseBigDecimal(String str) {
        return str == null || str.isEmpty() ? null : new BigDecimal(str);
    }

    /**
     * Returns the equivalent Integer representation of a String, if possible.
     *
     * @param str
     *            The String to try to parse.
     * @return null if the String is null or empty, its Integer value otherwise.
     * @throws NumberFormatException
     *             if the String cannot be parsed as an Integer.
     */
    public static Integer parseInteger(String str) {
        return str == null || str.isEmpty() ? null : Integer.parseInt(str);
    }

}
