package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents paravirtualized rng device.
 * The device is determined by 3 parameters
 *  - bytes - determines how many bytes are permitted to be consumed per period,
 *  - period - duration of a period in milliseconds,
 *  - source - determines the backend for the device.
 *  For more information about RNG device see libvirt documentation.
 */
public class VmRngDevice extends VmDevice implements Serializable {

    private static Logger log = LoggerFactory.getLogger(VmRngDevice.class);

    /** Enum representing source for RNG device backend. */
    public enum Source {
        /** /dev/random device is used as a backend; used before {@link #FIRST_URANDOM_VERSION} */
        RANDOM,
        /** /dev/urandom used since {@link #FIRST_URANDOM_VERSION} as replacement of {@link #RANDOM} */
        URANDOM,
        /** /dev/hwrng device (usually specialized HW generator) is used as a backend. */
        HWRNG;

        /**
         * We cannot remove 4.1 checks, because custom compatibility version of VM could still be set to version lower
         * than supported cluster level version.
         */
        public static final Version FIRST_URANDOM_VERSION = new Version(4, 1);

        public static Source getUrandomOrRandomFor(Version version) {
            if (version.greaterOrEquals(FIRST_URANDOM_VERSION)) {
                return URANDOM;
            }
            return RANDOM;
        }

        /**
         * @param oldVersion old compatibility version
         * @param newVersion new compatibility version
         * @return whether or not update of random/urandom rng device is required;
         *         false if some of the arguments is null
         */
        public static boolean urandomRandomUpdateRequired(Version oldVersion, Version newVersion) {
            if (Objects.equals(oldVersion, newVersion)) {
                return false;
            }
            if (oldVersion == null || newVersion == null) {
                return false;
            }
            return (newVersion.greaterOrEquals(VmRngDevice.Source.FIRST_URANDOM_VERSION)
                    && oldVersion.less(VmRngDevice.Source.FIRST_URANDOM_VERSION))
                    || (oldVersion.greaterOrEquals(VmRngDevice.Source.FIRST_URANDOM_VERSION)
                    && newVersion.less(VmRngDevice.Source.FIRST_URANDOM_VERSION));
        }
    }

    public static Set<Source> csvToSourcesSet(String csvSources) {
        Set<Source> result = new HashSet<>();
        if (csvSources != null) {
            for (String chunk : csvSources.split(",")) {
                if (!StringHelper.isNullOrEmpty(chunk)) {
                    try {
                        Source src = Source.valueOf(chunk);
                        if (src != null) {
                            result.add(src);
                        }
                    } catch (IllegalArgumentException ex) {
                        log.warn("Unknown RNG source '{}'", chunk);
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
        this(new VmDeviceId(null, null), createSpecPars(null, null, Source.URANDOM));
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
        setPlugged(true);
        setManaged(true);
        setSpecParams(specPars);
    }

    public void updateSourceByVersion(Version clusterVersion) {
        final Source source = getSource();
        if (source == Source.URANDOM || source == Source.RANDOM) {
            setSource(Source.getUrandomOrRandomFor(clusterVersion));
        }
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
            return Source.URANDOM;
        }
    }

    public void setSource(Source source) {
        if (source == null) {
            getSpecParams().put(SOURCE_STRING, Source.URANDOM.name().toLowerCase());
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
            } catch (Exception ignore) { }
        }

        return null;
    }
}
