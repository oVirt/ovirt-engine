package org.ovirt.engine.core.bll;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.common.errors.EngineMessage.ACTION_TYPE_FAILED_CANNOT_MIGRATE_MACS_DUE_TO_DUPLICATES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerCluster;
import org.ovirt.engine.core.bll.validator.ValidationResultMatchers;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.utils.ReplacementUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MoveMacsTest {

    private final CommandContext commandContext = new CommandContext(new EngineContext());

    @Mock
    private MacPoolPerCluster macPoolPerCluster;

    @Mock
    private MacPool sourceMacPool;

    @Mock
    private MacPool targetMacPool;

    @Mock
    private VmNicDao vmNicDao;

    @InjectMocks
    private MoveMacs underTest;


    private Guid sourceMacPoolId;
    private Guid targetMacPoolId;
    private List<String> macsToMigrate;
    private Cluster cluster;

    @BeforeEach
    public void setUp() {
        sourceMacPoolId = Guid.newGuid();
        targetMacPoolId = Guid.newGuid();
        macsToMigrate = new ArrayList<>(Arrays.asList("mac1", "mac2", "mac3"));

        when(macPoolPerCluster.getMacPoolById(sourceMacPoolId, commandContext)).thenReturn(sourceMacPool);

        when(macPoolPerCluster.getMacPoolById(targetMacPoolId, commandContext)).thenReturn(targetMacPool);
        when(macPoolPerCluster.getMacPoolById(targetMacPoolId)).thenReturn(targetMacPool);

        cluster = createCluster(sourceMacPoolId);
    }

    private Cluster createCluster(Guid sourceMacPoolId) {
        Cluster cluster = new Cluster();
        cluster.setId(Guid.newGuid());
        cluster.setMacPoolId(sourceMacPoolId);
        return cluster;
    }

    @Test
    public void testMigrateMacsToAnotherMacPoolWithSuccessfulDuplicityCheck() {
        underTest.migrateMacsToAnotherMacPool(sourceMacPoolId, targetMacPoolId, macsToMigrate, commandContext);

        verify(macPoolPerCluster).getMacPoolById(sourceMacPoolId, commandContext);
        verify(macPoolPerCluster).getMacPoolById(targetMacPoolId, commandContext);

        InOrder inOrder = inOrder(sourceMacPool, targetMacPool);

        inOrder.verify(sourceMacPool).freeMacs(macsToMigrate);
        inOrder.verify(targetMacPool).addMacs(macsToMigrate);
    }

    @Test
    public void testMigrateMacsToAnotherMacPoolWithUnsuccessfulDuplicityCheck() {
        //this simulates situation, where last mac cannot be added, because it already exists in target Mac Pool.
        List<String> macsFailedTobeAdded = Collections.singletonList(macsToMigrate.get(0));
        when(targetMacPool.addMacs(any())).thenReturn(macsFailedTobeAdded);

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> underTest.migrateMacsToAnotherMacPool(sourceMacPoolId, targetMacPoolId, macsToMigrate, commandContext));
        assertEquals(underTest.createMessageCannotChangeClusterDueToDuplicatesInTargetPool(macsFailedTobeAdded), e.getMessage());
    }

    @Test
    public void testMigrateMacsWhenNothingToMigrate() {
        underTest.migrateMacsToAnotherMacPool(sourceMacPoolId,
                targetMacPoolId,
                Collections.emptyList(),
                commandContext);

        verifyNoMoreInteractions(macPoolPerCluster, sourceMacPool, targetMacPool);
    }

    @Test
    public void testMigrateMacsWhenSourceAndTargetMacPoolsAreEqual() {
        underTest.migrateMacsToAnotherMacPool(sourceMacPoolId,
                sourceMacPoolId,
                macsToMigrate,
                commandContext);

        verifyNoMoreInteractions(macPoolPerCluster, sourceMacPool, targetMacPool);
    }

    @Test
    public void canMigrateMacsToAnotherMacPoolWhenSourceAndTargetMacPoolIdAreEqual() {
        Cluster cluster = new Cluster();
        cluster.setMacPoolId(sourceMacPoolId);
        assertThat(underTest.canMigrateMacsToAnotherMacPool(cluster, sourceMacPoolId), isValid());
    }

    @Test
    public void canMigrateMacsToAnotherMacPoolWhenThereAreThereIsNothingToMigrate() {
        when(vmNicDao.getAllMacsByClusterId(cluster.getId())).thenReturn(Collections.emptyList());

        assertThat(underTest.canMigrateMacsToAnotherMacPool(cluster, targetMacPoolId), isValid());
    }

    @Test
    public void canMigrateMacsToAnotherMacPoolWhenThereAreNoDuplicates() {
        when(vmNicDao.getAllMacsByClusterId(cluster.getId())).thenReturn(macsToMigrate);

        assertThat(underTest.canMigrateMacsToAnotherMacPool(cluster, targetMacPoolId), isValid());
    }

    @Test
    public void canMigrateMacsToAnotherMacPoolWhenTheresDuplicityAmongMacToBeMigrated() {
        //first mac will be duplicated
        macsToMigrate.add(macsToMigrate.get(0));

        when(vmNicDao.getAllMacsByClusterId(cluster.getId())).thenReturn(macsToMigrate);

        EngineMessage engineMessage = ACTION_TYPE_FAILED_CANNOT_MIGRATE_MACS_DUE_TO_DUPLICATES;
        Collection<String> replacements = ReplacementUtils.getListVariableAssignmentString(engineMessage,
                Collections.singleton(macsToMigrate.get(0)));

        assertThat(underTest.canMigrateMacsToAnotherMacPool(cluster, targetMacPoolId),
                ValidationResultMatchers.failsWith(engineMessage, replacements));
    }

    @Test
    public void canMigrateMacsToAnotherMacPoolWhenMacToBeMigratedAlreadyExistInTargetPool() {
        //first mac will be already used.
        String macUsedInTargetMacPool = macsToMigrate.get(0);
        when(targetMacPool.isMacInUse(macUsedInTargetMacPool)).thenReturn(true);

        when(vmNicDao.getAllMacsByClusterId(cluster.getId())).thenReturn(macsToMigrate);

        EngineMessage engineMessage = ACTION_TYPE_FAILED_CANNOT_MIGRATE_MACS_DUE_TO_DUPLICATES;
        Collection<String> replacements = ReplacementUtils.getListVariableAssignmentString(engineMessage,
                Collections.singleton(macUsedInTargetMacPool));

        assertThat(underTest.canMigrateMacsToAnotherMacPool(cluster, targetMacPoolId),
                ValidationResultMatchers.failsWith(engineMessage, replacements));
    }

    @Test
    public void canMigrateMacsToAnotherMacPoolWhenThereIsAnyDuplicityAndThisIsAllowedValidationSucceeds() {
        when(targetMacPool.isDuplicateMacAddressesAllowed()).thenReturn(true);

        when(vmNicDao.getAllMacsByClusterId(cluster.getId())).thenReturn(macsToMigrate);

        assertThat(underTest.canMigrateMacsToAnotherMacPool(cluster, targetMacPoolId), isValid());
    }

    @Test
    public void canMigrateMacsToAnotherMacPoolWithMultipleClusters() {
        //we are testing 4 clusters at once, their macs to be moved has to be merged and processed at once.
        //cluster 2 and 4 will be ignored, because migration targetMacPoolId->targetMacPoolId will be skipped.
        //therefore we are actually migrating only 2 cluster, among them there's duplicate mac1 to be migrated, which
        //should be blocked, since targetMacPoolId disallows duplicates.
        List<Cluster> clusters = Arrays.asList(
                createClusterAndMockMacs(sourceMacPoolId, Collections.singletonList("mac1")),
                createClusterAndMockMacs(targetMacPoolId, Collections.singletonList("mac2")),
                createClusterAndMockMacs(sourceMacPoolId, Collections.singletonList("mac1")),
                createClusterAndMockMacs(targetMacPoolId, Collections.singletonList("mac2"))
        );

        EngineMessage engineMessage = ACTION_TYPE_FAILED_CANNOT_MIGRATE_MACS_DUE_TO_DUPLICATES;
        Collection<String> replacements = ReplacementUtils.getListVariableAssignmentString(engineMessage,
                Collections.singleton("mac1"));

        assertThat(underTest.canMigrateMacsToAnotherMacPool(clusters, targetMacPoolId),
                ValidationResultMatchers.failsWith(engineMessage, replacements));

    }

    private Cluster createClusterAndMockMacs(Guid sourceMacPoolId, List<String> macsToMigrate) {
        Cluster cluster = createCluster(sourceMacPoolId);
        when(vmNicDao.getAllMacsByClusterId(cluster.getId())).thenReturn(macsToMigrate);


        return cluster;
    }

}
