package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmInterfacePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.webadmin.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.webadmin.widget.renderer.NullSafeRenderer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class VmInterfacePopupView extends AbstractModelBoundPopupView<VmInterfaceModel> implements VmInterfacePopupPresenterWidget.ViewDef {
    interface Driver extends SimpleBeanEditorDriver<VmInterfaceModel, VmInterfacePopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, VmInterfacePopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Path("name.entity")
    EntityModelTextBoxEditor nameEditor;

    @UiField(provided = true)
    @Path("network.selectedItem")
    ListModelListBoxEditor<Object> networkEditor;

    @UiField(provided = true)
    @Path("nicType.selectedItem")
    ListModelListBoxEditor<Object> nicTypeEditor;

    @UiField
    @Ignore
    CheckBox enableManualMacCheckbox;

    @UiField
    @Ignore
    Label enableManualMacCheckboxLabel;

    @UiField
    @Path("MAC.entity")
    EntityModelTextBoxEditor MACEditor;

    @Inject
    public VmInterfacePopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initManualWidgets();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        MACEditor.setEnabled(false);
        localize(constants);
        Driver.driver.initialize(this);
    }

    // TODO: Localize
    private void localize(ApplicationConstants constants) {
        nameEditor.setLabel("Name");
        networkEditor.setLabel("Network");
        nicTypeEditor.setLabel("Type");
        enableManualMacCheckboxLabel.setText("Specify custom MAC address");
    }

    private void initManualWidgets() {
        networkEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((network) object).getname();
            }
        });

        nicTypeEditor = new ListModelListBoxEditor<Object>(new EnumRenderer());
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    @Override
    public void edit(final VmInterfaceModel iface) {
        Driver.driver.edit(iface);

        enableManualMacCheckbox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                iface.getMAC().setIsChangable(enableManualMacCheckbox.getValue());
            }
        });
    }

    @Override
    public VmInterfaceModel flush() {
        return Driver.driver.flush();
    }

}
