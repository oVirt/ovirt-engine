package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsGroupOperationParameters;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.utils.ObjectUtils;
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
public class AddClusterRM extends IEnlistmentNotification {

    public AddClusterRM(String correlationId) {
        super(correlationId);
    }

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
        String clusterName = clusterModel.getName().getEntity();

        if (!StringHelper.isNullOrEmpty(clusterName)) {

            AsyncDataProvider.getInstance().getClusterListByName(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object model, Object returnValue) {

                            context.clusterFoundByName = Linq.firstOrDefault((Iterable<VDSGroup>) returnValue);
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
        String clusterName = clusterModel.getName().getEntity();

        if (candidate == null || !ObjectUtils.objectsEqual(candidate.getName(), clusterName)) {

            // Try to find existing cluster with the specified name.
            VDSGroup cluster = context.clusterFoundByName;

            if (cluster != null) {

                enlistmentContext.setClusterId(cluster.getId());

                context.enlistment = null;
                enlistment.prepared();
            } else {

                Version version = clusterModel.getVersion().getSelectedItem();

                cluster = new VDSGroup();
                cluster.setName(clusterName);
                cluster.setdescription(clusterModel.getDescription().getEntity());
                cluster.setStoragePoolId(enlistmentContext.getDataCenterId());
                cluster.setcpu_name(clusterModel.getCPU().getSelectedItem().getCpuName());
                cluster.setmax_vds_memory_over_commit(clusterModel.getMemoryOverCommit());
                cluster.setCountThreadsAsCores(Boolean.TRUE.equals(clusterModel.getVersionSupportsCpuThreads().getEntity())
                        && Boolean.TRUE.equals(clusterModel.getCountThreadsAsCores().getEntity()));
                cluster.setTransparentHugepages(version.compareTo(new Version("3.0")) >= 0); //$NON-NLS-1$
                cluster.setcompatibility_version(version);
                cluster.setMigrateOnError(clusterModel.getMigrateOnErrorOption());
                VdsGroupOperationParameters parameters = new VdsGroupOperationParameters(cluster);
                parameters.setCorrelationId(getCorrelationId());
                Frontend.getInstance().runAction(VdcActionType.AddVdsGroup, parameters,
                        new IFrontendActionAsyncCallback() {
                            @Override
                            public void executed(FrontendActionAsyncResult result) {

                                VdcReturnValueBase returnValue = result.getReturnValue();

                                context.addVDSGroupReturnValue = returnValue;
                                prepare3();
                            }
                        });
            }

        } else {
            enlistmentContext.setClusterId(configureModel.getCluster().getClusterId());

            context.enlistment = null;
            enlistment.prepared();
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
            enlistment.prepared();

        } else {
            enlistment.forceRollback();
        }
    }

    @Override
    public void commit(Enlistment enlistment) {
        enlistment.done();
    }

    @Override
    public void rollback(Enlistment enlistment) {
        enlistment.done();
    }

    private final Context context = new Context();

    public static final class Context {

        public Enlistment enlistment;
        public VDSGroup clusterFoundByName;
        public VdcReturnValueBase addVDSGroupReturnValue;
    }
}
