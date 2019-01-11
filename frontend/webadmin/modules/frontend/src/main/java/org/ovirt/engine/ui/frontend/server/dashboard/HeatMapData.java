package org.ovirt.engine.ui.frontend.server.dashboard;

import java.util.List;

/**
 * This class represents all the blocks of data for the heat maps for all resources.
 */
public class HeatMapData {
    private List<HeatMapBlock> cpu;
    private List<HeatMapBlock> memory;
    private List<HeatMapBlock> storage;
    private List<HeatMapBlock> vdoSavings;

    public List<HeatMapBlock> getCpu() {
        return cpu;
    }

    public void setCpu(List<HeatMapBlock> cpu) {
        this.cpu = cpu;
    }

    public List<HeatMapBlock> getMemory() {
        return memory;
    }

    public void setMemory(List<HeatMapBlock> memory) {
        this.memory = memory;
    }

    public List<HeatMapBlock> getStorage() {
        return storage;
    }

    public void setStorage(List<HeatMapBlock> storage) {
        this.storage = storage;
    }

    public List<HeatMapBlock> getVdoSavings() {
        return vdoSavings;
    }

    public void setVdoSavings(List<HeatMapBlock> vdoSavings) {
        this.vdoSavings = vdoSavings;
    }
}
