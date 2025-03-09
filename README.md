# Minecraft Clone - Java OpenGL

A simple Minecraft clone developed in Java using LWJGL (Lightweight Java Game Library) for OpenGL rendering.

## Requirements

- **Java 17** (not compatible with earlier versions)
- LWJGL 3.x
- Java-compatible IDE VScode
- OS compatible with macOS arm64 and Windows x86

## Features

- 3D block rendering
- Camera movement using WASD keys and more
- Texture support
- Background rendering
- Chunk handling
- World generation

## Project Structure

The project is organized in the following packages:

- `core`: Contains main classes like `Engine` and `Window`
- `graphics`: Contains the `Render` class to manage the view
- `world`: Contains the `World` class for block rendering
- `scene`: Contains the `Camera` to manage looking around

## Installation

1. Clone the repository

```bash
git clone https://github.com/riccardo2001/minecraft.git
```

2. Import the project into your IDE

3. Make sure you have LWJGL dependencies in your classpath. If using Maven, add to your `pom.xml`:

```xml
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <lwjgl.version>3.3.2</lwjgl.version>
        <joml.version>1.10.8</joml.version>
    </properties>

    <dependencies>
        <!-- Library LWJGL -->
        <dependency>
            <groupId>org.lwjgl</groupId>
            <artifactId>lwjgl</artifactId>
            <version>${lwjgl.version}</version>
        </dependency>

        <dependency>
            <groupId>org.lwjgl</groupId>
            <artifactId>lwjgl-opengl</artifactId>
            <version>${lwjgl.version}</version>
        </dependency>

        <dependency>
            <groupId>org.lwjgl</groupId>
            <artifactId>lwjgl-glfw</artifactId>
            <version>${lwjgl.version}</version>
        </dependency>

        <dependency>
            <groupId>org.lwjgl</groupId>
            <artifactId>lwjgl-stb</artifactId>
            <version>${lwjgl.version}</version>
        </dependency>

        <!-- Library native per ARM64 -->
        <dependency>
            <groupId>org.lwjgl</groupId>
            <artifactId>lwjgl</artifactId>
            <version>${lwjgl.version}</version>
            <classifier>${native.classifier}</classifier>
        </dependency>

        <dependency>
            <groupId>org.lwjgl</groupId>
            <artifactId>lwjgl-opengl</artifactId>
            <version>${lwjgl.version}</version>
            <classifier>${native.classifier}</classifier>
        </dependency>

        <dependency>
            <groupId>org.lwjgl</groupId>
            <artifactId>lwjgl-glfw</artifactId>
            <version>${lwjgl.version}</version>
            <classifier>${native.classifier}</classifier>
        </dependency>

        <dependency>
            <groupId>org.lwjgl</groupId>
            <artifactId>lwjgl-stb</artifactId>
            <version>${lwjgl.version}</version>
            <classifier>${native.classifier}</classifier>
            <scope>runtime</scope>
        </dependency>

        <!-- Libreria JOML -->
        <dependency>
            <groupId>org.joml</groupId>
            <artifactId>joml</artifactId>
            <version>1.10.8</version>
        </dependency>

    </dependencies>
```

4. Compile with maven:

- Windows

```bash
mvn clean install -Pwindows
```

- Macos

```bash
mvn clean install -Pmacos-arm64
```

## Starting the Game

Run the `Main` class (in the main package) to start the game.

```java
public static void main(String[] args) {
    Main main = new Main();
    Engine gameEng = new Engine("Minecraft ", new Window.WindowOptions(), main);
    gameEng.start();
}
```

## Java prompt starting on MacOs
```bash
/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home/bin/java -XstartOnFirstThread -jar minecraft-1.0.jar
```

## Java prompt starting on Windows
```bash
/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home/bin/java -jar minecraft-1.0.jar
```



## Bash command for class union
```bash
find ~/minecraft/src/main/java -type f -name "*.java" -exec cat {} + > output.txt 
```


## Controls

- `W`: Move forward
- `S`: Move backward
- `A`: Move left
- `D`: Move right
- `L-CTRL`: Move down
- `SPACE`: Move up
- `MOUSE`: Look around
- `ESC`: Pause/Resume


## ⚠️ Warning

**This project requires Java 17.** It is not compatible with earlier or later versions due to the use of modern Java features and libraries that require Java 17.

## Planned Extensions

- Fps optimization
- Basic physics (gravity, collisions)
- Inventory system
- Crafting

## Contributing

1. Fork the repository
2. Create a branch for your feature (`git checkout -b feature/feature-name`)
3. Commit your changes (`git commit -am 'Add a new feature'`)
4. Push to the branch (`git push origin feature/feature-name`)
5. Open a Pull Request
