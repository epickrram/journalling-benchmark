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
    public void write(final ByteBuffer data) throws IOException
    {
        assignJournal(data.remaining());

        positionInFile += currentJournal.write(data, positionInFile);
    }
}
