package org.ovirt.engine.api.restapi.types;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Agent;
import org.ovirt.engine.api.model.Agents;
import org.ovirt.engine.api.model.PowerManagement;
import org.ovirt.engine.api.restapi.utils.AgentComparator;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.compat.Guid;

@Deprecated
/**
 * This class maintains backwards compatibility by mapping Power-Management to VDS
 * as if there are still only two agents, 'primary' and 'secondary'. The agent which
 * is considered 'primary' is the one with the smallest 'order', and the agent which
 * is considered 'secondary' is the one with the next smaller order. Additional agents
 * are ignored in this context.
 *
 * In the new design, Fence-Agents are a sub-collection of a Host (.../api/hosts/xxx/fenceagents).
 * In the old structure, Fence agents were displayed and editable directly in the host (.../api/hosts/xxx):
 * <host>
 *    <power_management>
 *        <agents>
 *            <agent.../>
 *            <agent.../>
 *        </agents>
 *    </power_management>
 * </host>
 *
 * To add to this, the old structure already contains backwards-compatibility to the days
 * when only a single agents existed. Back then there was no use for a FenceAgent entity,
 * and the fields, which are now associated with an agent, existed directly inside PowerManagement:
 *
 * <host>
 *    <power_management>
 *        <ip>xxx</ip>       -->fill IP of 'primary' agent here
 *        <port>yyy</port>   -->fill port of 'primary' agent here
 *        <user>aaa</user>   -->fill username of 'primary' agent here
 *        ....               -->....
 *    </power_management>
 * </host>
 *
 * So this complexity needs to be maintained as well.
 */
public class DeprecatedPowerManagementMapper {

    @Deprecated
    public static List<FenceAgent> map(PowerManagement pm, List<FenceAgent> agents) {
        if (agents == null) {
            agents = new LinkedList<FenceAgent>();
        }
        if (pm != null) {
            if (pmAgentFieldsEmpty(pm)) {
                // client supplied empty <power_management/>, this means all PM agents should be deleted.
                agents.clear();
            } else {
                agents = mapPowerManagement(pm, agents);
            }
        }
        return agents;
    }

    /**
     * Map PowerManagement object to the host.
     */
    private static List<FenceAgent> mapPowerManagement(PowerManagement pm, List<FenceAgent> agents) {
        if (isPmContainsAgents(pm)) {
            // In case there are agents in the PowerManagement, and also PowerManagement agent-related fields are filled
            // with values, map judiciously considering both sources.
            agents = mapPowerManagementConsideringAgents(pm, agents);
        }
        else {
            // In case there are no agents, but the agent related fields in PowerManagement object are filled, create an
            // Agent from the values in these fields.
            agents = mapPowerManagementNoAgents(pm, agents);
        }
        return agents;
    }

    private static List<FenceAgent> mapPowerManagementConsideringAgents(PowerManagement pm,
            List<FenceAgent> agentsEngine) {
        // get and sort the API agents (agents found in PowerManagement object).
        List<Agent> agentsApi = pm.getAgents().getAgents();
        Collections.sort(agentsApi, new AgentComparator());

        // sort the existing agents in the engine.
        Collections.sort(agentsEngine, new FenceAgent.FenceAgentOrderComparator());

        FenceAgent primaryAgent = getPrimaryAgent(agentsEngine);
        mapPrimaryAgent(pm, agentsApi.get(0), primaryAgent);
        if (agentsApi.size() > 1) {
            mapAgent(agentsApi.get(1), getSecondaryAgent(agentsEngine));
        } else if (agentsEngine.size() > 1) {
            agentsEngine.remove(1); // client supplied 1 agent, 2 agents exist in the engine, this means the second
                              // agent should be deleted.
        }
        return agentsEngine;
    }

    private static List<FenceAgent> mapPowerManagementNoAgents(PowerManagement pm, List<FenceAgent> agents) {
        Collections.sort(agents, new FenceAgent.FenceAgentOrderComparator());
        FenceAgent agent = getPrimaryAgent(agents);
        agent.setIp(pm.getAddress());
        agent.setType(pm.getType());
        agent.setUser(pm.getUsername());
        agent.setPassword(pm.getPassword());
        if (pm.getOptions() != null) {
            String modelOptions = HostMapper.map(pm.getOptions(), null);
            if (!modelOptions.equals(agent.getOptions())) {
                agent.setOptions(modelOptions);
            }
        }
        agent.setOrder(1);
        return agents;
    }

