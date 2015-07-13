# journalling-benchmark


Usage
=====

1. clone this repository
2. run ./gradlew bundleJar
3. run java -jar ./build/libs/journalling-benchmark-all-0.0.3.jar -h

Parameters
==========

* -j - number of journals to write
* -d - target directory for journals
* -i - measurement iterations
* -s - file size (bytes)
* -c - cpu affinity for journaller thread (default none)
* -f - output format (SHORT|LONG|DETAIL)
* -w - number of times to write the same block
* -t - journaller type
    * seek: RandomAccessFile seek/write
    * pwrite: FileChannel write

