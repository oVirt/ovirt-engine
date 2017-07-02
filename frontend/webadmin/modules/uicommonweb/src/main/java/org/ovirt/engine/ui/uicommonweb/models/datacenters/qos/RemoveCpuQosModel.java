package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;


import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class RemoveCpuQosModel extends RemoveQosModel<CpuQos> {

    public RemoveCpuQosModel(ListModel<CpuQos> sourceListModel) {
        super(sourceListModel);
    }

    @Override
    public String getTitle() {
        return ConstantsManager.getInstance().getConstants().removeCpuQoSTitle();
    }

    @Override
    protected QueryType getUsingEntitiesByQosIdQueryType() {
        return QueryType.GetCpuProfilesByCpuQosId;
    }

    @Override
    protected String getRemoveQosMessage(int size) {
        return ConstantsManager.getInstance().getMessages().removeCpuQoSMessage(size);
    }

    @Override
    protected String getRemoveQosHashName() {
        return "remove_cpu_qos"; //$NON-NLS-1$
    }

    @Override
    protected HelpTag getRemoveQosHelpTag() {
        return HelpTag.remove_cpu_qos;
    }

    @Override
    protected ActionType getRemoveActionType() {
        return ActionType.RemoveCpuQos;
    }

}
