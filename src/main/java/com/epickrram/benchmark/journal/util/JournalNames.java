package com.epickrram.benchmark.journal.util;

public enum JournalNames
{
    JOURNAL_NAMES;

    public String getJournalFilename(final int number)
    {
        return String.format("journalling-benchmark-%d.journal", number);
    }
}
