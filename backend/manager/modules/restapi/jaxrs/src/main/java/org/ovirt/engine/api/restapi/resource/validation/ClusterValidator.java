package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.MigrateOnError;
import org.ovirt.engine.api.model.RngSource;

@ValidatedClass(clazz = Cluster.class)
public class ClusterValidator implements Validator<Cluster> {

    private CPUValidator cpuValidator = new CPUValidator();
    private MigrationOptionsValidator migrationOptionsValidator = new MigrationOptionsValidator();

    @Override
    public void validateEnums(Cluster cluster) {
        if (cluster != null) {
            if (cluster.isSetCpu()) {
                cpuValidator.validateEnums(cluster.getCpu());
            }
            if (cluster.isSetErrorHandling() && cluster.getErrorHandling().isSetOnError()) {
                validateEnum(MigrateOnError.class, cluster.getErrorHandling().getOnError(), true);
            }
            if (cluster.isSetRequiredRngSources()) {
                for (String rngSourceStr : cluster.getRequiredRngSources().getRequiredRngSources()) {
                    validateEnum(RngSource.class, rngSourceStr, true);
                }
            }
            if (cluster.isSetMigration()) {
                migrationOptionsValidator.validateEnums(cluster.getMigration());
            }
        }
    }
}
