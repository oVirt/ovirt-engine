package org.ovirt.engine.core.bll.pm;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;

public class PowerManagementHelper {

    /**
     * Get an iterator over the fencing-agents of this host. Agents are returned sorted by their 'order' attribute -
     * lowest order first, highest order last.
     */
    public static AgentsIterator getAgentsIterator(List<FenceAgent> fenceAgents) {
        orderAgents(fenceAgents);
        return new AgentsIterator(fenceAgents);
    }

    public static void orderAgents(List<FenceAgent> fenceAgents) {
        synchronized (fenceAgents) {
            Collections.sort(fenceAgents, new FenceAgent.FenceAgentOrderComparator());
        }
    }

    public static class AgentsIterator implements Iterator<List<FenceAgent>> {

        private List<FenceAgent> agents; // assumes agents are ordered.
        int pos;
        int size;

        public AgentsIterator(List<FenceAgent> agents) {
            super();
            pos = 0;
            size = agents.size();
            this.agents = agents;
        }

        @Override
        public boolean hasNext() {
            return size >= (pos + 1);
        }

        @Override
        public List<FenceAgent> next() {
            List<FenceAgent> agentsWithSameOrder = new LinkedList<>();
            FenceAgent agent = agents.get(pos);
            agentsWithSameOrder.add(agent);
            pos += 1;
            int order = agent.getOrder();
            while (pos < agents.size()) {
                agent = agents.get(pos);
                if (agent.getOrder() == order) {
                    agentsWithSameOrder.add(agent);
                    pos += 1;
                } else {
                    break;
                }
            }
            return agentsWithSameOrder;
        }

        @Override
        public void remove() {
            // never used, no need to implement.
            throw new UnsupportedOperationException();
        }

    }
}
