package org.ovirt.engine.api.restapi.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.ServerCpu;

public class ServerCpuParser {

    /* Format is <cpu>;<cpu>;...
     */
    public static List<ServerCpu> parseCpus(String str) {
        List<ServerCpu> cpus = new ArrayList<ServerCpu>();

        for (String cpu : str.split("[;]", -1)) {
            if (!cpu.isEmpty()) {
                cpus.add(parseCpu(cpu));
            }
        }

        return cpus;
    }

    /* Format is <level>:<name>:<flag>,<flag>,...:<verb>
     *
     * e.g. 3:Intel Xeon Core2:vmx,nx,model_Conroe:Conroe
     *
     */
    public static ServerCpu parseCpu(String str) {
        String[] parts = str.split("[:]", -1);

        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid CPU description: '" + str + "'");
        }

        HashSet<String> flags = new HashSet<String>();
        for (String flag : parts[2].split("[,]", -1)) {
            if (!flag.isEmpty()) {
                flags.add(flag);
            }
        }

        return new ServerCpu(parts[1],
                             Integer.parseInt(parts[0].trim()),
                             flags,
                             parts[3]);
    }
}
