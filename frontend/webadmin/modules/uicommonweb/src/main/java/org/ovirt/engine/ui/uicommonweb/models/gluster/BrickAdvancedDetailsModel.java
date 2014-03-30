package org.ovirt.engine.ui.uicommonweb.models.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterClientInfo;
import org.ovirt.engine.core.common.businessentities.gluster.Mempool;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class BrickAdvancedDetailsModel extends Model {

    private EntityModel<String> brick;
    private BrickPropertiesModel brickProperties;
    private ListModel<EntityModel<GlusterClientInfo>> clients;
    private MemoryStatisticsModel memoryStatistics;
    private ListModel<EntityModel<Mempool>> memoryPools;

    public BrickAdvancedDetailsModel() {
        setBrick(new EntityModel<String>());
        setBrickProperties(new BrickPropertiesModel());
        setClients(new ListModel<EntityModel<GlusterClientInfo>>());
        setMemoryStatistics(new MemoryStatisticsModel());
        setMemoryPools(new ListModel<EntityModel<Mempool>>());
    }

    public BrickPropertiesModel getBrickProperties() {
        return brickProperties;
    }

    public void setBrickProperties(BrickPropertiesModel brickProperties) {
        this.brickProperties = brickProperties;
    }

    public EntityModel<String> getBrick() {
        return brick;
    }

    public void setBrick(EntityModel<String> brick) {
        this.brick = brick;
    }

    public ListModel<EntityModel<GlusterClientInfo>> getClients() {
        return clients;
    }

    public void setClients(ListModel<EntityModel<GlusterClientInfo>> clients) {
        this.clients = clients;
    }

    public MemoryStatisticsModel getMemoryStatistics() {
        return memoryStatistics;
    }

    public void setMemoryStatistics(MemoryStatisticsModel memoryStatistics) {
        this.memoryStatistics = memoryStatistics;
    }

    public ListModel<EntityModel<Mempool>> getMemoryPools() {
        return memoryPools;
    }

    public void setMemoryPools(ListModel<EntityModel<Mempool>> memoryPools) {
        this.memoryPools = memoryPools;
    }
}
