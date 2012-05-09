package org.ovirt.engine.ui.common.view;

import org.ovirt.engine.ui.common.CommonApplicationResources;

import com.google.gwt.editor.client.Editor.Ignore;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

/**
 * Base implementation of the LoginPopupView. Currently adds only the setAutoHideOnNavigationEventEnabled() to
 * initWidget. TODO: check if bigger portion of the LoginPopupView can not be moved to this class
 */
public class AbstractLoginPopupView extends AbstractPopupView<DecoratedPopupPanel> {

    @UiField(provided=true)
    @Ignore
    public ListBox localeBox;

    @UiField(provided=true)
    @Ignore
    public Label selectedLocale;

    public AbstractLoginPopupView(EventBus eventBus, CommonApplicationResources resources) {
        super(eventBus, resources);
        initLocalizationEditor();
    }

    @Override
    protected void initWidget(DecoratedPopupPanel widget) {
        super.initWidget(widget);

        setAutoHideOnNavigationEventEnabled(true);
    }

    private void initLocalizationEditor(){
        localeBox = new ListBox();
        selectedLocale= new Label();

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
        localeBox.addChangeHandler(new ChangeHandler() {
          @Override
        public void onChange(ChangeEvent event) {
            String localeName = localeBox.getValue(localeBox.getSelectedIndex());
            String localeString = ""; //$NON-NLS-1$

            if (!localeName.equals("default")){ //$NON-NLS-1$
                localeString = "?locale=" + localeName; //$NON-NLS-1$
            }
            Window.open(getHostPageLocation() + localeString, "_self", //$NON-NLS-1$
                ""); //$NON-NLS-1$
          }
        });
    }

    /**
     * Get the URL of the page, without an hash of query string.
     *
     * @return the location of the page
     */
    private static native String getHostPageLocation()
    /*-{
      var s = $doc.location.href;

      // Pull off any hash.
      var i = s.indexOf('#');
      if (i != -1)
        s = s.substring(0, i);

      // Pull off any query string.
      i = s.indexOf('?');
      if (i != -1)
        s = s.substring(0, i);

      // Ensure a final slash if non-empty.
      return s;
    }-*/;
}
