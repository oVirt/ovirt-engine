package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.CpuVendor;
import org.ovirt.engine.core.compat.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CpuFlagsManagerHandler implements BackendService {
    private static final Logger log = LoggerFactory.getLogger(CpuFlagsManagerHandler.class);
    private static Map<Version, CpuFlagsManager> managersDictionary = new HashMap<>();

    @PostConstruct
    public void initDictionaries() {
        log.info("Start initializing dictionaries");
        managersDictionary.clear();
        for (Version ver : Config.<HashSet<Version>> getValue(ConfigValues.SupportedClusterLevels)) {
            managersDictionary.put(ver, new CpuFlagsManager(ver));
        }
       log.info("Finished initializing dictionaries");
    }

    public String getCpuId(String name, Version ver) {
        final CpuFlagsManager cpuFlagsManager = managersDictionary.get(ver);
        return cpuFlagsManager != null ? cpuFlagsManager.getVDSVerbDataByCpuName(name) : null;
    }

    public String getCpuNameByCpuId(String name, Version ver) {
        final CpuFlagsManager cpuFlagsManager = managersDictionary.get(ver);
        return cpuFlagsManager != null ? cpuFlagsManager.getCpuNameByCpuId(name) : null;
    }

    public ArchitectureType getArchitectureByCpuName(String name, Version ver) {
        final CpuFlagsManager cpuFlagsManager = managersDictionary.get(ver);
        return cpuFlagsManager != null ? cpuFlagsManager.getArchitectureByCpuName(name) : null;
    }

    public List<ServerCpu> allServerCpuList(Version ver) {
        final CpuFlagsManager cpuFlagsManager = managersDictionary.get(ver);
        return cpuFlagsManager != null ? cpuFlagsManager.getAllServerCpuList() : new ArrayList<>();
    }

    /**
     * Returns missing CPU flags if any, or null if the server match the cluster
     * CPU flags
     *
     * @return list of missing CPU flags
     */
    public List<String> missingServerCpuFlags(String clusterCpuName, String serverFlags, Version ver) {
        final CpuFlagsManager cpuFlagsManager = managersDictionary.get(ver);
        return cpuFlagsManager != null ? cpuFlagsManager.missingServerCpuFlags(clusterCpuName, serverFlags) : null;
    }

    public boolean checkIfCpusSameManufacture(String cpuName1, String cpuName2, Version ver) {
        final CpuFlagsManager cpuFlagsManager = managersDictionary.get(ver);
        return cpuFlagsManager != null ? cpuFlagsManager.checkIfCpusSameManufacture(cpuName1, cpuName2) : false;
    }

    public boolean checkIfCpusExist(String cpuName, Version ver) {
        final CpuFlagsManager cpuFlagsManager = managersDictionary.get(ver);
        return cpuFlagsManager != null ? cpuFlagsManager.checkIfCpusExist(cpuName) : false;
    }

    /**
     * Finds max server cpu by server cpu flags only
     */
    public ServerCpu findMaxServerCpuByFlags(String flags, Version ver) {
        final CpuFlagsManager cpuFlagsManager = managersDictionary.get(ver);
        return cpuFlagsManager != null ? cpuFlagsManager.findMaxServerCpuByFlags(flags) : null;
    }


    public Version getLatestDictionaryVersion() {
        return Collections.max(managersDictionary.keySet());
    }

    public List<ServerCpu> getSupportedServerCpuList(Version ver, String maxCpuName) {
        final CpuFlagsManager cpuFlagsManager = managersDictionary.get(ver);
        return cpuFlagsManager != null ? cpuFlagsManager.getSupportedServerCpuList(maxCpuName) : new ArrayList<>();
    }

    private static class CpuFlagsManager {
        private List<ServerCpu> intelCpuList;
        private List<ServerCpu> amdCpuList;
        private List<ServerCpu> ibmCpuList;
        private List<ServerCpu> s390CpuList;
        private List<ServerCpu> allCpuList = new ArrayList<>();
        private Map<String, ServerCpu> intelCpuByNameDictionary = new HashMap<>();
        private Map<String, ServerCpu> amdCpuByNameDictionary = new HashMap<>();
        private Map<String, ServerCpu> ibmCpuByNameDictionary = new HashMap<>();
        private Map<String, ServerCpu> s390CpuByNameDictionary = new HashMap<>();
        private Map<String, ServerCpu> intelCpuByVdsNameDictionary = new HashMap<>();
        private Map<String, ServerCpu> amdCpuByVdsNameDictionary = new HashMap<>();
        private Map<String, ServerCpu> ibmCpuByVdsNameDictionary = new HashMap<>();
        private Map<String, ServerCpu> s390CpuByVdsNameDictionary = new HashMap<>();

        public CpuFlagsManager(Version ver) {
            initDictionaries(ver);
        }

        public ArchitectureType getArchitectureByCpuName(String cpuName) {
            ServerCpu cpu = getServerCpuByName(cpuName);
            if (cpu != null) {
                return cpu.getArchitecture();
            }

            return ArchitectureType.undefined;
        }

        public ServerCpu getServerCpuByName(String cpuName) {
            ServerCpu result = null;
            if (cpuName != null) {
                result = intelCpuByNameDictionary.get(cpuName);

                if (result == null) {
                    result = amdCpuByNameDictionary.get(cpuName);
                }

                if (result == null) {
                    result = ibmCpuByNameDictionary.get(cpuName);
                }

                if (result == null) {
                    result = s390CpuByNameDictionary.get(cpuName);
                }
            }
            return result;
        }

        @SuppressWarnings("synthetic-access")
        public void initDictionaries(Version ver) {
            // init dictionaries
            intelCpuByNameDictionary.clear();
            amdCpuByNameDictionary.clear();
            ibmCpuByNameDictionary.clear();
            s390CpuByNameDictionary.clear();
            allCpuList.clear();

            String[] cpus = Config.<String> getValue(ConfigValues.ServerCPUList, ver.toString()).split("[;]", -1);
            for (String cpu : cpus) {

                if (!StringUtils.isEmpty(cpu)) {
                    // [0]-level, [1]-name, [2]-flags, [3]-verb, [4]-arch
                    final String[] info = cpu.split("[:]", -1);

                    if (info.length == 5) {
                        // if no flags at all create new list instead of split
                        Set<String> flgs =
                                StringUtils.isEmpty(info[2]) ? new HashSet<>()
                                        : new HashSet<>(Arrays.asList(info[2].split("[,]", -1)));

                        String arch = info[4].trim();
                        ArchitectureType archType = ArchitectureType.valueOf(arch);

                        String levelString = info[0].trim();
                        int level = 0;

                        if (StringUtils.isNotEmpty(levelString)) {
                            level = Integer.parseInt(levelString);
                        }

                        ServerCpu sc = new ServerCpu(info[1], level, flgs, info[3], archType);
                        if (sc.getFlags().contains(CpuVendor.INTEL.getFlag())) {
                            intelCpuByNameDictionary.put(sc.getCpuName(), sc);
                            intelCpuByVdsNameDictionary.put(sc.getVdsVerbData(), sc);
                        } else if (sc.getFlags().contains(CpuVendor.AMD.getFlag())) {
                            amdCpuByNameDictionary.put(sc.getCpuName(), sc);
                            amdCpuByVdsNameDictionary.put(sc.getVdsVerbData(), sc);
                        } else if (sc.getFlags().contains(CpuVendor.IBM.getFlag())) {
                            ibmCpuByNameDictionary.put(sc.getCpuName(), sc);
                            ibmCpuByVdsNameDictionary.put(sc.getVdsVerbData(), sc);
                        } else if (sc.getFlags().contains(CpuVendor.IBMS390.getFlag())) {
                            s390CpuByNameDictionary.put(sc.getCpuName(), sc);
                            s390CpuByVdsNameDictionary.put(sc.getVdsVerbData(), sc);
                        }

                        allCpuList.add(sc);
                    } else {
                        log.error("Error getting info for CPU '{}', not in expected format.", cpu);
                    }
                }
            }
            intelCpuList = new ArrayList<>(intelCpuByNameDictionary.values());
            amdCpuList = new ArrayList<>(amdCpuByNameDictionary.values());
            ibmCpuList = new ArrayList<>(ibmCpuByNameDictionary.values());
            s390CpuList = new ArrayList<>(s390CpuByNameDictionary.values());

            Comparator<ServerCpu> cpuComparator = Comparator.comparingInt(ServerCpu::getLevel);

            // Sort by the highest cpu level so the highest cpu match will be
            // selected first
            Collections.sort(intelCpuList, cpuComparator);
            Collections.sort(amdCpuList, cpuComparator);
            Collections.sort(ibmCpuList, cpuComparator);
            Collections.sort(s390CpuList, cpuComparator);
        }

        public String getVDSVerbDataByCpuName(String name) {
            String result = null;
            ServerCpu sc = null;
            if (name != null) {
                if ((sc = intelCpuByNameDictionary.get(name)) != null
                        || (sc = amdCpuByNameDictionary.get(name)) != null
                        || (sc = ibmCpuByNameDictionary.get(name)) != null
                        || (sc = s390CpuByNameDictionary.get(name)) != null) {
                    result = sc.getVdsVerbData();
                }
            }
            return result;
        }

        public String getCpuNameByCpuId(String vdsName) {
            String result = null;
            ServerCpu sc = null;
            if (vdsName != null) {
                if ((sc = intelCpuByVdsNameDictionary.get(vdsName)) != null
                        || (sc = amdCpuByVdsNameDictionary.get(vdsName)) != null
                        || (sc = ibmCpuByVdsNameDictionary.get(vdsName)) != null
                        || (sc = s390CpuByVdsNameDictionary.get(vdsName)) != null) {
                    result = sc.getCpuName();
                }
            }
            return result;
        }

        public List<ServerCpu> getAllServerCpuList() {
            return allCpuList;
        }

        /**
         * Returns missing CPU flags if any, or null if the server match the
         * cluster CPU flags
         *
         * @return list of missing CPU flags
         */
        public List<String> missingServerCpuFlags(String clusterCpuName, String serverFlags) {
            ServerCpu clusterCpu = null;
            List<String> missingFlags = null;

            Set<String> lstServerflags =
                    StringUtils.isEmpty(serverFlags) ? new HashSet<>()
                            : new HashSet<>(Arrays.asList(serverFlags.split("[,]", -1)));

            // first find cluster cpu
            if (clusterCpuName != null
                    && ((clusterCpu = intelCpuByNameDictionary.get(clusterCpuName)) != null
                            || (clusterCpu = amdCpuByNameDictionary.get(clusterCpuName)) != null
                            || (clusterCpu = ibmCpuByNameDictionary.get(clusterCpuName)) != null
                            || (clusterCpu = s390CpuByNameDictionary.get(clusterCpuName)) != null
                    )) {
                for (String flag : clusterCpu.getFlags()) {
                    if (!lstServerflags.contains(flag)) {
                        if (missingFlags == null) {
                            missingFlags = new ArrayList<>();
                        }
                        missingFlags.add(flag);
                    }
                }
            }
            return missingFlags;
        }

        /**
         * Return true if given flag list contains all flags of given ServerCpu
         * object's flags.
         */
        private boolean checkIfFlagsContainsCpuFlags(ServerCpu clusterCpu, Set<String> lstServerflags) {
            return CollectionUtils.intersection(clusterCpu.getFlags(), lstServerflags).size() == clusterCpu.getFlags()
                    .size();
        }

        /**
         * This method returns true if the given cpus are from the same
         * manufacturer (intel or amd)
         */
        public boolean checkIfCpusSameManufacture(String cpuName1, String cpuName2) {
            if (cpuName1 == null || cpuName2 == null) {
                return false;
            }

            if (intelCpuByNameDictionary.containsKey(cpuName1)) {
                return intelCpuByNameDictionary.containsKey(cpuName2);
            }

            if (amdCpuByNameDictionary.containsKey(cpuName1)) {
                return amdCpuByNameDictionary.containsKey(cpuName2);
            }

            if (ibmCpuByNameDictionary.containsKey(cpuName1)) {
                return  ibmCpuByNameDictionary.containsKey(cpuName2);
            }

            if (s390CpuByNameDictionary.containsKey(cpuName1)) {
                return s390CpuByNameDictionary.containsKey(cpuName2);
            }

            return false;
        }

        public boolean checkIfCpusExist(String cpuName) {
            return cpuName != null
                    && (intelCpuByNameDictionary.containsKey(cpuName)
                            || amdCpuByNameDictionary.containsKey(cpuName)
                            || ibmCpuByNameDictionary.containsKey(cpuName)
                            || s390CpuByNameDictionary.containsKey(cpuName));
        }

        /**
         * Finds max server cpu by server cpu flags only
         */
        public ServerCpu findMaxServerCpuByFlags(String flags) {
            ServerCpu result = null;
            Set<String> lstFlags = StringUtils.isEmpty(flags) ? new HashSet<>()
                    : new HashSet<>(Arrays.asList(flags.split("[,]", -1)));

            if (lstFlags.contains(CpuVendor.INTEL.getFlag())) {
                for (int i = intelCpuList.size() - 1; i >= 0; i--) {
                    if (checkIfFlagsContainsCpuFlags(intelCpuList.get(i), lstFlags)) {
                        result = intelCpuList.get(i);
                        break;
                    }
                }
            } else if (lstFlags.contains(CpuVendor.AMD.getFlag())) {
                for (int i = amdCpuList.size() - 1; i >= 0; i--) {
                    if (checkIfFlagsContainsCpuFlags(amdCpuList.get(i), lstFlags)) {
                        result = amdCpuList.get(i);
                        break;
                    }
                }
            } else if (lstFlags.contains(CpuVendor.IBM.getFlag())) {
                for (int i = ibmCpuList.size() - 1; i >= 0; i--) {
                    if (checkIfFlagsContainsCpuFlags(ibmCpuList.get(i), lstFlags)) {
                        result = ibmCpuList.get(i);
                        break;
                    }
                }
            } else if (lstFlags.contains(CpuVendor.IBMS390.getFlag())) {
                for (int i = s390CpuList.size() - 1; i >= 0; i--) {
                    if (checkIfFlagsContainsCpuFlags(s390CpuList.get(i), lstFlags)) {
                        result = s390CpuList.get(i);
                        break;
                    }
                }
            }
            return result;
        }




        /**
         * Returns a list with all CPU's which are with a lower CPU level than the given CPU.
         *
         * @return list of supported CPUs.
         */
        public List<ServerCpu> getSupportedServerCpuList(String maxCpuName) {

            List<ServerCpu> supportedCpus = new ArrayList<>();
            if (intelCpuByNameDictionary.containsKey(maxCpuName)) {
                ServerCpu selected = intelCpuByNameDictionary.get(maxCpuName);
                int selectedCpuIndex = intelCpuList.indexOf(selected);
                for (int i = 0; i <= selectedCpuIndex; i++) { // list is sorted by level
                    supportedCpus.add(intelCpuList.get(i));
                }
            } else if (ibmCpuByNameDictionary.containsKey(maxCpuName)) {
                    ServerCpu selected = ibmCpuByNameDictionary.get(maxCpuName);
                    int selectedCpuIndex =ibmCpuList.indexOf(selected);
                    for (int i = 0; i <= selectedCpuIndex; i++) {
                        supportedCpus.add(ibmCpuList.get(i));
                    }
            } else if (amdCpuByNameDictionary.containsKey(maxCpuName)) {
                ServerCpu selected = amdCpuByNameDictionary.get(maxCpuName);
                int selectedCpuIndex =amdCpuList.indexOf(selected);
                for (int i = 0; i <= selectedCpuIndex; i++) {
                    supportedCpus.add(amdCpuList.get(i));
                }
            } else if (s390CpuByNameDictionary.containsKey(maxCpuName)) {
                ServerCpu selected = s390CpuByNameDictionary.get(maxCpuName);
                int selectedCpuIndex = s390CpuList.indexOf(selected);
                for (int i = 0; i <= selectedCpuIndex; i++) {
                    supportedCpus.add(s390CpuList.get(i));
                }
            }
            return supportedCpus;

        }
    }

    public int compareCpuLevels(String cpuName1, String cpuName2, Version ver) {
        final CpuFlagsManager cpuFlagsManager = managersDictionary.get(ver);
        ServerCpu server1 = null;
        ServerCpu server2 = null;
        if (cpuFlagsManager != null) {
            server1 = cpuFlagsManager.getServerCpuByName(cpuName1);
            server2 = cpuFlagsManager.getServerCpuByName(cpuName2);
        }
        int server1Level = (server1 != null) ? server1.getLevel() : 0;
        int server2Level = (server2 != null) ? server2.getLevel() : 0;
        return server1Level - server2Level;
    }

    public boolean isCpuUpdatable(String cpuName, Version ver) {
        final CpuFlagsManager cpuFlagsManager = managersDictionary.get(ver);
        ServerCpu server = null;

        if (cpuFlagsManager != null) {
            server = cpuFlagsManager.getServerCpuByName(cpuName);
        }

        int serverLevel = (server != null) ? server.getLevel() : 0;
        return serverLevel != 0;
    }

}
