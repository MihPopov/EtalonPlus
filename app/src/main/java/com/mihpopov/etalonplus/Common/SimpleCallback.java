package com.mihpopov.etalonplus.Common;

/**
 * Интерфейс колбэка для асинхронных операций.
 */
public interface SimpleCallback<T> {

    void onLoad(T data);

}