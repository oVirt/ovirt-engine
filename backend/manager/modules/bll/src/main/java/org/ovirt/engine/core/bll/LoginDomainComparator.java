package org.ovirt.engine.core.bll;

import java.util.Comparator;

/**
 * Used to sort the login domain list on the login page. Business rule is sort alphabetically,
 * but put "internal" at the end.
 */
public class LoginDomainComparator implements Comparator<String> {

    private String internal;

    public LoginDomainComparator(String internal) {
        this.internal = internal;
    }

    @Override
    public int compare(String a, String b) {
        if (a == b) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        if (a.equals(internal)) return 1;
        if (b.equals(internal)) return -1;
        return a.compareTo(b);
    }

}
