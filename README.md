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
|                          CRC32 Code                           |
+---------------+---------------+---------------+---------------+
|                           Reserved                            |
+---------------+---------------+---------------+---------------+
|                           Reserved                            |
+---------------+---------------+---------------+---------------+
|                           Reserved                            |
+---------------+---------------+---------------+---------------+
|                           Reserved                            |
+---------------+---------------+---------------+---------------+
 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7  4 bytes
```

In this:
- Magic Number: A magic number, it is always 0x01711EF3, 0b0001 0111 0001 0001 1110 1111 0011.
- Version Number: A version number of this huff file, now it should be 0b0000 0001, which is 1.
- Trie Length: The length of huffman trie structure.
- Source Length: The length of file of the uncompressed, the full file. 
It takes 8 bytes which is a 2^64 bits gigantic number. It will support up to 2.4 x 10^5 TB. 
Just pay attention to the concatenate operation: the upper 32 bit is the upper field in the file.
- Reserved: No use at now, should be all 0.
- CRC32 Code: The CRC32 code of the original file, should check it after decompression.

**Because these headers is useless and unnecessary, the program will count compression rate without header.**

#### Content
The construction of trie is below.
For example, there are 5 characters a, b, c, d and e, with frequency of
0.6, 0.1, 0.1, 0.1, 0.1. This bushy tree is just like this:

![img.png](img.png)

The prefix free codec table is:

| Character | Codec in Binary |
|:---------:|:---------------:|
|     a     |        0        |
|     b     |       100       |
|     c     |       101       |
|     d     |       110       |
|     e     |       111       |

In the beginning of content, there is a bit for indicating this leaf is a true leaf,
or just another parent of other leaves.

So, the binary structure of this tree is:
```
                     1                   2                   3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1  32 bits
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|1|ASCII 'a' in bin | 000 |0|0|1|ASCII 'b' in bin | 100 |1|ASCII|
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|'c'in bin| 101 |0|1|ASCII 'd' in bin | 110 |1|ASCII 'e' in bin |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
| 111 | pending |           Compressed File Content             |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7  4 bytes
```
Actually, the program won't distinguish the character is ASCII or not indeed.
It compresses binary straightly (Same effect to ASCII).

In English, the content structure is started by 0 or 1, which is represented to
this node is the leaf node or not, then followed by content -- the key of the map,
finally ended with 3 bits wide huffman codec -- the value of the map.

And next to the pending bits, there is actual compressed file content, which wouldn't
end with pending because before compressed file content, they are already aligned by bytes.

### Modules
#### Bits
##### Output
I designd a class that can print bits/bytes to output stream, also a file.
It is simply to use, just like this:
```java
BitsOut bo = new BitsOut(); // default is System.out
BitsOut bo2 = new BitsOut("/home/evyde/", "test.out");
BitsOut bo3 = new BitsOut(new ByteArrayOutputStream());

bo.write(33333); // write int
bo2.write(true); // write bit
bo3.write(2); // write byte

bo.close(); // close and flush
bo2.close();
bo3.close();
```
It nicely uses abstracted "stream" as the output and process bit-wise things.

##### Input

## License

AGPLv3, see LICENSE file to get more information.
