# ![icon](https://mrexplode.github.io/resources/ltc4j/icon32.png) ltc4j [![Build Status](https://travis-ci.org/MrExplode/ltc4j.svg?branch=master)](https://travis-ci.org/MrExplode/ltc4j) [![Total alerts](https://img.shields.io/lgtm/alerts/g/MrExplode/ltc4j.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/MrExplode/ltc4j/alerts/) [![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/MrExplode/ltc4j.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/MrExplode/ltc4j/context:java)

SMPTE LTC signal generator for Java

**Disclaimer:** At this moment, doesn't work

This library focuses on generating ltc signals. This implies that you have to handle the timing, but also this makes slowing and accerlating time possible.
## Usage
 Framerates:
  - `Framerate.FRAMERATE_24`
  - `Framerate.FRAMERATE_25`
  - `Framerate.FRAMERATE_DROPFRAME`
  - `Framerate.FRAMERATE_30`

**An example of the** `LTCGenerator` **class**
```java
int hour, min, sec, frame = ... // times
Mixer mixer = ... // your output
LTCGenerator generator = new LTCGenerator(mixer, Framerate.FRAMERATE_25);
generator.init(); // handle the exception
// by calling start method, the generator will begin playing timecode
generator.start();
// the currenty set time will be played.
generator.setTime(hour, min, sec, frame);
```
**Using the** `LTCPacket` **class**
If you want to handle the the outputting on your own, you might want to use this class.
```java
int hour, min, sec, frame = ...
LTCPacket packet = new LTCPacket(hour, min, sec, frame, Framerate.FRAMERATE_25);
byte[] data = packet.asByteArray();
// work with data
```
**An example of timing**
```java
long start = 0;
long time = 0;
int framerate = ... // your framerate
LTCGenerator generator = ... // the generator instance
while (System.currentTimeMillis() >= time + (1000 / framerate) {
    long elapsed = time - start;
    long var = elapsed;
    int hour = (int) (var / 60 / 60 / 1000);
    var = var - (hour * 60 * 60 * 1000);
    int min = (int) (var / 60 / 1000);
    var = value - (min * 60 * 1000);
    int sec = (int) (var / 1000);
    var = value - (sec * 1000);
    int frame = (int) (var / (1000 / framerate));
    // work with time values
    generator.setTime(hour, min, sec, frame);
}
```