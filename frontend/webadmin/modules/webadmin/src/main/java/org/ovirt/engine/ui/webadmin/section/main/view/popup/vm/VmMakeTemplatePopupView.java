package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmMakeTemplatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.webadmin.widget.Align;
import org.ovirt.engine.ui.webadmin.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.renderer.NullSafeRenderer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class VmMakeTemplatePopupView extends AbstractModelBoundPopupView<UnitVmModel>
        implements VmMakeTemplatePopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<UnitVmModel, VmMakeTemplatePopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, VmMakeTemplatePopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Path(value = "name.entity")
    EntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    EntityModelTextBoxEditor descriptionEditor;

    @UiField(provided = true)
    @Path(value = "cluster.selectedItem")
    ListModelListBoxEditor<Object> clusterEditor;

    @UiField(provided = true)
    @Path(value = "storageDomain.selectedItem")
    ListModelListBoxEditor<Object> storageDomainEditor;

    @UiField(provided = true)
    @Path(value = "isTemplatePrivate.entity")
    EntityModelCheckBoxEditor isTemplatePrivateEditor;

    @UiField
    Label message;

    @Inject
    public VmMakeTemplatePopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initListBoxEditors();
        initCheckBoxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        Driver.driver.initialize(this);
    }

    void initListBoxEditors() {
        clusterEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((VDSGroup) object).getname();
            }
        });

        storageDomainEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((storage_domains) object).getstorage_name();
            }
        });
    }

    void initCheckBoxEditors() {
        isTemplatePrivateEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    void localize(ApplicationConstants constants) {
        nameEditor.setLabel(constants.makeTemplatePopupNameLabel());
        descriptionEditor.setLabel(constants.makeTemplatePopupDescriptionLabel());
        clusterEditor.setLabel(constants.makeTemplateClusterLabel());
        storageDomainEditor.setLabel(constants.makeTemplateStorageDomainLabel());
        isTemplatePrivateEditor.setLabel(constants.makeTemplateIsTemplatePrivateEditorLabel());
    }

    @Override
    public void edit(UnitVmModel object) {
        Driver.driver.edit(object);
    }

    @Override
    public UnitVmModel flush() {
        return Driver.driver.flush();
    }

    @Override
    public void focus() {
        nameEditor.setFocus(true);
    }

}
