package com.epickrram.benchmark.journal.impl;

import com.epickrram.benchmark.journal.Journaller;
import com.epickrram.benchmark.journal.util.Closer;

import java.io.Closeable;
import java.io.IOException;

public abstract class AbstractJournaller<T> implements Journaller
{
    private final long fileSize;
    protected final JournalAllocator<T> journalAllocator;
    private final boolean appendOnly;
    protected T currentJournal;
    protected long positionInFile;

    protected AbstractJournaller(final long fileSize, final JournalAllocator<T> journalAllocator, final boolean appendOnly)
    {
        this.fileSize = fileSize;
        this.journalAllocator = journalAllocator;
        this.appendOnly = appendOnly;
    }

    @Override
    public void complete()
    {
        positionInFile = 0;
        journalAllocator.reset();
    }

    protected void assignJournal(final int messageSize) throws IOException
    {
        if(currentJournal == null || shouldRoll(positionInFile, messageSize))
        {
            roll();
            positionInFile = 0;
        }
    }

    private T getNextJournal() throws IOException
    {
        return journalAllocator.getNextJournal();
    }

    private boolean shouldRoll(final long position, final long messageSize)
    {
        return (appendOnly ? currentJournal == null : position + messageSize > fileSize);
    }

    private void roll() throws IOException
    {
        if(currentJournal instanceof Closeable)
        {
            Closer.close((Closeable) currentJournal);
        }
        currentJournal = getNextJournal();
    }
}
