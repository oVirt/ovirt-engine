package org.ovirt.engine.ui.webadmin.widget.host;

import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.ui.common.css.OvirtCss;
import org.ovirt.engine.ui.common.widget.AddRemoveRowWidget;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.FenceAgentListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.FenceAgentModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.FenceAgentModelProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FenceAgentsEditor extends AddRemoveRowWidget<FenceAgentListModel, FenceAgentModel, FenceAgentWidget> {

    interface WidgetUiBinder extends UiBinder<Widget, FenceAgentsEditor> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @UiField
    @Ignore
    Label header;

    @UiField
    @Ignore
    Label newAgentLabel;

    @UiField
    Button newAgentButton;

    private HandlerRegistration addClickHandlerRegistration;

    private FenceAgentListModel listModel;

    //Need this to 'initialize' the model which attaches the appropriate handlers.
    final FenceAgentModelProvider modelProvider;

    private boolean isEnabled;

    @Inject
    public FenceAgentsEditor(FenceAgentModelProvider modelProvider) {
        showGhost = false;
        showAddButton = false;
        this.modelProvider = modelProvider;
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        newAgentButton.setIcon(IconType.PLUS);
        addHandlers();
    }

    private void addHandlers() {
        if (addClickHandlerRegistration != null) {
            addClickHandlerRegistration.removeHandler();
        }
        addClickHandlerRegistration = newAgentButton.addClickHandler(event -> {
            if (!items.isEmpty()) {
                Pair<FenceAgentModel, FenceAgentWidget> modelWidgetPair = items.get(items.size() - 1);
                getEntry(modelWidgetPair.getSecond()).removeLastButton();
            }
            Pair<FenceAgentModel, FenceAgentWidget> item = addGhostEntry();
            onAdd(item.getFirst(), item.getSecond());
            item.getFirst().edit();
        });
    }

    @Override
    protected void init(FenceAgentListModel listModel) {
        this.listModel = listModel;
        super.init(listModel);
        header.setText(constants.agentsBySequentialOrder());
        newAgentLabel.setText(constants.addNewFenceAgent());
        setEnabled(listModel.getIsChangable());
    }

    @Override
    protected FenceAgentWidget createWidget(final FenceAgentModel model) {
        modelProvider.initializeModel(model);
        FenceAgentWidget widget = new FenceAgentWidget();
        widget.addUpClickHandler(event -> {
            listModel.moveUp(model);
            updateButtonState();
        });
        widget.addDownClickHandler(event -> {
            listModel.moveDown(model);
            updateButtonState();
        });
        widget.addRemoveConcurrentGroupClickHandler(model, event -> {
            listModel.removeConcurrent(model);
            listModel.updateConcurrentList();
        });
        for (final FenceAgentModel concurrentModel: model.getConcurrentList()) {
            modelProvider.initializeModel(concurrentModel);
            widget.addRemoveConcurrentGroupClickHandler(concurrentModel, event -> {
                listModel.removeConcurrent(concurrentModel);
                listModel.updateConcurrentList();
            });
        }
        widget.edit(model);
        model.getManagementIp().getEntityChangedEvent().addListener((ev, sender, args) -> {
            listModel.updateConcurrentList();
            for (Pair<FenceAgentModel, FenceAgentWidget> modelWidgetPair: items) {
                modelWidgetPair.getSecond().refresh();
            }
        });
        model.getConcurrentSelectList().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            if (sender instanceof ListModel) {
                @SuppressWarnings("unchecked")
                ListModel<String> sourceListModel = (ListModel<String>) sender;
                if(sourceListModel.getItems() != null && !sourceListModel.getItems().isEmpty()
                        && sourceListModel.getItems() instanceof List) {
                    List<String> options = (List<String>) sourceListModel.getItems();
                    if (!options.get(0).equals(sourceListModel.getSelectedItem())) {
                        //Another option selected.
                        listModel.makeConcurrent(model, sourceListModel.getSelectedItem());
                    }
                }
            }
        });
        return widget;
    }

    private void updateButtonState() {
        if (!items.isEmpty()) {
            for (Pair<FenceAgentModel, FenceAgentWidget> modelWidgetPair: items) {
                modelWidgetPair.getSecond().enableUpButton(!modelWidgetPair.equals(items.get(0)) && isEnabled);
                modelWidgetPair.getSecond().enableDownButton(
                        !modelWidgetPair.equals(items.get(items.size() - 1)) && isEnabled);
            }
        }
    }

    @Override
    protected FenceAgentModel createGhostValue() {
        final FenceAgentModel ghostModel = new FenceAgentModel();
        ghostModel.getPmType().setItems(listModel.getPmTypes());
        ghostModel.setHost(listModel.getHostModel());
        ghostModel.setOrder(listModel.getItems().size() + 1);
        return ghostModel;
    }

    @Override
    protected boolean isGhost(FenceAgentModel model) {
        //If there is no management ip, this is a ghost model.
        return !model.hasAddress();
    }

    @Override
    protected void cleanupModelItems() {
        //Don't clean up items on refresh, when the host dialog closes this gets cleaned up.
    }

    @Override
    public void cleanup() {
        super.cleanup();
        super.cleanupModelItems();
    }

    public void setValue(FenceAgentListModel model) {
        listModel = model;
    }

    @Override
    protected void onAdd(final FenceAgentModel value, FenceAgentWidget widget) {
        listModel.getItems().add(value);
        listModel.updateConcurrentList();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        super.setEnabled(enabled);
        newAgentButton.setEnabled(enabled);
        if (enabled) {
            newAgentLabel.removeStyleName(OvirtCss.LABEL_DISABLED);
            header.removeStyleName(OvirtCss.LABEL_DISABLED);
        } else {
            newAgentLabel.addStyleName(OvirtCss.LABEL_DISABLED);
            header.addStyleName(OvirtCss.LABEL_DISABLED);
        }
        updateButtonState();
    }

    @Override
    protected boolean vetoRemoveWidget(Pair<FenceAgentModel, FenceAgentWidget> item,
            FenceAgentModel value, FenceAgentWidget widget) {
        value.confirmRemove();
        //Always veto, the confirm handler will remove if needed.
        return true;
    }
}
