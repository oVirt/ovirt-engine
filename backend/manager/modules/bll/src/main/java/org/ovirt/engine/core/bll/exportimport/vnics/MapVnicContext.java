package org.ovirt.engine.core.bll.exportimport.vnics;

import static org.ovirt.engine.core.common.flow.HandlerOutcome.SUCCESS;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.flow.Context;
import org.ovirt.engine.core.common.flow.Flow;
import org.ovirt.engine.core.common.flow.Handler;
import org.ovirt.engine.core.common.flow.HandlerOutcome;
import org.ovirt.engine.core.compat.Guid;

public class MapVnicContext extends Context {

    private Flow<MapVnicContext> flow;
    private Guid clusterId;
    private ExternalVnicProfileMapping profileMapping;
    private VmNetworkInterface ovfVnic;
    private VnicProfile vnicProfileFoundByDao;

    MapVnicContext() {
        super();
    }

    MapVnicContext(String id) {
        super(id);
    }

    public Flow<MapVnicContext> getFlow() {
        return flow;
    }

    public MapVnicContext setFlow(Flow<MapVnicContext> flow) {
        this.flow = flow;
        return this;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public MapVnicContext setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
        return this;
    }

    public ExternalVnicProfileMapping getProfileMapping() {
        return profileMapping;
    }

    public MapVnicContext setProfileMapping(ExternalVnicProfileMapping profileMapping) {
        this.profileMapping = profileMapping;
        return this;
    }

    public VmNetworkInterface getOvfVnic() {
        return ovfVnic;
    }

    public MapVnicContext setOvfVnic(VmNetworkInterface ovfVnic) {
        this.ovfVnic = ovfVnic;
        return this;
    }

    public void setVnicProfileFoundByDao(VnicProfile vnicProfileFromDao) {
        this.vnicProfileFoundByDao = vnicProfileFromDao;
    }

    public VnicProfile getVnicProfileFoundByDao() {
        return vnicProfileFoundByDao;
    }

   /**
     * @return the last handler of the flow
     */
   @SuppressWarnings("unchecked")
    public Class<Handler> getLastHandler() {
        return (Class<Handler>) flowTrace.peek().get(1);
    }

   /**
     * @return the last outcome of the flow
     */
    public HandlerOutcome getLastOutcome() {
        return  (HandlerOutcome) flowTrace.peek().get(0);
    }

    public boolean isSuccessful() {
        return SUCCESS.equals(getLastOutcome());
    }

    public boolean hasException() {
        return exception != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MapVnicContext)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        MapVnicContext that = (MapVnicContext) o;
        return Objects.equals(getFlow(), that.getFlow()) &&
                Objects.equals(getClusterId(), that.getClusterId()) &&
                Objects.equals(getProfileMapping(), that.getProfileMapping()) &&
                Objects.equals(getOvfVnic(), that.getOvfVnic()) &&
                Objects.equals(getVnicProfileFoundByDao(), that.getVnicProfileFoundByDao());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getFlow(), getClusterId(), getProfileMapping(), getOvfVnic(), getVnicProfileFoundByDao());
    }

    public String print() {
        return String.format("%s {profile %s, network %s}", getId(), getOvfVnic().getVnicProfileName(), getOvfVnic().getNetworkName());
    }

}
