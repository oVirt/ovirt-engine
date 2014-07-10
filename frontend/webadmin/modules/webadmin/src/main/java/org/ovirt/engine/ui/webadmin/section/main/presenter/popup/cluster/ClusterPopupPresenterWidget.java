package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;

public class ClusterPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ClusterModel, ClusterPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ClusterModel> {

        void allowClusterWithVirtGlusterEnabled(boolean value);

        void setSpiceProxyOverrideExplanation(String explanation);
    }

    private ApplicationMessages messages;

    @Inject
    public ClusterPopupPresenterWidget(EventBus eventBus, ViewDef view, ApplicationMessages messages) {
        super(eventBus, view);

        this.messages = messages;
    }

    @Override
    public void init(final ClusterModel model) {
        super.init(model);

        model.getPropertyChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).propertyName;
                if ("AllowClusterWithVirtGlusterEnabled".equals(propName)) { //$NON-NLS-1$
                    getView().allowClusterWithVirtGlusterEnabled(model.getAllowClusterWithVirtGlusterEnabled());
                }
            }
        });

        String spiceProxyInConfig =
                (String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.SpiceProxyDefault);
        String spiceProxyMessage =
                StringHelper.isNullOrEmpty(spiceProxyInConfig) ? messages.noSpiceProxyDefined() : spiceProxyInConfig;
        getView().setSpiceProxyOverrideExplanation(messages.consoleOverrideSpiceProxyMessage(messages.consoleOverrideDefinedInGlobalConfig(),
                spiceProxyMessage));
    }

}
