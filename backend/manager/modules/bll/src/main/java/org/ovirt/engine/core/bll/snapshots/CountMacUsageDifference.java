package org.ovirt.engine.core.bll.snapshots;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class CountMacUsageDifference {

    private final Map<String, Long> fromUsageCount;
    private final Map<String, Long> toUsageCount;
    private final boolean toMacsAreExpectedToBeAlreadyAllocated;
    private final List<String> extraMacs;
    private final List<String> missingMacs;


    public CountMacUsageDifference(Stream<String> from, Stream<String> to) {
        this(from, to, false);
    }

    public CountMacUsageDifference(Stream<String> from,
            Stream<String> to,
            boolean toMacsAreExpectedToBeAlreadyAllocated) {

        fromUsageCount = countMacUsage(from);
        toUsageCount = countMacUsage(to);

        this.toMacsAreExpectedToBeAlreadyAllocated = toMacsAreExpectedToBeAlreadyAllocated;

        extraMacs = countExtraMacs();
        missingMacs = countMissingMacs();
    }

    private Map<String, Long> countMacUsage(Stream<String> str) {
        Map<String, Long> result = str
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return Collections.unmodifiableMap(result);
    }

    public Map<String, Long> getFromUsageCount() {
        return fromUsageCount;
    }

    public Long getFromUsageCount(String macAddress) {
        return getUsageCountOrZero(getFromUsageCount(), macAddress);
    }

    public Map<String, Long> getToUsageCount() {
        return toUsageCount;
    }

    public Long getToUsageCount(String macAddress) {
        return getUsageCountOrZero(getToUsageCount(), macAddress);
    }

    private Stream<String> countMacReleases(String mac) {
        long currentUsage = getUsageCountOrZero(fromUsageCount, mac);
        long targetUsage = getUsageCountOrZero(toUsageCount, mac);

        long requiredNumberOfReleases = Math.max(0, currentUsage - targetUsage);

        return repeatMacNTimes(mac, requiredNumberOfReleases);
    }

    public List<String> getExtraMacs() {
        return extraMacs;
    }

    private List<String> countExtraMacs() {
        return Collections.unmodifiableList(
                fromUsageCount.keySet().stream().flatMap(this::countMacReleases).collect(Collectors.toList()));
    }

    private Stream<String> countMacAcquires(String mac) {
        if (this.toMacsAreExpectedToBeAlreadyAllocated) {
            return Stream.empty();
        }

        long targetUsage = getUsageCountOrZero(toUsageCount, mac);
        long currentUsage = getUsageCountOrZero(fromUsageCount, mac);

        long macToAcquireCount = Math.max(0, targetUsage - currentUsage);
        return repeatMacNTimes(mac, macToAcquireCount);
    }

    private long getUsageCountOrZero(Map<String, Long> usageCount, String mac) {
        Long count = usageCount.get(mac);
        return count == null ? 0 : count;
    }

    public List<String> getMissingMacs() {
        return missingMacs;
    }

    public List<String> countMissingMacs() {
        return Collections.unmodifiableList(
                toUsageCount.keySet().stream().flatMap(this::countMacAcquires).collect(Collectors.toList()));
    }

    private Stream<String> repeatMacNTimes(String mac, long times) {
        return LongStream.range(0, times).boxed().map(e -> mac);
    }

    /**
     * @param macAddress mac address
     * @return positive number, if mac is used more after operation and vice versa.
     */
    public long usageDifference(String macAddress) {
        return getToUsageCount(macAddress) -
                getFromUsageCount(macAddress);
    }

    /**
     * @return mapping from mac to its maximum usage, regardless if it's from `from` or `to` mac collection.
     */
    public Map<String, Long> maxUsage() {
        Stream<String> distinctMacs =
                Stream.concat(fromUsageCount.keySet().stream(), toUsageCount.keySet().stream()).distinct();
        Map<String, Long> maximumMacUsage = distinctMacs.collect(Collectors.toMap(Function.identity(),
                mac -> Math.max(getFromUsageCount(mac), getToUsageCount(mac))));
        return maximumMacUsage;
    }
}
