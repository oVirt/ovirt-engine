package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import java.util.Collection;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.form.key_value.KeyValueWidget;
import org.ovirt.engine.ui.common.widget.uicommon.storage.AbstractStorageView;
import org.ovirt.engine.ui.uicommonweb.models.storage.ManagedBlockStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueLineModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;


public class ManagedBlockStorageView extends AbstractStorageView<ManagedBlockStorageModel> {

    private static final String VOLUME_DRIVER = "volume_driver"; //$NON-NLS-1$

    interface Driver extends UiCommonEditorDriver<ManagedBlockStorageModel, ManagedBlockStorageView> {
    }

    interface ViewUiBinder extends UiBinder<Widget, ManagedBlockStorageView> {

        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<ManagedBlockStorageView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Ignore
    KeyValueWidget<KeyValueModel> driverOptionsEditor;

    @UiField
    @Ignore
    KeyValueWidget<KeyValueModel> driverSensitiveOptionsEditor;

    @UiField
    Label message;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public ManagedBlockStorageView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    @Override
    public void edit(ManagedBlockStorageModel object) {
        driver.edit(object);
        driverOptionsEditor.edit(object.getDriverOptions());
        driverSensitiveOptionsEditor.edit(object.getDriverSensitiveOptions());

        // In case of a new storage domain, set the first parameter to VOLUME_DRIVER
        // which is required and common to all drivers
        Collection<KeyValueLineModel> keyValueLineModelCollection = driverOptionsEditor.getModel().getItems();
        KeyValueLineModel keyValueLineModel = keyValueLineModelCollection.iterator().next();
        if (keyValueLineModelCollection.size() == 1 && keyValueLineModel != null
                && keyValueLineModel.getEditableKey().getEntity().equals("")) {
             keyValueLineModel.getEditableKey().setEntity(VOLUME_DRIVER);
        }
    }

    @Override
    public ManagedBlockStorageModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void focus() {}

}
