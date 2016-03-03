package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster;

import org.ovirt.engine.core.common.businessentities.MigrationBandwidthLimitType;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.common.presenter.AbstractTabbedModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.HasEnabledWithHints;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.external.Optional;
import org.ovirt.engine.ui.uicompat.external.StringUtils;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ClusterPopupPresenterWidget extends AbstractTabbedModelBoundPopupPresenterWidget<ClusterModel, ClusterPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractTabbedModelBoundPopupPresenterWidget.ViewDef<ClusterModel> {

        void allowClusterWithVirtGlusterEnabled(boolean value);

        void setSpiceProxyOverrideExplanation(String explanation);

        HasEnabledWithHints getMigrationBandwidthLimitTypeEditor();

        HasEnabledWithHints getCustomMigrationBandwidthLimitEditor();
    }

    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public ClusterPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final ClusterModel model) {
        super.init(model);

        model.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {

            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                String propName = args.propertyName;
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

        getModel().getVersion().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                final Version selectedVersion = getModel().getVersion().getSelectedItem();
                if (selectedVersion == null) {
                    return;
                }

                if (AsyncDataProvider.getInstance().isMigrationPoliciesSupported(selectedVersion)) {
                    getView().getMigrationBandwidthLimitTypeEditor().setEnabled(true);
                    updateCustomMigrationBandwidthLimitEnabledState(model, null);
                } else {
                    final String supportedVersions = StringUtils.join(
                            AsyncDataProvider.getInstance().getMigrationPoliciesSupportedVersions(), ", "); //$NON-NLS-1$
                    final String message = messages.onlyAvailableInCompatibilityVersions(supportedVersions);
                    getView().getMigrationBandwidthLimitTypeEditor().disable(message);
                    updateCustomMigrationBandwidthLimitEnabledState(model, message);
                }
            }
        });

        getModel().getMigrationBandwidthLimitType().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                updateCustomMigrationBandwidthLimitEnabledState(model, null);
            }
        });
    }

    /**
     * @param disabilityHint pass `null` for no hint
     */
    private void updateCustomMigrationBandwidthLimitEnabledState(ClusterModel clusterModel, String disabilityHint) {
        final Optional<Boolean> isEnabledOptional = computeCustomMigrationBandwidthLimitEnabledState(clusterModel);
        if (!isEnabledOptional.isPresent()) {
            return;
        }
        if (disabilityHint != null && !isEnabledOptional.get()) {
            getView().getCustomMigrationBandwidthLimitEditor().disable(disabilityHint);
            return;
        }
        getView().getCustomMigrationBandwidthLimitEditor().setEnabled(isEnabledOptional.get());
    }

    /**
     * @return true ~ enable, false ~ disable, null ~ can't be computed
     */
    private Optional<Boolean> computeCustomMigrationBandwidthLimitEnabledState(ClusterModel clusterModel) {
        final Version clusterVersion = clusterModel.getVersion().getSelectedItem();
        final MigrationBandwidthLimitType limitType =
                clusterModel.getMigrationBandwidthLimitType().getSelectedItem();
        if (clusterVersion == null || limitType == null) {
            return Optional.empty();
        }

        final Optional<Boolean> enabled = Optional.of(AsyncDataProvider.getInstance().isMigrationPoliciesSupported(clusterVersion)
                && limitType == MigrationBandwidthLimitType.CUSTOM);
        return enabled;
    }
}
