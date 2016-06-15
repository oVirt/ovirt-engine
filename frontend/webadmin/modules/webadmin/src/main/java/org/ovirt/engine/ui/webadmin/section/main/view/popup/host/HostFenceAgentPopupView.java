package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.StringRenderer;
import org.ovirt.engine.ui.uicommonweb.models.hosts.FenceAgentModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostFenceAgentPopupPresenterWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class HostFenceAgentPopupView extends AbstractModelBoundPopupView<FenceAgentModel> implements HostFenceAgentPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<FenceAgentModel, HostFenceAgentPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, HostFenceAgentPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<HostFenceAgentPopupView> {

        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private final Driver driver = GWT.create(Driver.class);

    @UiField
    @Path(value = "managementIp.entity")
    @WithElementId("managementIp")
    StringEntityModelTextBoxEditor pmAddressEditor;

    @UiField
    @Path(value = "pmUserName.entity")
    @WithElementId("pmUserName")
    StringEntityModelTextBoxEditor pmUserNameEditor;

    @UiField
    @Path(value = "pmPassword.entity")
    @WithElementId("pmPassword")
    StringEntityModelPasswordBoxEditor pmPasswordEditor;

    @UiField(provided = true)
    @Path(value = "pmType.selectedItem")
    @WithElementId("pmType")
    ListModelListBoxEditor<String> pmTypeEditor;

    @UiField
    @Path(value = "pmPort.entity")
    @WithElementId("pmPort")
    IntegerEntityModelTextBoxEditor pmPortEditor;

    @UiField
    @Path(value = "pmSlot.entity")
    @WithElementId("pmSlot")
    StringEntityModelTextBoxEditor pmSlotEditor;

    @UiField
    @Path(value = "pmOptions.entity")
    @WithElementId("pmOptions")
    StringEntityModelTextBoxEditor pmOptionsEditor;

    @UiField(provided=true)
    @Path(value = "pmEncryptOptions.entity")
    @WithElementId("pmEncryptOptions")
    EntityModelCheckBoxEditor pmEncryptOptionsEditor;

    @UiField
    @Ignore
    Label pmOptionsExplanationLabel;

    @UiField
    @Ignore
    Label testMessage;

    @UiField(provided=true)
    @Path(value = "pmSecure.entity")
    @WithElementId("pmSecure")
    EntityModelCheckBoxEditor pmSecureEditor;

    @UiField
    UiCommandButton testButton;

    @Inject
    public HostFenceAgentPopupView(EventBus eventBus) {
        super(eventBus);
        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();
        driver.initialize(this);
    }

    private void initEditors() {
        pmTypeEditor = new ListModelListBoxEditor<>(new StringRenderer<String>());
        pmEncryptOptionsEditor= new EntityModelCheckBoxEditor(Align.RIGHT);
        pmEncryptOptionsEditor.setUsePatternFly(true);
        pmSecureEditor= new EntityModelCheckBoxEditor(Align.RIGHT);
        pmSecureEditor.setUsePatternFly(true);
    }

    private void localize() {
        testButton.setLabel(constants.hostPopupTestButtonLabel());

        pmAddressEditor.setLabel(constants.hostPopupPmAddressLabel());
        pmUserNameEditor.setLabel(constants.hostPopupPmUserNameLabel());
        pmPasswordEditor.setLabel(constants.hostPopupPmPasswordLabel());
        pmTypeEditor.setLabel(constants.hostPopupPmTypeLabel());
        pmPortEditor.setLabel(constants.hostPopupPmPortLabel());
        pmSlotEditor.setLabel(constants.hostPopupPmSlotLabel());
        pmOptionsEditor.setLabel(constants.hostPopupPmOptionsLabel());
        pmOptionsExplanationLabel.setText(constants.hostPopupPmOptionsExplanationLabel());
        pmSecureEditor.setLabel(constants.hostPopupPmSecureLabel());
        pmEncryptOptionsEditor.setLabel(constants.hostPopupPmEncryptOptionsLabel());

    }

    @Override
    public void updatePmSlotLabelText(boolean ciscoUcsSelected) {
        pmSlotEditor.setLabel(ciscoUcsSelected ? constants.hostPopupPmCiscoUcsSlotLabel() : constants.hostPopupPmSlotLabel());
    }

    @Override
    public void edit(final FenceAgentModel object) {
        driver.edit(object);

        testButton.setCommand(object.getTestCommand());
    }

    @Override
    public void setMessage(String message) {
        testMessage.setText(message);
    }


    @Override
    public FenceAgentModel flush() {
        return driver.flush();
    }

    @Override
    public void focusInput() {
        pmAddressEditor.setFocus(true);
    }

    @Override
    public int setTabIndexes(int nextTabIndex) {
        pmAddressEditor.setTabIndex(++nextTabIndex);
        pmUserNameEditor.setTabIndex(++nextTabIndex);
        pmPasswordEditor.setTabIndex(++nextTabIndex);
        pmTypeEditor.setTabIndex(++nextTabIndex);
        pmPortEditor.setTabIndex(++nextTabIndex);
        pmSlotEditor.setTabIndex(++nextTabIndex);
        pmOptionsEditor.setTabIndex(++nextTabIndex);
        pmSecureEditor.setTabIndex(++nextTabIndex);
        pmEncryptOptionsEditor.setTabIndex(++nextTabIndex);
        testButton.setTabIndex(++nextTabIndex);
        return nextTabIndex;
    }

    @Override
    public HasUiCommandClickHandlers getTestButton() {
        return testButton;
    }

}
