package com.epickrram.benchmark.journal.instrument;

import org.HdrHistogram.Histogram;

import java.io.PrintWriter;

import static java.lang.String.format;

public enum OutputFormat
{
    LONG
            {
                @Override
                public void output(final Histogram histogram, final PrintWriter printWriter)
                {
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
            },
    SHORT
            {
                @Override
                public void output(final Histogram histogram, final PrintWriter printWriter)
                {
                    printWriter.append(format("%.2f,%d,%d,%d,%d,%d,%d,%d,%d%n",
                                    histogram.getMean(), histogram.getMinValue(),
                                    histogram.getValueAtPercentile(50.0d),
                                    histogram.getValueAtPercentile(90.0d),
                                    histogram.getValueAtPercentile(99.0d),
                                    histogram.getValueAtPercentile(99.9d),
                                    histogram.getValueAtPercentile(99.99d),
                                    histogram.getMaxValue(),
                                    histogram.getTotalCount()));
                    printWriter.flush();
                }
            },
    DETAIL
            {
                @Override
                public void output(final Histogram histogram, final PrintWriter printWriter)
                {
                    histogram.outputPercentileDistribution(System.out, 1.0d);
                }
            };

    public abstract void output(final Histogram histogram, final PrintWriter printWriter);
}
