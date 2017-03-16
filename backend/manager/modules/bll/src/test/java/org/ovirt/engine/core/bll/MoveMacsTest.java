package org.ovirt.engine.core.bll;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerCluster;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class MoveMacsTest {

    private final CommandContext commandContext = new CommandContext(new EngineContext());

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private MacPoolPerCluster macPoolPerCluster;

    @Mock
    private MacPool sourceMacPool;

    @Mock
    private MacPool targetMacPool;

    @InjectMocks
    private MoveMacs underTest;


    private Guid sourceMacPoolId;
    private Guid targetMacPoolId;
    private List<String> macsToMigrate;

    @Before
    public void setUp() throws Exception {
        sourceMacPoolId = Guid.newGuid();
        targetMacPoolId = Guid.newGuid();
        macsToMigrate = Arrays.asList("mac1", "mac2", "mac3");

        when(macPoolPerCluster.getMacPoolById(sourceMacPoolId, commandContext)).thenReturn(sourceMacPool);
        when(macPoolPerCluster.getMacPoolById(targetMacPoolId, commandContext)).thenReturn(targetMacPool);
    }

    @Test
    public void testMigrateMacsToAnotherMacPool() throws Exception {
        underTest.migrateMacsToAnotherMacPool(sourceMacPoolId, targetMacPoolId, macsToMigrate, false, commandContext);

        verify(macPoolPerCluster).getMacPoolById(sourceMacPoolId, commandContext);
        verify(macPoolPerCluster).getMacPoolById(targetMacPoolId, commandContext);

        InOrder inOrder = Mockito.inOrder(sourceMacPool, targetMacPool);

        inOrder.verify(sourceMacPool).freeMacs(macsToMigrate);
        inOrder.verify(targetMacPool).forceAddMacs(macsToMigrate);
    }

    @Test
    public void testMigrateMacsToAnotherMacPoolWithSuccessfulDuplicityCheck() throws Exception {
        underTest.migrateMacsToAnotherMacPool(sourceMacPoolId, targetMacPoolId, macsToMigrate, true, commandContext);

        verify(macPoolPerCluster).getMacPoolById(sourceMacPoolId, commandContext);
        verify(macPoolPerCluster).getMacPoolById(targetMacPoolId, commandContext);

        InOrder inOrder = Mockito.inOrder(sourceMacPool, targetMacPool);

        inOrder.verify(sourceMacPool).freeMacs(macsToMigrate);
        inOrder.verify(targetMacPool).addMacs(macsToMigrate);
    }

    @Test
    public void testMigrateMacsToAnotherMacPoolWithUnsuccessfulDuplicityCheck() throws Exception {
        //this simulates situation, where last mac cannot be added, because it already exists in target Mac Pool.
        List<String> macsToMigrate = Collections.singletonList(this.macsToMigrate.get(0));
        when(targetMacPool.addMacs(anyList())).thenReturn(macsToMigrate);

        String expectedMessage = underTest.createMessageCannotChangeClusterDueToDuplicatesInTargetPool(macsToMigrate);
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(expectedMessage);

        underTest.migrateMacsToAnotherMacPool(sourceMacPoolId, targetMacPoolId, this.macsToMigrate, true, commandContext);

        verify(macPoolPerCluster).getMacPoolById(sourceMacPoolId, commandContext);
        verify(macPoolPerCluster).getMacPoolById(targetMacPoolId, commandContext);

        InOrder inOrder = Mockito.inOrder(sourceMacPool, targetMacPool);

        inOrder.verify(sourceMacPool).freeMacs(this.macsToMigrate);
        inOrder.verify(targetMacPool).addMacs(this.macsToMigrate);
    }

}
