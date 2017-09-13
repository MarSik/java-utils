package com.github.marsik.util.functional;

@FunctionalInterface
public interface ConsumerWithException<T> {
    void accept(T value) throws Exception;
}
