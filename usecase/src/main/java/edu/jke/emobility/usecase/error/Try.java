package edu.jke.emobility.usecase.error;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Covers the result of a potentially failing operation.
 *
 * A try object contains either the successful result of an operation or its error object, e.g. exception.
 * Instead of throwing the execption and thus separate the path of successful and failing results, the
 * Try object covers both cases.
 *
 * Example:
 * <pre>
 *      Try<String,Throwable> result = Try.failable(this::operation);
 *      ...
 *      result
 *          .map(value -> to-another-form)
 *          .onSuccess(otherValue -> do-something-with-it)
 *          .onFailure(error -> handle-that-also);
 * </pre>
 */
public abstract class Try<V,E> {

    public static <V> Try<V,Throwable> success(V value) {
        return success(value, ex -> ex);
    }

    public static <V,E> Try<V,E> success(V value, Function<Throwable,E> errorMapper) {
        requireNonNull(value);
        requireNonNull(errorMapper);
        return new Success<>(value, errorMapper);
    }

    public static <V,E> Try<V,E> failure(E error) {
        requireNonNull(error);
        return new Failure<>(error);
    }

    @FunctionalInterface
    public interface CheckedSupplier<T,Ex extends Throwable> {
        T get() throws Ex;
    }
    public static <V> Try<V,Throwable> failable(CheckedSupplier<V,Throwable> operation) {
        return failable(operation, ex -> ex);
    }

    public static <V,E> Try<V,E> failable(CheckedSupplier<V,Throwable> operation, Function<Throwable,E> errorMapper) {
        requireNonNull(operation);
        requireNonNull(errorMapper);
        V value;
        try {
            value = operation.get();
        } catch (Throwable t) {
            return Try.failure(errorMapper.apply(t));
        }
        return Try.success(value, errorMapper);
    }

    public abstract <U> Try<U,E> map(Function<? super V, ? extends U> f);

    public abstract <U> Try<U,E> flatMap(Function<? super V, Try<U,E>> f);

    public Try<V,E> onSuccess(Consumer<V> consumer) { return this; }

    public Try<V,E> onFailure(Consumer<E> errorConsumer) { return this; }

    public boolean isSuccess() { return false; }

    public boolean isFailure() { return false; }

    public abstract V get();

    public abstract V orElse(V defaultValue);

    public abstract E error();

    @SafeVarargs
    public static <C,E> Try<C, Collection<E>> aggregate(Supplier<C> producer, Function<Throwable,Collection<E>> errorMapper, Try<?,E>... components) {
        if (Arrays.stream(components).allMatch(Try::isSuccess)) {
            return Try.success(producer.get(), errorMapper);
        } else {
            List<E> allErrors = Arrays.stream(components)
                    .filter(Try::isFailure)
                    .map(Try::error)
                    .collect(Collectors.toList());
            return Try.failure(allErrors);
        }
    }

    /** Success case */
    private static class Success<V,E> extends Try<V,E> {
        private final V value;
        private final Function<Throwable, E> errorMapper;

        public Success(V value, Function<Throwable,E> errorMapper) {
            this.value = value;
            this.errorMapper = errorMapper;
        }

        @Override
        public Try<V,E> onSuccess(Consumer<V> consumer) {
            consumer.accept(value);
            return this;
        }

        @Override
        public <U> Try<U,E> map(Function<? super V, ? extends U> f) {
            requireNonNull(f);
            try {
                return Try.success(f.apply(value), errorMapper);
            } catch (Throwable t) {
                return Try.failure(errorMapper.apply(t));
            }
        }

        @Override
        public <U> Try<U,E> flatMap(Function<? super V, Try<U,E>> f) {
            requireNonNull(f);
            try {
                return f.apply(value);
            } catch (Throwable t) {
                return Try.failure(errorMapper.apply(t));
            }
        }

        @Override
        public boolean isSuccess() { return true; }

        @Override
        public V get() { return value; }

        @Override
        public V orElse(V defaultValue) { return value; }

        @Override
        public E error() { return null; }
    }

    /** Failure case */
    private static class Failure<V,E> extends Try<V,E> {
        private final E error;

        public Failure(E error) {
            this.error = error;
        }

        @Override
        public Try<V,E> onFailure(Consumer<E> consumer) {
            consumer.accept(error);
            return this;
        }

        @Override
        public <U> Try<U,E> map(Function<? super V, ? extends U> f) {
            requireNonNull(f);
            return Try.failure(error);
        }

        @Override
        public <U> Try<U,E> flatMap(Function<? super V, Try<U,E>> f) {
            requireNonNull(f);
            return Try.failure(error);
        }

        @Override
        public boolean isFailure() { return true; }

        @Override
        public V get() {
            // sadly, proper pattern matching is still experimental in Java 17
            if (error instanceof RuntimeException) {
                throw (RuntimeException)error;
            } else if (error instanceof Throwable) {
                throw new RuntimeException((Throwable)error);
            } else {
                throw new RuntimeException(error.toString());
            }
        }

        @Override
        public V orElse(V defaultValue) { return defaultValue; }

        @Override
        public E error() { return error; }
    }

}
