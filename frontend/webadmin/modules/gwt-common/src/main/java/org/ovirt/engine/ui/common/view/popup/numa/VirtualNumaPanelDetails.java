package org.ovirt.engine.ui.common.view.popup.numa;

import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBox;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.uicommonweb.models.hosts.numa.VNodeModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class VirtualNumaPanelDetails extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, VirtualNumaPanelDetails> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface Style extends CssResource {
        String numaDetailComboBoxButton();
    }

    @UiField(provided = true)
    static final CommonApplicationConstants constants = AssetProvider.getConstants();

    @UiField
    Style style;

    @UiField(provided = true)
    public ListModelListBox<NumaTuneMode> numaTuneMode;

    @Inject
    public VirtualNumaPanelDetails() {
        numaTuneMode = new ListModelListBox<>(new EnumRenderer<NumaTuneMode>());
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        applyStyles();
    }

    private void applyStyles() {
        numaTuneMode.addButtonStyleName(style.numaDetailComboBoxButton());
    }

    public void setModel(VNodeModel nodeModel) {
        // insert values to dropdown
        numaTuneMode.setAcceptableValues(nodeModel.getNumaTuneModeList().getItems());

        // set selected value
        numaTuneMode.setValue(nodeModel.getNumaTuneModeList().getSelectedItem());

        numaTuneMode.setEnabled(nodeModel.getNumaTuneModeList().getIsChangable());

        // sync view to model
        numaTuneMode.addHandler(event -> {
            if (event.getValue() instanceof NumaTuneMode) {
                nodeModel.getNumaTuneModeList().setSelectedItem((NumaTuneMode) event.getValue());
            }
        }, ValueChangeEvent.getType());
    }
}
