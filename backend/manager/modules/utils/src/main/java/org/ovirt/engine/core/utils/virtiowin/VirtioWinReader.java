package org.ovirt.engine.core.utils.virtiowin;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.VirtioWinLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class VirtioWinReader implements VirtioWinLoader {

    private final OsRepository osRepository = SimpleDependencyInjector.getInstance().get(OsRepository.class);

    private static Logger log = LoggerFactory.getLogger(VirtioWinReader.class);
    private static Path directoryPath = FileSystems.getDefault().getPath(Config.<String>getValue(ConfigValues.VirtioWinIsoPath));
    private static final Pattern VIRTIO_REGEX = Pattern.compile("virtio-win-(?<version>([0-9]+\\.){1,2}([0-9]+))[.\\w]*.[i|I][s|S][o|O]$");
    private List<Map<String, String>> fullAgentList;
    private String agentVersion;
    private Version lastVirtioVersion;
    private String virtioIsoName;

    @Override
    public void load() {
        try {
            File dir = directoryPath.toFile();
            if (!dir.exists()) {
                log.warn("Directory '{}' doesn't exist.", dir.getPath());
            } else {
                File[] files = dir.listFiles();
                if (files == null || files.length <= 0) {
                    return;
                } else {
                    Version newVirtioVersion = null;
                    for (File file : files) {
                        Matcher m = VIRTIO_REGEX.matcher(file.getName().toLowerCase());
                        if (m.find() && m.groupCount() > 0) {
                            newVirtioVersion = new Version(m.group(1));
                            virtioIsoName = file.getName();
                            break;
                        }
                    }
                    if (newVirtioVersion == null || lastVirtioVersion != null && newVirtioVersion.lessOrEquals(lastVirtioVersion)) {
                        return;
                    } else {
                        lastVirtioVersion = newVirtioVersion;
                        log.info("New VirtIO-Win ISO was found.");
                    }
                }

                for (File file : files) {
                    if (file.getName().equals("agents.json")) {
                        log.info("Loading file '{}'", file.getPath());
                        ObjectMapper objectMapper = new ObjectMapper();
                        // convert JSON file to map
                        Map<?, ?> map = objectMapper.readValue(file, Map.class);
                        fullAgentList = (List<Map<String, String>>) map.get("agents");
                        break;
                    }
                }
                if (fullAgentList == null) {
                    log.warn("No manifest file was found for VirtIO-Win");
                }
            }
        } catch (IOException e) {
            log.error("Couldn't read VirtIO-Win drivers");
        }
    }

    @Override
    public String getAgentVersionByOsName(int osId) {
        String lookUpArch = "x86";
        if (osRepository.get64bitOss().contains(osId)) {
            lookUpArch = "amd64";
        }
        try {
            readAgents(lookUpArch);
        } catch (Exception e) {
            log.debug("Missing agent details to the os ID: {}", osId);
        }
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

    public String getVirtioIsoName() {
        return virtioIsoName;
    }
}
