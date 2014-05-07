package org.ovirt.engine.api.restapi.types;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.ovirt.engine.api.model.Agent;
import org.ovirt.engine.api.model.Agents;
import org.ovirt.engine.api.model.Option;
import org.ovirt.engine.api.model.Options;
import org.ovirt.engine.api.model.PowerManagement;
import org.ovirt.engine.core.common.businessentities.FenceAgent;
import org.ovirt.engine.core.compat.Guid;

public class DeprecatedPowerManagementMapperTest extends Assert {

    @Test
    public void testMapPmToHost() {

        PowerManagement powerMgmt = createPowerManagement();

        List<FenceAgent> fenceAgents = DeprecatedPowerManagementMapper.map(powerMgmt, null);
        Collections.sort(fenceAgents, new FenceAgent.FenceAgentOrderComparator());
        assertEquals(fenceAgents.get(0).getIp(), "1.1.1.112");
        assertEquals(fenceAgents.get(0).getPassword(), "passwd");
        assertEquals(fenceAgents.get(0).getType(), "apc");
        assertEquals(fenceAgents.get(0).getUser(), "user");
        assertEquals(fenceAgents.get(0).getOrder(), 1);

    }

    @Test
    public void testMapPmToHost_Priority_To_PM_Fields_Over_Agents() {

        // create PM object with address="1.1.1.112"
        PowerManagement powerMgmt = new PowerManagement();
        powerMgmt.setAddress("1.1.1.112");
        powerMgmt.setUsername("pm_user");
        powerMgmt.setType("apc_2");
        // create Agent with address="1.1.1.111"
        Agent agent = createAgent("1.1.1.111", "apc", "user", "passwd", 123, 1);
        powerMgmt.setAgents(new Agents());
        powerMgmt.getAgents().getAgents().add(agent);
        List<FenceAgent> agents = DeprecatedPowerManagementMapper.map(powerMgmt, null);
        // The value that's in the PM object should have been selected (not the value in the agent).
        assertEquals("1.1.1.112", agents.get(0).getIp());
        assertEquals("pm_user", agents.get(0).getUser());
        assertEquals("passwd", agents.get(0).getPassword());
        assertEquals("apc_2", agents.get(0).getType());

    }

    @Test
    public void testMapHostToPm() {


    }

    private FenceAgent createFenceAgent(Guid id,
            String ip,
            String type,
            String user,
            String password,
            int port,
            String options,
            int order) {
        FenceAgent agent = new FenceAgent();
        agent.setId(id);
        agent.setIp(ip);
        agent.setType(type);
        agent.setUser(user);
        agent.setPassword(password);
        agent.setPort(port);
        agent.setOptions(options);
        agent.setOrder(order);
        return agent;
    }

    private Agent createAgent(String ip,
            String type,
            String user,
            String password,
            int port,
            int order) {
        Agent agent = new Agent();
        agent.setAddress(ip);
        agent.setUsername(user);
        agent.setPassword(password);
        agent.setPort(port);
        Option option = new Option();
        option.setName("option_name");
        option.setValue("option_value");
        agent.setOptions(new Options());
        agent.getOptions().getOptions().add(option);
        agent.setOrder(order);
        return agent;
    }

    private PowerManagement createPowerManagement() {
        PowerManagement powerMgmt = new PowerManagement();
        powerMgmt.setAddress("1.1.1.112");
        powerMgmt.setEnabled(true);
        powerMgmt.setPassword("passwd");
        powerMgmt.setType("apc");
        powerMgmt.setUsername("user");
        powerMgmt.setKdumpDetection(true);
        return powerMgmt;
    }

    @Test
    public void testMapHostToPmDeprecated() {
        String[] ip = { "1.1.1.111", "1.1.1.112" };
        int i = 0;
        List<FenceAgent> agents = new LinkedList<>();
        FenceAgent primaryAgent =
                createFenceAgent(new Guid("00000000-0000-0000-0000-000000000000"),
                        ip[0],
                        "apc",
                        "user",
                        "password",
                        80,
                        "secure=true",
                        1);
        agents.add(primaryAgent);
        FenceAgent secondaryAgent =
                createFenceAgent(new Guid("11111111-1111-1111-1111-111111111111"),
                        ip[1],
                        "apc",
                        "user",
                        "password",
                        80,
                        "secure=true",
                        1); // concurrent
        agents.add(secondaryAgent);
        PowerManagement powerMgmt = DeprecatedPowerManagementMapper.map(agents, null);
        assertEquals(powerMgmt.getAgents().getAgents().size(), 2);
        for (Agent agent : powerMgmt.getAgents().getAgents()) {
            assertEquals(agent.getAddress(), ip[i]);
            assertEquals(agent.getType(), "apc");
            assertEquals(agent.getUsername(), "user");
            assertTrue(agent.isConcurrent());
            assertEquals(agent.getOptions().getOptions().get(0).getName(), "secure");
            assertEquals(agent.getOptions().getOptions().get(0).getValue(), "true");
            if (i > 0) {
                assertEquals(agent.isConcurrent(), true);
            }
            i++;
        }
    }
}
