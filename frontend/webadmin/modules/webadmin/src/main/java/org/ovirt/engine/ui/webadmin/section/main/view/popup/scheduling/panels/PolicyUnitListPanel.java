package org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling.panels;

import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.NewClusterPolicyModel;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.DragDropEventBase;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.user.client.ui.FlowPanel;

public class PolicyUnitListPanel extends FlowPanel {
    private static final int EVENT_DATA_COUNT = 3;
    protected NewClusterPolicyModel model;
    protected String type;
    protected boolean used;

    public PolicyUnitListPanel(String type, boolean used) {
        this.used = used;
        this.type = type;
        getElement().getStyle().setOverflow(Overflow.AUTO);
        getElement().setDraggable(Element.DRAGGABLE_FALSE);
        registerToDragEvents();
    }

    private boolean doDrag(DragDropEventBase<?> event, boolean isDrop) {
        String dragDropEventData = PolicyUnitPanel.getDragDropEventData(event, isDrop);
        String[] split = dragDropEventData.split(" "); //$NON-NLS-1$
        if (split != null && split.length == EVENT_DATA_COUNT) {
            if (!type.equals(split[0])) {
                return false;
            } else if (Boolean.valueOf(split[2]).equals(used)) {
                return false;
            }
        }
        PolicyUnit policyUnit = model.getPolicyUnitsMap().get(Guid.createGuidFromString(split[1]));
        if (PolicyUnitPanel.FILTER.equals(split[0])) {
            if (used) {
                model.addFilter(policyUnit, false, 0);
            } else {
                model.removeFilter(policyUnit);
            }
        } else if (FunctionPolicyUnitPanel.FUNCTION.equals(split[0])) {
            if (used) {
                model.addFunction(policyUnit);
            } else {
                model.removeFunction(policyUnit);
            }
        }
        return true;
    }

    private void registerToDragEvents() {
        // drag over
        addBitlessDomHandler(event -> {
            // without registering to this event dnd doesn't get triggered
        }, DragOverEvent.getType());
        // drag leave
        addBitlessDomHandler(event -> {
            // without registering to this event dnd doesn't get triggered
        }, DragLeaveEvent.getType());
        addBitlessDomHandler(event -> {
            event.preventDefault();
            doDrag(event, true);
        }, DropEvent.getType());
    }

    public NewClusterPolicyModel getModel() {
        return model;
    }

    public void setModel(NewClusterPolicyModel model) {
        this.model = model;
    }
}
