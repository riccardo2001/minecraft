# Minecraft Clone - Java OpenGL

A simple Minecraft clone developed in Java using LWJGL (Lightweight Java Game Library) for OpenGL rendering.

## Requirements

- **Java 17** (not compatible with earlier versions)
- LWJGL 3.x
- Java-compatible IDE VScode
- OS compatible with macOS arm64

## Features

- 3D block rendering
- Camera movement using WASD keys
- Simple lighting system
- Background rendering

## Project Structure

The project is organized in the following packages:

- `core`: Contains main classes like `Game` and `Window`
- `graphics`: Contains the `Camera` class to manage the view
- `world`: Contains the `Block` class for block rendering
- `input`: Contains the `InputHandler` to manage keyboard inputs

## Installation

1. Clone the repository
```bash
git clone https://github.com/riccardo2001/minecraft.git
```

2. Import the project into your IDE

3. Make sure you have LWJGL dependencies in your classpath. If using Maven, add to your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>org.lwjgl</groupId>
        <artifactId>lwjgl</artifactId>
        <version>3.3.2</version>
    </dependency>
    <dependency>
        <groupId>org.lwjgl</groupId>
        <artifactId>lwjgl-opengl</artifactId>
        <version>3.3.2</version>
    </dependency>
    <dependency>
        <groupId>org.lwjgl</groupId>
        <artifactId>lwjgl-glfw</artifactId>
        <version>3.3.2</version>
    </dependency>
    <!-- Natives for your operating system -->
    <dependency>
        <groupId>org.lwjgl</groupId>
        <artifactId>lwjgl</artifactId>
        <version>3.3.2</version>
        <classifier>natives-windows</classifier> <!-- change based on your OS -->
    </dependency>
    <dependency>
        <groupId>org.lwjgl</groupId>
        <artifactId>lwjgl-opengl</artifactId>
        <version>3.3.2</version>
        <classifier>natives-windows</classifier> <!-- change based on your OS -->
    </dependency>
    <dependency>
        <groupId>org.lwjgl</groupId>
        <artifactId>lwjgl-glfw</artifactId>
        <version>3.3.2</version>
        <classifier>natives-windows</classifier> <!-- change based on your OS -->
    </dependency>
</dependencies>
```

## Starting the Game

Run the `Main` class (in the main package) to start the game.

```java
public class Main {
    public static void main(String[] args) {
        Game game = new Game();
        game.run();
    }
}
```

## Controls

- `W`: Move forward
- `S`: Move backward
- `A`: Move left
- `D`: Move right

## ⚠️ Warning

**This project requires Java 17.** It is not compatible with earlier or later versions due to the use of modern Java features and libraries that require Java 17.

## Planned Extensions

- Procedural terrain generation system
- Basic physics (gravity, collisions)
- Textures for blocks
- Inventory system
- Crafting

## Contributing

1. Fork the repository
2. Create a branch for your feature (`git checkout -b feature/feature-name`)
3. Commit your changes (`git commit -am 'Add a new feature'`)
4. Push to the branch (`git push origin feature/feature-name`)
5. Open a Pull Request