package org.ovirt.engine.ui.uicommonweb;

import org.ovirt.engine.core.compat.Version;

/**
 * Contains extension methods for some common operation.
 */
@SuppressWarnings("unused")
public final class Extensions
{
    /**
     * Separates a selected strings (by selector expression) with a separator character.
     */
    // public static string Separate<TSource>(this IEnumerable<TSource> source, char separator, Func<TSource, string>
    // selector)
    // {
    // string str = source.Select(selector).Aggregate(String.Empty, (c, d) => c += d + separator);
    // if (!String.IsNullOrEmpty(str))
    // {
    // str = str.Remove(str.Length - 1);
    // }

    // return str;
    // }

    /**
     * Flattens an exception tree, exception itself and all inner ones.
     */
    public static Iterable<RuntimeException> Flatten(RuntimeException source)
    {
        java.util.ArrayList<RuntimeException> result = new java.util.ArrayList<RuntimeException>();
        RuntimeException ex = source;

        // TODO: Can't convert to Java.
        // while (ex != null)
        // {
        // result.Add(ex);
        // ex = ex.InnerException;
        // }

        result.add(ex);

        return result;
    }

    /**
     * Applies a specific action to each element of enumeration. Pay attention! Enumeration is done on the source itself
     * rather than on a new instance as it done in case of ToList.ForEach When using this method don't change
     * enumeration by adding of removing elements.
     */
    // public static void Each<T>(this IEnumerable<T> source, Action<T> action)
    // {
    // foreach (var item in source)
    // {
    // action(item);
    // }
    // }

    // public static void EachRecursive<T>(this T source, Func<T, IEnumerable<T>> childrenSelector, Action<T,
    // IEnumerable<T>> action)
    // {
    // var children = childrenSelector(source);
    // action(source, children);

    // if (children != null)
    // {
    // children.Each(a => a.EachRecursive(childrenSelector, action));
    // }
    // }

    /**
     * Returns a friendly version, for example 4.4.x.x returned as 2.1.x.x.
     */
    public static Version GetFriendlyVersion(Version source)
    {
        if (source != null)
        {
            int major = source.getMajor();
            int minor = source.getMinor();

            if (major == 4 && minor == 4)
            {
                major = 2;
                minor = 1;
            }
            else if (major == 4 && minor == 5)
            {
                major = 2;
                minor = 2;
            }
            else if (major == 4 && minor == 9)
            {
                major = 3;
                minor = 0;
            }
            if (source.getBuild() == -1)
            {
                return new Version(major, minor);
            }
            if (source.getRevision() == -1)
            {
                return new Version(major, minor, source.getBuild());
            }
            return new Version(major, minor, source.getBuild(), source.getRevision());
        }
        return new Version();
    }

    /**
     * Sort by comparer created at runtime
     *
     * <typeparam name="TSource"></typeparam> <typeparam name="TValue"></typeparam>
     *
     * @param source
     * @param selector
     */
    // public static void Sort<TSource, TValue>(this List<TSource> source,
    // Func<TSource, TValue> selector)
    // {
    // var comparer = Comparer<TValue>.Default;
    // source.Sort((a, b) => comparer.Compare(selector(a), selector(b)));
    // }
}
