package com.epickrram.benchmark.journal.setup;

import com.epickrram.benchmark.journal.Journaller;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static com.epickrram.benchmark.journal.util.JournalNames.JOURNAL_NAMES;
import static java.nio.ByteBuffer.allocateDirect;
import static java.nio.channels.FileChannel.open;

public final class FilePreallocator
{
    private static final int BLOCK_SIZE = Journaller.BLOCK_SIZE;

    private final Path targetDir;
    private final long fileSize;
    private final int fileCount;

    public FilePreallocator(final Path targetDir, final long fileSize, final int fileCount)
    {
        this.targetDir = targetDir;
        this.fileSize = fileSize;
        this.fileCount = fileCount;
    }

    public void preallocate() throws IOException
    {
        System.out.println("Preallocating " + fileCount + " files");
        final ByteBuffer buffer = allocateDirect(BLOCK_SIZE);
        buffer.putInt(0xDEADC0DE);
        for(int i = 0; i < fileCount; i++)
        {
            try(final FileChannel channel = createFile(i))
            {
                long remaining = fileSize;
                while(remaining > 0)
                {
                    buffer.clear();
                    remaining -= channel.write(buffer);
                }
            }
        }
    }

    private FileChannel createFile(final int number) throws IOException
    {
        return open(Paths.get(targetDir.toString(), JOURNAL_NAMES.getJournalFilename(number)), StandardOpenOption.CREATE,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

}
