package com.epickrram.benchmark.journal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public interface Journaller
{
    int BLOCK_SIZE = 4096;
    long DELAY_BETWEEN_BLOCKS_NANOS = TimeUnit.MICROSECONDS.toNanos(50L);

    void write(final ByteBuffer data) throws IOException;
    void complete();
}
