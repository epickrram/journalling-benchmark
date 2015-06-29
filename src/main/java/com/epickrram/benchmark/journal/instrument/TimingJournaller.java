package com.epickrram.benchmark.journal.instrument;

import com.epickrram.benchmark.journal.Journaller;
import org.HdrHistogram.Histogram;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

public final class TimingJournaller implements Journaller
{
    private static final long HIGHEST_TRACKABLE_VALUE = TimeUnit.SECONDS.toNanos(1L);

    private final Journaller delegate;
    private final Histogram histogram = new Histogram(HIGHEST_TRACKABLE_VALUE, 4);
    private boolean recording;

    public TimingJournaller(final Journaller delegate)
    {
        this.delegate = delegate;
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
            printWriter.append(format("== %s ==%n", "write latency"));
            printWriter.append(format("%-6s%20f%n", "mean", histogram.getMean()));
            printWriter.append(format("%-6s%20d%n", "min", histogram.getMinValue()));
            printWriter.append(format("%-6s%20d%n", "50.00%", histogram.getValueAtPercentile(50.0d)));
            printWriter.append(format("%-6s%20d%n", "90.00%", histogram.getValueAtPercentile(90.0d)));
            printWriter.append(format("%-6s%20d%n", "99.00%", histogram.getValueAtPercentile(99.0d)));
            printWriter.append(format("%-6s%20d%n", "99.90%", histogram.getValueAtPercentile(99.9d)));
            printWriter.append(format("%-6s%20d%n", "99.99%", histogram.getValueAtPercentile(99.99d)));
            printWriter.append(format("%-6s%20d%n", "max", histogram.getMaxValue()));
            printWriter.append(format("%-6s%20d%n", "count", histogram.getTotalCount()));
            printWriter.append("\n");
            printWriter.flush();
        }

        histogram.reset();
    }

    public void setRecording(final boolean recording)
    {
        this.recording = recording;
    }
}
