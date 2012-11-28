package org.ovirt.engine.core.utils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import org.apache.commons.lang.CharSet;
import org.apache.commons.lang.RandomStringUtils;

/**
 * <code>RandomUtils</code> is a singleton class with more powerful random generating methods.
 * <P>
 * Useful methods include:
 * <UL>
 * <LI><code>nextXXX()</code> for the <tt>byte</tt> and <tt>short</tt> types.
 * <LI><code>nextXXX(mod)</code> for all types.
 * <LI><code>nextXXX(min, max)</code> for all types.
 * <LI><code>pickRandom()</code> - picks a random element from a given <code>List</code> or <code>Set</code>.
 * </UL>
 *
 * @see java.util.Random
 */
@SuppressWarnings("serial")
public final class RandomUtils extends Random {

    /* --- Class constants --- */

    /** Error message for a case when the min is larger than max, */
    private static final String MIN_MAX_ERROR = "min must be less than or equal to max";

    /** The first printable character. */
    private static final char FIRST_PRINTABLE_CHAR = ' ';

    /** The last printable character. */
    private static final char LAST_PRINTABLE_CHAR = '~';

    /** The first XML printable character. */
    private static final char FIRST_XML_PRINTABLE_CHAR = 'A';

    /** The last XML printable character. */
    private static final char LAST_XML_PRINTABLE_CHAR = 'Z';

    /** The legal characters for an entity name. */
    private static final char[] LEGAL_PROPERTY_CHARS =
            (CharSet.ASCII_ALPHA.toString() + CharSet.ASCII_NUMERIC.toString() + "_.").toCharArray();

    /* --- Class Fields --- */

    /** The single(ton) instance. */
    private static RandomUtils instance = new RandomUtils();

    /* --- Instance Fields --- */

    /** The seed that was last set. */
    private long seed;

    /* --- Constructor --- */

    /**
     * Private constructor so that only we can instantiate the instance.
     */
    private RandomUtils() {
    }

    /* --- Singleton-related Methods --- */

    /**
     * Returns the single(ton) instance.
     */
    public static RandomUtils instance() {
        return instance;
    }

    /**
     * Returns the single(ton) instance set with the given seed.
     */
    public static RandomUtils instance(long seed) {
        instance.setSeed(seed);

        return instance;
    }

    /* --- Seed-related Methods --- */

    /**
     * The last seed is saved, so it is possible to {@link #getSeed()} it later. Since {@link Random}'s
     * seed is private, I am obliged to save my own copy.
     *
     * See {@link Random#setSeed(long)}.
     */
    @Override
    public synchronized void setSeed(long seed) {
        super.setSeed(seed);
        this.seed = seed;
    }

    /**
     * Returns the seed that was set last.
     */
    public synchronized long getSeed() {
        return seed;
    }

    /* --- Byte-related Methods --- */

    /**
     * Randomizes a <tt>byte</tt> value.
     */
    public byte nextByte() {
        return (byte) super.nextInt();
    }

    /**
     * Randomize a <tt>byte</tt> value between 0 (inclusive) and the specified value (exclusive).
     */
    public byte nextByte(byte b) {
        return (byte) super.nextInt(b);
    }

    /**
     * Randomize a <tt>byte</tt> value in the given range [min, max].
     */
    public byte nextByte(byte min, byte max) {
        if (min > max) {
            throw new IllegalArgumentException(
                    MIN_MAX_ERROR);
        }

        return (byte) (min + nextByte((byte) (max - min + 1)));
    }

    /* --- Short-related Methods --- */

    /**
     * Randomizes a <tt>short</tt> value.
     */
    public short nextShort() {
        return (short) super.nextInt();
    }

    /**
     * Randomize a <tt>short</tt> value between 0 (inclusive) and the specified value (exclusive).
     */
    public short nextShort(short s) {
        return (short) super.nextInt(s);
    }

    /**
     * Randomize a <tt>short</tt> value in the given range [min, max].
     */
    public short nextShort(short min, short max) {
        if (min > max) {
            throw new IllegalArgumentException(
                    MIN_MAX_ERROR);
        }

        return (short) (min + nextShort((short) (max - min + 1)));
    }

    /* --- Integer-related Methods --- */

    /**
     * Randomize an <tt>int</tt> value in the given range [min, max].
     */
    public int nextInt(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException(
                    MIN_MAX_ERROR);
        }

