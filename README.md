# My MediaInfo Parser

A Java library for parsing media information, based on the [vlcj-info](https://github.com/caprica/vlcj-info) library.  
Currently requires Java 21 and requires https://mediaarea.net/en/MediaInfo to be installed.

## Example Usage

```java
MediaInfo mediaInfo = MediaInfoParser.parseFile(filePath);

// Print all sections and their fields
mediaInfo.getSections().forEach((type, section) -> {
    System.out.println("Section: " + type);
    section.getFieldValues().forEach((key, value) -> {
        System.out.printf("  %s: %s%n", key, value);
    });
    System.out.println();
});
```

## License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE) file for details.

## Acknowledgments

This project is a derivative work of the [vlcj-info](https://github.com/caprica/vlcj-info) library, licensed under the GNU General Public License v3.0. Modifications include:
- Updated to Java 21 with stream-based parsing.
- Added `SectionType` enum for type-safe section handling.
- Improved parsing for `Menu` section chapters and values containing colons.
- Simplified data model by merging `Sections` into `Section`.

## Source Code

The complete source code, including modifications, is available in this repository.