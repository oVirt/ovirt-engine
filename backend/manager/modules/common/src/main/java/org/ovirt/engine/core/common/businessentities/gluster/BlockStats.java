package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;

public class BlockStats implements Serializable {

    private static final long serialVersionUID = 4858404369835014372L;
    private double size;
    private double blockRead;
    private double blockWrite;

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public double getBlockRead() {
        return blockRead;
    }

    public void setBlockRead(double blockRead) {
        this.blockRead = blockRead;
    }

    public double getBlockWrite() {
        return blockWrite;
    }

    public void setBlockWrite(double blockWrite) {
        this.blockWrite = blockWrite;
    }

}
