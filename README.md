# Huffman Compressor

哈夫曼压缩/解压缩器，是JLU的数据结构作业。

## Design

### File Structure

The file compressed will be end of .huff, which contains header like below:

#### Header
```
                     1                   2                   3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1  32 bits
+---------------+---------------+---------------+---------------+
|                          Magic Number                         |
+---------------+---------------+---------------+---------------+
| VersionNumber |    Reserved   |           Trie Length         |
+---------------+---------------+---------------+---------------+
|                         Source Length                         |
+---------------+---------------+---------------+---------------+
|                         Source Length                         |
+---------------+---------------+---------------+---------------+
|                           Time Stamp                          |
+---------------+---------------+---------------+---------------+
|                           Time Stamp                          |
+---------------+---------------+---------------+---------------+
|                           Reserved                            |
+---------------+---------------+---------------+---------------+
|                          CRC32 Code                           |
+---------------+---------------+---------------+---------------+
|                           Reserved                            |
+---------------+---------------+---------------+---------------+
 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7  4 bytes
```

In this:
- Magic Number: A magic number, it is always be 0x01711EF3, 0b0001 0111 0001 0001 1110 1111 0011.
- Version Number: A version number of this huff file, now it should be 0b0000 0001, which is 1.
- Trie Length: The length of huffman trie structure.
- Source Length: The length of file of the uncompressed, the full file. 
It takes 8 bytes which is a 2^64 bits gigantic number. It will support up to 2.4 x 10^5 TB. 
Just pay attention to the concatenate operation: the upper 32 bit is the upper field in the file.
- Time Stamp: Same to the source length, it is a 64bit number, who stores the time of this huff file was created.
- Reserved: No use at now, should be all 0.
- CRC32 Code: The CRC32 code of the original file, should check it after decompression.

**Because these headers is useless and unnecessary, the program will count compression rate without header.**

## License

AGPLv3, see LICENSE file to get more information.
