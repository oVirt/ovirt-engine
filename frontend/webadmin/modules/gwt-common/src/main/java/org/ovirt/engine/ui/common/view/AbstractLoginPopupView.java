package org.ovirt.engine.ui.common.view;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.uicommon.ClientAgentType;
import org.ovirt.engine.ui.common.widget.dialog.DialogBoxWithKeyHandlers;
import org.ovirt.engine.ui.common.widget.dialog.PopupNativeKeyPressHandler;
import org.ovirt.engine.ui.frontend.utils.FrontendUrlUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Ignore;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
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

    interface MotdAnchorTemplate extends SafeHtmlTemplates {
        @Template("<a href=\"{0}\" target=\"blank\">{1}</a>")
        SafeHtml anchor(String url, String text);
    }

    private static final String DEFAULT_LOCALE = "default"; //$NON-NLS-1$

    private static MotdAnchorTemplate template;

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

        // Populate the locale list box with available locales
        boolean foundDefaultLocale = false;
        for (String localeName : localeNames) {
            if (!DEFAULT_LOCALE.equals(localeName)) {
                String nativeName = LocaleInfo.getLocaleNativeDisplayName(localeName);
                localeBox.addItem(nativeName, localeName);

                if (localeName.equals(currentLocale)) {
                    setSelectedLocale(localeBox.getItemCount() - 1);
                    foundDefaultLocale = true;
                }
            }
        }

        // When no available locale matches the current locale, select the first available locale
        if (!foundDefaultLocale && localeNames.length > 0) {
            setSelectedLocale(0);
        }

        if (clientAgentType.isIE8OrBelow()) {
            selectedLocale.getElement().getStyle().setOpacity(0);
        }

        localeBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                String localeQueryParam = LocaleInfo.getLocaleQueryParam();
                String localeString = "?" + localeQueryParam + "=" + localeBox.getValue(localeBox.getSelectedIndex()); //$NON-NLS-1$ //$NON-NLS-2$
                Window.open(FrontendUrlUtils.getCurrentPageURL() + localeString, "_self", ""); //$NON-NLS-1$ //$NON-NLS-2$
            }
        });
    }

    void setSelectedLocale(int index) {
        localeBox.setSelectedIndex(index);
        selectedLocale.setText(localeBox.getItemText(index));
    }

    MotdAnchorTemplate getTemplate() {
        if (template == null) {
            template = GWT.create(MotdAnchorTemplate.class);
        }
        return template;
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

    protected void setErrorMessageLabel(Label errorMessage, SafeHtml text) {
        if (text != null) {
            errorMessage.getElement().setInnerSafeHtml(text);
        } else {
            errorMessage.getElement().setInnerHTML(null);
        }
    }

    public String getMotdAnchorHtml(String url) {
        return getTemplate().anchor(url, url).asString();
    }
}
