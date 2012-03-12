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

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public final class CpuFlagsManagerHandler {
    private static Map<Version, CpuFlagsManager> _managersDictionary =
            new HashMap<Version, CpuFlagsManager>();

    public static void InitDictionaries() {
        _managersDictionary.clear();
        for (Version ver : Config.<HashSet<Version>> GetValue(ConfigValues.SupportedClusterLevels)) {
            _managersDictionary.put(ver, new CpuFlagsManager(ver));
        }
    }

    public static String GetVDSVerbDataByCpuName(String name, Version ver) {
        final CpuFlagsManager cpuFlagsManager = _managersDictionary.get(ver);
        if (cpuFlagsManager != null) {
            return cpuFlagsManager.GetVDSVerbDataByCpuName(name);
        }
        return null;
    }

    public static List<ServerCpu> AllServerCpuList(Version ver) {
        final CpuFlagsManager cpuFlagsManager = _managersDictionary.get(ver);
        if (cpuFlagsManager != null) {
            return cpuFlagsManager.getAllServerCpuList();
        }
        return new ArrayList<ServerCpu>();
    }

    public static ServerCpu FindMaxServerCpu(String clusterCpuName, String serverFlags, Version ver) {
        final CpuFlagsManager cpuFlagsManager = _managersDictionary.get(ver);
        if (cpuFlagsManager != null) {
            return cpuFlagsManager.FindMaxServerCpu(clusterCpuName, serverFlags);
        }
        return null;
    }

    /**
     * Returns missing CPU flags if any, or null if the server match the cluster
     * CPU flags
     *
     * @param clusterCpuName
     * @param serverFlags
     * @param ver
     * @return list of missing CPU flags
     */
    public static List<String> missingServerCpuFlags(String clusterCpuName, String serverFlags, Version ver) {
        final CpuFlagsManager cpuFlagsManager = _managersDictionary.get(ver);
        if (cpuFlagsManager != null) {
            return cpuFlagsManager.missingServerCpuFlags(clusterCpuName, serverFlags);
        }
        return null;
    }

    public static boolean CheckIfServerAndClusterCanFit(String clusterCpuName, String serverFlags, Version ver) {
        final CpuFlagsManager cpuFlagsManager = _managersDictionary.get(ver);
        if (cpuFlagsManager != null) {
            return cpuFlagsManager.CheckIfServerAndClusterCanFit(clusterCpuName, serverFlags);
        }
        return false;
    }

    public static boolean CheckIfCpusSameManufacture(String cpuName1, String cpuName2, Version ver) {
        final CpuFlagsManager cpuFlagsManager = _managersDictionary.get(ver);
        if (cpuFlagsManager != null) {
            return cpuFlagsManager.CheckIfCpusSameManufacture(cpuName1, cpuName2);
        }
        return false;
    }

    public static boolean CheckIfCpusExist(String cpuName, Version ver) {
        final CpuFlagsManager cpuFlagsManager = _managersDictionary.get(ver);
        if (cpuFlagsManager != null) {
            return cpuFlagsManager.CheckIfCpusExist(cpuName);
        }
        return false;
    }

    /**
     * Returns all CPU lower than the given CPU INCLUDING IT !!!
     *
     * @param cpuName
     * @return
     */
    public static List<ServerCpu> GetAllServerCpusBelowCpu(String cpuName, Version ver) {
        final CpuFlagsManager cpuFlagsManager = _managersDictionary.get(ver);
        if (cpuFlagsManager != null) {
            return cpuFlagsManager.GetAllServerCpusBelowCpu(cpuName);
        }
        return new ArrayList<ServerCpu>();
    }

    /**
     * Finds max server cpu by server cpu flags only
     *
     * @param flags
     * @return
     */
    public static ServerCpu FindMaxServerCpuByFlags(String flags, Version ver) {
        final CpuFlagsManager cpuFlagsManager = _managersDictionary.get(ver);
        if (cpuFlagsManager != null) {
            return cpuFlagsManager.FindMaxServerCpuByFlags(flags);
        }
        return null;
    }

    private static class CpuFlagsManager {
        private List<ServerCpu> _intelCpuList;
        private List<ServerCpu> _amdCpuList;
        private List<ServerCpu> _allCpuList = new ArrayList<ServerCpu>();
        private Map<String, ServerCpu> _intelCpuByNameDictionary =
                new HashMap<String, ServerCpu>();
        private Map<String, ServerCpu> _amdCpuByNameDictionary =
                new HashMap<String, ServerCpu>();
        private final String _intelFlag = "vmx";

        public CpuFlagsManager(Version ver) {
            InitDictionaries(ver);
        }

        public ServerCpu getServerCpuByName(String cpuName) {
            ServerCpu result = null;
            if (cpuName != null) {
                result = _intelCpuByNameDictionary.get(cpuName);
                if (result == null) {
                    result = _amdCpuByNameDictionary.get(cpuName);
                }
            }
            return result;
        }

        @SuppressWarnings("synthetic-access")
        public void InitDictionaries(Version ver) {
            // init dictionaries
            _intelCpuByNameDictionary.clear();
            _amdCpuByNameDictionary.clear();
            _allCpuList.clear();

            String[] cpus = Config.<String> GetValue(ConfigValues.ServerCPUList, ver.toString()).split("[;]", -1);
            for (String cpu : cpus) {
                if (!StringHelper.isNullOrEmpty(cpu)) {
                    // [0]-level, [1]-name, [2]-flags, [3]-verb
                    final String[] info = cpu.split("[:]", -1);
                    if (info.length == 4) {
                        // if no flags at all create new list instead of split
                        HashSet<String> flgs =
                                (StringHelper.isNullOrEmpty(info[2])) ? new HashSet<String>()
                                        : new HashSet<String>(Arrays.asList(info[2].split("[,]", -1)));

                        ServerCpu sc = new ServerCpu(info[1], Integer.parseInt(info[0].trim()), flgs, info[3]);
                        if (sc.getFlags().contains(_intelFlag)) {
                            _intelCpuByNameDictionary.put(sc.getCpuName(), sc);
                        } else {
                            _amdCpuByNameDictionary.put(sc.getCpuName(), sc);
                        }
                        _allCpuList.add(sc);
                    } else {
                        log.errorFormat("Error getting info for CPU: {0}, not in expected format.", cpu);
                    }
                }
            }
            _intelCpuList = new ArrayList<ServerCpu>(_intelCpuByNameDictionary.values());
            _amdCpuList = new ArrayList<ServerCpu>(_amdCpuByNameDictionary.values());

            Comparator<ServerCpu> cpuComparator = new Comparator<ServerCpu>() {
                @Override
                public int compare(ServerCpu o1, ServerCpu o2) {
                    return Integer.valueOf(o1.getLevel()).compareTo(o2.getLevel());
                }
            };

            // Sort by the highest cpu level so the highest cpu match will be
            // selected first
            Collections.sort(_intelCpuList, cpuComparator);
            Collections.sort(_amdCpuList, cpuComparator);
        }

        public String GetVDSVerbDataByCpuName(String name) {
            String result = null;
            ServerCpu sc = null;
            if (name != null) {
                if ((sc = _intelCpuByNameDictionary.get(name)) != null
                        || (sc = _amdCpuByNameDictionary.get(name)) != null) {
                    result = sc.getVdsVerbData();
                }
            }
            return result;
        }

        public List<ServerCpu> getAllServerCpuList() {
            return _allCpuList;
        }

        /**
         * Finds max server cpu by cluster name and server cpu flags
         *
         * @param clusterCpuName
         * @param serverFlags
         * @return
         */
        public ServerCpu FindMaxServerCpu(String clusterCpuName, String serverFlags) {
            ServerCpu result = null;
            ServerCpu clusterCpu = null;
            // if there are flags but no cluster or cant find cluster
            if (!StringHelper.isNullOrEmpty(serverFlags) && clusterCpuName != null) {
                if (!((clusterCpu = _intelCpuByNameDictionary.get(clusterCpuName)) != null)
                        && !((clusterCpu = _amdCpuByNameDictionary.get(clusterCpuName)) != null)) {
                    result = FindMaxServerCpuByFlags(serverFlags);
                } else {
                    HashSet<String> lstFlags = new HashSet<String>(
                            Arrays.asList(serverFlags.split("[,]", -1)));

                    // check if to search in intel or amd
                    result =
                            (lstFlags.contains(_intelFlag)) ? FindServerCpuByFlags(lstFlags, clusterCpu, _intelCpuList)
                                    : FindServerCpuByFlags(lstFlags, clusterCpu, _amdCpuList);
                }
            }
            return result;
        }

        private ServerCpu FindServerCpuByFlags(Set<String> lstFlags, ServerCpu clusterCpu,
                List<ServerCpu> fullList) {
            ServerCpu result = null;

            int i;
            // check if server ok with cluster
            if (CheckIfFlagsContainsCpuFlags(clusterCpu, lstFlags)) {
                // then should look up
                for (i = fullList.indexOf(clusterCpu) + 1; i < fullList.size(); i++) {
                    if (!CheckIfFlagsContainsCpuFlags(fullList.get(i), lstFlags)) {
                        break;
                    }
                }
                result = fullList.get(i - 1);
            } else {
                // then should look down
                for (i = fullList.indexOf(clusterCpu) - 1; i >= 0; i--) {
                    if (CheckIfFlagsContainsCpuFlags(fullList.get(i), lstFlags)) {
                        break;
                    }
                }
                // If i is lower than 0 then server cpu could not found
                if (i >= 0) {
                    result = fullList.get(i);
                }
            }
            return result;
        }

        /**
         * Returns missing CPU flags if any, or null if the server match the
         * cluster CPU flags
         *
         * @param clusterCpuName
         * @param serverFlags
         * @return list of missing CPU flags
         */
        public List<String> missingServerCpuFlags(String clusterCpuName, String serverFlags) {
            ServerCpu clusterCpu = null;
            List<String> missingFlags = null;

            HashSet<String> lstServerflags =
                    (StringHelper.isNullOrEmpty(serverFlags)) ? new HashSet<String>()
                            : new HashSet<String>(Arrays.asList(serverFlags.split("[,]", -1)));

            // first find cluster cpu
            if (clusterCpuName != null
                    && ((clusterCpu = _intelCpuByNameDictionary.get(clusterCpuName)) != null || (clusterCpu =
                            _amdCpuByNameDictionary
                                    .get(clusterCpuName)) != null)) {
                for (String flag : clusterCpu.getFlags()) {
                    if (!lstServerflags.contains(flag)) {
                        if (missingFlags == null) {
                            missingFlags = new ArrayList<String>();
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
         *
         * @param clusterCpu
         * @param lstServerflags
         * @return
         */
        private boolean CheckIfFlagsContainsCpuFlags(ServerCpu clusterCpu, Set<String> lstServerflags) {
            return CollectionUtils.intersection(clusterCpu.getFlags(), lstServerflags).size() == clusterCpu.getFlags()
                    .size();
        }

        /**
         * This method only check if server and cluster have the same cpu (intel
         * or amd)
         *
         * @param clusterCpuName
         * @param serverFlags
         * @return
         */
        public boolean CheckIfServerAndClusterCanFit(String clusterCpuName, String serverFlags) {
            if (clusterCpuName == null) {
                return false;
            } else {
                if (StringHelper.isNullOrEmpty(serverFlags)) {
                    return true;
                } else {
                    Set<String> lstServerflags = new HashSet<String>(
                            Arrays.asList(serverFlags.split("[,]", -1)));

                    return (lstServerflags.contains(_intelFlag)) ? _intelCpuByNameDictionary
                            .containsKey(clusterCpuName) : _amdCpuByNameDictionary.containsKey(clusterCpuName);
                }
            }
        }

        /**
         * This method returns true if the given cpus are from the same
         * manufature (intel or amd)
         *
         * @param cpuName1
         * @param cpuName2
         * @return
         */
        public boolean CheckIfCpusSameManufacture(String cpuName1, String cpuName2) {
            boolean result = false;
            if (cpuName1 != null && cpuName2 != null) {
                result = (_intelCpuByNameDictionary.containsKey(cpuName1)) ? _intelCpuByNameDictionary
                        .containsKey(cpuName2) : _amdCpuByNameDictionary.containsKey(cpuName2);
            }
            return result;
        }

        public boolean CheckIfCpusExist(String cpuName) {
            return cpuName != null
                    && (_intelCpuByNameDictionary.containsKey(cpuName) || _amdCpuByNameDictionary.containsKey(cpuName));
        }

        /**
         * Returns all CPU lower than the given CPU INCLUDING IT !!!
         *
         * @param cpuName
         * @return
         */
        public List<ServerCpu> GetAllServerCpusBelowCpu(String cpuName) {
            List<ServerCpu> result = new ArrayList<ServerCpu>();
            if (cpuName != null) {
                ServerCpu sc = null;
                // find server cpu object
                if ((sc = _intelCpuByNameDictionary.get(cpuName)) != null) {
                    result = _intelCpuList.subList(0, _intelCpuList.indexOf(sc) + 1);
                } else if ((sc = _amdCpuByNameDictionary.get(cpuName)) != null) {
                    result = _amdCpuList.subList(0, _amdCpuList.indexOf(sc) + 1);
                }
            }
            return result;
        }

        /**
         * Finds max server cpu by server cpu flags only
         *
         * @param flags
         * @return
         */
        public ServerCpu FindMaxServerCpuByFlags(String flags) {
            ServerCpu result = null;
            HashSet<String> lstFlags = (StringHelper.isNullOrEmpty(flags)) ? new HashSet<String>()
                    : new HashSet<String>(Arrays.asList(flags.split("[,]", -1)));

            if (lstFlags.contains(_intelFlag)) {
                for (int i = _intelCpuList.size() - 1; i >= 0; i--) {
                    if (CheckIfFlagsContainsCpuFlags(_intelCpuList.get(i), lstFlags)) {
                        result = _intelCpuList.get(i);
                        break;
                    }
                }
            } else {
                for (int i = _amdCpuList.size() - 1; i >= 0; i--) {
                    if (CheckIfFlagsContainsCpuFlags(_amdCpuList.get(i), lstFlags)) {
                        result = _amdCpuList.get(i);
                        break;
                    }
                }
            }
            return result;
        }

    }

    public static int compareCpuLevels(String cpuName1, String cpuName2, Version ver) {
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

    private static final Log log = LogFactory.getLog(CpuFlagsManagerHandler.class);

}
