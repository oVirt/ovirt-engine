package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.action.ChangeVDSClusterParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicompat.Enlistment;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IEnlistmentNotification;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PreparingEnlistment;

@SuppressWarnings("unused")
public class ChangeHostClusterRM implements IEnlistmentNotification {

    @Override
    public void prepare(PreparingEnlistment enlistment) {

        context.enlistment = enlistment;

        prepare1();
    }

    public void prepare1() {

        PreparingEnlistment enlistment = (PreparingEnlistment) context.enlistment;
        EnlistmentContext enlistmentContext = (EnlistmentContext) context.enlistment.getContext();
        HostListModel model = enlistmentContext.getModel();
        ConfigureLocalStorageModel configureModel = (ConfigureLocalStorageModel) model.getWindow();

        VDS host = (VDS) model.getSelectedItem();

        if (!enlistmentContext.getClusterId().equals(host.getvds_group_id())) {

            enlistmentContext.setOldClusterId(host.getvds_group_id());

            Frontend.RunAction(VdcActionType.ChangeVDSCluster, new ChangeVDSClusterParameters(enlistmentContext.getClusterId(), host.getId()),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        VdcReturnValueBase returnValue = result.getReturnValue();

                        context.changeVDSClusterReturnValue = returnValue;
                        prepare2();
                    }
                });
        } else {
            context.enlistment = null;
            enlistment.Prepared();
        }
    }

    public void prepare2() {

        PreparingEnlistment enlistment = (PreparingEnlistment) context.enlistment;
        VdcReturnValueBase returnValue = context.changeVDSClusterReturnValue;

        context.enlistment = null;

        if (returnValue != null && returnValue.getSucceeded()) {

            enlistment.Prepared();
        } else {
            enlistment.ForceRollback();
        }
    }

    //    @Override
    //    public void Prepare(PreparingEnlistment enlistment) {
    //
    //        ConfigureLocalStorageModel model = (ConfigureLocalStorageModel) getModel().getWindow();
    //        if (!model.getDontChangeHostCluster()) {
    //            VDS host = (VDS) getModel().getSelectedItem();
    //            VdcReturnValueBase returnValue =
    //                Frontend.RunAction(VdcActionType.ChangeVDSCluster,
    //                    new ChangeVDSClusterParameters(getData().getClusterId(), host.getvds_id()));
    //
    //            if (returnValue != null && returnValue.getSucceeded()) {
    //                enlistment.Prepared();
    //            } else {
    //                enlistment.ForceRollback();
    //            }
    //        } else {
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
        public VdcReturnValueBase changeVDSClusterReturnValue;
    }
}
