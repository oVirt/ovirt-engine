package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import org.gwtbootstrap3.client.ui.Alert;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelTypeAheadChangeableListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextAreaLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeParameterModel;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.VolumeParameterPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class VolumeParameterPopupView extends AbstractModelBoundPopupView<VolumeParameterModel> implements VolumeParameterPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<VolumeParameterModel, VolumeParameterPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, VolumeParameterPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VolumeParameterPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField(provided = true)
    @Path(value = "keyList.selectedItem")
    @WithElementId("keyListBox")
    public ListModelTypeAheadChangeableListBoxEditor keyListBoxEditor;

    @UiField
    @Path(value = "description.entity")
    public StringEntityModelTextAreaLabelEditor descriptionEditor;

    @UiField
    @Path(value = "value.entity")
    public StringEntityModelTextBoxEditor valueEditor;

    @UiField
    @Ignore
    public Alert message;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();

    @Inject
    public VolumeParameterPopupView(EventBus eventBus) {
        super(eventBus);
        initComboBox();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    void initComboBox() {
        keyListBoxEditor = new ListModelTypeAheadChangeableListBoxEditor(
                new ListModelTypeAheadChangeableListBoxEditor.NullSafeSuggestBoxRenderer() {
                    @Override
                    public String getDisplayStringNullSafe(String data) {
                        return typeAheadNameTemplateNullSafe(data);
                    }
                });
    }

    private String typeAheadNameTemplateNullSafe(String name) {
        if (name != null && !name.trim().isEmpty()) {
            return templates.typeAheadName(name).asString();
        } else {
            return templates.typeAheadEmptyContent().asString();
        }
    }
    @Override
    public void edit(final VolumeParameterModel object) {
        driver.edit(object);
    }

    @Override
    public VolumeParameterModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);
        this.message.setText(message);
        this.message.setVisible(StringHelper.isNotNullOrEmpty(message));
    }

}
