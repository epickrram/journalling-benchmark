package com.epickrram.benchmark.journal.util;

import java.io.Closeable;
import java.io.IOException;

public final class Closer
{
    private Closer() {}

    public static void close(final Closeable closeable)
    {
        if(closeable != null)
        {
            try
            {
                closeable.close();
            }
            catch (final IOException e)
            {
                // ignore
            }
        }
    }
}
