package org.ovirt.engine.ui.common.view;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.uicommon.ClientAgentType;
import org.ovirt.engine.ui.common.widget.dialog.DialogBoxWithKeyHandlers;
import org.ovirt.engine.ui.common.widget.dialog.PopupNativeKeyPressHandler;
import org.ovirt.engine.ui.frontend.utils.FrontendUrlUtils;

import com.google.gwt.editor.client.Editor.Ignore;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

/**
 * Base implementation of the login dialog.
 * <p>
 * TODO: check if bigger portion of the LoginPopupView can not be moved to this class
 */
public abstract class AbstractLoginPopupView extends AbstractPopupView<DialogBoxWithKeyHandlers> {

    @UiField(provided = true)
    @Ignore
    public ListBox localeBox;

    @UiField(provided = true)
    @Ignore
    public Label selectedLocale;

    private final ClientAgentType clientAgentType;

    public AbstractLoginPopupView(EventBus eventBus,
            CommonApplicationResources resources,
            ClientAgentType clientAgentType) {
        super(eventBus, resources);
        this.clientAgentType = clientAgentType;
        initLocalizationEditor();
    }

    @Override
    protected void initWidget(DialogBoxWithKeyHandlers widget) {
        super.initWidget(widget);
        setAutoHideOnNavigationEventEnabled(true);
    }

    private void initLocalizationEditor() {
        localeBox = new ListBox();
        selectedLocale = new Label();

        // Add the option to change the locale
        String currentLocale = LocaleInfo.getCurrentLocale().getLocaleName();
        String[] localeNames = LocaleInfo.getAvailableLocaleNames();

        for (String localeName : localeNames) {
            String nativeName = LocaleInfo.getLocaleNativeDisplayName(localeName);
            if (localeName.equals("default")) { //$NON-NLS-1$
                nativeName = "English"; //$NON-NLS-1$
            }

            localeBox.addItem(nativeName, localeName);
            if (localeName.equals(currentLocale)) {
                localeBox.setSelectedIndex(localeBox.getItemCount() - 1);
                selectedLocale.setText(localeBox.getItemText(localeBox.getSelectedIndex()));
            }
        }

        if (clientAgentType.isIE8OrBelow()) {
            selectedLocale.getElement().getStyle().setOpacity(0);
        }

        localeBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                String localeName = localeBox.getValue(localeBox.getSelectedIndex());
                String localeString = ""; //$NON-NLS-1$

                if (!localeName.equals("default")) { //$NON-NLS-1$
                    localeString = "?locale=" + localeName; //$NON-NLS-1$
                }
                Window.open(FrontendUrlUtils.getCurrentPageURL() + localeString, "_self", ""); //$NON-NLS-1$ //$NON-NLS-2$
            }
        });
    }

    @Override
    public HasClickHandlers getCloseButton() {
        return null;
    }

    @Override
    public HasClickHandlers getCloseIconButton() {
        return null;
    }

    @Override
    public void setPopupKeyPressHandler(PopupNativeKeyPressHandler keyPressHandler) {
        asWidget().setKeyPressHandler(keyPressHandler);
    }

}
