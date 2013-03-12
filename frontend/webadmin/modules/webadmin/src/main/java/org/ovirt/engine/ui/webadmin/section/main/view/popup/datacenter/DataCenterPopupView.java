package org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter;

import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.DataCenterPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class DataCenterPopupView extends AbstractModelBoundPopupView<DataCenterModel> implements DataCenterPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<DataCenterModel, DataCenterPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, DataCenterPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<DataCenterPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Path(value = "name.entity")
    @WithElementId
    EntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    @WithElementId
    EntityModelTextBoxEditor descriptionEditor;

    @UiField(provided = true)
    @Path(value = "storageTypeList.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> storageTypeListEditor;

    @UiField(provided = true)
    @Path(value = "version.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> versionEditor;

    @UiField(provided = true)
    @Path(value = "quotaEnforceTypeListModel.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> quotaEnforceTypeEditor;

    @UiField
    Style style;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public DataCenterPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initListBoxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize(constants);
        addContentStyleName(style.contentStyle());
        driver.initialize(this);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    void initListBoxEditors() {
        storageTypeListEditor = new ListModelListBoxEditor<Object>(new EnumRenderer());

        versionEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((Version) object).getValue();
            }
        });

        quotaEnforceTypeEditor = new ListModelListBoxEditor<Object>(new EnumRenderer());
    }

    void localize(ApplicationConstants constants) {
        nameEditor.setLabel(constants.nameLabel());
        descriptionEditor.setLabel(constants.descriptionLabel());
        storageTypeListEditor.setLabel(constants.dataCenterPopupStorageTypeLabel());
        versionEditor.setLabel(constants.dataCenterPopupVersionLabel());
        quotaEnforceTypeEditor.setLabel(constants.dataCenterPopupQuotaEnforceTypeLabel());
    }

    @Override
    public void edit(DataCenterModel object) {
        driver.edit(object);
    }

    @Override
    public DataCenterModel flush() {
        return driver.flush();
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    public void addContentStyleName(String styleName) {
        this.asWidget().addContentStyleName(styleName);
    }

    interface Style extends CssResource {
        String contentStyle();
    }

}
