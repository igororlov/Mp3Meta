# Intro
MP3 Song Metadata Transliteration (Cyrillic to Latin). Written in Java (Spring Boot + Maven). Spring Boot is actually not needed now, but used because functionality might be extended in future.

# Based on
- [icu4j](https://mvnrepository.com/artifact/com.ibm.icu/icu4j) for transliteration
- [mp3agic](https://github.com/mpatric/mp3agic) for mp3 file metadata reading

# How to configure and run
0. Open `Mp3MetaApplication.java`
1. Choose transliteration algorithm (e.g. `RUSSIAN_TO_LATIN`)
2. Choose root directory (with *.mp3 files). IMPORTANT: Try with directory containing one mp3 file first.
Can be run as a simple Java Application (in Eclipse open `Mp3MetaApplication.java` and right click `Run As > Java Application`) or from terminal `./mvnw spring-boot:run`
