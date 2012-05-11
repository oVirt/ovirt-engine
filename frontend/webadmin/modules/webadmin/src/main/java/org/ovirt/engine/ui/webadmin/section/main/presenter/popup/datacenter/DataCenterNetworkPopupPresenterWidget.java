package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.common.SelectionTreeNodeModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterNetworkModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AbstractNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.model.ModelListTreeViewModel;
import org.ovirt.engine.ui.webadmin.uicommon.model.SimpleSelectionTreeNodeModel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.inject.Inject;

public class DataCenterNetworkPopupPresenterWidget extends AbstractNetworkPopupPresenterWidget<DataCenterNetworkModel, DataCenterNetworkPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractNetworkPopupPresenterWidget.ViewDef<DataCenterNetworkModel> {

        ModelListTreeViewModel<SelectionTreeNodeModel, SimpleSelectionTreeNodeModel> getTreeViewModel();

        void setMessageLabel(String label);

        void setInputFieldsEnabled(boolean enabled);

        void setDetachAllVisible(boolean visible);

        HasClickHandlers getDetachAll();

    }

    @Inject
    public DataCenterNetworkPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final DataCenterNetworkModel model) {
        // Let the parent do its work
        super.init(model);

        // Listen to Properties
        model.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                DataCenterNetworkModel model = (DataCenterNetworkModel) sender;
                String propertyName = ((PropertyChangedEventArgs) args).PropertyName;

                if ("ClusterTreeNodes".equals(propertyName)) { //$NON-NLS-1$
                    // update tree data
                    ArrayList<SelectionTreeNodeModel> clusterTreeNodes = model.getClusterTreeNodes();
                    ModelListTreeViewModel<SelectionTreeNodeModel, SimpleSelectionTreeNodeModel> modelListTreeViewModel =
                            getView().getTreeViewModel();
                    List<SimpleSelectionTreeNodeModel> rootNodes =
                            SimpleSelectionTreeNodeModel.fromList(clusterTreeNodes);
                    modelListTreeViewModel.setRoot(rootNodes);
                    AsyncDataProvider<SimpleSelectionTreeNodeModel> asyncTreeDataProvider =
                            modelListTreeViewModel.getAsyncTreeDataProvider();
                    asyncTreeDataProvider.updateRowCount(rootNodes.size(), true);
                    asyncTreeDataProvider.updateRowData(0, rootNodes);
                } else if ("Message".equals(propertyName)) { //$NON-NLS-1$
                    getView().setMessageLabel(model.getMessage());
                }
            }
        });

        // Listen to "IsEnabled" property
        model.getIsEnabled().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                EntityModel entity = (EntityModel) sender;
                boolean inputFieldsEnabled = (Boolean) entity.getEntity();
                getView().setInputFieldsEnabled(inputFieldsEnabled);
            }
        });

        // Listen to "DetachAllAvailable" property
        model.getDetachAllAvailable().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                EntityModel entity = (EntityModel) sender;
                getView().setDetachAllVisible((Boolean) entity.getEntity());
            }
        });

        registerHandler(getView().getDetachAll().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                model.getDetachAllCommand().Execute();
            }
        }));
    }

}
