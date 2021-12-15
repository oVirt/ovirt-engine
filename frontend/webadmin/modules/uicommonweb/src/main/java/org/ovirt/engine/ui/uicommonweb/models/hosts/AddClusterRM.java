package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.Objects;

import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ClusterOperationParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterModel;
import org.ovirt.engine.ui.uicompat.Enlistment;
import org.ovirt.engine.ui.uicompat.IEnlistmentNotification;
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
        HostListModel<?> model = enlistmentContext.getModel();
        ConfigureLocalStorageModel configureModel = (ConfigureLocalStorageModel) model.getWindow();

        ClusterModel clusterModel = configureModel.getCluster();
        String clusterName = clusterModel.getName().getEntity();

        if (!StringHelper.isNullOrEmpty(clusterName)) {

            AsyncDataProvider.getInstance().getClusterListByName(new AsyncQuery<>(
                            returnValue -> {

                                context.clusterFoundByName = Linq.firstOrNull(returnValue);
                                prepare2();
                            }),
                    clusterName);
        } else {
            prepare2();
        }
    }

    public void prepare2() {

        PreparingEnlistment enlistment = (PreparingEnlistment) context.enlistment;
        EnlistmentContext enlistmentContext = (EnlistmentContext) enlistment.getContext();
        HostListModel<?> model = enlistmentContext.getModel();
        ConfigureLocalStorageModel configureModel = (ConfigureLocalStorageModel) model.getWindow();

        Cluster candidate = configureModel.getCandidateCluster();
        ClusterModel clusterModel = configureModel.getCluster();
        String clusterName = clusterModel.getName().getEntity();

        if (candidate == null || !Objects.equals(candidate.getName(), clusterName)) {

            // Try to find existing cluster with the specified name.
            Cluster cluster = context.clusterFoundByName;

            if (cluster != null) {

                enlistmentContext.setClusterId(cluster.getId());

                context.enlistment = null;
                enlistment.prepared();
            } else {

                Version version = clusterModel.getVersion().getSelectedItem();

                cluster = new Cluster();
                cluster.setName(clusterName);
                cluster.setDescription(clusterModel.getDescription().getEntity());
                cluster.setStoragePoolId(enlistmentContext.getDataCenterId());
                cluster.setMaxVdsMemoryOverCommit(clusterModel.getMemoryOverCommit());
                cluster.setSmtDisabled(Boolean.TRUE.equals(clusterModel.getSmtDisabled().getEntity()));
                cluster.setCountThreadsAsCores(Boolean.TRUE.equals(clusterModel.getCountThreadsAsCores().getEntity()));
                cluster.setTransparentHugepages(true);
                cluster.setCompatibilityVersion(version);
                cluster.setMigrateOnError(clusterModel.getMigrateOnErrorOption());
                ClusterOperationParameters parameters = new ClusterOperationParameters(cluster);
                parameters.setCorrelationId(getCorrelationId());
                Frontend.getInstance().runAction(ActionType.AddCluster, parameters,
                        result -> {

                            ActionReturnValue returnValue = result.getReturnValue();

                            context.addClusterReturnValue = returnValue;
                            prepare3();
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
        ActionReturnValue returnValue = context.addClusterReturnValue;

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
        public Cluster clusterFoundByName;
        public ActionReturnValue addClusterReturnValue;
    }
}
