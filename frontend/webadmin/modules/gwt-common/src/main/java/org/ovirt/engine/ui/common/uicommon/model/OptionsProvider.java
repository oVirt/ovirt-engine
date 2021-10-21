package org.ovirt.engine.ui.common.uicommon.model;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.section.main.presenter.OptionsPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.options.EditOptionsModel;
import org.ovirt.engine.ui.uicommonweb.models.options.OptionsModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class OptionsProvider extends TabModelProvider<OptionsModel> {

    private final Provider<OptionsPopupPresenterWidget> optionsPopupProvider;

    @Inject
    public OptionsProvider(EventBus eventBus,
                           Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
                           Provider<OptionsPopupPresenterWidget> optionsPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider);
        this.optionsPopupProvider = optionsPopupProvider;
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(OptionsModel source,
            UICommand lastExecutedCommand,
            Model windowModel) {
        if (windowModel instanceof EditOptionsModel) {
            return optionsPopupProvider.get();
        } else {
            return super.getModelPopup(source, lastExecutedCommand, windowModel);
        }
    }

}