        return (min + super.nextInt(max - min + 1));
    }

    /* --- Long-related Methods --- */

    /**
     * Randomize a <tt>long</tt> value between 0 (inclusive) and the specified value (exclusive).
     */
    public long nextLong(long l) {
        if (l <= 0) {
            throw new IllegalArgumentException("l must be greater than 0!");
        }

        long rand = super.nextLong();
        if (rand == Long.MIN_VALUE) {
            rand++;
        }
        return (Math.abs(rand) % l);
    }

    /**
     * Randomize a <tt>long</tt> value in the given range [min, max].
     */
    public long nextLong(long min, long max) {
        if (min > max) {
            throw new IllegalArgumentException(
                    MIN_MAX_ERROR);
        }

        return (min + nextLong(max - min + 1));
    }

    /* --- Float-related Methods --- */

    /**
     * Randomize a <tt>float</tt> value between 0.0 (inclusive) and the specified value (exclusive).
     */
    public float nextFloat(float f) {
        return nextFloat(f, false);
    }

    /**
     * Randomize a <tt>float</tt> value between 0.0 (inclusive) and the specified value (inclusive or exclusive as
     * required).
     *
     * @param inclusive
     *            Whether or not, the returned value should include the given one.
     */
    public float nextFloat(float f, boolean inclusive) {
        if (f <= 0.0F) {
            throw new IllegalArgumentException("f must be greater than 0!");
        }

        // Randomize a float
        float rand = super.nextFloat();

        // If the returned value should not include the given one,
        // make sure that the randomized float is not exactly 1.0
        if (!inclusive) {
            while (rand == 1.0F) {
                rand = super.nextFloat();
            }
        }

        return (rand * f);
    }

    /**
     * Randomize a <tt>float</tt> value in the given range [min, max].
     */
    public float nextFloat(float min, float max) {
        if (min > max) {
            throw new IllegalArgumentException(
                    MIN_MAX_ERROR);
        }

        return (min + nextFloat(max - min, true));
    }

    /* --- Double-related Methods --- */

    /**
     * Randomize a <tt>double</tt> value between 0.0 (inclusive) and the specified value (exclusive).
     */
    public double nextDouble(double d) {
        return nextDouble(d, false);
    }

    /**
     * Randomize a <tt>double</tt> value between 0.0 (inclusive) and the specified value (inclusive or exclusive as
     * required).
     *
     * @param inclusive
     *            Whether or not, the returned value should include the given one.
     */
    public double nextDouble(double d, boolean inclusive) {
        if (d <= 0.0D) {
            throw new IllegalArgumentException("d must be greater than 0!");
        }

        // Randomize a double
        double rand = super.nextDouble();

        // If the returned value should not include the given one,
        // make sure that the randomized float is not exactly 1.0
        if (!inclusive) {
            while (rand == 1.0D) {
                rand = super.nextDouble();
            }
        }

        return (rand * d);
    }

    /* --- Collections-related Methods --- */

    /**
     * Picks a random element from the given <code>Collection</code>.
     */
    public <T> T pickRandom(Collection<T> c) {
        int elementIndex = super.nextInt(c.size());

        Iterator<T> iter = c.iterator();
        for (int i = 0; i < elementIndex; ++i) {
            iter.next();
        }

        return iter.next();
    }

    /* --- Array-related Methods --- */

    /**
     * Picks a random element from the given array.
     */
    public <T> T pickRandom(T[] o) {
        return pickRandom(Arrays.asList(o));
    }

    /* --- String-related Methods --- */

    /**
     * Randomize a <code>String</code>.
     *
     * @param length
     *            The requested length of the string.
     * @param printable
     *            Whether or not, the string should contain only printable characters.
     */
    public String nextString(int length, boolean printable) {
        if (printable) {
            byte[] data = new byte[length];

            for (int i = 0; i < length; ++i) {
                data[i] = (byte) nextInt(
                        FIRST_PRINTABLE_CHAR,
                        LAST_PRINTABLE_CHAR);
            }

            return new String(data);

        }
        return new String(nextBytes(length));
    }

    /**
     * Randomize a valid numeric string.
     *
     * @param length
     *            The requested length of the string.
     */
    public String nextNumericString(int length) {
        return Long.toString(nextLong(
                (long) Math.pow(10, length - 1), (long) (Math.pow(10, length) - 1)));
    }

    /**
     * Randomize a valid XML Element name.
     *
     * @param length
     *            The requested length of the string.
     */
    public String nextXmlString(int length) {
        byte[] data = new byte[length];
        for (int i = 0; i < length; ++i) {
            data[i] = (byte) nextInt(
                    FIRST_XML_PRINTABLE_CHAR,
                    LAST_XML_PRINTABLE_CHAR);
        }

        return new String(data);
    }

    /**
     * Randomize a valid entity name.
     *
     * @param length
     *            The requested length of the string.
     */
    public String nextPropertyString(int length) {
        return nextString(length, LEGAL_PROPERTY_CHARS);
    }

    /**
     * Randomize a printable <code>String</code>.
     *
     * @param length
     *            The requested length of the string.
     */
    public String nextString(int length) {
        return nextString(length, true);
    }

    /**
     * Randomize a <code>String</code> made up of the given characters
     *
     * @param length
     *            The requested length of the string.
     */
    public String nextString(int length, char[] chars) {
        return RandomStringUtils.random(length, 0, chars.length, false, false, chars, this);
    }

    /**
     * Randomize a <code>String</code> of a length in the given range [min, max].
     *
     * @param printable
     *            Whether or not, the string should contain only printable characters.
     */
    public String nextString(int min, int max, boolean printable) {
        return nextString(nextInt(min, max), printable);
    }

    /**
     * Randomize a printable <code>String</code> of a length in the given range [min, max].
     */
    public String nextString(int min, int max) {
        return nextString(nextInt(min, max), true);
    }

    /* --- General Utility Methods --- */

    /**
     * Creates a <tt>byte</tt> array of the specified size, initialized with random values.
     */
    public byte[] nextBytes(int size) {
        byte[] data = new byte[size];
        nextBytes(data);
        return data;
    }

    /* -- Big Integer related methods -- */

    /**
     * generates a new big integer with the desired number of bits the generated number will always be positive.
     *
     * @param numOfBits
     *            the number of bits of the Big Integer
     * @return the randomized big integer.
     */
    public BigInteger nextBigInt(int numOfBits) {
        return new BigInteger(numOfBits, this);
    }

    /**
     * Returns a random value from an enum.
     *
     * @param <T>
     *            The enum type.
     * @param enumClass
     *            The enum class to randomize.
     *
     * @return A random enum from the given enum, or null if got null.
     */
    public <T extends Enum<?>> T nextEnum(Class<T> enumClass) {
        if (enumClass == null) {
            return null;
        }

        return pickRandom(enumClass.getEnumConstants());
    }
}
