package org.ovirt.engine.core.searchbackend.gluster;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class GlusterVolumeCrossRefAutoCompleterTest {
    private final GlusterVolumeCrossRefAutoCompleter comp = GlusterVolumeCrossRefAutoCompleter.INSTANCE;

    @Test
    public void testValues() {
        List<String> comps = Arrays.asList(comp.getCompletion("C"));
        assertTrue("cluster", comps.contains("Cluster"));

        assertTrue(comp.getCompletion("Z").length == 0);
    }
}
