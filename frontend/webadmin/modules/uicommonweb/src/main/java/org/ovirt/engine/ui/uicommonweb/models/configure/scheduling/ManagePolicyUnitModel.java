package org.ovirt.engine.ui.uicommonweb.models.configure.scheduling;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.parameters.RemoveExternalPolicyUnitParameters;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class ManagePolicyUnitModel extends Model {
    private final Event<EventArgs> refreshPolicyUnitsEvent = new Event<>("RefreshPolicyUnitsEvent", ManagePolicyUnitModel.class); //$NON-NLS-1$
    private ListModel PolicyUnits;

    public ListModel getPolicyUnits() {
        return PolicyUnits;
    }

    public void setPolicyUnits(ListModel policyUnits) {
        PolicyUnits = policyUnits;
    }

    public Event<EventArgs> getRefreshPolicyUnitsEvent() {
        return refreshPolicyUnitsEvent;
    }

    public void remove(final PolicyUnit policyUnit) {
        Frontend.getInstance().runAction(ActionType.RemoveExternalPolicyUnit,
                new RemoveExternalPolicyUnitParameters(policyUnit.getId()), new IFrontendActionAsyncCallback() {

                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        getRefreshPolicyUnitsEvent().raise(this, null);
                    }
                });
    }

    @Override
    public void cleanup() {
        cleanupEvents(getRefreshPolicyUnitsEvent());
        super.cleanup();
    }
}
