package org.ovirt.engine.core.common.businessentities.gluster;

import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;

/**
 * Interface for a Gluster Entity which support asynchronous task processing.
 */
public interface GlusterTaskSupport {

    /**
     * Returns the AsyncTask currently active in the Gluster entity
     * @return the AsyncTask currently active in the Gluster entity
     */
    public GlusterAsyncTask getAsyncTask();

    /**
     * Set the AsyncTask to Gluster entity
     * @param task Currently active in the Gluster entity
     */
    public void setAsyncTask(GlusterAsyncTask task);
}
