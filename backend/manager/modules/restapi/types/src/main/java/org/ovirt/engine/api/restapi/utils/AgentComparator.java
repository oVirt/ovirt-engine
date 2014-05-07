package org.ovirt.engine.api.restapi.utils;

import java.util.Comparator;

import org.ovirt.engine.api.model.Agent;

public class AgentComparator implements Comparator<Agent> {

    @Override
    public int compare(Agent agent1, Agent agent2) {
        return agent1.getOrder().compareTo(agent2.getOrder());
    }
}
