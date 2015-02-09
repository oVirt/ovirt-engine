package org.ovirt.engine.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class NumaUtils {
    public static VdsNumaNode getVdsNumaNodeByIndex(List<VdsNumaNode> numaNodes, int index) {
        for (VdsNumaNode numaNode : numaNodes) {
            if (index == numaNode.getIndex()) {
                return numaNode;
            }
        }
        return null;
    }

    public static String buildStringFromListForNuma(Collection<Integer> list) {
        if (!list.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Integer item : list) {
                sb.append(item);
                sb.append(",");
            }
            return sb.deleteCharAt(sb.length() - 1).toString();
        }
        return "";
    }

    public static List<Integer> getNodeIndexList(List<VdsNumaNode> nodeList) {
        List<Integer> nodeIndexes = new ArrayList<>(nodeList.size());
        for (VdsNumaNode item : nodeList) {
            nodeIndexes.add(item.getIndex());
        }
        return nodeIndexes;
    }

    public static List<Integer> getPinnedNodeIndexList(List<Pair<Guid, Pair<Boolean, Integer>>> nodeList) {
        List<Integer> nodeIndexes = new ArrayList<>(nodeList.size());
        for (Pair<Guid, Pair<Boolean, Integer>> item : nodeList) {
            if (item.getSecond().getFirst() && item.getFirst() != null) {
                nodeIndexes.add(item.getSecond().getSecond());
            }
        }
        return nodeIndexes;
    }

}
