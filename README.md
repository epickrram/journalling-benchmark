# journalling-benchmark


Usage
=====

1. clone this repository
2. run ./gradlew bundleJar
3. run java -jar ./build/libs/journalling-benchmark-all-0.0.1.jar -h

Parameters
==========

* -c - number of journals to write
* -d - target directory for journals
* -i - measurement iterations
* -s - file size (bytes)
* -f - output format (SHORT|LONG)
* -w - number of times to write the same block
* -t - journaller type
    * seek: RandomAccessFile seek/write
    * pwrite: FileChannel write

