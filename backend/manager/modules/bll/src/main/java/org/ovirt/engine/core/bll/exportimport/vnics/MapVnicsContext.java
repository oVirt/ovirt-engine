package org.ovirt.engine.core.bll.exportimport.vnics;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.flow.Context;
import org.ovirt.engine.core.compat.Guid;

public class MapVnicsContext extends Context {

    private Guid clusterId;
    private String vmName;
    // inputs from request: all the vnics in the OVF of the VM
    private List<VmNetworkInterface> ovfVnics;
    // inputs from request: all the mappings that the user supplied in the request body
    private Collection<ExternalVnicProfileMapping> externalVnicProfileMappings;
    // result of matching ovf vnics to the user mappings:
    private Map<VmNetworkInterface, ExternalVnicProfileMapping> matched = new HashMap<>();
    // context per ovf vnic that carries only the data relevant to the particular vnic
    private List<MapVnicContext> contexts = new LinkedList<>();
    // list of vnics that could not be associated with a profile on the engine
    private List<String> nonAssociableVnics = new LinkedList<>();

    public MapVnicsContext() {
    }

    public MapVnicsContext(String id) {
        super(id);
    }

    public MapVnicsContext setVmName(String vmName) {
        this.vmName = vmName;
        return this;
    }

    public String getVmName() {
        return vmName;
    }

    public MapVnicsContext setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
        return this;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public Collection<ExternalVnicProfileMapping> getUserMappings() {
        return externalVnicProfileMappings;
    }

    public MapVnicsContext setUserMappings(Collection<ExternalVnicProfileMapping> externalVnicProfileMappings) {
        this.externalVnicProfileMappings = externalVnicProfileMappings;
        return this;
    }

    public List<VmNetworkInterface> getOvfVnics() {
        return ovfVnics;
    }

    public MapVnicsContext setOvfVnics(List<VmNetworkInterface> ovfVnics) {
        this.ovfVnics = ovfVnics;
        return this;
    }

    public List<MapVnicContext> getContexts() {
        return contexts;
    }

    public boolean hasContexts() {
        return !CollectionUtils.isEmpty(contexts);
    }

    public Map<VmNetworkInterface, ExternalVnicProfileMapping> getMatched() {
        return matched;
    }

    public List<String> getNonAssociableVnics() {
        return nonAssociableVnics;
    }

    public void addNonAssociableVnic(String vnic) {
        nonAssociableVnics.add(vnic);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MapVnicsContext)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        MapVnicsContext that = (MapVnicsContext) o;
        return Objects.equals(getOvfVnics(), that.getOvfVnics()) &&
                Objects.equals(externalVnicProfileMappings, that.externalVnicProfileMappings) &&
                Objects.equals(getMatched(), that.getMatched()) &&
                Objects.equals(getContexts(), that.getContexts()) &&
                Objects.equals(getClusterId(), that.getClusterId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getOvfVnics(), externalVnicProfileMappings,
                getMatched(), getContexts(), getClusterId());
    }
}
