package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdsGroupParametersBase;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;

@CustomLogFields({ @CustomLogField("VdsGroupName") })
public abstract class VdsGroupCommandBase<T extends VdsGroupParametersBase> extends CommandBase<T> {
    private VDSGroup _vdsGroup;

    public VdsGroupCommandBase(T parameters) {
        super(parameters);
    }

    @Override
    protected VDSGroup getVdsGroup() {
        if (_vdsGroup == null) {
            _vdsGroup = DbFacade.getInstance().getVdsGroupDAO().get(
                    getParameters().getVdsGroupId());
        }
        return _vdsGroup;
    }

    public String getVdsGroupName() {
        if (getVdsGroup() != null) {
            return getVdsGroup().getname();
        } else {
            return null;
        }
    }

    @Override
    protected boolean canDoAction() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__CLUSTER);
        return true;
    }

    @Override
    public Map<Guid, VdcObjectType> getPermissionCheckSubjects() {
        return Collections.singletonMap(getParameters().getVdsGroupId(), VdcObjectType.VdsGroups);
    }
}
