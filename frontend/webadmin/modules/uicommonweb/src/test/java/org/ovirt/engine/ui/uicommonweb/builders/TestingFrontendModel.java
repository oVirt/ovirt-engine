package org.ovirt.engine.ui.uicommonweb.builders;

import java.util.Objects;

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
        return Objects.hash(
                property1,
                property2
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TestingFrontendModel)) {
            return false;
        }
        TestingFrontendModel other = (TestingFrontendModel) obj;
        return Objects.equals(property1, other.property1)
                && Objects.equals(property2, other.property2);
    }

    @Override
    public String toString() {
        return "FrontendModel [property1=" + property1 + ", property2=" + property2 + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}
