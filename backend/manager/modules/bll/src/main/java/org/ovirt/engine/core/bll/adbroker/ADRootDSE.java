package org.ovirt.engine.core.bll.adbroker;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;



/**
 * Holds information queried from a root DSE
 */
public class ADRootDSE implements RootDSE {


    private boolean mixedFunctionalityLevel;
    private String defaultNamingContext;

    public ADRootDSE() {
    }

    public ADRootDSE(boolean mixedFunctionalityLevel, String defaultNamingContext) {
        this.mixedFunctionalityLevel = mixedFunctionalityLevel;
        this.defaultNamingContext = defaultNamingContext;
    }

    public ADRootDSE(Attributes rootDseRecords) throws NumberFormatException, NamingException {
        boolean mixedMode =
                Integer.parseInt(rootDseRecords.get(ADRootDSEAttributes.domainControllerFunctionality.name()).get().toString()) == 4;
        String namingContext = rootDseRecords.get(ADRootDSEAttributes.defaultNamingContext.name()).get().toString();
        this.mixedFunctionalityLevel = mixedMode;
        this.defaultNamingContext = namingContext;
    }

    @Override
    public void setDefaultNamingContext(String defaultNamingContext) {
        this.defaultNamingContext = defaultNamingContext;
    }

    @Override
    public String getDefaultNamingContext() {
        return defaultNamingContext;
    }

    public void setMixedFunctionalityLevel(boolean mixedFunctionalityLevel) {
        this.mixedFunctionalityLevel = mixedFunctionalityLevel;
    }

    public boolean isMixedFunctionalityLevel() {
        return mixedFunctionalityLevel;
    }

}
