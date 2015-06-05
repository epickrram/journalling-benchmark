package com.epickrram.benchmark.journal;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.epickrram.benchmark.journal.impl.JournalAllocator;
import com.epickrram.benchmark.journal.impl.PositionalWriteJournaller;
import com.epickrram.benchmark.journal.impl.SeekThenWriteJournaller;
import com.epickrram.benchmark.journal.instrument.TimingJournaller;
import com.epickrram.benchmark.journal.setup.FilePreallocator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.function.Function;

public final class TestMain
{
    public static void main(final String[] args) throws IOException
    {
        final Config config = new Config();
        new JCommander(config).parse(args);

        final Path journalDir = Paths.get(config.journalDir);

        Journaller journaller = null;
        Function<Integer, ByteBuffer> bufferFactory = null;
        if("pwrite".equals(config.journallerType))
        {
            journaller = new PositionalWriteJournaller(config.fileSize, new JournalAllocator<>(journalDir, fileChannelFactory()));
            bufferFactory = ByteBuffer::allocateDirect;
        }
        else if("seek".equals(config.journallerType))
        {
            journaller = new SeekThenWriteJournaller(config.fileSize, new JournalAllocator<>(journalDir, randomAccessFileFactory()));
            bufferFactory = ByteBuffer::allocate;
        }
        else
        {
            System.out.println("Please specify journaller type: -t [seek|pwrite].");
            System.exit(1);
        }

        preallocateFiles(config, journalDir);

        final TimingJournaller timingJournaller = new TimingJournaller(journaller);

        System.out.println("Doing warm-up");
        new Driver(bufferFactory, timingJournaller, config.fileCount, config.fileSize, 1).execute();

        timingJournaller.setRecording(true);

        System.out.println("Starting measurement for journaller " + config.journallerType);
        new Driver(bufferFactory, timingJournaller, config.fileCount, config.fileSize, config.measurementIterations).execute();
    }

    private static void preallocateFiles(final Config config, final Path journalDir) throws IOException
    {
        new FilePreallocator(journalDir, config.fileSize, config.fileCount).preallocate();
    }

    private static final class Config
    {
        @Parameter(names = "-d", description = "journal dir")
        private String journalDir = System.getProperty("java.io.tmpdir");
        @Parameter(names = "-s", description = "file size (bytes), default 1Mb")
        private int fileSize = 1024 * 1024;
        @Parameter(names = "-c", description = "number of journals, default 50")
        private int fileCount = 50;
        @Parameter(names = "-t", description = "journaller type")
        private String journallerType;
        @Parameter(names = "-i", description = "measurement iterations, default 5")
        private int measurementIterations = 5;
    }

    private static Function<Path, RandomAccessFile> randomAccessFileFactory()
    {
        return (path) -> {
            try
            {
                return new RandomAccessFile(path.toString(), "rw");
            }
            catch (FileNotFoundException e)
            {
                throw new RuntimeException("Could not open file for writing: " + path.toString());
            }
        };
    }

    private static Function<Path, FileChannel> fileChannelFactory()
    {
        return (path) -> {
            try
            {
                return FileChannel.open(path, StandardOpenOption.WRITE);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Could not open file for writing: " + path.toString());
            }
        };
    }
}
