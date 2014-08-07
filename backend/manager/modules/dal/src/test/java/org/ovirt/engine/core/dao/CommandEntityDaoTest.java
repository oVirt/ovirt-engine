package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;

public class CommandEntityDaoTest extends BaseGenericDaoTestCase<Guid, CommandEntity, CommandEntityDao> {

    @Override
    protected CommandEntity generateNewEntity() {

        CommandEntity commandEntity = new CommandEntity();
        commandEntity.setCommandType(VdcActionType.AddBond);
        commandEntity.setCreatedAt(new Date(System.currentTimeMillis()));
        commandEntity.setId(Guid.newGuid());
        commandEntity.setCommandStatus(CommandStatus.ACTIVE);
        VdcActionParametersBase params = new VdcActionParametersBase();
        commandEntity.setCommandParameters(params);
        return commandEntity;
    }

    @Override
    protected void updateExistingEntity() {
        // TODO Auto-generated method stub

    }

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.EXISTING_COMMAND_ENTITY_ID;
    }

    @Override
    protected CommandEntityDao prepareDao() {
        return dbFacade.getCommandEntityDao();
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEneitiesTotalCount() {
        return 3;
    }

    @Test
    public void testRemove() {
        CommandEntity cmd = dbFacade.getCommandEntityDao().get(getExistingEntityId());
        assertNotNull(cmd);
        dbFacade.getCommandEntityDao().remove(getExistingEntityId());
        CommandEntity cmdAfterRemoval = dbFacade.getCommandEntityDao().get(getExistingEntityId());
        assertNull(cmdAfterRemoval);
    }

    @Test
    public void testGetAll() {
        List<CommandEntity> cmds = dbFacade.getCommandEntityDao().getAll();
        assertNotNull(cmds);
        assertTrue(cmds.size() > 0);
    }

}
