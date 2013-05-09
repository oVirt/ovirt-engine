package org.ovirt.engine.ui.uicommonweb;

import java.util.ArrayList;

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
    public static Iterable<RuntimeException> flatten(RuntimeException source)
    {
        ArrayList<RuntimeException> result = new ArrayList<RuntimeException>();
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
