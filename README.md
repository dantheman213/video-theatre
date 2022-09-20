# Video Theatre

Play many videos concurrently, in a grid, on all monitors. Great for performances, visual fx, screensavers, and more.

## Getting Started

Currently in alpha but working. Needs some updates to choose custom directories, custom grid count, and more.

## Controls

### Changing videos manually in the grid

Toggle 0-9 to change video on screen at grid location (1 for first item in grid and so on. 0 is used for 10th position if your grid is configured to that many videos). 

### Chaging videos on different monitors

No modifier is monitor 1, holding down CTRL+[0-9] for monitor 2, hold down ALT+[0-9] for monitor 3.

### Mute / unmute specific video

Same rules as above just add the SHIFT modifier to your key combo.

### Toggle videos repeating or not

After starting session, use the `R` key to toggle repeat off/on.

### Exit application

Use the `ESC` key to exit the application.

## Build Application

https://www.graalvm.org/22.2/reference-manual/native-image/

```
jar cfvm build\libs\video-theatre.jar META-INF\MANIFEST.MF -C build .
C:\graalvm-ce-java17-22.2.0\bin\native-image.cmd -classpath build\classes -jar build\libs\video-theatre.jar
```
