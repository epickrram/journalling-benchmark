package com.epickrram.benchmark.journal.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public final class PositionalWriteJournaller extends AbstractJournaller<FileChannel>
{
    private static final ByteBuffer READER_BUFFER = ByteBuffer.allocateDirect(4096);

    public PositionalWriteJournaller(final long fileSize, final JournalAllocator<FileChannel> journalAllocator,
                                     final boolean isPreallocatingBlocks)
    {
        super(fileSize, journalAllocator, isPreallocatingBlocks);
    }

    @Override
    public void write(final ByteBuffer data, boolean newBlock) throws IOException
    {
        positionInFile += (newBlock) ? data.remaining() : 0;
        assignJournal(data.remaining());

        currentJournal.write(data, positionInFile);
    }

    @Override
    protected void preloadJournal(final FileChannel journal) throws IOException
    {
        while(journal.position() < journal.size())
        {
            READER_BUFFER.clear();
            journal.read(READER_BUFFER);
        }
        journal.close();
    }
}