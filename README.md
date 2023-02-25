# ParticleLife

A simple particle simulation that shows surprisingly complex behaviors.

## Run

If you have Maven installed you can run it like this after compiling:

```bash
mvn exec:java
```

With arguments:

```bash
mvn exec:java -Dexec.args="-w 800 600"
```

The following arguments are currently available:

| Short | Long          | Description                                           | Default  |
|-------|---------------|-------------------------------------------------------|----------|
| -h    | --help        | Display a help message and quit.                      |          |
| -s    | --seed        | Set the initial seed for the random number generator. |          |
| -w    | --window-size | The window width and height in pixels.                | 1024 768 |
| -f    | --full-screen | Run in full screen mode. Press ESC to quit.           |          |

## Keybindings

| Key   | Action                   |
|-------|--------------------------|
| ESC   | Quit                     |
| SPACE | Pause                    |
| N     | New simulation           |
| R     | Reset current simulation |

## How to get Processing as a Maven dependency

This project relies on [Processing](https://processing.org) for the visualisation and interactivity.
It is currently not available on maven central.
However, there is still a way you can use it with Maven.
Download Processing from processing.org, unpack it, then install it into your local Maven repository using this command:

```bash
mvn install:install-file \
    -Dfile=<path to core.jar> \
    -DgroupId=org.processing \
    -DartifactId=core \
    -Dversion=<version> \
    -Dpackaging=jar \
    -DgeneratePom=true
```

Make sure to replace the placeholders for the path and the version.

## Inspiration

- <https://www.redblobgames.com/x/2234-hunar-alife-simulation>
- <https://github.com/redblobgames/2234-hunar-alife-simulation>
- <https://www.youtube.com/watch?v=0Kx4Y9TVMGg>
- <https://github.com/hunar4321/particle-life>
