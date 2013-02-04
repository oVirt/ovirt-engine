package org.ovirt.engine.ui.uicommonweb.builders;

import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.ILogger;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

class TestingFrontendModel extends EntityModel {

    private String property1;

    private String property2;

    public TestingFrontendModel(String property1, String property2) {
        super();
        this.property1 = property1;
        this.property2 = property2;
    }

    public String getProperty1() {
        return property1;
    }

    public void setProperty1(String property1) {
        this.property1 = property1;
    }

    public String getProperty2() {
        return property2;
    }

    public void setProperty2(String property2) {
        this.property2 = property2;
    }

    @Override
    protected Configurator lookupConfigurator() {
        return null;
    }

    @Override
    protected ILogger lookupLogger() {
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((property1 == null) ? 0 : property1.hashCode());
        result = prime * result + ((property2 == null) ? 0 : property2.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TestingFrontendModel other = (TestingFrontendModel) obj;
        if (property1 == null) {
            if (other.property1 != null)
                return false;
        } else if (!property1.equals(other.property1))
            return false;
        if (property2 == null) {
            if (other.property2 != null)
                return false;
        } else if (!property2.equals(other.property2))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "FrontendModel [property1=" + property1 + ", property2=" + property2 + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}
