package org.ovirt.engine.ui.common.view;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;

import com.google.gwt.editor.client.Editor.Ignore;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.ListBox;

/**
 * Base implementation of the LoginPopupView. Currently adds only the setAutoHideOnNavigationEventEnabled() to
 * initWidget. TODO: check if bigger portion of the LoginPopupView can not be moved to this class
 */
public class AbstractLoginPopupView extends AbstractPopupView<DecoratedPopupPanel> {

    @UiField(provided=true)
    @Ignore
    public ListModelListBoxEditor<Object> localizationEditor;

    @UiField
    public Style style;

    public AbstractLoginPopupView(EventBus eventBus, CommonApplicationResources resources) {
        super(eventBus, resources);
        initLocalizationEditor();
    }

    @Override
    protected void initWidget(DecoratedPopupPanel widget) {
        super.initWidget(widget);

        localizationEditor.addLabelStyleName(style.localizationLabel());
        setAutoHideOnNavigationEventEnabled(true);
    }

    protected void localize(CommonApplicationConstants constants) {
        localizationEditor.setLabel(constants.loginFormLocalizationLabel());
    }

    private void initLocalizationEditor(){
        localizationEditor = new ListModelListBoxEditor<Object>();

        // Add the option to change the locale
        final ListBox localeBox = localizationEditor.asListBox();
        String currentLocale = LocaleInfo.getCurrentLocale().getLocaleName();
        if (currentLocale.equals("default")) { //$NON-NLS-1$
          currentLocale = "en"; //$NON-NLS-1$
        }
        String[] localeNames = LocaleInfo.getAvailableLocaleNames();
        for (String localeName : localeNames) {
          if (!localeName.equals("default")) { //$NON-NLS-1$
            String nativeName = LocaleInfo.getLocaleNativeDisplayName(localeName);
            localeBox.addItem(nativeName, localeName);
            if (localeName.equals(currentLocale)) {
              localeBox.setSelectedIndex(localeBox.getItemCount() - 1);
            }
          }
        }
        localeBox.addChangeHandler(new ChangeHandler() {
          @Override
        public void onChange(ChangeEvent event) {
            String localeName = localeBox.getValue(localeBox.getSelectedIndex());
            Window.open(getHostPageLocation() + "?locale=" + localeName, "_self", //$NON-NLS-1$ //$NON-NLS-2$
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

    public interface Style extends CssResource {
        String localizationLabel();
    }

}
