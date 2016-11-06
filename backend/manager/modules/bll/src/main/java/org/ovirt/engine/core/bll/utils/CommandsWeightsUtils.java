package org.ovirt.engine.core.bll.utils;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.inject.Singleton;

@Singleton
public final class CommandsWeightsUtils {
    private CommandsWeightsUtils() {
    }

    /**
     * The method gets a total weight and a map containing a mapping between a key and it's part
     * of the given weight. The method will divide the weight between the different keys with rounding,
     * making sure that the total divided weight is equal to the provided total.
     *
     * @param weightParts map containing the part of each key in the total weight
     * @param totalWeight the total weight
     * @return a map containing the weight value of each provided key.
     */
    public Map<String, Integer> adjust(Map<String, Double> weightParts, int totalWeight) {
        Map<String, Integer> adjustedWeights = weightParts.entrySet().stream().collect(
                Collectors.toMap(Entry::getKey, v -> (int) (v.getValue() * totalWeight)));

        int totalDiff = totalWeight - adjustedWeights.values().stream().mapToInt(x -> x).sum();
        if (totalDiff == 0) {
            return adjustedWeights;
        }

        adjustedWeights.entrySet().stream().limit(totalDiff).forEach(x -> x.setValue(x.getValue() + 1));
        return adjustedWeights;
    }
}
