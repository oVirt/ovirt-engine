package org.ovirt.engine.core.bll.scheduling;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.ovirt.engine.core.common.utils.ToStringBuilder;

/**
 * Holds memory information about one numa node
 *
 */
public class NumaNodeMemoryConsumption {

    private long memoryMB = 0L;

    private Map<Integer, Integer> hugePages = new HashMap<>();

    public NumaNodeMemoryConsumption() {
    }

    public NumaNodeMemoryConsumption(long memMB, Optional<Integer> hugePageSizeKB) {
        if (hugePageSizeKB.isPresent()) {
            this.hugePages.put(hugePageSizeKB.get(), (int) memMB * 1024 / hugePageSizeKB.get());
        } else {
            this.memoryMB = memMB;
        }
    }

    public long getMemoryMB() {
        return memoryMB;
    }

    public void setMemoryMB(long memTotal) {
        this.memoryMB = memTotal;
    }

    public Map<Integer, Integer> getHugePages() {
        return hugePages;
    }

    public static NumaNodeMemoryConsumption merge(NumaNodeMemoryConsumption req1, NumaNodeMemoryConsumption req2) {
        if (req1 == null && req2 == null) {
            return new NumaNodeMemoryConsumption();
        }

        if (req1 == null) {
            return req2;
        }

        if (req2 == null) {
            return req1;
        }

        NumaNodeMemoryConsumption result = new NumaNodeMemoryConsumption();
        result.setMemoryMB(req1.getMemoryMB() + req2.getMemoryMB());
        req1.getHugePages().forEach((size, amount) -> {
            result.getHugePages().merge(size, amount, Integer::sum);
        });
        req2.getHugePages().forEach((size, amount) -> {
            result.getHugePages().merge(size, amount, Integer::sum);
        });

        return result;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("memoryMB", getMemoryMB())
                .append("hugepages", getHugePages())
                .build();
    }
}
