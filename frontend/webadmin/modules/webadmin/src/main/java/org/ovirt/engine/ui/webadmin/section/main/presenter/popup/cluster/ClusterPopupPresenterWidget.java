package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster;

import java.util.Optional;

import org.ovirt.engine.core.common.businessentities.MigrationBandwidthLimitType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.common.presenter.AbstractTabbedModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.HasEnabledWithHints;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterModel;
import org.ovirt.engine.ui.uicommonweb.models.macpool.MacPoolModel;
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

        void updateMacPool(MacPoolModel macPoolModel);

        HasUiCommandClickHandlers getMacPoolButton();

        void makeMacPoolButtonInvisible();
    }

    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public ClusterPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final ClusterModel model) {
        super.init(model);

        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;
            if ("AllowClusterWithVirtGlusterEnabled".equals(propName)) { //$NON-NLS-1$
                getView().allowClusterWithVirtGlusterEnabled(model.getAllowClusterWithVirtGlusterEnabled());
            }
        });
        String spiceProxyInConfig =
                (String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigValues.SpiceProxyDefault);
        String spiceProxyMessage =
                StringHelper.isNullOrEmpty(spiceProxyInConfig) ? messages.noSpiceProxyDefined() : spiceProxyInConfig;
        getView().setSpiceProxyOverrideExplanation(messages.consoleOverrideSpiceProxyMessage(messages.consoleOverrideDefinedInGlobalConfig(),
                spiceProxyMessage));

        getModel().getVersion().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            final Version selectedVersion = getModel().getVersion().getSelectedItem();
            if (selectedVersion == null) {
                return;
            }

            getView().getMigrationBandwidthLimitTypeEditor().setEnabled(true);
            updateCustomMigrationBandwidthLimitEnabledState(model, null);
        });

        getModel().getMigrationBandwidthLimitType().getSelectedItemChangedEvent().addListener((ev, sender, args) ->
                updateCustomMigrationBandwidthLimitEnabledState(model, null));

        model.getMacPoolModel().getEntityChangedEvent().addListener((ev, sender, args) -> getView().updateMacPool(model.getMacPoolModel()));

        final UICommand addMacPoolCommand = model.getAddMacPoolCommand();
        if (addMacPoolCommand == null) {
            getView().makeMacPoolButtonInvisible();
        } else {
            getView().getMacPoolButton().setCommand(addMacPoolCommand);
            registerHandler(getView().getMacPoolButton().addClickHandler(event -> getView().getMacPoolButton().getCommand().execute(model)));
        }
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

        final Optional<Boolean> enabled = Optional.of(limitType == MigrationBandwidthLimitType.CUSTOM);
        return enabled;
    }
}
