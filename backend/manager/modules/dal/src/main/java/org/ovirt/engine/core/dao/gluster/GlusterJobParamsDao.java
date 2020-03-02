package org.ovirt.engine.core.dao.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterJobParams;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.Dao;

public interface GlusterJobParamsDao extends Dao {

    public void save(Guid jobId, List<GlusterJobParams> params);

    public List<GlusterJobParams> getJobParamsById(Guid jobId);

    public void remove(Guid jobId);

}
