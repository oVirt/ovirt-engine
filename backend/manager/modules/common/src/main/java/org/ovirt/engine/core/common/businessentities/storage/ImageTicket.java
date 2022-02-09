package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.compat.Guid;

public class ImageTicket implements BusinessEntity<Guid> {

    private static final long serialVersionUID = 6233250895873388748L;

    private Guid id;

    private long size;

    private String url;

    private int timeout;

    private int inactivityTimeout;

    private String[] ops;

    private boolean sparse;

    private String transferId;

    private String filename;

    private boolean dirty;

    public ImageTicket() {
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getInactivityTimeout() {
        return inactivityTimeout;
    }

    public void setInactivityTimeout(int timeout) {
        this.inactivityTimeout = timeout;
    }

    public String[] getOps() {
        return ops;
    }

    public void setOps(String[] ops) {
        this.ops = ops;
    }

    public boolean isSparse() {
        return sparse;
    }

    public void setSparse(boolean sparse) {
        this.sparse = sparse;
    }

    public String getTransferId() {
        return transferId;
    }

    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public Map<String, Object> toDict() {
        Map<String, Object> ticketDict = new HashMap<>();
        ticketDict.put("uuid", id.toString());
        ticketDict.put("timeout", timeout);
        ticketDict.put("inactivity_timeout", inactivityTimeout);
        ticketDict.put("size", size);
        ticketDict.put("url", url);
        ticketDict.put("ops", ops);
        ticketDict.put("sparse", sparse);
        ticketDict.put("transfer_id", transferId);
        ticketDict.put("dirty", dirty);
        // filename is null by default, and only specified by the UI
        if (filename != null) {
            ticketDict.put("filename", filename);
        }
        return ticketDict;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ImageTicket that = (ImageTicket) o;
        return size == that.size &&
                timeout == that.timeout &&
                inactivityTimeout == that.inactivityTimeout &&
                sparse == that.sparse &&
                Objects.equals(id, that.id) &&
                Objects.equals(url, that.url) &&
                Arrays.equals(ops, that.ops) &&
                Objects.equals(transferId, that.transferId) &&
                Objects.equals(filename, that.filename) &&
                dirty == that.dirty;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, size, url, timeout, inactivityTimeout, sparse, transferId, filename, dirty);
        result = 31 * result + Arrays.hashCode(ops);
        return result;
    }
}
