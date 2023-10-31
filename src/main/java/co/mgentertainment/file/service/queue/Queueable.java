package co.mgentertainment.file.service.queue;

import java.io.Closeable;

/**
 * @author larry
 * @createTime 2023/9/23
 * @description Queueable
 */
public interface Queueable<T> extends Closeable {

    /**
     * 入队列
     *
     * @param t
     */
    void put(T t);
}