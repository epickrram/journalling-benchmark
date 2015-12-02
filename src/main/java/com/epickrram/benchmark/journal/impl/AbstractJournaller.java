package com.epickrram.benchmark.journal.impl;

import com.epickrram.benchmark.journal.Journaller;
import com.epickrram.benchmark.journal.util.Closer;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public abstract class AbstractJournaller<T> implements Journaller
{
    private final long fileSize;
    private final boolean isPreallocatingBlocks;
    private final AtomicReference<T> nextJournal = new AtomicReference<>();

    protected final JournalAllocator<T> journalAllocator;
    protected T currentJournal;
    protected long positionInFile;

    protected AbstractJournaller(final long fileSize, final JournalAllocator<T> journalAllocator,
                                 final boolean isPreallocatingBlocks)
    {
        this.fileSize = fileSize;
        this.journalAllocator = journalAllocator;
        this.isPreallocatingBlocks = isPreallocatingBlocks;

        if(isPreallocatingBlocks)
        {
            final Thread preloadWorker = new Thread(new PreloadJob());
            preloadWorker.setDaemon(true);
            preloadWorker.start();
        }
    }

    @Override
    public void complete()
    {
        positionInFile = 0;
        currentJournal = null;
    }

    protected void assignJournal(final int messageSize) throws IOException
    {
        if(currentJournal == null || shouldRoll(positionInFile, messageSize))
        {
            roll();
            positionInFile = 0;
        }
    }

    protected abstract void preloadJournal(final T journal) throws IOException;

    private T getNextJournal() throws IOException
    {
        return journalAllocator.getNextJournal();
    }

    private boolean shouldRoll(final long position, final long messageSize)
    {
        return position + messageSize > fileSize;
    }

    private void roll() throws IOException
    {
        if(currentJournal instanceof Closeable)
        {
            Closer.close((Closeable) currentJournal);
        }
        currentJournal = getNextJournal();
        if(isPreallocatingBlocks)
        {
            nextJournal.lazySet(journalAllocator.peekNextJournal());
        }
    }

    private final class PreloadJob implements Runnable
    {
        @Override
        public void run()
        {
            while(!Thread.currentThread().isInterrupted())
            {
                try
                {
                    final T toPreload = AbstractJournaller.this.nextJournal.get();
                    if(toPreload != null && nextJournal.compareAndSet(toPreload, null))
                    {
                        preloadJournal(toPreload);
                    }
                }
                catch (IOException e)
                {
                    System.err.println("Failed to preload journal: " + e.getMessage());
                }
                finally
                {
                    LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1L));
                }
            }
        }
    }
}
