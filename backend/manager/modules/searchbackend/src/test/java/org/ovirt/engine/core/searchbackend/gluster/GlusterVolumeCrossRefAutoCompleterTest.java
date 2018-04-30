package org.ovirt.engine.core.searchbackend.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class GlusterVolumeCrossRefAutoCompleterTest {
    private final GlusterVolumeCrossRefAutoCompleter comp = GlusterVolumeCrossRefAutoCompleter.INSTANCE;

    @Test
    public void testValues() {
        List<String> comps = Arrays.asList(comp.getCompletion("C"));
        assertTrue(comps.contains("Cluster"), "cluster");

        assertEquals(0, comp.getCompletion("Z").length);
    }
}
