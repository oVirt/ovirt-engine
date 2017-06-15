package org.ovirt.engine.ui.webadmin.section.main.view.popup.profile;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.uicommonweb.models.profiles.DiskProfileBaseModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.profile.DiskProfilePopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class DiskProfilePopupView extends AbstractModelBoundPopupView<DiskProfileBaseModel> implements DiskProfilePopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<DiskProfileBaseModel, DiskProfilePopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, DiskProfilePopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<DiskProfilePopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Path("name.entity")
    @WithElementId("name")
    StringEntityModelTextBoxEditor nameEditor;

    @UiField
    @Path("description.entity")
    @WithElementId("description")
    StringEntityModelTextBoxEditor descriptionEditor;

    @UiField(provided = true)
    @Path(value = "qos.selectedItem")
    @WithElementId("qos")
    public ListModelListBoxEditor<StorageQos> qosEditor;

    @UiField(provided = true)
    @Path("parentListModel.selectedItem")
    ListModelListBoxEditor<StorageDomain> storageDomainEditor;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public DiskProfilePopupView(EventBus eventBus) {
        super(eventBus);
        storageDomainEditor = new ListModelListBoxEditor<>(new NameRenderer<StorageDomain>());
        qosEditor = new ListModelListBoxEditor<>(new NameRenderer<StorageQos>());
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    private void localize() {
        nameEditor.setLabel(constants.profileNameLabel());
        descriptionEditor.setLabel(constants.profileDescriptionLabel());
        storageDomainEditor.setLabel(constants.diskProfileStorageDomainLabel());
        qosEditor.setLabel(constants.diskProfileQosLabel());
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    @Override
    public void edit(DiskProfileBaseModel object) {
        driver.edit(object);
    }

    @Override
    public DiskProfileBaseModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }
    @Override
    public int setTabIndexes(int nextTabIndex) {
        storageDomainEditor.setTabIndex(nextTabIndex++);
        nameEditor.setTabIndex(nextTabIndex++);
        descriptionEditor.setTabIndex(nextTabIndex++);
        qosEditor.setTabIndex(nextTabIndex++);

        return nextTabIndex;
    }
}
