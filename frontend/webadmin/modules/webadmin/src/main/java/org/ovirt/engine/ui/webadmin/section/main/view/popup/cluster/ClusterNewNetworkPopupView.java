package org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster;

import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterNewNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.WebAdminModelBoundPopupView;
import org.ovirt.engine.ui.webadmin.widget.dialog.SimpleDialogPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class ClusterNewNetworkPopupView extends WebAdminModelBoundPopupView<ClusterNetworkModel> implements ClusterNewNetworkPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<ClusterNetworkModel, ClusterNewNetworkPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ClusterNewNetworkPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Path(value = "name.entity")
    EntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    EntityModelTextBoxEditor descriptionEditor;

    @UiField
    @Path(value = "isStpEnabled.entity")
    EntityModelCheckBoxEditor isStpEnabledEditor;

    @UiField
    @Path(value = "hasVLanTag.entity")
    EntityModelCheckBoxEditor hasVLanTagEditor;

    @UiField
    @Path(value = "vLanTag.entity")
    EntityModelTextBoxEditor vLanTagEditor;

    @UiField
    @Path(value = "dataCenterName")
    SpanElement dataCenterNameLabel;

    @Inject
    public ClusterNewNetworkPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        Driver.driver.initialize(this);
    }

    void localize(ApplicationConstants constants) {
        nameEditor.setLabel(constants.clusterNewNetworkNameLabel());
        descriptionEditor.setLabel(constants.clusterNewNetworkDescriptionLabel());
        hasVLanTagEditor.setLabel(constants.clusterNewNetworkPopupVlanEnabledLabel());
        isStpEnabledEditor.setLabel(constants.clusterNewNetworkPopupStpEnabledLabel());
        vLanTagEditor.setLabel(constants.clusterNewNetworkPopupVlanIdLabel());
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
}
