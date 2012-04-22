package org.ovirt.engine.ui.common.binding;

/**
 * Represents a Java statement that sets the generated DOM element ID into the corresponding field.
 */
public class ElementIdStatement {

    private final String fieldExpression;
    private final String elementId;

    public ElementIdStatement(String fieldExpression, String elementId) {
        this.fieldExpression = fieldExpression;
        this.elementId = elementId;
    }

    public String buildIdSetterStatement() {
        return String.format("setElementId(%s, \"%s\")", fieldExpression, elementId); //$NON-NLS-1$
    }

    public String buildGuardCondition() {
        StringBuilder sb = new StringBuilder();

        String[] subPaths = getSubPaths(fieldExpression);
        for (int i = 0; i < subPaths.length; i++) {
            sb.append(subPaths[i]).append(" != null"); //$NON-NLS-1$

            if (i < subPaths.length - 1) {
                sb.append(" && "); //$NON-NLS-1$
            }
        }

        return sb.toString();
    }

    String[] getSubPaths(String path) {
        String[] pathElements = path.split("\\."); //$NON-NLS-1$
        String[] result = new String[pathElements.length];

        for (int i = 0; i < pathElements.length; i++) {
            String currentElement = pathElements[i];

            if (currentElement.isEmpty()) {
                throw new IllegalStateException("Malformed path: " + path); //$NON-NLS-1$
            }

            if (i == 0) {
                result[i] = currentElement;
            } else {
                result[i] = result[i - 1] + "." + currentElement; //$NON-NLS-1$
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return elementId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((elementId == null) ? 0 : elementId.hashCode());
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
        ElementIdStatement other = (ElementIdStatement) obj;
        if (elementId == null) {
            if (other.elementId != null)
                return false;
        } else if (!elementId.equals(other.elementId))
            return false;
        return true;
    }

}
