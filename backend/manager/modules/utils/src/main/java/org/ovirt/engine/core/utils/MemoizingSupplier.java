package org.ovirt.engine.core.utils;

import java.util.function.Supplier;

/**
 * This is similar to Google's Suppliers#MemoizingSupplier but is not thread-safe
 */
public class MemoizingSupplier<T> implements Supplier<T> {
    private final Supplier<T> delegate;
    private boolean initialized;
    private T value;

    public MemoizingSupplier(Supplier<T> delegate) {
      this.delegate = delegate;
    }

    public T get() {
      if (!initialized) {
        value = delegate.get();
        initialized = true;
      }
      return value;
    }
}
