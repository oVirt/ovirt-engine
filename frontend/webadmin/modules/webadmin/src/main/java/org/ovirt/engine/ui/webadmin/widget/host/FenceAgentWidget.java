package org.ovirt.engine.ui.webadmin.widget.host;

import java.util.HashMap;
import java.util.Map;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.css.OvirtCss;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelLabel;
import org.ovirt.engine.ui.common.widget.renderer.StringRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.hosts.FenceAgentModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

public class FenceAgentWidget extends AbstractModelBoundPopupWidget<FenceAgentModel>
    implements HasValueChangeHandlers<FenceAgentModel>, HasEnabled {

    interface Driver extends UiCommonEditorDriver<FenceAgentModel, FenceAgentWidget> {
    }

    public interface Style extends CssResource {
        String highlightRow();
        String fakeAnchor();
    }

    public interface WidgetUiBinder extends UiBinder<Widget, FenceAgentWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @UiField
    PushButton editFenceAgent;

    @UiField
    Button up;

    @UiField
    Button down;

    @UiField
    @Path(value = "managementIp.entity")
    Label agentLabel;

    @UiField
    @Ignore
    Label concurrentGroupLabel;

    @UiField
    @Path(value = "order.entity")
    IntegerEntityModelLabel orderLabel;

    @UiField(provided = true)
    @Path(value = "concurrentSelectList.selectedItem")
    ListModelListBoxEditor<String> concurrentList;

    @UiField
    Column labelColumn;

    @UiField
    Column concurrentListColumn;

    @UiField
    FlowPanel concurrentPanel;

    @UiField
    Row topRow;

    @UiField
    Style style;

    FenceAgentModel model;

    private Map<FenceAgentModel, ClickHandler> removeConcurrentGroupClickHandlerMap = new HashMap<>();

    public FenceAgentWidget() {
        initEditors();
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
        concurrentGroupLabel.setText(constants.concurrentAgentGroupLabel());
        up.setIcon(IconType.ARROW_UP);
        down.setIcon(IconType.ARROW_DOWN);
    }

    private void initEditors() {
        concurrentList = new ListModelListBoxEditor<>(new StringRenderer<String>());
        concurrentList.setUsePatternFly(true);
        concurrentList.hideLabel();
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
            fenceAgentModel.getConcurrentSelectList().getPropertyChangedEvent().addListener(
                    (ev, sender, args) -> {
                        if ("IsAvailable".equals(args.propertyName)) { //$NON-NLS-1$
                            determineLabelValue(model);
                        }
                    });
        }
    }

    private void determineLabelValue(FenceAgentModel model) {
        if (StringHelper.isNullOrEmpty(model.getManagementIp().getEntity())) {
            showControls(false);
        } else if (model.isInConcurrentGroup()) {
            agentLabel.setVisible(false);
            concurrentGroupLabel.setVisible(true);
            topRow.addStyleName(style.highlightRow());
            showControls(true);
            showConcurrentGroup(model);
        } else {
            agentLabel.setVisible(true);
            topRow.removeStyleName(style.highlightRow());
            concurrentGroupLabel.setVisible(false);
            agentLabel.setText(model.getDisplayString());
            showControls(true);
        }
    }

    private void showConcurrentGroup(FenceAgentModel model) {
        editFenceAgent.setVisible(false);
        FenceAgentConcurrentWidget concurrentWidget = new FenceAgentConcurrentWidget();
        concurrentWidget.addRemoveConcurrentGroupClickHandler(removeConcurrentGroupClickHandlerMap.get(model));
        concurrentWidget.edit(model);
        concurrentPanel.add(concurrentWidget);
        for (FenceAgentModel concurrentModel : model.getConcurrentList()) {
            concurrentWidget = new FenceAgentConcurrentWidget();
            concurrentWidget.addRemoveConcurrentGroupClickHandler(
                    removeConcurrentGroupClickHandlerMap.get(concurrentModel));
            concurrentWidget.edit(concurrentModel);
            concurrentPanel.add(concurrentWidget);
        }
    }

    private void clearConcurrentGroup() {
        concurrentPanel.clear();
    }

    private void showControls(boolean visible) {
        clearConcurrentGroup();
        editFenceAgent.setVisible(visible);
        up.setVisible(visible);
        down.setVisible(visible);
        orderLabel.setVisible(visible);
        concurrentListColumn.setVisible(model.getConcurrentSelectList().getIsAvailable()
                && !model.isInConcurrentGroup());
        if (concurrentListColumn.isVisible()) {
            labelColumn.setSize(ColumnSize.SM_5);
        } else {
            labelColumn.setSize(ColumnSize.SM_9);
        }
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

    public void addUpClickHandler(ClickHandler handler) {
        up.addClickHandler(handler);
    }

    public void addDownClickHandler(ClickHandler handler) {
        down.addClickHandler(handler);
    }

    public void addRemoveConcurrentGroupClickHandler(FenceAgentModel model, ClickHandler handler) {
        removeConcurrentGroupClickHandlerMap.put(model, handler);
    }

    /**
     * Enabled or disable the 'up' button in the widget.
     * @param value {@code true} to enable, {@code false} to disable
     */
    public void enableUpButton(boolean value) {
        up.setEnabled(value);
    }

    /**
     * Enabled or disable the 'down' button in the widget.
     * @param value {@code true} to enable, {@code false} to disable
     */
    public void enableDownButton(boolean value) {
        down.setEnabled(value);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<FenceAgentModel> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public boolean isEnabled() {
        return editFenceAgent.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        concurrentList.setEnabled(enabled);
        for (int i = 0; i < concurrentPanel.getWidgetCount(); i++) {
            IsWidget widget = concurrentPanel.getWidget(i);
            if (widget instanceof HasEnabled) {
                ((HasEnabled)widget).setEnabled(enabled);
            }
        }
        editFenceAgent.setEnabled(enabled);
        if (enabled) {
            orderLabel.removeStyleName(OvirtCss.LABEL_DISABLED);
            concurrentGroupLabel.removeStyleName(OvirtCss.LABEL_DISABLED);
            agentLabel.removeStyleName(OvirtCss.LABEL_DISABLED);
            agentLabel.addStyleName(style.fakeAnchor());
        } else {
            orderLabel.addStyleName(OvirtCss.LABEL_DISABLED);
            concurrentGroupLabel.addStyleName(OvirtCss.LABEL_DISABLED);
            agentLabel.addStyleName(OvirtCss.LABEL_DISABLED);
            agentLabel.removeStyleName(style.fakeAnchor());
        }
    }

    public void refresh() {
        determineLabelValue(model);
    }
}