    /**
     * Map the fence agents from this host into the PowerManagement object in the API.
     */
    public static PowerManagement map(VDS host, PowerManagement pm) {
        if (pm == null) {
            pm = new PowerManagement();
        }
        List<FenceAgent> agents = host.getFenceAgents();
        if (agents != null && !agents.isEmpty()) {
            Collections.sort(agents, new FenceAgent.FenceAgentOrderComparator());
            // Set agent-related fields directly in PowerManagement object (for backwards compatibility).
            mapAgentToPm(agents.get(0), pm);
            if (!host.isPmEnabled()) {
                pm.setType(null);
            }
            pm.setAgents(new Agents());
            pm.getAgents().getAgents().add(new Agent());
            // map the first agent
            mapAgent(agents.get(0), pm.getAgents().getAgents().get(0));
            if (agents.size() > 1) {
                pm.getAgents().getAgents().add(new Agent());
                // map the second agent (if exists).
                mapAgent(agents.get(1), pm.getAgents().getAgents().get(1));
                setConcurrent(pm, agents);
            }
        }
        return pm;
    }

    /**
     * Return 'true' if agent-related fields are empty in this PowerManagement object (samples 'address' field).
     */
    private static boolean pmAgentFieldsEmpty(PowerManagement pm) {
        return StringUtils.isEmpty(pm.getAddress())
                && (!pm.isSetAgents() || pm.getAgents().getAgents().isEmpty() || StringUtils.isEmpty(pm.getAgents()
                        .getAgents()
                        .get(0)
                        .getAddress()));
    }

    /**
     * Return 'true' if this PowerManagement object contains agents.
     */
    private static boolean isPmContainsAgents(PowerManagement pm) {
        return pm.isSetAgents() && pm.getAgents().getAgents().size() > 0;
    }

    /**
     * Return the primary agent in the engine (create it if doesn't exist).
     */
    private static FenceAgent getPrimaryAgent(List<FenceAgent> fenceAgents) {
        if (fenceAgents.isEmpty()) {
            fenceAgents.add(new FenceAgent());
        }
        return fenceAgents.get(0);
    }

    /**
     * Return the secondary agent in the engine (create it if doesn't exist).
     */
    private static FenceAgent getSecondaryAgent(List<FenceAgent> fenceAgents) {
        assert (!fenceAgents.isEmpty()); // this method called after verifying that at least 1 agent exists.
        if (fenceAgents.size() == 1) {
            fenceAgents.add(new FenceAgent());
        }
        return fenceAgents.get(1);
    }

    /**
     * Map the primary agent from the API to the engine. The way to do it, for backwards-compatibility reasons, is this:
     * For each field being mapped (e.g: IP) - if the field has a valid value inside PowerManagement object, and this
     * value is different than the value in the engine - set it. If not, then if the field being mapped has a valid
     * value in the API agent object, and this value is different than the value in the engine - set it. Otherwise, do
     * nothing.
     */
    private static void mapPrimaryAgent(PowerManagement pm, Agent agentApi, FenceAgent agentEngine) {
        setManagementIp(agentApi, agentEngine, pm);
        setManagementType(agentApi, agentEngine, pm);
        setManagementUser(agentApi, agentEngine, pm);
        setManagementPassword(agentApi, agentEngine, pm);
        setManagementOptions(agentApi, agentEngine, pm);
        agentEngine.setOrder(agentApi.getOrder());
        agentEngine.setPort(agentApi.getPort());
    }

    private static void setManagementIp(Agent agentApi, FenceAgent agentEngine, PowerManagement pm) {
        if (pm.isSetAddress() && !pm.getAddress().equals(agentEngine.getIp())) {
            agentEngine.setIp(pm.getAddress());
        } else if (agentApi.isSetAddress() && !agentApi.getAddress().equals(agentEngine.getIp())) {
            agentEngine.setIp(agentApi.getAddress());
        }
    }

    private static void setManagementType(Agent agentApi, FenceAgent agentEngine, PowerManagement pm) {
        if (pm.isSetType() && !pm.getType().equals(agentEngine.getType())) {
            agentEngine.setType(pm.getType());
        } else if (agentApi.isSetType() && !agentApi.getType().equals(agentEngine.getType())) {
            agentEngine.setType(agentApi.getType());
        }
    }

