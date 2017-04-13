package org.ovirt.engine.ui.webadmin.widget.host;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.ui.common.css.OvirtCss;
import org.ovirt.engine.ui.common.widget.AddRemoveRowWidget;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.FenceProxyModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.FenceProxyModelProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HostProxySourceEditor extends AddRemoveRowWidget<ListModel<FenceProxyModel>, FenceProxyModel,
    HostProxySourceWidget> {

    interface WidgetUiBinder extends UiBinder<Widget, HostProxySourceEditor> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @UiField
    @Ignore
    Label header;

    @UiField
    @Ignore
    Label newProxyLabel;

    @UiField
    Button newProxyButton;

    private final FenceProxyModelProvider modelProvider;

    private HandlerRegistration addClickHandlerRegistration;

    private ListModel<FenceProxyModel> listModel;

    private boolean isEnabled;

    @Inject
    public HostProxySourceEditor(FenceProxyModelProvider modelProvider) {
        showGhost = false;
        showAddButton = false;
        this.modelProvider = modelProvider;
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        newProxyButton.setIcon(IconType.PLUS);
        addHandlers();
    }

    private void addHandlers() {
        if (addClickHandlerRegistration != null) {
            addClickHandlerRegistration.removeHandler();
        }
        addClickHandlerRegistration = newProxyButton.addClickHandler(event -> {
            if (!items.isEmpty()) {
                Pair<FenceProxyModel, HostProxySourceWidget> modelWidgetPair = items.get(items.size() - 1);
                getEntry(modelWidgetPair.getSecond()).removeLastButton();
            }
            Pair<FenceProxyModel, HostProxySourceWidget> item = addGhostEntry();
            onAdd(item.getFirst(), item.getSecond());
            item.getFirst().edit(listModel);
        });
    }

    @Override
    protected HostProxySourceWidget createWidget(FenceProxyModel value) {
        modelProvider.initializeModel(value);
        final HostProxySourceWidget widget = new HostProxySourceWidget();
        widget.addUpClickHandler(event -> {
            proxyUp(widget.getModel());
            updateButtonState();
        });
        widget.addDownClickHandler(event -> {
            proxyDown(widget.getModel());
            updateButtonState();
        });
        widget.edit(value);
        return widget;
    }

    @Override
    protected void init(ListModel<FenceProxyModel> listModel) {
        this.listModel = listModel;
        super.init(listModel);
        header.setText(constants.hostPopupSourceText());
        newProxyLabel.setText(constants.hostPopupAddProxyPreferenceType());
        setEnabled(listModel.getIsChangable());
        updateButtonState();
    }

    @Override
    protected FenceProxyModel createGhostValue() {
        return new FenceProxyModel();
    }

    @Override
    protected boolean isGhost(FenceProxyModel value) {
        return value == null || value.getEntity() == null || "".equals(value.getEntity()); //$NON-NLS-1$
    }

    public void setValue(ListModel<FenceProxyModel> value) {
        this.listModel = value;
    }

    private void updateButtonState() {
        if (!items.isEmpty()) {
            for (Pair<FenceProxyModel, HostProxySourceWidget> pair: items) {
                pair.getSecond().enableUpButton(!pair.equals(items.get(0)) && isEnabled);
                pair.getSecond().enableDownButton(!pair.equals(items.get(items.size() - 1)) && isEnabled);
                pair.getSecond().setOrder(items.indexOf(pair) + 1);
            }
        }
    }

    @Override
    protected void onRemove(FenceProxyModel value, HostProxySourceWidget widget) {
        super.onRemove(value, widget);
        updateButtonState();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        isEnabled = enabled;
        if (enabled) {
            header.removeStyleName(OvirtCss.LABEL_DISABLED);
            newProxyLabel.removeStyleName(OvirtCss.LABEL_DISABLED);
        } else {
            header.addStyleName(OvirtCss.LABEL_DISABLED);
            newProxyLabel.addStyleName(OvirtCss.LABEL_DISABLED);
        }
        newProxyButton.setEnabled(enabled);
        updateButtonState();
    }

    private void proxyUp(FenceProxyModel model) {
        if (listModel.getItems() == null) {
            return;
        }

        List<FenceProxyModel> list = new ArrayList<>(listModel.getItems());
        int selectedItemIndex = list.indexOf(model);

        // Check whether the selected item is first in the list.
        if (selectedItemIndex > 0) {
            list.remove(selectedItemIndex);
            list.add(selectedItemIndex - 1, model);

            listModel.setItems(list);
            listModel.setSelectedItem(model);
        }
    }

    private void proxyDown(FenceProxyModel model) {
        if (listModel.getItems() == null) {
            return;
        }

        List<FenceProxyModel> list = new ArrayList<>(listModel.getItems());
        int selectedItemIndex = list.indexOf(model);

        // Check whether the selected item is first in the list.
        if (selectedItemIndex < list.size()) {
            list.remove(selectedItemIndex);
            list.add(selectedItemIndex + 1, model);

            listModel.setItems(list);
            listModel.setSelectedItem(model);
        }
    }

    @Override
    protected boolean vetoRemoveWidget(Pair<FenceProxyModel, HostProxySourceWidget> item,
            FenceProxyModel value, HostProxySourceWidget widget) {
        boolean veto = false;
        if (listModel.getItems().size() <= 1) {
            veto = true;
            value.warnUserOnLimit();
        }
        return veto;
    }
}
