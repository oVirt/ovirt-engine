package org.ovirt.engine.core.utils.virtiowin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.utils.VirtioWinLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtioWinReader implements VirtioWinLoader {

    private final OsRepository osRepository = SimpleDependencyInjector.getInstance().get(OsRepository.class);

    private Logger log = LoggerFactory.getLogger(VirtioWinReader.class);
    private List<Map<String, String>> fullAgentList;
    private String agentVersion;

    public void init(Path directoryPath) {
        try {
            load(directoryPath);
        } catch (IOException e) {
            log.error("Couldn't read VirtIO-Win drivers");
        }
    }

    @Override
    public void load(Path directoryPath) throws IOException {
        File dir = directoryPath.toFile();
        if (dir.exists()) {
            File[] files = dir.listFiles((dir1, name) -> name.endsWith(".json"));

            if (files != null) {
                for (File file : files) {
                    log.info("Loading file '{}'", file.getPath());

                    ObjectMapper objectMapper = new ObjectMapper();
                    // convert JSON file to map
                    Map<?, ?> map = objectMapper.readValue(file, Map.class);
                    if (file.getName().equals("agents.json")) {
                        fullAgentList = (List<Map<String, String>>) map.get("agents");
                    }
                }
            } else {
                log.warn("No manifest file was found for VirtIO-Win");
            }
        } else {
            log.error("Directory '{}' doesn't exist.", dir.getPath());
        }
    }

    @Override
    public String getAgentVersionByOsName(int osId) {
        String lookUpArch = "x86";
        if (osRepository.get64bitOss().contains(osId)) {
            lookUpArch = "amd64";
        }
        readAgents(lookUpArch);
        return agentVersion;
    }

    private void readAgents(String lookUpArch) {
        for (Object o : fullAgentList) {
            Map<String, String> tmp = (Map<String, String>) o;
            String arch = tmp.get("arch");
            String agentName = tmp.get("name");
            if (arch.equals(lookUpArch) && agentName.toLowerCase().contains("qemu")) {
                for (Map.Entry<String, String> entry : tmp.entrySet()) {
                    if (entry.getKey().equals("agent_version")) {
                        // VDSM reports to the Z version.
                        agentVersion = entry.getValue().substring(0, entry.getValue().indexOf("-"));
                    }
                }
            }
        }
    }
}
