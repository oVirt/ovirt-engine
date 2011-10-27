package org.ovirt.engine.core.bll;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;

public class GetoVirtISOsQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetoVirtISOsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        java.util.ArrayList<String> filesNames = new java.util.ArrayList<String>();
        File directory = new File(Config.resolveOVirtISOsRepositoryPath());
        if (directory.isDirectory()) {
            for (File fileWithPath : directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".iso");
                }
            })) {
                filesNames.add(fileWithPath.getName());
            }
        } else {
            log.errorFormat("ovirt ISOs directory not found. Search in: {0}", directory.getPath());
        }
        Collections.sort(filesNames);
        getQueryReturnValue().setReturnValue(filesNames);
    }

    private static LogCompat log = LogFactoryCompat.getLog(GetoVirtISOsQuery.class);
}
