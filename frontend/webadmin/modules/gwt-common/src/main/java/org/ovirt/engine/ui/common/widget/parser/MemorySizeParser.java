package org.ovirt.engine.ui.common.widget.parser;

import java.text.ParseException;

import org.ovirt.engine.core.compat.StringHelper;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.text.shared.Parser;

public class MemorySizeParser implements Parser<Integer> {

    @Override
    public Integer parse(CharSequence text) throws ParseException {
        MatchResult match = RegExp.compile("^(\\d*)\\s*(\\w*)$").exec(text.toString()); //$NON-NLS-1$
        if (match == null) {
            return 0;
        }
        String prefix = match.getGroup(1);
        String suffix = match.getGroup(2);
        Integer size = null;

        try {
            size = Integer.parseInt(prefix);
        } catch (NumberFormatException e) {
            return 0;
        }

        if (suffix.equalsIgnoreCase("TB") || suffix.equalsIgnoreCase("T")) { //$NON-NLS-1$ $NON-NLS-2$
            return size * 1024 * 1024;
        } else if (suffix.equalsIgnoreCase("GB") || suffix.equalsIgnoreCase("G")) {  //$NON-NLS-1$ $NON-NLS-2$
            return size * 1024;
        } else if (suffix.equalsIgnoreCase("MB") || suffix.equalsIgnoreCase("M")) { //$NON-NLS-1$ $NON-NLS-2$
            return size;
        } else if (StringHelper.isNullOrEmpty(suffix)) {
            return size;
        } else {
            return 0; // disallow garbled suffixes
        }
    }

}
