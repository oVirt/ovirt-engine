package org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter;

import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.DataCenterPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.webadmin.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.webadmin.widget.renderer.NullSafeRenderer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class DataCenterPopupView extends AbstractModelBoundPopupView<DataCenterModel> implements DataCenterPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<DataCenterModel, DataCenterPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, DataCenterPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Path(value = "name.entity")
    EntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    EntityModelTextBoxEditor descriptionEditor;

    @UiField(provided = true)
    @Path(value = "storageTypeList.selectedItem")
    ListModelListBoxEditor<Object> storageTypeListEditor;

    @UiField(provided = true)
    @Path(value = "version.selectedItem")
    ListModelListBoxEditor<Object> versionEditor;

    @Inject
    public DataCenterPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initListBoxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        Driver.driver.initialize(this);
    }

    void initListBoxEditors() {
        storageTypeListEditor = new ListModelListBoxEditor<Object>(new EnumRenderer());

        versionEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((Version) object).getValue();
            }
        });
    }

    void localize(ApplicationConstants constants) {
        nameEditor.setLabel(constants.dataCenterPopupNameLabel());
        descriptionEditor.setLabel(constants.dataCenterPopupDescriptionLabel());
        storageTypeListEditor.setLabel(constants.dataCenterPopupStorageTypeLabel());
        versionEditor.setLabel(constants.dataCenterPopupVersionLabel());
    }

    @Override
    public void edit(DataCenterModel object) {
        Driver.driver.edit(object);
    }

    @Override
    public DataCenterModel flush() {
        return Driver.driver.flush();
    }

    @Override
    public void focus() {
        nameEditor.setFocus(true);
    }

}
