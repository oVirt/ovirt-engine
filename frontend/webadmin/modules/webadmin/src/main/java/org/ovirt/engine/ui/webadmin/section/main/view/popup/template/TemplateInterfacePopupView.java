package org.ovirt.engine.ui.webadmin.section.main.view.popup.template;

import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.template.TemplateInterfacePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.WebAdminModelBoundPopupView;
import org.ovirt.engine.ui.webadmin.widget.dialog.SimpleDialogPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class TemplateInterfacePopupView extends WebAdminModelBoundPopupView<VmInterfaceModel> implements TemplateInterfacePopupPresenterWidget.ViewDef {
    interface Driver extends SimpleBeanEditorDriver<VmInterfaceModel, TemplateInterfacePopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, TemplateInterfacePopupView> {
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

    @Inject
    public TemplateInterfacePopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initManualWidgets();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        Driver.driver.initialize(this);
    }

    // TODO: Localize
    private void localize(ApplicationConstants constants) {
        nameEditor.setLabel("Name");
        networkEditor.setLabel("Network");
        nicTypeEditor.setLabel("Type");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
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
    }

    @Override
    public VmInterfaceModel flush() {
        return Driver.driver.flush();
    }
}
