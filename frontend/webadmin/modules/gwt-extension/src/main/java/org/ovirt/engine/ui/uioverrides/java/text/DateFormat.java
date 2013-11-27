package java.text;

public class DateFormat {

    /** DateFormat styling constants */

    /**
     * Constant for full style pattern.
     */
    public static final int FULL = 0;
    /**
     * Constant for long style pattern.
     */
    public static final int LONG = 1;
    /**
     * Constant for medium style pattern.
     */
    public static final int MEDIUM = 2;
    /**
     * Constant for short style pattern.
     */
    public static final int SHORT = 3;
    /**
     * Constant for default style pattern.  Its value is MEDIUM.
     */
    public static final int DEFAULT = MEDIUM;

    public String format(java.util.Date date) {
        // No-op, don't use this method in client code
        return null;
    }

    public void setTimeZone(java.util.TimeZone zone) {
        // No-op, don't use this method in client code
    }

}
