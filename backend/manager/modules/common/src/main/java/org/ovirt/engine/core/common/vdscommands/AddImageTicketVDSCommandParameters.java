package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;


public class AddImageTicketVDSCommandParameters extends ImageTicketVDSCommandParametersBase {
    private String[] operations;
    private long size;
    private String url;

    public AddImageTicketVDSCommandParameters() {
    }

    public AddImageTicketVDSCommandParameters(Guid vdsId,
            Guid ticketId,
            String[] operations,
            long timeout,
            long size,
            String url) {
        super(vdsId, ticketId, timeout);
        this.operations = operations;
        this.size = size;
        this.url = url;
    }

    public String[] getOperations() {
        return operations;
    }

    public long getSize() {
        return size;
    }

    public String getUrl() {
        return url;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("operations", getOperations())
                .append("size", getSize())
                .append("url", getUrl());
    }
}
