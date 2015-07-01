package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.compat.Guid;

public interface FenceAgentDao extends GenericDao<FenceAgent, Guid> {

    public List<FenceAgent> getFenceAgentsForHost(Guid hostId);

    public FenceAgent get(Guid id);

    public void save(FenceAgent agent);

    public void update(FenceAgent agent);

    public void remove(Guid id);

    public void removeByVdsId(Guid vdsId);

}
