package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsGroupOperationParameters;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterModel;
import org.ovirt.engine.ui.uicompat.Enlistment;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IEnlistmentNotification;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PreparingEnlistment;

@SuppressWarnings("unused")
public class AddClusterRM implements IEnlistmentNotification {

    @Override
    public void prepare(PreparingEnlistment enlistment) {

        context.enlistment = enlistment;

        // Fetch all necessary data to make code flat.
        prepare1();
    }

    public void prepare1() {

        EnlistmentContext enlistmentContext = (EnlistmentContext) context.enlistment.getContext();
        HostListModel model = enlistmentContext.getModel();
        ConfigureLocalStorageModel configureModel = (ConfigureLocalStorageModel) model.getWindow();

        ClusterModel clusterModel = configureModel.getCluster();
        String clusterName = (String) clusterModel.getName().getEntity();

        if (!StringHelper.isNullOrEmpty(clusterName)) {

            AsyncDataProvider.GetClusterListByName(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object model, Object returnValue) {

                        context.clusterFoundByName = Linq.FirstOrDefault((Iterable<VDSGroup>) returnValue);
                        prepare2();
                    }
                }),
                clusterName);
        } else {
            prepare2();
        }
    }

    public void prepare2() {

        PreparingEnlistment enlistment = (PreparingEnlistment) context.enlistment;
        EnlistmentContext enlistmentContext = (EnlistmentContext) enlistment.getContext();
        HostListModel model = enlistmentContext.getModel();
        ConfigureLocalStorageModel configureModel = (ConfigureLocalStorageModel) model.getWindow();

        VDSGroup candidate = configureModel.getCandidateCluster();
        ClusterModel clusterModel = configureModel.getCluster();
        String clusterName = (String) clusterModel.getName().getEntity();

        if (candidate == null || candidate.getname() != clusterName) {

            // Try to find existing cluster with the specified name.
            VDSGroup cluster = context.clusterFoundByName;

            if (cluster != null) {

                enlistmentContext.setClusterId(cluster.getId());

                context.enlistment = null;
                enlistment.Prepared();
            } else {

                Version version = (Version) clusterModel.getVersion().getSelectedItem();

                cluster = new VDSGroup();
                cluster.setname(clusterName);
                cluster.setdescription((String) clusterModel.getDescription().getEntity());
                cluster.setstorage_pool_id(enlistmentContext.getDataCenterId());
                cluster.setcpu_name(((ServerCpu) clusterModel.getCPU().getSelectedItem()).getCpuName());
                cluster.setmax_vds_memory_over_commit(clusterModel.getMemoryOverCommit());
                cluster.setTransparentHugepages(version.compareTo(new Version("3.0")) >= 0);
                cluster.setcompatibility_version(version);
                cluster.setMigrateOnError(clusterModel.getMigrateOnErrorOption());

                Frontend.RunAction(VdcActionType.AddVdsGroup, new VdsGroupOperationParameters(cluster),
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendActionAsyncResult result) {

                            VdcReturnValueBase returnValue = result.getReturnValue();

                            context.addVDSGroupReturnValue = returnValue;
                            prepare3();
                        }
                    });
            }

        } else {
            enlistmentContext.setClusterId(configureModel.getCluster().getClusterId().getValue());

            context.enlistment = null;
            enlistment.Prepared();
        }
    }

    private void prepare3() {

        PreparingEnlistment enlistment = (PreparingEnlistment) context.enlistment;
        EnlistmentContext enlistmentContext = (EnlistmentContext) enlistment.getContext();
        VdcReturnValueBase returnValue = context.addVDSGroupReturnValue;

        context.enlistment = null;

        if (returnValue != null && returnValue.getSucceeded()) {

            enlistmentContext.setClusterId((Guid) returnValue.getActionReturnValue());

            context.enlistment = null;
            enlistment.Prepared();

        } else {
            enlistment.ForceRollback();
        }
    }

    //    @Override
    //    public void Prepare(PreparingEnlistment enlistment)
    //    {
    //        ConfigureLocalStorageModel model = (ConfigureLocalStorageModel) getModel().getWindow();
    //        if (!model.getDontCreateCluster())
    //        {
    //            ClusterModel m = model.getCluster();
    //
    //            String name = (String) m.getName().getEntity();
    //
    //            // Try to find existing cluster with the specified name.
    //            VDSGroup cluster = DataProvider.GetClusterByName(name);
    //            if (cluster != null)
    //            {
    //                getData().setClusterId(cluster.getID());
    //                enlistment.Prepared();
    //            }
    //            else
    //            {
    //                Version version = (Version) m.getVersion().getSelectedItem();
    //
    //                cluster = new VDSGroup();
    //                cluster.setname(name);
    //                cluster.setdescription((String) m.getDescription().getEntity());
    //                cluster.setstorage_pool_id(getData().getDataCenterId());
    //                cluster.setcpu_name(((ServerCpu) m.getCPU().getSelectedItem()).getCpuName());
    //                cluster.setmax_vds_memory_over_commit(m.getMemoryOverCommit());
    //                cluster.setTransparentHugepages(version.compareTo(new Version("3.0")) >= 0);
    //                cluster.setcompatibility_version(version);
    //                cluster.setMigrateOnError(m.getMigrateOnErrorOption());
    //
    //                VdcReturnValueBase returnValue =
    //                        Frontend.RunAction(VdcActionType.AddVdsGroup, new VdsGroupOperationParameters(cluster));
    //
    //                if (returnValue != null && returnValue.getSucceeded())
    //                {
    //                    getData().setClusterId((Guid) returnValue.getActionReturnValue());
    //                    enlistment.Prepared();
    //                }
    //                else
    //                {
    //                    enlistment.ForceRollback();
    //                }
    //            }
    //        }
    //        else
    //        {
    //            enlistment.Prepared();
    //        }
    //    }

    @Override
    public void commit(Enlistment enlistment) {
        enlistment.Done();
    }

    @Override
    public void rollback(Enlistment enlistment) {
        enlistment.Done();
    }


    private final Context context = new Context();

    public final class Context {

        public Enlistment enlistment;
        public VDSGroup clusterFoundByName;
        public VdcReturnValueBase addVDSGroupReturnValue;
    }
}
