package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsGroupOperationParameters;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterModel;
import org.ovirt.engine.ui.uicompat.Enlistment;
import org.ovirt.engine.ui.uicompat.EnlistmentOptions;
import org.ovirt.engine.ui.uicompat.PreparingEnlistment;
import org.ovirt.engine.ui.uicompat.Transaction;

@SuppressWarnings("unused")
public class AddClusterRM extends BaseRM
{
    public AddClusterRM(HostListModel model, DataBag data)
    {
        super(model, data);
        Transaction.Current.EnlistVolatile(this, EnlistmentOptions.None);
        ConfigureLocalStorageModel configureLocalStorageModel = (ConfigureLocalStorageModel) getModel().getWindow();
        if (configureLocalStorageModel.getDontCreateCluster())
        {
            getData().setClusterId(configureLocalStorageModel.getCluster().getClusterId().getValue());
        }
        new ChangeHostClusterRM(getModel(), getData());
    }

    @Override
    public void Prepare(PreparingEnlistment preparingEnlistment)
    {
        ConfigureLocalStorageModel model = (ConfigureLocalStorageModel) getModel().getWindow();
        if (!model.getDontCreateCluster())
        {
            ClusterModel m = model.getCluster();

            String name = (String) m.getName().getEntity();

            // Try to find existing cluster with the specified name.
            VDSGroup cluster = DataProvider.GetClusterByName(name);
            if (cluster != null)
            {
                getData().setClusterId(cluster.getID());
                preparingEnlistment.Prepared();
            }
            else
            {
                Version version = (Version) m.getVersion().getSelectedItem();

                cluster = new VDSGroup();
                cluster.setname(name);
                cluster.setdescription((String) m.getDescription().getEntity());
                cluster.setstorage_pool_id(getData().getDataCenterId());
                cluster.setcpu_name(((ServerCpu) m.getCPU().getSelectedItem()).getCpuName());
                cluster.setmax_vds_memory_over_commit(m.getMemoryOverCommit());
                cluster.setTransparentHugepages(version.compareTo(new Version("3.0")) >= 0);
                cluster.setcompatibility_version(version);
                cluster.setMigrateOnError(m.getMigrateOnErrorOption());

                VdcReturnValueBase returnValue =
                        Frontend.RunAction(VdcActionType.AddVdsGroup, new VdsGroupOperationParameters(cluster));

                if (returnValue != null && returnValue.getSucceeded())
                {
                    getData().setClusterId((Guid) returnValue.getActionReturnValue());
                    preparingEnlistment.Prepared();
                }
                else
                {
                    preparingEnlistment.ForceRollback();
                }
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
