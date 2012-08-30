package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;


/**
 * The gluster volume memory status Mall Info.
 *
 */
public class MallInfo implements Serializable {

    private static final long serialVersionUID = -5333436625976301182L;

    private int arena;

    private int ordblks;

    private int smblks;

    private int hblks;

    private int hblkhd;

    private int usmblks;

    private int fsmblks;

    private int uordblks;

    private int fordblks;

    private int keepcost;

    public int getArena() {
        return arena;
    }

    public void setArena(int arena) {
        this.arena = arena;
    }

    public int getOrdblks() {
        return ordblks;
    }

    public void setOrdblks(int ordblks) {
        this.ordblks = ordblks;
    }

    public int getSmblks() {
        return smblks;
    }

    public void setSmblks(int smblks) {
        this.smblks = smblks;
    }

    public int getHblks() {
        return hblks;
    }

    public void setHblks(int hblks) {
        this.hblks = hblks;
    }

    public int getHblkhd() {
        return hblkhd;
    }

    public void setHblkhd(int hblkhd) {
        this.hblkhd = hblkhd;
    }

    public int getUsmblks() {
        return usmblks;
    }

    public void setUsmblks(int usmblks) {
        this.usmblks = usmblks;
    }

    public int getFsmblks() {
        return fsmblks;
    }

    public void setFsmblks(int fsmblks) {
        this.fsmblks = fsmblks;
    }

    public int getUordblks() {
        return uordblks;
    }

    public void setUordblks(int uordblks) {
        this.uordblks = uordblks;
    }

    public int getFordblks() {
        return fordblks;
    }

    public void setFordblks(int fordblks) {
        this.fordblks = fordblks;
    }

    public int getKeepcost() {
        return keepcost;
    }

    public void setKeepcost(int keepcost) {
        this.keepcost = keepcost;
    }
 }
