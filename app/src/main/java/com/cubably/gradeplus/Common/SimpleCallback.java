package com.cubably.gradeplus.Common;

/**
 * Интерфейс колбэка для асинхронных операций.
 */
public interface SimpleCallback<T> {

    void onLoad(T data);

}