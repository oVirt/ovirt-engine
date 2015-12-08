package org.ovirt.engine.ui.common.section.main.presenter;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.EditOptionsModel;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;

public class OptionsPopupPresenterWidget
        extends AbstractModelBoundPopupPresenterWidget<EditOptionsModel, OptionsPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<EditOptionsModel> {
        <T extends HasChangeHandlers & HasValue<String> & HasText> T getPublicKeyEditor();

    }

    @Inject
    public OptionsPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(EditOptionsModel model) {
        super.init(model);
        registerHandler(getView().getPublicKeyEditor().addChangeHandler(new ChangeHandler() {
            /**
             * It replaces arbitrary number of '\n' by '\n\n' to visually separate lines.
             */
            @Override
            public void onChange(ChangeEvent event) {
                final String originalValue = getView().getPublicKeyEditor().getValue();
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
                    getView().getPublicKeyEditor().setText(lineSeparatedRecords);
                }
            }
        }));
    }

    @Override
    protected void handleEnterKey() {
        // preventing confirmation by <Enter>
    }
}
