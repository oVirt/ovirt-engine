package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ChangeVDSClusterParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicompat.Enlistment;
import org.ovirt.engine.ui.uicompat.IEnlistmentNotification;
import org.ovirt.engine.ui.uicompat.PreparingEnlistment;

@SuppressWarnings("unused")
public class ChangeHostClusterRM extends IEnlistmentNotification {

    public ChangeHostClusterRM(String correlationId) {
        super(correlationId);
    }

    @Override
    public void prepare(PreparingEnlistment enlistment) {

        context.enlistment = enlistment;

        prepare1();
    }

    public void prepare1() {

        PreparingEnlistment enlistment = (PreparingEnlistment) context.enlistment;
        EnlistmentContext enlistmentContext = (EnlistmentContext) context.enlistment.getContext();
        HostListModel<?> model = enlistmentContext.getModel();

        VDS host = model.getSelectedItem();

        if (!enlistmentContext.getClusterId().equals(host.getClusterId())) {

            enlistmentContext.setOldClusterId(host.getClusterId());
            ChangeVDSClusterParameters parameters =
                    new ChangeVDSClusterParameters(enlistmentContext.getClusterId(), host.getId());
            parameters.setCorrelationId(getCorrelationId());
            Frontend.getInstance().runAction(ActionType.ChangeVDSCluster,
                    parameters,
                    result -> {

                        ActionReturnValue returnValue = result.getReturnValue();

                        context.changeVDSClusterReturnValue = returnValue;
                        prepare2();
                    });
        } else {
            context.enlistment = null;
            enlistment.prepared();
        }
    }

    public void prepare2() {

        PreparingEnlistment enlistment = (PreparingEnlistment) context.enlistment;
        ActionReturnValue returnValue = context.changeVDSClusterReturnValue;

        context.enlistment = null;

        if (returnValue != null && returnValue.getSucceeded()) {

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
        public ActionReturnValue changeVDSClusterReturnValue;
    }
}
