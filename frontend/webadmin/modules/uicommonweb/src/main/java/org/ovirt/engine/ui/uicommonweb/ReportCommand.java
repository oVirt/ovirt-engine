package org.ovirt.engine.ui.uicommonweb;

public class ReportCommand extends UICommand {
    private final String idParamName;
    private final String uriValue;
    public final boolean isMultiple;

    public ReportCommand(String name,
            String idParamName,
            boolean isMultiple,
            String uriValue,
            ICommandTarget target) {
        super(name, target);
        this.idParamName = idParamName;
        this.isMultiple = isMultiple;
        this.uriValue = uriValue;
    }

    public String getIdParamName() {
        return idParamName;
    }

    public boolean isMultiple() {
        return isMultiple;
    }

    public String getUriValue() {
        return uriValue;
    }
}
