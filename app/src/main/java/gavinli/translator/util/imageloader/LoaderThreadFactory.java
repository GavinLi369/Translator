package gavinli.translator.util.imageloader;

import android.support.annotation.NonNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by gavin on 10/15/17.
 */

public class LoaderThreadFactory implements ThreadFactory {
    private static final String THREAD_NAME = "ImageLoader-";

    private static AtomicInteger mThreadNumber = new AtomicInteger();

    @Override
    public Thread newThread(@NonNull Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(THREAD_NAME + mThreadNumber.getAndIncrement());
        return thread;
    }
}
