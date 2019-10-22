/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class LinkCreatorTest {
    @Test
    public void testCombine() {
        assertEquals("/foo/bar", LinkCreator.combine("/foo", "bar"));
        assertEquals("/foo/bar", LinkCreator.combine("/foo/", "bar"));
        assertEquals("/foo/bar", LinkCreator.combine("/foo/", "/bar"));
        assertEquals("/foo/bar", LinkCreator.combine("/foo", "/bar"));
    }
}
