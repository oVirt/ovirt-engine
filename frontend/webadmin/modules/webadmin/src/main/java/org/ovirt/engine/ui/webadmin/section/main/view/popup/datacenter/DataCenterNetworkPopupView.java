package org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogButton;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.common.SelectionTreeNodeModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterNetworkModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.DataCenterNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.WebAdminModelBoundPopupView;
import org.ovirt.engine.ui.webadmin.uicommon.model.ModelListTreeViewModel;
import org.ovirt.engine.ui.webadmin.uicommon.model.SimpleSelectionTreeNodeModel;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelCellTree;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.inject.Inject;

public class DataCenterNetworkPopupView extends WebAdminModelBoundPopupView<DataCenterNetworkModel> implements DataCenterNetworkPopupPresenterWidget.ViewDef {

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
    @Path(value = "isStpEnabled.entity")
    EntityModelCheckBoxEditor stpSupport;

    @UiField(provided = true)
    @Path(value = "hasVLanTag.entity")
    EntityModelCheckBoxEditor vlanTagging;

    @UiField
    @Path(value = "vLanTag.entity")
    EntityModelTextBoxEditor vlanTag;

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
        stpSupport = new EntityModelCheckBoxEditor(Align.RIGHT);
        vlanTagging = new EntityModelCheckBoxEditor(Align.RIGHT);
        initListBoxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        // detachAll.setVisible(false);
        Driver.driver.initialize(this);
    }

    @Override
    public void edit(DataCenterNetworkModel object) {
        Driver.driver.edit(object);

        final UICommand detachAllCommand = object.getDetachAllCommand();
        vlanTag.setEnabled((Boolean) object.getHasVLanTag().getEntity());

        // Listen to Properties
        object.getPropertyChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                DataCenterNetworkModel model = (DataCenterNetworkModel) sender;
                String propertyName = ((PropertyChangedEventArgs) args).PropertyName;
                if ("ClusterTreeNodes".equals(propertyName)) {
                    // update tree data
                    ArrayList<SelectionTreeNodeModel> clusterTreeNodes = model.getClusterTreeNodes();
                    @SuppressWarnings("unchecked")
                    ModelListTreeViewModel<SelectionTreeNodeModel, SimpleSelectionTreeNodeModel> modelListTreeViewModel =
                            (ModelListTreeViewModel<SelectionTreeNodeModel, SimpleSelectionTreeNodeModel>) tree.getTreeViewModel();
                    List<SimpleSelectionTreeNodeModel> rootNodes =
                            SimpleSelectionTreeNodeModel.fromList(clusterTreeNodes);
                    modelListTreeViewModel.setRoot(rootNodes);
                    AsyncDataProvider<SimpleSelectionTreeNodeModel> asyncTreeDataProvider =
                            modelListTreeViewModel.getAsyncTreeDataProvider();
                    asyncTreeDataProvider.updateRowCount(rootNodes.size(), true);
                    asyncTreeDataProvider.updateRowData(0, rootNodes);
                }
                if ("Message".equals(propertyName)) {
                    messageLabel.setText(model.getMessage());
                }
            }
        });

        // Listen to "IsEnabled" property
        object.getIsEnabled().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                EntityModel entity = (EntityModel) sender;
                if (!(Boolean) entity.getEntity()) {
                    nameEditor.setEnabled(false);
                    descriptionEditor.setEnabled(false);
                    stpSupport.setEnabled(false);
                    vlanTagging.setEnabled(false);
                    vlanTag.setEnabled(false);
                }
            }
        });

        // Listen to "DetachAllAvailable" property
        object.getDetachAllAvailable().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                EntityModel entity = (EntityModel) sender;
                detachAll.setVisible((Boolean) entity.getEntity());
            }
        });

        // Listen to "HasVLanTag" property
        object.getHasVLanTag().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                EntityModel entity = (EntityModel) sender;
                vlanTag.setEnabled((Boolean) entity.getEntity());
            }
        });

        detachAll.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                detachAllCommand.Execute();
            }
        });

    }

    @Override
    public DataCenterNetworkModel flush() {
        return Driver.driver.flush();
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    void initListBoxEditors() {
    }

    void localize(ApplicationConstants constants) {
        mainLabel.setText(constants.dataCenterNetworkPopupLabel());
        subLabel.setText(constants.dataCenterNetworkPopupSubLabel());
        assignLabel.setText(constants.dataCenterNetworkPopupAssignLabel());
        nameEditor.setLabel(constants.dataCenterPopupNameLabel());
        descriptionEditor.setLabel(constants.dataCenterPopupDescriptionLabel());
        stpSupport.setLabel("STP Support");
        vlanTagging.setLabel("Enable VLAN tagging");
    }

}
