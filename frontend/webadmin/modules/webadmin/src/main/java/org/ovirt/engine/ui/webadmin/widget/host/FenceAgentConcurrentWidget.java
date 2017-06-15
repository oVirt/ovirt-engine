package org.ovirt.engine.ui.webadmin.widget.host;

import org.gwtbootstrap3.client.ui.Button;
import org.ovirt.engine.ui.common.css.OvirtCss;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.hosts.FenceAgentModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

public class FenceAgentConcurrentWidget extends AbstractModelBoundPopupWidget<FenceAgentModel> implements HasEnabled {

    interface Driver extends UiCommonEditorDriver<FenceAgentModel, FenceAgentConcurrentWidget> {
    }

    public interface WidgetUiBinder extends UiBinder<Widget, FenceAgentConcurrentWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    public interface Style extends CssResource {
        String fakeAnchor();
    }

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @UiField
    PushButton editFenceAgent;

    @UiField
    @Path(value = "managementIp.entity")
    Label agentLabel;

    @UiField
    Button removeConcurrentGroup;

    @UiField
    Style style;

    FenceAgentModel model;

    public FenceAgentConcurrentWidget() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
        removeConcurrentGroup.setText(constants.detachFenceAgentFromGroup());
    }

    @Override
    public void edit(FenceAgentModel fenceAgentModel) {
        if (this.model != null && fenceAgentModel != null && !fenceAgentModel.equals(this.model)) {
            // Clean up the model.
            driver.cleanup();
        }
        if (fenceAgentModel != null) {
            driver.edit(fenceAgentModel);
            this.model = fenceAgentModel;
            determineLabelValue(fenceAgentModel);
            fenceAgentModel.getManagementIp().getEntityChangedEvent().addListener((ev, sender, args) -> determineLabelValue(model));
        }
    }

    private void determineLabelValue(FenceAgentModel model) {
        agentLabel.setText(model.getDisplayString());
    }

    @Override
    public FenceAgentModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        // Don't cleanup the model here as it will stop the edit dialog from opening twice, the model gets cleaned
        // up when the host dialog closes.
    }

    @UiHandler("editFenceAgent")
    void handleEditClick(ClickEvent event) {
        edit();
    }

    @UiHandler("agentLabel")
    void handleFenceNameClick(ClickEvent event) {
        edit();
    }

    private void edit() {
        if (isEnabled()) {
            model.edit();
        }
    }

    public void addRemoveConcurrentGroupClickHandler(ClickHandler handler) {
        if (handler != null) {
            removeConcurrentGroup.addClickHandler(handler);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        editFenceAgent.setEnabled(enabled);
        removeConcurrentGroup.setEnabled(enabled);
        if (enabled) {
            agentLabel.removeStyleName(OvirtCss.LABEL_DISABLED);
            agentLabel.addStyleName(style.fakeAnchor());
        } else {
            agentLabel.addStyleName(OvirtCss.LABEL_DISABLED);
            agentLabel.removeStyleName(style.fakeAnchor());
        }
    }

    @Override
    public boolean isEnabled() {
        return editFenceAgent.isEnabled();
    }
}
