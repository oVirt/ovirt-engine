package org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter;

import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogButton;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.common.SelectionTreeNodeModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterNetworkModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.DataCenterNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.model.ModelListTreeViewModel;
import org.ovirt.engine.ui.webadmin.uicommon.model.SimpleSelectionTreeNodeModel;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelCellTree;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class DataCenterNetworkPopupView extends AbstractModelBoundPopupView<DataCenterNetworkModel> implements DataCenterNetworkPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<DataCenterNetworkModel, DataCenterNetworkPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, DataCenterNetworkPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Ignore
    Label mainLabel;

    @UiField
    @Ignore
    Label subLabel;

    @UiField
    @Ignore
    Label assignLabel;

    @UiField
    @Path(value = "name.entity")
    EntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    EntityModelTextBoxEditor descriptionEditor;

    @UiField(provided = true)
    @Path(value = "isVmNetwork.entity")
    EntityModelCheckBoxEditor isVmNetworkEditor;

    @UiField(provided = true)
    @Path(value = "hasVLanTag.entity")
    EntityModelCheckBoxEditor vlanTagging;

    @UiField
    @Path(value = "vLanTag.entity")
    EntityModelTextBoxEditor vlanTag;

    @UiField(provided = true)
    @Path(value = "hasMtu.entity")
    EntityModelCheckBoxEditor hasMtuEditor;

    @UiField
    @Path(value = "mtu.entity")
    EntityModelTextBoxEditor mtuEditor;

    @UiField
    @Ignore
    EntityModelCellTree<SelectionTreeNodeModel, SimpleSelectionTreeNodeModel> tree;

    @UiField
    @Ignore
    Label messageLabel;

    @UiField
    @Ignore
    SimpleDialogButton detachAll;

    @Inject
    public DataCenterNetworkPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        isVmNetworkEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        vlanTagging = new EntityModelCheckBoxEditor(Align.RIGHT);
        hasMtuEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        // detachAll.setVisible(false);
        Driver.driver.initialize(this);
    }

    void localize(ApplicationConstants constants) {
        mainLabel.setText(constants.dataCenterNetworkPopupLabel());
        subLabel.setText(constants.dataCenterNetworkPopupSubLabel());
        assignLabel.setText(constants.dataCenterNetworkPopupAssignLabel());
        nameEditor.setLabel(constants.dataCenterPopupNameLabel());
        descriptionEditor.setLabel(constants.dataCenterPopupDescriptionLabel());
        isVmNetworkEditor.setLabel(constants.dataCenterPopupVmNetworkLabel());
        vlanTagging.setLabel(constants.dataCenterPopupEnableVlanTagLabel());
        hasMtuEditor.setLabel(constants.dataCenterPopupEnableMtuLabel());
    }

    @Override
    public void edit(DataCenterNetworkModel object) {
        Driver.driver.edit(object);
    }

    @Override
    public DataCenterNetworkModel flush() {
        return Driver.driver.flush();
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    @Override
    public void setVLanTagEnabled(boolean flag) {
        vlanTag.setEnabled(flag);
    }

    @Override
    public void setMtuEnabled(boolean flag) {
        mtuEditor.setEnabled(flag);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ModelListTreeViewModel<SelectionTreeNodeModel, SimpleSelectionTreeNodeModel> getTreeViewModel() {
        return (ModelListTreeViewModel<SelectionTreeNodeModel, SimpleSelectionTreeNodeModel>) tree.getTreeViewModel();
    }

    @Override
    public void setMessageLabel(String label) {
        messageLabel.setText(label);
    }

    @Override
    public void setInputFieldsEnabled(boolean enabled) {
        nameEditor.setEnabled(enabled);
        descriptionEditor.setEnabled(enabled);
        isVmNetworkEditor.setEnabled(enabled);
        vlanTagging.setEnabled(enabled);
        vlanTag.setEnabled(enabled);
        hasMtuEditor.setEnabled(enabled);
        mtuEditor.setEnabled(enabled);
    }

    @Override
    public void setDetachAllVisible(boolean visible) {
        detachAll.setVisible(visible);
    }

    @Override
    public HasClickHandlers getDetachAll() {
        return detachAll;
    }

}
