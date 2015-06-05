package com.epickrram.benchmark.journal.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import static com.epickrram.benchmark.journal.util.JournalNames.JOURNAL_NAMES;

public final class JournalAllocator<T>
{
    private final Path journalDir;
    private final Function<Path, T> journalFactory;
    private int journalNumber;

    public JournalAllocator(final Path journalDir, final Function<Path, T> journalFactory)
    {
        this.journalDir = journalDir;
        this.journalFactory = journalFactory;
    }

    public void reset()
    {
        journalNumber = 0;
    }

    T getNextJournal() throws IOException
    {
        return journalFactory.apply(Paths.get(journalDir.toString(), JOURNAL_NAMES.getJournalFilename(journalNumber++)));
    }
}