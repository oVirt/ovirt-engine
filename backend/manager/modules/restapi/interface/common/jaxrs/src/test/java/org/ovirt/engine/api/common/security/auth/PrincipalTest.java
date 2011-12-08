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

package org.ovirt.engine.api.common.security.auth;

import org.junit.Assert;
import org.junit.Test;


public class PrincipalTest extends Assert {

    private void assertNotEquals(Principal a, Principal b) {
        assertFalse(a.equals(b));
    }

    @Test
    public void testEquals() {
        Principal a = new Principal("zog", "zig", "zogabongs");
        Principal b = new Principal("zog", "zig", "zogabongs");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testEqualsNone() {
        Principal a = new Principal(null, null, null);
        Principal b = Principal.NONE;
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testNotEqualsNull() {
        Principal a = new Principal("zog", "zig", "zogabongs");
        assertNotEquals(a, null);
    }

    @Test
    public void testNotEqualsNullDomain() {
        Principal a = new Principal("zog", "zig", "zogabongs");
        Principal b = new Principal(null, "zig", "zogabongs");
        assertNotEquals(a, b);
    }

    @Test
    public void testNotEqualsNullUser() {
        Principal a = new Principal("zog", "zig", "zogabongs");
        Principal b = new Principal("zog", null, "zogabongs");
        assertNotEquals(a, b);
    }

    @Test
    public void testNotEqualsNullSecret() {
        Principal a = new Principal("zog", "zig", "zogabongs");
        Principal b = new Principal("zog", "zig", null);
        assertNotEquals(a, b);
    }

    @Test
    public void testNotEqualsRandomDomain() {
        Principal a = new Principal("zog", "zig", "zogabongs");
        Principal b = new Principal("dustintheturkey", "zig", "zogabongs");
        assertNotEquals(a, b);
    }

    @Test
    public void testNotEqualsRandomUser() {
        Principal a = new Principal("zog", "zig", "zogabongs");
        Principal b = new Principal("zog", "dustintheturkey", "zogabongs");
        assertNotEquals(a, b);
    }

    @Test
    public void testNotEqualsRandomSecret() {
        Principal a = new Principal("zog", "zig", "zogabongs");
        Principal b = new Principal("zog", "zig", "dustintheturkey");
        assertNotEquals(a, b);
    }
}
