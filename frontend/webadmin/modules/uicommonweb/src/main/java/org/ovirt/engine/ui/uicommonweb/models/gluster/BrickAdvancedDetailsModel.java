package org.ovirt.engine.ui.uicommonweb.models.gluster;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class BrickAdvancedDetailsModel extends Model {

    private EntityModel brick;
    private BrickPropertiesModel brickProperties;
    private ListModel clients;
    private MemoryStatisticsModel memoryStatistics;
    private ListModel memoryPools;

    public BrickAdvancedDetailsModel() {
        setBrick(new EntityModel());
        setBrickProperties(new BrickPropertiesModel());
        setClients(new ListModel());
        setMemoryStatistics(new MemoryStatisticsModel());
        setMemoryPools(new ListModel());
    }

    public BrickPropertiesModel getBrickProperties() {
        return brickProperties;
    }

    public void setBrickProperties(BrickPropertiesModel brickProperties) {
        this.brickProperties = brickProperties;
    }

    public EntityModel getBrick() {
        return brick;
    }

    public void setBrick(EntityModel brick) {
        this.brick = brick;
    }

    public ListModel getClients() {
        return clients;
    }

    public void setClients(ListModel clients) {
        this.clients = clients;
    }

    public MemoryStatisticsModel getMemoryStatistics() {
        return memoryStatistics;
    }

    public void setMemoryStatistics(MemoryStatisticsModel memoryStatistics) {
        this.memoryStatistics = memoryStatistics;
    }

    public ListModel getMemoryPools() {
        return memoryPools;
    }

    public void setMemoryPools(ListModel memoryPools) {
        this.memoryPools = memoryPools;
    }
}
