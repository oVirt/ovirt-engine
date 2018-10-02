package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;


public class AddImageTicketVDSCommandParameters extends ImageTicketVDSCommandParametersBase {
    private String[] operations;
    private long size;
    private String url;
    private String filename;
    private boolean sparse;
    private Guid transferId;

    public AddImageTicketVDSCommandParameters() {
    }

    public AddImageTicketVDSCommandParameters(Guid vdsId,
            Guid ticketId,
            Guid transferId,
            String[] operations,
            long timeout,
            long size,
            String url,
            String filename,
            boolean sparse) {
        super(vdsId, ticketId, timeout);
        this.transferId = transferId;
        this.operations = operations;
        this.size = size;
        this.url = url;
        this.filename = filename;
        this.sparse = sparse;
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

    public String getFilename() {
        return filename;
    }

    public boolean isSparse() {
        return sparse;
    }

    public void setSparse(boolean sparse) {
        this.sparse = sparse;
    }

    public Guid getTransferId() {
        return transferId;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("operations", getOperations())
                .append("size", getSize())
                .append("url", getUrl())
                .append("filename", getFilename())
                .append("sparse", isSparse())
                .append("transferId", getTransferId());
    }
}
