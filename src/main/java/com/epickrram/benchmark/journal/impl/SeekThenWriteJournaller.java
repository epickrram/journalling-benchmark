package com.epickrram.benchmark.journal.impl;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public final class SeekThenWriteJournaller extends AbstractJournaller<RandomAccessFile>
{
    public SeekThenWriteJournaller(final long fileSize, final JournalAllocator<RandomAccessFile> journalAllocator)
    {
        super(fileSize, journalAllocator);
    }

    @Override
    public void write(final ByteBuffer data) throws IOException
    {
        assignJournal(data.remaining());

        currentJournal.seek(positionInFile);
        final int messageSize = data.remaining();
        currentJournal.write(data.array(), data.position(), data.remaining());
        positionInFile += messageSize;
    }
}
