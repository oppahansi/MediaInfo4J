<p align="middle">
    <img alt="GitHub build" src="https://github.com/oppahansi/MediaInfo4J/actions/workflows/ci.yml/badge.svg">
    <img alt="GitHub" src="https://img.shields.io/github/license/oppahansi/MediaInfo4J">
    <img alt="GitHub issues" src="https://img.shields.io/github/issues/oppahansi/MediaInfo4J">
    <img alt="GitHub pull requests" src="https://img.shields.io/github/issues-pr/oppahansi/MediaInfo4J">
    <img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/oppahansi/MediaInfo4J">

</p>

<p align="middle">
<img alt="GitHub last commit" src="https://img.shields.io/badge/status-in%20development-blue">
</p>

# MediaInfo4J
A Java library for parsing media information using the [MediaInfo](https://mediaarea.net/en/MediaInfo) library.  
This library provides a simple interface to extract metadata from media files.  

## Installing

### Gradle
```groovy
implementation group: 'io.github.oppahansi', name: 'MediaInfo4J', version: '0.2.1'
```

### Maven
```xml
<dependency>
    <groupId>io.github.oppahansi</groupId>
    <artifactId>MediaInfo4J</artifactId>
    <version>0.2.0</version>
</dependency>
```

## Usage

### From a file path
```java
MediaInfoParser parser = new MediaInfoParser()
MediaInfo mediaInfo = parser.parseFile(filePath);

// Print all sections and their fields
mediaInfo.print();
```
or
```java
MediaInfo mediaInfo = MediaInfo.fromFile(filePath);

// Print all sections and their fields
mediaInfo.print();
```


### From a string
```java
MediaInfoParser parser = new MediaInfoParser()
MediaInfo mediaInfo = parser.parseData(dataString);

// Print all sections and their fields
mediaInfo.print();
```
or
```java
MediaInfo mediaInfo = MediaInfo.fromData(dataString);

// Print all sections and their fields
mediaInfo.print();
```

## Features
- Parsing of media information from files using the MediaInfo native library.
- Extraction of metadata such as format, duration, codec, chapters, and more from media files.
- Simple Java API to access sections and fields of parsed media info.
- Support for reading media info from both file paths and raw data strings.
- Better parsing, than in vlcj-info:
- - Support for Chapter fields and values
- More methods for checking the validity for files:
- - CRC32
- - Adler32
- - MD5
- - SHA1
- - SHA256
- - SHA512
- New Menu fields for better chapter parsing:
- - `ChapterCount` (Total number of chapters)
- - `ChapterName x` (ChapterName 1 -> : en:Time for Ignition...)
- - `ChapterTimestamp x` (ChapterTimestamp 1 -> 00:00:00.000...)


## Acknowledgments
This project was inspired by and partially derived from the [vlcj-info](https://github.com/caprica/vlcj-info) library.  
And therefore code is being made public and released under the same license.
