package org.ovirt.engine.core.common.queries;

public class SearchReturnValue extends VdcQueryReturnValue {
    private static final long serialVersionUID = -3741619784123689926L;

    private boolean _validSearch;

    public boolean getIsSearchValid() {
        return _validSearch;
    }

    public void setIsSearchValid(boolean value) {
        _validSearch = value;
    }

    public SearchReturnValue() {
    }
}
