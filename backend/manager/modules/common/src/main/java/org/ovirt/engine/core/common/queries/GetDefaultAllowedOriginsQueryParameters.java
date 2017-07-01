package org.ovirt.engine.core.common.queries;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class GetDefaultAllowedOriginsQueryParameters extends QueryParametersBase {
    private static final long serialVersionUID = -4231839007150359638L;

    private Set<String> suffixes = new HashSet<>();

    public GetDefaultAllowedOriginsQueryParameters() {
    }

    public void addSuffix(String s) {
        this.suffixes.add(s);
    }

    public void addSuffixes(Collection<String> suffixes) {
        this.suffixes.addAll(suffixes);
    }

    public Set<String> getSuffixes() {
        return suffixes;
    }
}
