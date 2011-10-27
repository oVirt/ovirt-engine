package org.ovirt.engine.ui.userportal.client.util;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Translator;

public class UserPortalTools {

	static DateTimeFormat generalDateTimeFormat = DateTimeFormat.getFormat("yyyy-MMM-dd, HH:mm");
	static DateTimeFormat fullDateTimeFormat = DateTimeFormat.getFormat("yyyy-MMM-dd, HH:mm:ss");
	
	static public String getSizeString(int size) {
		if (size < 1024) {
			return size + "MB";
		}
		return (size / 1024) + "GB";
	}

	static public String getTranslatedEnum(Enum value) {
		Translator translator = EnumTranslator.Create(value.getClass());
		return translator.get(value);
	}
	
    static public String formatDate(Date date) {
		if (date == null)
			return null;
		return generalDateTimeFormat.format(date);
	}
    
    static public String formatDateFull(Date date) {
        if (date == null)
            return null;
        return fullDateTimeFormat.format(date);
    }
	
	static public String getSafeId(String unsafeId) {
	    if (unsafeId == null) return "";
	    
	    return unsafeId.replace(" ", "").replace(".", "").replace("-", "_");
	}

	static public String getShortString(String s, int maxLength) {
		if (s == null)
			return "";

		if (s.length() > maxLength) {
			return s.substring(0, maxLength-3) + "...";
		}

		return s;
	}
	
}
