package org.ovirt.engine.ui.webadmin.section.main.presenter.popup;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.templates.ImportTemplatesModel;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.inject.Inject;

public class ImportTemplatesPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ImportTemplatesModel, ImportTemplatesPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ImportTemplatesModel> {

        HasEnabled getLoadVmsFromExportDomainButton();

        HasEnabled getLoadOvaButton();
    }

    @Inject
    public ImportTemplatesPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final ImportTemplatesModel model) {
        super.init(model);
        addDataCenterListener();
        addExportDomainListener();
        updateExportDomainLoadButtonEnabledState();
    }

    private void addDataCenterListener() {
        getModel().getDataCenters().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if ("IsChangable".equals(args.propertyName)) { //$NON-NLS-1$
                final boolean enabled = getModel().getDataCenters().getIsChangable();
                updateExportDomainLoadButtonEnabledState();
                getView().getLoadOvaButton().setEnabled(enabled);
            }
        });
    }

    private void addExportDomainListener() {
        getModel().getExportDomain().getPropertyChangedEvent().addListener((ev, sender, args) -> updateExportDomainLoadButtonEnabledState());
    }

    private void updateExportDomainLoadButtonEnabledState() {
        final boolean enabled = getModel().getExportDomain().getEntity() != null
                && getModel().getDataCenters().getIsChangable();
        getView().getLoadVmsFromExportDomainButton().setEnabled(enabled);
    }
}

