package org.ovirt.engine.api.restapi.types;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.GlusterBrick;
import org.ovirt.engine.api.model.GlusterBrickMemoryInfo;
import org.ovirt.engine.api.model.GlusterClient;
import org.ovirt.engine.api.model.GlusterClients;
import org.ovirt.engine.api.model.GlusterMemoryPool;
import org.ovirt.engine.api.model.GlusterMemoryPools;
import org.ovirt.engine.core.common.businessentities.gluster.BrickDetails;
import org.ovirt.engine.core.common.businessentities.gluster.BrickProperties;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterClientInfo;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeAdvancedDetails;
import org.ovirt.engine.core.common.businessentities.gluster.MemoryStatus;
import org.ovirt.engine.core.common.businessentities.gluster.Mempool;

public class GlusterBrickDetailMapper {

    @Mapping (from=GlusterBrick.class, to=GlusterVolumeAdvancedDetails.class)
    public static GlusterVolumeAdvancedDetails map(GlusterBrick model, GlusterVolumeAdvancedDetails toEntity) {
        //AdvancedDetails is a read only from server and no support for setting these.
        //Hence mapping from REST model to Business entity not required.
        GlusterVolumeAdvancedDetails entity = (toEntity == null) ? new GlusterVolumeAdvancedDetails() : toEntity;
        return entity;
    }

    @Mapping (from=GlusterVolumeAdvancedDetails.class, to=GlusterBrick.class)
    public static GlusterBrick map(GlusterVolumeAdvancedDetails fromEntity, GlusterBrick toModel) {
        GlusterBrick model = (toModel == null) ? new GlusterBrick() : toModel;

        if (fromEntity.getBrickDetails() == null) {
            return model;
        }
        //Since the getDetails call is for a single brick the list size will always be 1 - so get the first element
        BrickDetails detail = (fromEntity.getBrickDetails().size() > 0) ? fromEntity.getBrickDetails().get(0) : null;

        if (detail == null) {
            return model;
        }

        model = mapBrickProperties(detail, model);

        if (detail.getClients()!= null) {
            model.setGlusterClients(new GlusterClients());
            for (GlusterClientInfo clientEntity : detail.getClients()) {
                model.getGlusterClients().getGlusterClients().add(map(clientEntity));
            }
        }

        if (detail.getMemoryStatus() != null && detail.getMemoryStatus().getMemPools() != null) {
            model.setMemoryPools(new GlusterMemoryPools());
            for (Mempool pool: detail.getMemoryStatus().getMemPools()) {
                model.getMemoryPools().getGlusterMemoryPools().add(map(pool));
            }
        }

        return model;
    }

    private static GlusterBrick mapBrickProperties(BrickDetails detail, GlusterBrick model) {
        if (detail.getBrickProperties() != null) {
            BrickProperties props = detail.getBrickProperties();
            if (StringUtils.isNotEmpty(props.getDevice())) {
                model.setDevice(props.getDevice());
            }
            if (StringUtils.isNotEmpty(props.getFsName())) {
                model.setFsName(props.getFsName());
            }
            if (StringUtils.isNotEmpty(props.getMntOptions())) {
                model.setMntOptions(props.getMntOptions());
            }
            model.setPid(props.getPid());
            model.setPort(props.getPort());
        }
        return model;
    }


    @Mapping (from=GlusterClientInfo.class, to=GlusterClient.class)
    public static GlusterClient map(GlusterClientInfo clientEntity) {
        GlusterClient clientModel = new GlusterClient();
        clientModel.setBytesRead(clientEntity.getBytesRead());
        clientModel.setBytesWritten(clientEntity.getBytesWritten());
        clientModel.setClientPort(clientEntity.getClientPort());
        if (StringUtils.isNotEmpty(clientEntity.getHostname())) {
            clientModel.setHostName(clientEntity.getHostname());
        }
        return clientModel;
    }

    @Mapping (from=MemoryStatus.class, to=GlusterBrickMemoryInfo.class)
    public static GlusterBrickMemoryInfo map(MemoryStatus memoryStatusEntity) {

        GlusterBrickMemoryInfo memInfo = new GlusterBrickMemoryInfo();
        if (memoryStatusEntity == null) {
            return null;
        }

        memInfo.setMemoryPools(new GlusterMemoryPools());
        for (Mempool pool:memoryStatusEntity.getMemPools()) {
            memInfo.getMemoryPools().getGlusterMemoryPools().add(map(pool));
        }
        return memInfo;
    }

    @Mapping (from=Mempool.class, to=GlusterMemoryPool.class)
    public static GlusterMemoryPool map(Mempool poolEntity) {
        GlusterMemoryPool poolModel = new GlusterMemoryPool();

        if (poolEntity == null) {
            return null;
        }

        if (StringUtils.isNotEmpty(poolEntity.getName())) {
            poolModel.setName(poolEntity.getName());
        }
        poolModel.setAllocCount(poolEntity.getAllocCount());
        poolModel.setColdCount(poolEntity.getColdCount());
        poolModel.setHotCount(poolEntity.getHotCount());
        poolModel.setMaxAlloc(poolEntity.getMaxAlloc());
        poolModel.setMaxStdalloc(poolEntity.getMaxStdAlloc());
        poolModel.setPaddedSize(poolEntity.getPadddedSize());
        poolModel.setPoolMisses(poolEntity.getPoolMisses());
        return poolModel;

    }

}
