package org.ovirt.engine.ui.webadmin.section.main.view.popup.macpool;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.macpool.SharedMacPoolModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class SharedMacPoolPopupView extends AbstractModelBoundPopupView<SharedMacPoolModel> {

    interface Driver extends UiCommonEditorDriver<SharedMacPoolModel, SharedMacPoolPopupView> {
    }

    private Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, SharedMacPoolPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Path(value = "name.entity")
    StringEntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    StringEntityModelTextBoxEditor descriptionEditor;

    @UiField
    @Ignore
    MacPoolWidget macPoolWidget;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SharedMacPoolPopupView(EventBus eventBus) {
        super(eventBus);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);

        nameEditor.setLabel(constants.macPoolPopupName());
        descriptionEditor.setLabel(constants.macPoolPopupDescription());
    }

    @Override
    public void edit(SharedMacPoolModel model) {
        driver.edit(model);
        macPoolWidget.edit(model);
    }

    @Override
    public SharedMacPoolModel flush() {
        macPoolWidget.flush();
        return driver.flush();
    }

    public void cleanup() {
        driver.cleanup();
    }

}
