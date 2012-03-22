package org.ovirt.engine.core.searchbackend.gluster;

import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.core.searchbackend.SearchObjectsBaseAutoCompleter;

/**
 * Cross reference auto completer for Gluster Volume entity.<br>
 * Adds the Cluster entity cross reference so that volumes<br>
 * can be filtered by a particular cluster.
 */
public class GlusterVolumeCrossRefAutoCompleter extends SearchObjectsBaseAutoCompleter {
    public final static GlusterVolumeCrossRefAutoCompleter INSTANCE = new GlusterVolumeCrossRefAutoCompleter();

    private GlusterVolumeCrossRefAutoCompleter() {
        mVerbs.put(SearchObjects.VDC_CLUSTER_OBJ_NAME, SearchObjects.VDC_CLUSTER_OBJ_NAME);
        buildCompletions();
    }
}
