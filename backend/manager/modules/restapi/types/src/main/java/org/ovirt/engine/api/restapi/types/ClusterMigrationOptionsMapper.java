package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.MigrationBandwidth;
import org.ovirt.engine.api.model.MigrationBandwidthAssignmentMethod;
import org.ovirt.engine.api.model.MigrationOptions;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.MigrationBandwidthLimitType;

public class ClusterMigrationOptionsMapper {

    @Mapping(from = Cluster.class, to = MigrationOptions.class)
    public static MigrationOptions map(Cluster entity, MigrationOptions template) {
        template = MigrationOptionsMapper.map(entity, template);

        MigrationBandwidth bandwidth = template.getBandwidth();
        if (bandwidth == null) {
            bandwidth = new MigrationBandwidth();
            template.setBandwidth(bandwidth);
        }

        switch (entity.getMigrationBandwidthLimitType()) {
            case AUTO:
                bandwidth.setAssignmentMethod(MigrationBandwidthAssignmentMethod.AUTO);
                break;
            case VDSM_CONFIG:
                bandwidth.setAssignmentMethod(MigrationBandwidthAssignmentMethod.HYPERVISOR_DEFAULT);
                break;
            case CUSTOM:
                bandwidth.setAssignmentMethod(MigrationBandwidthAssignmentMethod.CUSTOM);
                bandwidth.setCustomValue(entity.getCustomMigrationNetworkBandwidth());
                break;
        }

        return template;
    }

    public static void copyMigrationOptions(MigrationOptions model, Cluster entity) {
        MigrationOptionsMapper.copyMigrationOptions(model, entity);

        if (model.isSetBandwidth()) {
            MigrationBandwidth bandwidth = model.getBandwidth();
            if (bandwidth.getAssignmentMethod()!=null) {
                switch (bandwidth.getAssignmentMethod()) {
                    case AUTO:
                        entity.setMigrationBandwidthLimitType(MigrationBandwidthLimitType.AUTO);
                        break;
                    case HYPERVISOR_DEFAULT:
                        entity.setMigrationBandwidthLimitType(MigrationBandwidthLimitType.VDSM_CONFIG);
                        break;
                    case CUSTOM:
                        entity.setMigrationBandwidthLimitType(MigrationBandwidthLimitType.CUSTOM);
                        entity.setCustomMigrationNetworkBandwidth(model.getBandwidth().getCustomValue());
                }
            }
        }
    }

}
