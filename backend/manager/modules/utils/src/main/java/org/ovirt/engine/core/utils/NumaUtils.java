package org.ovirt.engine.core.utils;

import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VdsNumaNode;

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
}
