package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.uicommonweb.models.vms.hostdev.RepinHostModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.hostdev.VmRepinHostPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class VmRepinHostPopupView extends AbstractModelBoundPopupView<RepinHostModel> implements VmRepinHostPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<RepinHostModel, VmRepinHostPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, VmRepinHostPopupView> {
    }

    interface ViewIdHandler extends ElementIdHandler<VmRepinHostPopupView> {
    }

    @UiField(provided = true)
    @Path("pinnedHost.selectedItem")
    @WithElementId
    ListModelListBoxEditor<VDS> pinnedHostEditor;

    private final Driver driver;

    @Inject
    public VmRepinHostPopupView(EventBus eventBus, Driver driver, ViewUiBinder uiBinder, ViewIdHandler idHandler) {
        super(eventBus);
        initEditors();
        initWidget(uiBinder.createAndBindUi(this));
        idHandler.generateAndSetIds(this);
        this.driver = driver;
        driver.initialize(this);
    }

    private void initEditors() {
        pinnedHostEditor = new ListModelListBoxEditor<>(new NameRenderer<VDS>());
    }

    @Override
    public void edit(RepinHostModel object) {
        driver.edit(object);
    }

    @Override
    public RepinHostModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void focusInput() {
        pinnedHostEditor.setFocus(true);
    }
}
