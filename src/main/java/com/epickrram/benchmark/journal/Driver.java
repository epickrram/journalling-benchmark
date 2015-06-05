package com.epickrram.benchmark.journal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;

public final class Driver
{
    private static final int MESSAGE_SIZE = Journaller.BLOCK_SIZE;
    private final Function<Integer, ByteBuffer> bufferFactory;
    private final Journaller journaller;
    private final int fileCount;
    private final long fileSize;
    private final int iterations;

    public Driver(final Function<Integer, ByteBuffer> bufferFactory, final Journaller journaller, final int fileCount,
                  final long fileSize, final int iterations)
    {
        this.bufferFactory = bufferFactory;
        this.journaller = journaller;
        this.fileCount = fileCount;
        this.fileSize = fileSize;
        this.iterations = iterations;
    }

    public void execute() throws IOException
    {
        final ByteBuffer buffer = bufferFactory.apply(MESSAGE_SIZE);
        buffer.putInt(0xED0CDAED);

        for(int iteration = 0; iteration < iterations; iteration++)
        {
            for (int fileNum = 0; fileNum < fileCount; fileNum++)
            {
                long remaining = fileSize;
                while (remaining > MESSAGE_SIZE)
                {
                    buffer.clear();
                    journaller.write(buffer);
                    remaining -= MESSAGE_SIZE;
                    LockSupport.parkNanos(Journaller.DELAY_BETWEEN_BLOCKS_NANOS);
                }
            }

            journaller.complete();
        }
    }
}
