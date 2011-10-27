package org.ovirt.engine.core.compat;

import com.google.gwt.regexp.shared.MatchResult;

/**
 * 
 * Replacement for System.Text.RegularExpressions.Match
 * 
 * @author drankevi
 * 
 */
public class Match {

    private final int length;
    private final boolean success;
    private final String value;

    public Match(MatchResult matchResult) {
        if (matchResult != null) {
            value = matchResult.getGroup(0);
            length = matchResult.getGroupCount();
            success = true;
        } else {
            value = "";
            length = 0;
            success = false;

        }
    }

    public int getLength() {
        return length;
    }

    public String getValue() {
        return value;
    }

    public boolean Success() {
        return success;
    }

}
