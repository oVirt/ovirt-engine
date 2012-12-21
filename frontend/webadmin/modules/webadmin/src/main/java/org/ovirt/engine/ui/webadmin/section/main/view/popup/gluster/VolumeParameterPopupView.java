package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionInfo;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.ComboBox;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextAreaLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.StringRenderer;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeParameterModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.VolumeParameterPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class VolumeParameterPopupView extends AbstractModelBoundPopupView<VolumeParameterModel> implements VolumeParameterPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<VolumeParameterModel, VolumeParameterPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, VolumeParameterPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VolumeParameterPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    /*
     * @UiField(provided = true)
     *
     * @Path(value = "keyList.selectedItem") ListModelListBoxEditor<Object> keyListEditor;
     */

    @UiField(provided = true)
    @WithElementId("keyComboBox")
    ComboBox keyComboBox;

    @Path(value = "keyList.selectedItem")
    @WithElementId("keyListBox")
    ListModelListBoxEditor<Object> keyListBoxEditor;

    @Path(value = "selectedKey.entity")
    @WithElementId("keyTextBox")
    EntityModelTextBoxEditor keyTextBoxEditor;

    @UiField
    @Path(value = "description.entity")
    EntityModelTextAreaLabelEditor descriptionEditor;

    @UiField
    @Path(value = "value.entity")
    EntityModelTextBoxEditor valueEditor;

    @UiField
    @Ignore
    Label messageLabel;

    @Inject
    public VolumeParameterPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initComboBox();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize(constants);
        Driver.driver.initialize(this);
    }

    private void localize(ApplicationConstants constants) {
        keyListBoxEditor.setLabel(constants.optionKeyVolumeParameter());
        descriptionEditor.setLabel(constants.descriptionVolumeParameter());
        valueEditor.setLabel(constants.optionValueVolumeParameter());
    }

    void initComboBox() {
        keyListBoxEditor = new ListModelListBoxEditor<Object>(new StringRenderer<Object>() {
            @Override
            public String render(Object object) {
                GlusterVolumeOptionInfo optionInfo = (GlusterVolumeOptionInfo) object;
                if (optionInfo != null)
                {
                    return optionInfo.getKey();
                }
                return null;
            }
        });
        keyTextBoxEditor = new EntityModelTextBoxEditor();

        keyComboBox = new ComboBox(keyListBoxEditor, keyTextBoxEditor);
    }

    @Override
    public void edit(final VolumeParameterModel object) {
        Driver.driver.edit(object);
    }

    @Override
    public VolumeParameterModel flush() {
        return Driver.driver.flush();
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);
        messageLabel.setText(message);
    }
}
