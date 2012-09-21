package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@InternalCommandAttribute
public class RemoveVdsSpmIdCommand<T extends VdsActionParameters> extends AddVdsSpmIdCommand<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    public RemoveVdsSpmIdCommand(Guid commandId) {
        super(commandId);
    }

    public RemoveVdsSpmIdCommand(T parametars) {
        super(parametars);
    }

    @Override
    protected void executeCommand() {
        if (getParameters().isCompensationEnabled()) {
            getCompensationContext().snapshotEntity(DbFacade.getInstance().getVdsSpmIdMapDao().get(getVdsId()));
        }

        DbFacade.getInstance().getVdsSpmIdMapDao().remove(getVdsId());
        if (getParameters().isCompensationEnabled()) {
            getCompensationContext().stateChanged();
        }
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        // check that there is spm id for this vds
        return DbFacade.getInstance().getVdsSpmIdMapDao().get(getVdsId()) != null;
    }
}
