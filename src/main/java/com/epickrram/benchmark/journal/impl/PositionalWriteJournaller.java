package com.epickrram.benchmark.journal.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public final class PositionalWriteJournaller extends AbstractJournaller<FileChannel>
{
    private long positionInFile;

    public PositionalWriteJournaller(final long fileSize, final JournalAllocator<FileChannel> journalAllocator)
    {
        super(fileSize, journalAllocator);
    }

    @Override
    public void write(final ByteBuffer data, boolean newBlock) throws IOException
    {
        positionInFile += (newBlock) ? data.remaining() : 0;
        assignJournal(data.remaining());

        currentJournal.write(data, positionInFile);
    }
}
