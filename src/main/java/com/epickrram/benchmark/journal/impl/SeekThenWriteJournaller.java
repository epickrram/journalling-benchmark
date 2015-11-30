package com.epickrram.benchmark.journal.impl;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public final class SeekThenWriteJournaller extends AbstractJournaller<RandomAccessFile>
{
    public SeekThenWriteJournaller(final long fileSize, final JournalAllocator<RandomAccessFile> journalAllocator, final boolean appendOnly)
    {
        super(fileSize, journalAllocator, appendOnly);
    }

    @Override
    public void write(final ByteBuffer data, boolean newBlock) throws IOException
    {
        positionInFile += (newBlock) ? data.remaining() : 0;
        assignJournal(data.remaining());

        currentJournal.seek(positionInFile);
        currentJournal.write(data.array(), data.position(), data.remaining());
    }
}
