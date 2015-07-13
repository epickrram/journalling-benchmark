package com.epickrram.benchmark.journal;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.epickrram.benchmark.journal.impl.JournalAllocator;
import com.epickrram.benchmark.journal.impl.PositionalWriteJournaller;
import com.epickrram.benchmark.journal.impl.SeekThenWriteJournaller;
import com.epickrram.benchmark.journal.instrument.OutputFormat;
import com.epickrram.benchmark.journal.instrument.TimingJournaller;
import com.epickrram.benchmark.journal.setup.FilePreallocator;
import com.higherfrequencytrading.affinity.AffinitySupport;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;

import static com.epickrram.benchmark.journal.instrument.OutputFormat.valueOf;

public final class TestMain
{
    private static final int WARMUP_ITERATIONS = 5;

    public static void main(final String[] args) throws IOException
    {
        final Config config = new Config();
        final JCommander commander = new JCommander(config);
        commander.parse(args);

        if(config.help)
        {
            commander.usage();
            System.exit(0);
        }

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

        final TimingJournaller timingJournaller = new TimingJournaller(journaller, valueOf(config.outputFormat));

        System.out.println("Doing warm-up");
        new Driver(bufferFactory, timingJournaller, config.fileCount, config.fileSize,
                WARMUP_ITERATIONS, config.writesPerBlock).execute();

        setAffinity(config.cpuAffinity);
        performGc();

        timingJournaller.setRecording(true);

        System.out.println("Starting measurement for journaller " + config.journallerType);

        new Driver(bufferFactory, timingJournaller, config.fileCount, config.fileSize,
                config.measurementIterations, config.writesPerBlock).execute();
    }

    private static void setAffinity(final int cpuAffinity)
    {
        if(cpuAffinity != Config.NO_AFFINITY)
        {
            System.out.println("Setting journaller thread affinity to cpu: " + cpuAffinity);
            final long cpuListBitMask = cpuListToBitMask(new int[]{cpuAffinity});
            AffinitySupport.setAffinity(cpuListBitMask);
            if(AffinitySupport.getAffinity() != cpuListBitMask)
            {
                throw new IllegalStateException("Unable to set thread affinity to cpu: " + cpuAffinity);
            }
        }
    }

    private static long cpuListToBitMask(final int[] cpus)
    {
        long bitMask = 0L;

        for(int cpu : cpus)
        {
            bitMask |= 1L << cpu;
        }

        return bitMask;
    }

    private static void performGc()
    {
        System.out.println("Performing GC to clear out any garbage");
        System.gc();
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5L));
    }

    private static void preallocateFiles(final Config config, final Path journalDir) throws IOException
    {
        new FilePreallocator(journalDir, config.fileSize, config.fileCount).preallocate();
    }

    private static final class Config
    {
        private static final int NO_AFFINITY = -1;

        @Parameter(names = "-d", description = "journal dir")
        private String journalDir = System.getProperty("java.io.tmpdir");
        @Parameter(names = "-s", description = "file size (bytes)")
        private int fileSize = 1024 * 1024;
        @Parameter(names = "-j", description = "number of journals")
        private int fileCount = 50;
        @Parameter(names = "-t", description = "journaller type [seek|pwrite]")
        private String journallerType;
        @Parameter(names = "-i", description = "measurement iterations")
        private int measurementIterations = 5;
        @Parameter(names = "-h", description = "show help", help = true)
        private boolean help;
        @Parameter(names = "-w", description = "writes per block")
        private int writesPerBlock = 1;
        @Parameter(names = "-f", description = "output format")
        private String outputFormat = "LONG";
        @Parameter(names = "-c", description = "cpu affinity")
        private int cpuAffinity = NO_AFFINITY;
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
