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
    private static Map<Version, CpuFlagsManager> _managersDictionary = new HashMap<>();

    @PostConstruct
    public void initDictionaries() {
        log.info("Start initializing dictionaries");
        _managersDictionary.clear();
        for (Version ver : Config.<HashSet<Version>> getValue(ConfigValues.SupportedClusterLevels)) {
            _managersDictionary.put(ver, new CpuFlagsManager(ver));
        }
       log.info("Finished initializing dictionaries");
    }

    public String getCpuId(String name, Version ver) {
        final CpuFlagsManager cpuFlagsManager = _managersDictionary.get(ver);
        if (cpuFlagsManager != null) {
            return cpuFlagsManager.getVDSVerbDataByCpuName(name);
        }
        return null;
    }

    public String getCpuNameByCpuId(String name, Version ver) {
        final CpuFlagsManager cpuFlagsManager = _managersDictionary.get(ver);
        if (cpuFlagsManager != null) {
            return cpuFlagsManager.getCpuNameByCpuId(name);
        }
        return null;
    }

    public ArchitectureType getArchitectureByCpuName(String name, Version ver) {
        final CpuFlagsManager cpuFlagsManager = _managersDictionary.get(ver);
        if (cpuFlagsManager != null) {
            return cpuFlagsManager.getArchitectureByCpuName(name);
        }
        return null;
    }

    public List<ServerCpu> allServerCpuList(Version ver) {
        final CpuFlagsManager cpuFlagsManager = _managersDictionary.get(ver);
        if (cpuFlagsManager != null) {
            return cpuFlagsManager.getAllServerCpuList();
        }
        return new ArrayList<>();
    }

    /**
     * Returns missing CPU flags if any, or null if the server match the cluster
     * CPU flags
     *
     * @return list of missing CPU flags
     */
    public List<String> missingServerCpuFlags(String clusterCpuName, String serverFlags, Version ver) {
        final CpuFlagsManager cpuFlagsManager = _managersDictionary.get(ver);
        if (cpuFlagsManager != null) {
            return cpuFlagsManager.missingServerCpuFlags(clusterCpuName, serverFlags);
        }
        return null;
    }

    public boolean checkIfCpusSameManufacture(String cpuName1, String cpuName2, Version ver) {
        final CpuFlagsManager cpuFlagsManager = _managersDictionary.get(ver);
        if (cpuFlagsManager != null) {
            return cpuFlagsManager.checkIfCpusSameManufacture(cpuName1, cpuName2);
        }
        return false;
    }

    public boolean checkIfCpusExist(String cpuName, Version ver) {
        final CpuFlagsManager cpuFlagsManager = _managersDictionary.get(ver);
        if (cpuFlagsManager != null) {
            return cpuFlagsManager.checkIfCpusExist(cpuName);
        }
        return false;
    }

    /**
     * Finds max server cpu by server cpu flags only
     */
    public ServerCpu findMaxServerCpuByFlags(String flags, Version ver) {
        final CpuFlagsManager cpuFlagsManager = _managersDictionary.get(ver);
        if (cpuFlagsManager != null) {
            return cpuFlagsManager.findMaxServerCpuByFlags(flags);
        }
        return null;
    }


    public Version getLatestDictionaryVersion() {
        return Collections.max(_managersDictionary.keySet());

    }

    public List<ServerCpu> getSupportedServerCpuList(Version ver, String maxCpuName) {
        final CpuFlagsManager cpuFlagsManager = _managersDictionary.get(ver);
        if (cpuFlagsManager != null) {
            return cpuFlagsManager.getSupportedServerCpuList(maxCpuName);
        }
        return new ArrayList<>();

    }

    private static class CpuFlagsManager {
        private List<ServerCpu> _intelCpuList;
        private List<ServerCpu> _amdCpuList;
        private List<ServerCpu> _ibmCpuList;
        private List<ServerCpu> _allCpuList = new ArrayList<>();
        private Map<String, ServerCpu> _intelCpuByNameDictionary = new HashMap<>();
        private Map<String, ServerCpu> _amdCpuByNameDictionary = new HashMap<>();
        private Map<String, ServerCpu> _ibmCpuByNameDictionary = new HashMap<>();
        private Map<String, ServerCpu> _intelCpuByVdsNameDictionary = new HashMap<>();
        private Map<String, ServerCpu> _amdCpuByVdsNameDictionary = new HashMap<>();
        private Map<String, ServerCpu> _ibmCpuByVdsNameDictionary = new HashMap<>();

        public CpuFlagsManager(Version ver) {
            initDictionaries(ver);
        }

        public ArchitectureType getArchitectureByCpuName(String cpuName) {
            ServerCpu cpu = getServerCpuByName(cpuName);

            ArchitectureType result = ArchitectureType.undefined;

            if (cpu != null) {
                result = cpu.getArchitecture();
            }

            return result;
        }

        public ServerCpu getServerCpuByName(String cpuName) {
            ServerCpu result = null;
            if (cpuName != null) {
                result = _intelCpuByNameDictionary.get(cpuName);

                if (result == null) {
                    result = _amdCpuByNameDictionary.get(cpuName);
                }

                if (result == null) {
                    result = _ibmCpuByNameDictionary.get(cpuName);
                }
            }
            return result;
        }

        @SuppressWarnings("synthetic-access")
        public void initDictionaries(Version ver) {
            // init dictionaries
            _intelCpuByNameDictionary.clear();
            _amdCpuByNameDictionary.clear();
            _ibmCpuByNameDictionary.clear();
            _allCpuList.clear();

            String[] cpus = Config.<String> getValue(ConfigValues.ServerCPUList, ver.toString()).split("[;]", -1);
            for (String cpu : cpus) {

                if (!StringUtils.isEmpty(cpu)) {
                    // [0]-level, [1]-name, [2]-flags, [3]-verb, [4]-arch
                    final String[] info = cpu.split("[:]", -1);

                    if (info.length == 5) {
                        // if no flags at all create new list instead of split
                        HashSet<String> flgs =
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
                            _intelCpuByNameDictionary.put(sc.getCpuName(), sc);
                            _intelCpuByVdsNameDictionary.put(sc.getVdsVerbData(), sc);
                        } else if (sc.getFlags().contains(CpuVendor.AMD.getFlag())) {
                            _amdCpuByNameDictionary.put(sc.getCpuName(), sc);
                            _amdCpuByVdsNameDictionary.put(sc.getVdsVerbData(), sc);
                        } else if (sc.getFlags().contains(CpuVendor.IBM.getFlag())) {
                            _ibmCpuByNameDictionary.put(sc.getCpuName(), sc);
                            _ibmCpuByVdsNameDictionary.put(sc.getVdsVerbData(), sc);
                        }

                        _allCpuList.add(sc);
                    } else {
                        log.error("Error getting info for CPU '{}', not in expected format.", cpu);
                    }
                }
            }
            _intelCpuList = new ArrayList<>(_intelCpuByNameDictionary.values());
            _amdCpuList = new ArrayList<>(_amdCpuByNameDictionary.values());
            _ibmCpuList = new ArrayList<>(_ibmCpuByNameDictionary.values());

            Comparator<ServerCpu> cpuComparator = Comparator.comparingInt(ServerCpu::getLevel);

            // Sort by the highest cpu level so the highest cpu match will be
            // selected first
            Collections.sort(_intelCpuList, cpuComparator);
            Collections.sort(_amdCpuList, cpuComparator);
            Collections.sort(_ibmCpuList, cpuComparator);
        }

        public String getVDSVerbDataByCpuName(String name) {
            String result = null;
            ServerCpu sc = null;
            if (name != null) {
                if ((sc = _intelCpuByNameDictionary.get(name)) != null
                        || (sc = _amdCpuByNameDictionary.get(name)) != null
                        || (sc = _ibmCpuByNameDictionary.get(name)) != null) {
                    result = sc.getVdsVerbData();
                }
            }
            return result;
        }

        public String getCpuNameByCpuId(String vdsName) {
            String result = null;
            ServerCpu sc = null;
            if (vdsName != null) {
                if ((sc = _intelCpuByVdsNameDictionary.get(vdsName)) != null
                        || (sc = _amdCpuByVdsNameDictionary.get(vdsName)) != null
                        || (sc = _ibmCpuByVdsNameDictionary.get(vdsName)) != null) {
                    result = sc.getCpuName();
                }
            }
            return result;
        }

        public List<ServerCpu> getAllServerCpuList() {
            return _allCpuList;
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

            HashSet<String> lstServerflags =
                    StringUtils.isEmpty(serverFlags) ? new HashSet<>()
                            : new HashSet<>(Arrays.asList(serverFlags.split("[,]", -1)));

            // first find cluster cpu
            if (clusterCpuName != null
                    && ((clusterCpu = _intelCpuByNameDictionary.get(clusterCpuName)) != null
                            || (clusterCpu = _amdCpuByNameDictionary.get(clusterCpuName)) != null
                            || (clusterCpu = _ibmCpuByNameDictionary.get(clusterCpuName)) != null
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
            boolean result = false;
            if (cpuName1 != null && cpuName2 != null) {
                if (_intelCpuByNameDictionary.containsKey(cpuName1)) {
                    result = _intelCpuByNameDictionary.containsKey(cpuName2);
                } else if (_amdCpuByNameDictionary.containsKey(cpuName1)) {
                    result = _amdCpuByNameDictionary.containsKey(cpuName2);
                } else if (_ibmCpuByNameDictionary.containsKey(cpuName1)) {
                    result = _ibmCpuByNameDictionary.containsKey(cpuName2);
                }
            }

            return result;
        }

        public boolean checkIfCpusExist(String cpuName) {
            return cpuName != null
                    && (_intelCpuByNameDictionary.containsKey(cpuName)
                            || _amdCpuByNameDictionary.containsKey(cpuName)
                            || _ibmCpuByNameDictionary.containsKey(cpuName));
        }

        /**
         * Finds max server cpu by server cpu flags only
         */
        public ServerCpu findMaxServerCpuByFlags(String flags) {
            ServerCpu result = null;
            HashSet<String> lstFlags = StringUtils.isEmpty(flags) ? new HashSet<>()
                    : new HashSet<>(Arrays.asList(flags.split("[,]", -1)));

            if (lstFlags.contains(CpuVendor.INTEL.getFlag())) {
                for (int i = _intelCpuList.size() - 1; i >= 0; i--) {
                    if (checkIfFlagsContainsCpuFlags(_intelCpuList.get(i), lstFlags)) {
                        result = _intelCpuList.get(i);
                        break;
                    }
                }
            } else if (lstFlags.contains(CpuVendor.AMD.getFlag())) {
                for (int i = _amdCpuList.size() - 1; i >= 0; i--) {
                    if (checkIfFlagsContainsCpuFlags(_amdCpuList.get(i), lstFlags)) {
                        result = _amdCpuList.get(i);
                        break;
                    }
                }
            } else if (lstFlags.contains(CpuVendor.IBM.getFlag())) {
                for (int i = _ibmCpuList.size() - 1; i >= 0; i--) {
                    if (checkIfFlagsContainsCpuFlags(_ibmCpuList.get(i), lstFlags)) {
                        result = _ibmCpuList.get(i);
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
            if (_intelCpuByNameDictionary.containsKey(maxCpuName)) {
                ServerCpu selected = _intelCpuByNameDictionary.get(maxCpuName);
                int selectedCpuIndex = _intelCpuList.indexOf(selected);
                for (int i = 0; i <= selectedCpuIndex; i++) { // list is sorted by level
                    supportedCpus.add(_intelCpuList.get(i));
                }
            } else if (_ibmCpuByNameDictionary.containsKey(maxCpuName)) {
                    ServerCpu selected = _ibmCpuByNameDictionary.get(maxCpuName);
                    int selectedCpuIndex =_ibmCpuList.indexOf(selected);
                    for (int i = 0; i <= selectedCpuIndex; i++) {
                        supportedCpus.add(_ibmCpuList.get(i));
                    }
            } else if (_amdCpuByNameDictionary.containsKey(maxCpuName)) {
                ServerCpu selected = _amdCpuByNameDictionary.get(maxCpuName);
                int selectedCpuIndex =_amdCpuList.indexOf(selected);
                for (int i = 0; i <= selectedCpuIndex; i++) {
                    supportedCpus.add(_amdCpuList.get(i));
                }
            }
            return supportedCpus;

        }
    }

    public int compareCpuLevels(String cpuName1, String cpuName2, Version ver) {
        final CpuFlagsManager cpuFlagsManager = _managersDictionary.get(ver);
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
        final CpuFlagsManager cpuFlagsManager = _managersDictionary.get(ver);
        ServerCpu server = null;

        if (cpuFlagsManager != null) {
            server = cpuFlagsManager.getServerCpuByName(cpuName);
        }

        int serverLevel = (server != null) ? server.getLevel() : 0;
        return serverLevel != 0;
    }

}
