/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.HttpHeaders;

import org.junit.jupiter.api.Test;

public class DetailHelperTest {

    private static final String ACCEPTABLE = "application/xml";

    @Test
    public void testIncludeSingle() {
        doTestIncludes(";detail=statistics",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {true, false});
    }

    @Test
    public void testIncludeSome() {
        doTestIncludes(";detail=devices ;detail=statistics",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {true, false});
    }

    @Test
    public void testIncludeSomeCollapsed() {
        doTestIncludes(";detail=devices+statistics",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {true, false});
    }

    @Test
    public void testIncludeMore() {
        doTestIncludes(";detail=devices; detail=statistics; detail=tags; detail=permissions",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {true, true});
    }

    @Test
    public void testIncludeMoreCollapsed() {
        doTestIncludes(";detail=devices; detail=statistics+tags+permissions",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {true, true});
    }

    @Test
    public void testIncludeAll() {
        doTestIncludes(";detail=statistics; detail=permissions",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {true, true});
    }

    @Test
    public void testIncludeAllCollapsed() {
        doTestIncludes(";detail=statistics+permissions",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {true, true});
    }

    @Test
    public void testIncludeWithSpacePrefix() {
        doTestIncludes("; detail=statistics ; detail=permissions",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {true, true});
    }

    @Test
    public void testIncludeNone() {
        doTestIncludes("",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {false, false});
    }

    @Test
    public void testMainIncludedByDefault() {
        doTestIncludes(
            "",
            new String[] { "main" },
            new boolean[] { true }
        );
    }

    private void doTestIncludes(String spec, String[] rels, boolean[] expected) {

        HttpHeaders httpheaders = mock(HttpHeaders.class);
        List<String> requestHeaders = new ArrayList<>();
        when(httpheaders.getRequestHeader("Accept")).thenReturn(requestHeaders);
        requestHeaders.add(ACCEPTABLE + spec);

        for (int i = 0; i < rels.length; i++) {
            Set<String> details = DetailHelper.getDetails(httpheaders, null);
            assertEquals(expected[i], details.contains(rels[i]));
        }
    }

}
