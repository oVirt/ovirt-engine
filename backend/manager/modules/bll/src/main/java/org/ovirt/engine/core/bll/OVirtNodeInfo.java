package org.ovirt.engine.core.bll;

import java.io.File;
import java.util.List;
import java.util.LinkedList;
import java.util.regex.Pattern;

import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

public class OVirtNodeInfo {

    public static class Entry {
        public Pattern isoPattern;
        public File path;
        public String minimumVersion;
        public Pattern osPattern;
    }

    private static final Log log = LogFactory.getLog(OVirtNodeInfo.class);
    private static final String delimiter = ":";
    private static volatile OVirtNodeInfo instance;

    private List<Entry> info;

    public static synchronized OVirtNodeInfo getInstance() {
        if (instance == null) {
            instance = new OVirtNodeInfo();
        }
        else {
            if (instance == null) {
                synchronized(OVirtNodeInfo.class) {
                    if (instance == null) {
                        instance = new OVirtNodeInfo();
                    }
                }
            }
        }
        return instance;
    }

    public static void clearInstance() {
        synchronized(OVirtNodeInfo.class) {
            instance = null;
        }
    }

    private OVirtNodeInfo() {
        final String[] path = Config.resolveOVirtISOsRepositoryPath().split(delimiter);
        final String minimumVersion[] = Config.<String> getValue(ConfigValues.OvirtInitialSupportedIsoVersion).split(delimiter);

        // Node prefix is part of regex to list ISOs (OvirtIsoPrefix)
        // Regex: (ovirt-node)-(.*)\.iso (used to list ISOs)
        // Prefix Found: ovirt-node
        final String regexISO[] = Config.<String> getValue(ConfigValues.OvirtIsoPrefix).split(delimiter);

        // Node OS
        final String regexNode[] = Config.<String> getValue(ConfigValues.OvirtNodeOS).split(delimiter);

        info = new LinkedList<Entry>();

        log.debugFormat("NodeInfo: regexISO length {0} minimum length {1} path length {2} regexNode {3}",
                regexISO.length, minimumVersion.length, path.length, regexNode.length);

        if (regexISO.length != minimumVersion.length ||
                regexISO.length != path.length ||
                regexISO.length != regexNode.length) {
            throw new IllegalArgumentException("Illegal NodeInfo - length");
        }

        for (int i=0; i < regexISO.length; i++) {
            log.debugFormat("NodeInfo: regexISO {0} path {1}, minimumVersion {2} regexNode {3}",
                    regexISO[i], path[i], minimumVersion[i], regexNode[i]);
            Entry entry = new Entry();
            entry.isoPattern = Pattern.compile(regexISO[i]);
            entry.osPattern = Pattern.compile(regexNode[i], Pattern.CASE_INSENSITIVE);
            entry.path = new File(path[i]);
            entry.minimumVersion = minimumVersion[i];
            info.add(entry);
        }
    }

    public List<Entry> get() {
        return info;
    }
}
