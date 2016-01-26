package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionInfo;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.ComboBox;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextAreaLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.StringRenderer;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeParameterModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
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
    ComboBox<GlusterVolumeOptionInfo> keyComboBox;

    @Path(value = "keyList.selectedItem")
    @WithElementId("keyListBox")
    ListModelListBoxEditor<GlusterVolumeOptionInfo> keyListBoxEditor;

    @Path(value = "selectedKey.entity")
    @WithElementId("keyTextBox")
    StringEntityModelTextBoxEditor keyTextBoxEditor;

    @UiField
    @Path(value = "description.entity")
    StringEntityModelTextAreaLabelEditor descriptionEditor;

    @UiField
    @Path(value = "value.entity")
    StringEntityModelTextBoxEditor valueEditor;

    @UiField
    @Ignore
    Label messageLabel;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public VolumeParameterPopupView(EventBus eventBus) {
        super(eventBus);
        initComboBox();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize();
        driver.initialize(this);
    }

    private void localize() {
        keyListBoxEditor.setLabel(constants.optionKeyVolumeParameter());
        descriptionEditor.setLabel(constants.descriptionVolumeParameter());
        valueEditor.setLabel(constants.optionValueVolumeParameter());
    }

    void initComboBox() {
        keyListBoxEditor = new ListModelListBoxEditor<>(new StringRenderer<GlusterVolumeOptionInfo>() {
            @Override
            public String render(GlusterVolumeOptionInfo optionInfo) {
                if (optionInfo != null) {
                    return optionInfo.getKey();
                }
                return null;
            }
        });
        keyTextBoxEditor = new StringEntityModelTextBoxEditor();

        keyComboBox = new ComboBox<>(keyListBoxEditor, keyTextBoxEditor);
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
    public void setMessage(String message) {
        super.setMessage(message);
        messageLabel.setText(message);
    }

}
