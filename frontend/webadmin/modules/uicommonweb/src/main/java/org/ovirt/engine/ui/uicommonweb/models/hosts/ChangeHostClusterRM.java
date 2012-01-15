package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.action.ChangeVDSClusterParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicompat.Enlistment;
import org.ovirt.engine.ui.uicompat.EnlistmentOptions;
import org.ovirt.engine.ui.uicompat.PreparingEnlistment;
import org.ovirt.engine.ui.uicompat.Transaction;

@SuppressWarnings("unused")
public class ChangeHostClusterRM extends BaseRM
{
    public ChangeHostClusterRM(HostListModel model, DataBag data)
    {
        super(model, data);
        Transaction.Current.EnlistVolatile(this, EnlistmentOptions.None);
        ConfigureLocalStorageModel configureLocalStorageModel = (ConfigureLocalStorageModel) getModel().getWindow();
        // Remember an old cluster, to enable rollback.
        VDS host = (VDS) getModel().getSelectedItem();
        getData().setOldClusterId(host.getvds_group_id());
        new AddStorageDomainRM(getModel(), getData());
    }

    @Override
    public void Prepare(PreparingEnlistment preparingEnlistment)
    {
        ConfigureLocalStorageModel model = (ConfigureLocalStorageModel) getModel().getWindow();
        if (!model.getDontChangeHostCluster())
        {
            VDS host = (VDS) getModel().getSelectedItem();
            VdcReturnValueBase returnValue =
                    Frontend.RunAction(VdcActionType.ChangeVDSCluster,
                            new ChangeVDSClusterParameters(getData().getClusterId(), host.getvds_id()));

            if (returnValue != null && returnValue.getSucceeded())
            {
                preparingEnlistment.Prepared();
            }
            else
            {
                preparingEnlistment.ForceRollback();
            }
        }
        else
        {
            preparingEnlistment.Prepared();
        }
    }

    @Override
    public void Commit(Enlistment enlistment)
    {
        enlistment.Done();
    }

    @Override
    public void Rollback(Enlistment enlistment)
    {
    }

    @Override
    public void InDoubt(Enlistment enlistment)
    {
    }
}
