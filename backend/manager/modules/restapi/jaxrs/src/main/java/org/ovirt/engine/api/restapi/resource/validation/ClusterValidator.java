package org.ovirt.engine.api.restapi.resource.validation;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.MigrateOnError;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

@ValidatedClass(clazz = Cluster.class)
public class ClusterValidator implements Validator<Cluster> {

    private SchedulingPolicyValidator schedulingPolicyValidator = new SchedulingPolicyValidator();

    @Override
    public void validateEnums(Cluster cluster) {
        if (cluster != null) {
            if (cluster.isSetSchedulingPolicy()) {
                schedulingPolicyValidator.validateEnums(cluster.getSchedulingPolicy());
            }
            if (cluster.isSetErrorHandling() && cluster.getErrorHandling().isSetOnError()) {
                validateEnum(MigrateOnError.class, cluster.getErrorHandling().getOnError(), true);
            }
        }
    }
}
