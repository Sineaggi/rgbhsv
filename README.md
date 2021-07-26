Implementation of the RGB to HSV (and HSV to RGB) algorithms in java.

Includes an implementation using the new java vector api. Required java 16+, will likely need changes as the vector api continues to incubate.

Performance of the vectorized code is around 10 times slower than the non-vectorized code on java 16 and around 1.1 to 3 times faster on java 17ea.

Based off an old fork of https://github.com/kobalicek/simdtests which appears to be an implementation https://www.daaam.info/Downloads/Pdfs/proceedings/proceedings_2011/780.pdf.
Additional code from https://en.wikipedia.org/wiki/HSL_and_HSV#HSV_to_RGB_alternative

Current benchmarks on openjdk-17-ea+32_windows-x64_bin:

```
Benchmark                        Mode  Cnt     Score      Error  Units
JmhMain.ahsv_from_argb_scalar   thrpt    5   729.509 �    5.566  ops/s
JmhMain.ahsv_from_argb_scalar2  thrpt    5   768.171 �   15.984  ops/s
JmhMain.ahsv_from_argb_vector   thrpt    5   201.566 �    4.879  ops/s
JmhMain.ahsv_from_argb_vector2  thrpt    5  2021.413 �   42.252  ops/s
JmhMain.argb_from_ahsv_scalar   thrpt    5  1477.989 � 1588.476  ops/s
JmhMain.argb_from_ahsv_scalar2  thrpt    5  1664.318 �   23.403  ops/s
JmhMain.argb_from_ahsv_vector   thrpt    5   193.718 �    3.699  ops/s
JmhMain.argb_from_ahsv_vector2  thrpt    5  1753.816 �   37.496  ops/s
```
