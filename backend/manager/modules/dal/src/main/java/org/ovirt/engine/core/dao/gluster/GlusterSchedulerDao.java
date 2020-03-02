package org.ovirt.engine.core.dao.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterJobDetails;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.Dao;

public interface GlusterSchedulerDao extends Dao {

    public void save(GlusterJobDetails job);

    public GlusterJobDetails getGlusterJobById(Guid jobId);

    public List<GlusterJobDetails> getAllJobs();

    public void remove(Guid jobId);
}
