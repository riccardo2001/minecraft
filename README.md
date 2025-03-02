# Minecraft Clone - Java OpenGL

Un clone semplice di Minecraft sviluppato in Java utilizzando LWJGL (Lightweight Java Game Library) per il rendering OpenGL.

## Requisiti

- **Java 17 o superiore** (non compatibile con versioni precedenti)
- LWJGL 3.x
- IDE compatibile con Java (Eclipse, IntelliJ IDEA, VS Code con estensioni appropriate)

## Funzionalità

- Rendering di blocchi 3D
- Movimento della camera con i tasti WASD
- Sistema di illuminazione semplice
- Rendering dello sfondo

## Struttura del progetto

Il progetto è organizzato nei seguenti pacchetti:

- `core`: Contiene le classi principali come `Game` e `Window`
- `graphics`: Contiene la classe `Camera` per gestire la visuale
- `world`: Contiene la classe `Block` per la renderizzazione dei blocchi
- `input`: Contiene l'`InputHandler` per gestire gli input da tastiera

## Installazione

1. Clona il repository
```bash
git clone https://github.com/username/minecraft-clone.git
```

2. Importa il progetto nel tuo IDE

3. Assicurati di avere le dipendenze LWJGL nel tuo classpath. Se usi Maven, aggiungi al tuo `pom.xml`:

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
    <!-- Nativi per il tuo sistema operativo -->
    <dependency>
        <groupId>org.lwjgl</groupId>
        <artifactId>lwjgl</artifactId>
        <version>3.3.2</version>
        <classifier>natives-windows</classifier> <!-- cambia in base al tuo OS -->
    </dependency>
    <dependency>
        <groupId>org.lwjgl</groupId>
        <artifactId>lwjgl-opengl</artifactId>
        <version>3.3.2</version>
        <classifier>natives-windows</classifier> <!-- cambia in base al tuo OS -->
    </dependency>
    <dependency>
        <groupId>org.lwjgl</groupId>
        <artifactId>lwjgl-glfw</artifactId>
        <version>3.3.2</version>
        <classifier>natives-windows</classifier> <!-- cambia in base al tuo OS -->
    </dependency>
</dependencies>
```

## Avvio

Esegui la classe `Main` (nel pacchetto principale) per avviare il gioco.

```java
public class Main {
    public static void main(String[] args) {
        Game game = new Game();
        game.run();
    }
}
```

## Controlli

- `W`: Muovi avanti
- `S`: Muovi indietro
- `A`: Muovi a sinistra
- `D`: Muovi a destra

## ⚠️ Attenzione

**Questo progetto richiede Java 17 o superiore.** Non è compatibile con versioni precedenti a causa dell'utilizzo di funzionalità moderne di Java e librerie che richiedono Java 17+.

## Estensioni Previste

- Sistema di generazione procedurale del terreno
- Fisica di base (gravità, collisioni)
- Texture per i blocchi
- Sistema di inventario
- Crafting

## Contribuire

1. Fai un fork del repository
2. Crea un branch per la tua feature (`git checkout -b feature/nome-feature`)
3. Commit delle tue modifiche (`git commit -am 'Aggiungi una nuova feature'`)
4. Push al branch (`git push origin feature/nome-feature`)
5. Apri una Pull Request
