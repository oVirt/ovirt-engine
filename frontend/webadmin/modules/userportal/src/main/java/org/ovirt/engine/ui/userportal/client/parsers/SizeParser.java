package org.ovirt.engine.ui.userportal.client.parsers;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class SizeParser extends UPParser {

	@Override
	public String parse(String s) {
        if (s == null)
            return null;

        MatchResult match = RegExp.compile("(\\d*)\\s*(\\w*)").exec(s);
        String prefix = match.getGroup(1);
        String suffix = match.getGroup(2);
        Integer size = null;
        
        try {
            size = Integer.parseInt(prefix);
        } catch (NumberFormatException e) {
            return null;
        }

        if (suffix.equalsIgnoreCase("GB")) {
            size *= 1024;
            return size.toString();
        }

        if (suffix.equalsIgnoreCase("MB")) {
            return prefix;
        }

        // no suffix        
        return size.toString();

	}

	@Override
	public String format(String s) {
		if (s == null)
			return null;

		try {
			Integer i = Integer.parseInt(s);
			return ((i >= 1024 && i % 1024 == 0) ? (i / 1024 + " GB") : (i + " MB"));
		}
		catch(NumberFormatException e) {
		}
		return s;
	}

}