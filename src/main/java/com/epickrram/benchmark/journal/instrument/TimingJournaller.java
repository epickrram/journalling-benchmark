package com.epickrram.benchmark.journal.instrument;

import com.epickrram.benchmark.journal.Journaller;
import org.HdrHistogram.Histogram;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public final class TimingJournaller implements Journaller
{
    private static final long HIGHEST_TRACKABLE_VALUE = TimeUnit.SECONDS.toNanos(1L);

    private final Journaller delegate;
    private final OutputFormat outputFormat;
    private final Histogram histogram = new Histogram(HIGHEST_TRACKABLE_VALUE, 4);
    private boolean recording;

    public TimingJournaller(final Journaller delegate, final OutputFormat outputFormat)
    {
        this.delegate = delegate;
        this.outputFormat = outputFormat;
    }

    @Override
    public void write(final ByteBuffer data, boolean newBlock) throws IOException
    {
        final long startNanos = System.nanoTime();
        delegate.write(data, newBlock);

        final long durationNanos = System.nanoTime() - startNanos;
        if(recording)
        {
            histogram.recordValue(Math.min(HIGHEST_TRACKABLE_VALUE, durationNanos));
        }
    }

    @Override
    public void complete()
    {
        delegate.complete();

        if(recording)
        {
            final PrintWriter printWriter = new PrintWriter(System.out);
            outputFormat.output(histogram, printWriter);
        }

        histogram.reset();
    }

    public void setRecording(final boolean recording)
    {
        this.recording = recording;
    }
}
