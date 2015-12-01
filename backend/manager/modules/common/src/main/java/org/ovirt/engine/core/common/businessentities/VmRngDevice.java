package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.StringHelper;

/**
 * This class represents paravirtualized rng device.
 * The device is determined by 3 parameters
 *  - bytes - determines how many bytes are permitted to be consumed per period,
 *  - period - duration of a period in milliseconds,
 *  - source - determines the backend for the device.
 *  For more information about RNG device see libvirt documentation.
 */
public class VmRngDevice extends VmDevice implements Serializable {

    /**
     * Enum representing source for RNG device backend.
     * It can be either
     *  - "RANDOM" - /dev/random device is used as a backend, or
     *  - "HWRNG" - /dev/hwrng device (usually specialized HW generator) is used as a backend.
     */
    public enum Source {
        RANDOM,
        HWRNG;
    }

    public static Set<Source> csvToSourcesSet(String csvSources) {
        Set<Source> result = new HashSet<>();
        if (csvSources != null) {
            for (String chunk : csvSources.split(",")) {
                if (!StringHelper.isNullOrEmpty(chunk)) {
                    Source src = Source.valueOf(chunk);
                    if (src != null) {
                        result.add(src);
                    }
                }
            }
        }
        return result;
    }

    public static String sourcesToCsv(Collection<Source> sources) {
        if (sources == null) {
            return "";
        }

        StringBuilder resultBuilder = new StringBuilder("");
        for (Source source : sources) {
            resultBuilder.append(source.name());
            resultBuilder.append(",");
        }

        if (resultBuilder.length() > 0) {
            resultBuilder.deleteCharAt(resultBuilder.length() - 1);
        }

        return resultBuilder.toString();
    }

    public static final String BYTES_STRING = "bytes";
    public static final String PERIOD_STRING = "period";
    public static final String SOURCE_STRING = "source";

    public VmRngDevice() {
        this(new VmDeviceId(null, null), createSpecPars(null, null, Source.RANDOM));
    }

    public VmRngDevice(VmDevice dev) {
        this(dev.getId(), dev.getSpecParams());
    }

    public VmRngDevice(VmDeviceId id, Map<String, Object> specPars) {
        super();
        setId(id);
        setDevice(VmDeviceType.VIRTIO.getName());
        setType(VmDeviceGeneralType.RNG);
        setAddress("");
        setIsPlugged(true);
        setIsManaged(true);
        setSpecParams(specPars);
    }

    private static Map<String, Object> createSpecPars(Integer bytes, Integer period, Source source) {
        Map<String, Object> result = new HashMap<>();

        if (bytes != null) {
            result.put(BYTES_STRING, bytes.toString());
        }

        if (period != null) {
            result.put(PERIOD_STRING, period.toString());
        }

        result.put(SOURCE_STRING, source.name().toLowerCase());

        return result;
    }

    public Integer getBytes() {
        return integerOrNull(getSpecParams().get(BYTES_STRING));
    }

    public void setBytes(Integer bytes) {
        if (bytes == null) {
            getSpecParams().remove(BYTES_STRING);
        } else {
            getSpecParams().put(BYTES_STRING, bytes.toString());
        }
    }

    public Integer getPeriod() {
        return integerOrNull(getSpecParams().get(PERIOD_STRING));
    }

    public void setPeriod(Integer period) {
        if (period == null) {
            getSpecParams().remove(PERIOD_STRING);
        } else {
            getSpecParams().put(PERIOD_STRING, period.toString());
        }
    }

    public Source getSource() {
        try {
            return Source.valueOf(((String) getSpecParams().get(SOURCE_STRING)).toUpperCase());
        } catch (Exception e) {
            return Source.RANDOM;
        }
    }

    public void setSource(Source source) {
        if (source == null) {
            getSpecParams().put(SOURCE_STRING, Source.RANDOM.name().toLowerCase());
        } else {
            getSpecParams().put(SOURCE_STRING, source.name().toLowerCase());
        }
    }

    private Integer integerOrNull(Object o) {
        if (o instanceof Integer) {
            return (Integer) o;
        } else if (o instanceof String) {
            try {
                return Integer.parseInt((String) o);
            } catch (Exception e) { }
        }

        return null;
    }
}
