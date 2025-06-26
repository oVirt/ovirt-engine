package org.ovirt.engine.ui.common.section.main.presenter;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister;
import org.ovirt.engine.ui.uicommonweb.models.options.EditOptionsModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VncConsoleModel;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;

public class OptionsPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<EditOptionsModel, OptionsPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<EditOptionsModel> {
        interface DelegateProvider {
            HasChangeHandlers asHasChangeHandlers();
            HasValue<String> asHasValue();
            HasText asHasText();
        }
        DelegateProvider getPublicKeyEditor();

        HasValueChangeHandlers<Boolean> getConsoleVncNativeRadioButton();

        HasValueChangeHandlers<Boolean> getConsoleNoVncRadioButton();

        HasValueChangeHandlers<Boolean> getConsoleDefaultRadioButton();

        IEventListener<? super PropertyChangedEventArgs> createHomePageListener(EditOptionsModel model);

        HasValueChangeHandlers<Boolean> getHomePageDefaultSwitch();

        void setConsoleVncNativeSelected(boolean selected, ConsoleOptionsFrontendPersister consoleOptionsPersister, EditOptionsModel model);

        void setConsoleNoVncSelected(boolean selected, ConsoleOptionsFrontendPersister consoleOptionsPersister, EditOptionsModel model);

        void setConsoleDefaultSelected(boolean selected, ConsoleOptionsFrontendPersister consoleOptionsPersister, EditOptionsModel model);
    }

    private final ConsoleOptionsFrontendPersister consoleOptionsPersister;

    @Inject
    public OptionsPopupPresenterWidget(EventBus eventBus, ViewDef view,
            ConsoleOptionsFrontendPersister consoleOptionsFrontendPersister) {
        super(eventBus, view);
        this.consoleOptionsPersister = consoleOptionsFrontendPersister;
    }

    @Override
    public void init(EditOptionsModel model) {
        super.init(model);

        registerHandler(getView().getPublicKeyEditor().asHasChangeHandlers().addChangeHandler(new ChangeHandler() {
            /**
             * It replaces arbitrary number of '\n' by '\n\n' to visually separate lines.
             */
            @Override
            public void onChange(ChangeEvent event) {
                final String originalValue = getView().getPublicKeyEditor().asHasValue().getValue();
                String valueWithoutEmptyLines = originalValue;
                if (valueWithoutEmptyLines == null) {
                    return;
                }

                while (valueWithoutEmptyLines.contains("\n\n")) { //$NON-NLS-1$
                    valueWithoutEmptyLines =
                            valueWithoutEmptyLines.replaceAll("\\n\\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                final String lineSeparatedRecords = valueWithoutEmptyLines.replaceAll("\\n", "\n\n"); //$NON-NLS-1$ //$NON-NLS-2$
                if (!originalValue.equals(lineSeparatedRecords)) {
                    getView().getPublicKeyEditor().asHasText().setText(lineSeparatedRecords);
                }
            }
        }));

        registerHandler(getView().getConsoleVncNativeRadioButton().addValueChangeHandler(event -> {
            getView().setConsoleVncNativeSelected(event.getValue(), consoleOptionsPersister, model);
        }));

        registerHandler(getView().getConsoleNoVncRadioButton().addValueChangeHandler(event -> {
            getView().setConsoleNoVncSelected(event.getValue(), consoleOptionsPersister, model);
        }));

        registerHandler(getView().getConsoleDefaultRadioButton().addValueChangeHandler(event -> {
            getView().setConsoleDefaultSelected(event.getValue(), consoleOptionsPersister, model);
        }));

        String vncType = consoleOptionsPersister.loadGeneralVncType();
        if (vncType == null) {
            getView().setConsoleDefaultSelected(true, consoleOptionsPersister, model);
        } else {
            switch (VncConsoleModel.ClientConsoleMode.valueOf(vncType)) {
                case Native:
                    getView().setConsoleVncNativeSelected(true, consoleOptionsPersister, model);
                    break;
                case NoVnc:
                    getView().setConsoleNoVncSelected(true, consoleOptionsPersister, model);
                    break;
                default:
                    getView().setConsoleDefaultSelected(true, consoleOptionsPersister, model);
                    break;
            }
        }

        IEventListener<? super PropertyChangedEventArgs> homePageListener = getView().createHomePageListener(model);
        model.getIsHomePageCustom().getPropertyChangedEvent().addListener(homePageListener);
        registerHandler(() -> model.getIsHomePageCustom().getPropertyChangedEvent().removeListener(homePageListener));

        registerHandler(getView().getHomePageDefaultSwitch().addValueChangeHandler(event -> model.getIsHomePageCustom().setEntity(false)));
    }

    @Override
    protected void handleEnterKey() {
        // preventing confirmation by <Enter>
    }

}
