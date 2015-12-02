package com.epickrram.benchmark.journal.impl;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public final class SeekThenWriteJournaller extends AbstractJournaller<RandomAccessFile>
{
    public SeekThenWriteJournaller(final long fileSize, final JournalAllocator<RandomAccessFile> journalAllocator,
                                   final boolean isPreallocatingBlocks)
    {
        super(fileSize, journalAllocator, isPreallocatingBlocks);
    }

    @Override
    public void write(final ByteBuffer data, boolean newBlock) throws IOException
    {
        positionInFile += (newBlock) ? data.remaining() : 0;
        assignJournal(data.remaining());

        currentJournal.seek(positionInFile);
        currentJournal.write(data.array(), data.position(), data.remaining());
    }

    @Override
    protected void preloadJournal(final RandomAccessFile journal) throws IOException
    {
        for(int i = 0; i < journal.length(); i++)
        {
            journal.seek(i);
            journal.read();
        }
        journal.close();
    }
}