    private static void setManagementUser(Agent agentApi, FenceAgent agentEngine, PowerManagement pm) {
        if (pm.isSetUsername() && !pm.getUsername().equals(agentEngine.getUser())) {
            agentEngine.setUser(pm.getUsername());
        } else if (agentApi.isSetUsername()
                && !agentApi.getUsername().equals(agentEngine.getUser())) {
            agentEngine.setUser(agentApi.getUsername());
        }
    }

    private static void setManagementPassword(Agent agentApi, FenceAgent agentEngine, PowerManagement pm) {
        if (pm.isSetPassword() && !pm.getPassword().equals(agentEngine.getPassword())) {
            agentEngine.setPassword(pm.getPassword());
        } else if (agentApi.isSetPassword()
                && !agentApi.getPassword().equals(agentEngine.getPassword())) {
            agentEngine.setPassword(agentApi.getPassword());
        }
    }

    private static void setManagementOptions(Agent agentApi, FenceAgent agentEngine, PowerManagement pm) {
        if (pm.isSetOptions()) {
            String modelOptions = HostMapper.map(pm.getOptions(), null);
            if (!modelOptions.equals(agentEngine.getOptions())) {
                agentEngine.setOptions(modelOptions);
            }
        } else if (agentApi.isSetOptions()) {
            String agentOptions = HostMapper.map(agentApi.getOptions(), null);
            if (!agentOptions.equals(agentEngine.getOptions())) {
                agentEngine.setOptions(agentOptions);
            }
        }
    }

    /**
     * Map an agent from the API to the engine.
     */
    private static void mapAgent(Agent agentApi, FenceAgent agentEngine) {
        if (agentApi.isSetId()) {
            agentEngine.setId(Guid.createGuidFromString(agentApi.getId()));
        }
        if (agentApi.isSetAddress()) {
            agentEngine.setIp(agentApi.getAddress());
        }
        if (agentApi.isSetType()) {
            agentEngine.setType(agentApi.getType());
        }
        if (agentApi.isSetOptions()) {
            agentEngine.setOptions(HostMapper.map(agentApi.getOptions(), null));
        }
        if (agentApi.isSetUsername()) {
            agentEngine.setUser(agentApi.getUsername());
        }
        if (agentApi.isSetPassword()) {
            agentEngine.setPassword(agentApi.getPassword());
        }
        if (agentApi.isSetPort()) {
            agentEngine.setPort(agentApi.getPort());
        }
    }

    /**
     * Fill the fields in PowerManagement object, according to the values in the 'primary' agent. This is done for
     * backwards-compatibility.
     */
    private static void mapAgentToPm(FenceAgent primaryAgentEngine, PowerManagement pm) {
        pm.setType(primaryAgentEngine.getType());
        pm.setAddress(primaryAgentEngine.getIp());
        pm.setUsername(primaryAgentEngine.getUser());
        if (primaryAgentEngine.getOptionsMap() != null) {
            pm.setOptions(HostMapper.map(primaryAgentEngine.getOptionsMap(), null));
        }
    }

    /**
     * Map the engine FenceAgent object to the API Agent object.
     */
    private static void mapAgent(FenceAgent agentEngine, Agent agentApi) {
        agentApi.setId(agentEngine.getId().toString());
        agentApi.setType(agentEngine.getType());
        agentApi.setAddress(agentEngine.getIp());
        agentApi.setUsername(agentEngine.getUser());
        if (agentEngine.getOptionsMap() != null) {
            agentApi.setOptions(HostMapper.map(agentEngine.getOptionsMap(), null));
        }
        agentApi.setOrder(agentEngine.getOrder());
        agentApi.setPort(agentEngine.getPort());
        if (agentEngine.getOptions() != null && !agentEngine.getOptions().isEmpty()
                && agentEngine.getOptionsMap() != null) {
            agentApi.setOptions(HostMapper.map(agentEngine.getOptionsMap(), null));
        }
    }

    /**
     * Set the deprecated 'concurrent' field's value on the API 'Agent' objects.
     */
    private static void setConcurrent(PowerManagement pm, List<FenceAgent> fenceAgents) {
        assert fenceAgents.size() >= 2;
        boolean concurrent = fenceAgents.get(0).getOrder() == fenceAgents.get(1).getOrder();
        // When a second agent exists, 'concurrent' field is relevant for both agents, so here we
        // set it retroactively in the first agent.
        pm.getAgents().getAgents().get(0).setConcurrent(concurrent);
        pm.getAgents().getAgents().get(1).setConcurrent(concurrent);
    }

}
