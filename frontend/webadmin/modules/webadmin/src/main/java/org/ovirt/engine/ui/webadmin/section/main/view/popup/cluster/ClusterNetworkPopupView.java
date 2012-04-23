package org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster;

import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterNetworkPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class ClusterNetworkPopupView extends AbstractModelBoundPopupView<ClusterNetworkModel> implements ClusterNetworkPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<ClusterNetworkModel, ClusterNetworkPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ClusterNetworkPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Path(value = "name.entity")
    EntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    EntityModelTextBoxEditor descriptionEditor;

    @UiField
    @Path(value = "isVmNetwork.entity")
    EntityModelCheckBoxEditor isVmNetworkEditor;

    @UiField
    @Path(value = "hasVLanTag.entity")
    EntityModelCheckBoxEditor hasVLanTagEditor;

    @UiField
    @Path(value = "vLanTag.entity")
    EntityModelTextBoxEditor vLanTagEditor;

    @UiField
    @Path(value = "hasMtu.entity")
    EntityModelCheckBoxEditor hasMtuEditor;

    @UiField
    @Path(value = "mtu.entity")
    EntityModelTextBoxEditor mtuEditor;

    @UiField
    @Path(value = "dataCenterName")
    SpanElement dataCenterNameLabel;

    @Inject
    public ClusterNetworkPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        Driver.driver.initialize(this);
    }

    void localize(ApplicationConstants constants) {
        nameEditor.setLabel(constants.clusterNewNetworkNameLabel());
        descriptionEditor.setLabel(constants.clusterNewNetworkDescriptionLabel());
        isVmNetworkEditor.setLabel(constants.clusterNewNetworkPopupVmNetworkLabel());
        hasVLanTagEditor.setLabel(constants.clusterNewNetworkPopupVlanEnabledLabel());
        vLanTagEditor.setLabel(constants.clusterNewNetworkPopupVlanTagLabel());
        hasMtuEditor.setLabel(constants.clusterNewNetworkPopupMtuEnabledLabel());
        mtuEditor.setLabel(constants.clusterNewNetworkPopupMtuLabel());
    }

    @Override
    public void edit(ClusterNetworkModel model) {
        Driver.driver.edit(model);
    }

    @Override
    public ClusterNetworkModel flush() {
        return Driver.driver.flush();
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    @Override
    public void setDataCenterName(String name) {
        dataCenterNameLabel.setInnerText(name);
    }

    @Override
    public void setVLanTagEnabled(boolean flag) {
        vLanTagEditor.setEnabled(flag);
    }

    @Override
    public void setMtuEnabled(boolean flag) {
        mtuEditor.setEnabled(flag);
    }

}
