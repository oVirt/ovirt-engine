package org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter;

import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterNetworkModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.DataCenterNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.AbstractNetworkPopupView;

import com.google.gwt.event.shared.EventBus;

public abstract class DataCenterNetworkPopupView extends AbstractNetworkPopupView<DataCenterNetworkModel> implements DataCenterNetworkPopupPresenterWidget.ViewDef {

    public DataCenterNetworkPopupView(EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants,
            ApplicationTemplates templates) {
        super(eventBus, resources, constants, templates);
    }

}
