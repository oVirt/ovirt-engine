package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.common.errors.EngineMessage.ACTION_TYPE_FAILED_CANNOT_MIGRATE_MACS_DUE_TO_DUPLICATES;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerCluster;
import org.ovirt.engine.core.bll.network.macpool.ReadMacPool;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.utils.ReplacementUtils;

@Singleton
public class MoveMacs {

    @Inject
    VmNicDao vmNicDao;

    @Inject
    private MacPoolPerCluster poolPerCluster;

    /**
     * All MACs of given cluster are found, and all of them are {@link MacPool#freeMac(String) freed}
     * from source {@link MacPool macPool} and are {@link MacPool#addMacs(List)}  added}
     * to target {@link MacPool macPool}. If there is at least one mac becoming duplicate in process,
     * while duplicates are disallowed, exception will be thrown.
     * @param sourceCluster {@link Cluster} cluster instance before update.
     * @param targetMacPoolId macPool Id of updated cluster.
     * @param commandContext {@link CommandContext} instance of calling command.
     *
     */
    public void migrateMacsToAnotherMacPool(Cluster sourceCluster, Guid targetMacPoolId, CommandContext commandContext) {
        Objects.requireNonNull(sourceCluster);
        Objects.requireNonNull(targetMacPoolId);
        Objects.requireNonNull(commandContext);

        Guid sourceMacPoolId = Objects.requireNonNull(sourceCluster.getMacPoolId());
        Guid clusterId = Objects.requireNonNull(sourceCluster.getId());

        boolean macPoolChanged = !sourceMacPoolId.equals(targetMacPoolId);
        if (macPoolChanged) {
            List<String> macsToMigrate = getMacsForClusterId(clusterId);
            migrateMacsToAnotherMacPool(sourceMacPoolId, targetMacPoolId, macsToMigrate, commandContext);
        }
    }

    public ValidationResult canMigrateMacsToAnotherMacPool(List<Cluster> oldClusters, Guid targetMacPoolId) {
        return canMigrateMacsToAnotherMacPool(targetMacPoolId, getMacsFromAllClusters(oldClusters, targetMacPoolId));
    }

    List<String> getMacsFromAllClusters(List<Cluster> oldClusters, Guid targetMacPoolId) {
        //all macs combined from all clusters.
        return oldClusters.stream()
                .filter(cluster -> !Objects.equals(cluster.getMacPoolId(), targetMacPoolId))
                .map(Cluster::getId)
                .map(this::getMacsForClusterId)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public ValidationResult canMigrateMacsToAnotherMacPool(Cluster oldCluster, Guid newMacPoolId) {
        if (oldCluster.getMacPoolId().equals(newMacPoolId)) {
            return ValidationResult.VALID;
        }

        return canMigrateMacsToAnotherMacPool(
                newMacPoolId,
                getMacsForClusterId(oldCluster.getId()));
    }

    private List<String> getMacsForClusterId(Guid id) {
        return vmNicDao.getAllMacsByClusterId(id);
    }

    private ValidationResult canMigrateMacsToAnotherMacPool(Guid targetMacPoolId, List<String> macsToMigrate) {
        Objects.requireNonNull(targetMacPoolId);
        Objects.requireNonNull(macsToMigrate);

        return canMigrateMacsToAnotherMacPool(poolPerCluster.getMacPoolById(targetMacPoolId), macsToMigrate);
    }

    public ValidationResult canMigrateMacsToAnotherMacPool(ReadMacPool targetPool, List<String> macsToMigrate) {
        if (targetPool.isDuplicateMacAddressesAllowed()) {
            return ValidationResult.VALID;
        }

        Map<String, Long> occurrenceCount =
                macsToMigrate.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        List<String> problematicMacs = macsToMigrate
                .stream()
                .distinct()
                .filter(mac -> targetPool.isMacInUse(mac) || occurrenceCount.get(mac) > 1)
                .collect(Collectors.toList());

        EngineMessage engineMessage = ACTION_TYPE_FAILED_CANNOT_MIGRATE_MACS_DUE_TO_DUPLICATES;
        Collection<String> replacements = ReplacementUtils.getListVariableAssignmentString(engineMessage, problematicMacs);

        return ValidationResult.failWith(engineMessage, replacements).when(!problematicMacs.isEmpty());
    }

    public void migrateMacsToAnotherMacPool(Guid sourceMacPoolId,
            Guid targetMacPoolId,
            List<String> macsToMigrate,
            CommandContext commandContext) {

        Objects.requireNonNull(sourceMacPoolId);
        Objects.requireNonNull(targetMacPoolId);
        Objects.requireNonNull(macsToMigrate);
        Objects.requireNonNull(commandContext);

        if (macsToMigrate.isEmpty() || sourceMacPoolId.equals(targetMacPoolId)) {
            return;
        }

        MacPool sourcePool = poolPerCluster.getMacPoolById(sourceMacPoolId, commandContext);
        MacPool targetPool = poolPerCluster.getMacPoolById(targetMacPoolId, commandContext);

        sourcePool.freeMacs(macsToMigrate);
        List<String> notAddedMacs = targetPool.addMacs(macsToMigrate);
        boolean allMacsWereAdded = notAddedMacs.isEmpty();
        if (!allMacsWereAdded) {
            /* exception is thrown, because this is the easiest way, how to nullify updated VM data
             * and return macs to original pool.
             */
            throw new IllegalStateException(createMessageCannotChangeClusterDueToDuplicatesInTargetPool(notAddedMacs));
        }
    }

    String createMessageCannotChangeClusterDueToDuplicatesInTargetPool(List<String> notAddedMacs) {
        return "Cannot change cluster, some mac is already used in target cluster Mac Pool: " + notAddedMacs.toString();
    }
}
