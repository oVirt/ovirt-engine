package org.ovirt.engine.ui.webadmin.widget.host;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.ui.common.css.OvirtCss;
import org.ovirt.engine.ui.common.widget.AddRemoveRowWidget;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class HostProxySourceEditor extends AddRemoveRowWidget<ListModel<EntityModel<String>>, EntityModel<String>,
    HostProxySourceWidget> {

    interface WidgetUiBinder extends UiBinder<Widget, HostProxySourceEditor> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private final static ApplicationConstants constants = AssetProvider.getConstants();

    @UiField
    @Ignore
    Label header;

    ListModel<EntityModel<String>> listModel;

    private boolean isEnabled;

    public HostProxySourceEditor() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    protected HostProxySourceWidget createWidget(EntityModel<String> value) {
        final HostProxySourceWidget widget = new HostProxySourceWidget();
        widget.addUpClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                proxyUp(widget.getModel());
                updateButtonState();
            }
        });
        widget.addDownClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                proxyDown(widget.getModel());
                updateButtonState();
            }
        });
        widget.edit(value);
        return widget;
    }

    @Override
    protected void init(ListModel<EntityModel<String>> listModel) {
        this.listModel = listModel;
        super.init(listModel);
        header.setText(constants.hostPopupSourceText());
        updateButtonState();
    }

    @Override
    protected EntityModel<String> createGhostValue() {
        return new EntityModel<>();
    }

    @Override
    protected boolean isGhost(EntityModel<String> value) {
        return value == null || value.getEntity() == null || "".equals(value.getEntity()); //$NON-NLS-1$
    }

    public void setValue(ListModel<EntityModel<String>> value) {
        this.listModel = value;
    }

    private void updateButtonState() {
        if (!items.isEmpty()) {
            for (Pair<EntityModel<String>, HostProxySourceWidget> pair: items) {
                pair.getSecond().enableUpButton(!pair.equals(items.get(0)) && isEnabled);
                pair.getSecond().enableDownButton(!pair.equals(items.get(items.size() - 1)) && isEnabled);
                pair.getSecond().setOrder(items.indexOf(pair) + 1);
                getEntry(pair.getSecond()).removeAllButtons();
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        isEnabled = enabled;
        if (enabled) {
            header.removeStyleName(OvirtCss.LABEL_DISABLED);
        } else {
            header.addStyleName(OvirtCss.LABEL_DISABLED);
        }
        updateButtonState();
    }

    private void proxyUp(EntityModel<String> model) {
        if (listModel.getItems() == null) {
            return;
        }

        List<EntityModel<String>> list = new ArrayList<>(listModel.getItems());
        int selectedItemIndex = list.indexOf(model);

        // Check whether the selected item is first in the list.
        if (selectedItemIndex > 0) {
            list.remove(selectedItemIndex);
            list.add(selectedItemIndex - 1, model);

            listModel.setItems(list);
            listModel.setSelectedItem(model);
        }
    }

    private void proxyDown(EntityModel<String> model) {
        if (listModel.getItems() == null) {
            return;
        }

        List<EntityModel<String>> list = new ArrayList<>(listModel.getItems());
        int selectedItemIndex = list.indexOf(model);

        // Check whether the selected item is first in the list.
        if (selectedItemIndex < list.size()) {
            list.remove(selectedItemIndex);
            list.add(selectedItemIndex + 1, model);

            listModel.setItems(list);
            listModel.setSelectedItem(model);
        }
    }
}
