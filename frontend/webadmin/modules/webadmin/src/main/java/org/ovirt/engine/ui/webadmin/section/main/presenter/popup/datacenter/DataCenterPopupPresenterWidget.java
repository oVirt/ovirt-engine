package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter;

import org.ovirt.engine.ui.common.presenter.AbstractTabbedModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterModel;
import org.ovirt.engine.ui.uicommonweb.models.macpool.MacPoolModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class DataCenterPopupPresenterWidget extends AbstractTabbedModelBoundPopupPresenterWidget<DataCenterModel, DataCenterPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractTabbedModelBoundPopupPresenterWidget.ViewDef<DataCenterModel> {
        void updateMacPool(MacPoolModel macPoolModel);
        HasUiCommandClickHandlers getMacPoolButton();
    }

    @Inject
    public DataCenterPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final DataCenterModel model) {
        super.init(model);
        model.getMacPoolModel().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                getView().updateMacPool(model.getMacPoolModel());
            }
        });

        getView().getMacPoolButton().setCommand(model.getAddMacPoolCommand());
        getView().getMacPoolButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                getView().getMacPoolButton().getCommand().execute(model);
            }
        });
    }

}
