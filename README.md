# ShareSpace

## What is ShareSpace ?

ShareSpace is a Cross-Platform user driven marketplace to rent and lend various products and items another person might want to share.

It enables an easy interface for you to interact with a user driven platform to rent a selected item
or to lend an item you wish to share with the public.

ShareSpace is community focused and as such, handles that interaction between platform and community in a user first approach.

## Project Scope

ShareSpace was made for a University Project in accordance with the Frankfurt University of Applied Sciences.

The Developers are:
- Alexandros McCray (`alexandros [at] amccray [dot] com`)
- Lazar Milosevic
- Nadhir Hamdi
- Theo Deichmann

## Download

ShareSpace can be download from the [release](https://github.com/alexm-dev/ShareSpace/releases) page.  
It includes the submission that was build via our [Build Submission](https://github.com/alexm-dev/ShareSpace/actions/workflows/submission.yml) workflow.

## Submission

The submission zip includes the fat shaded jar for each OS as well as the binaries for Windows, Linux.  
Included inside `/app/windows/`, `/app/linux/`of the submission zip

To run the application with the jar prefer the terminal:
```sh
# For Windows
java -jar /path/to/ShareSpace-<version>-windows.jar

# For Linux
java -jar /path/to/ShareSpace-<version>-linux.jar

# For macOS
java -jar /path/to/ShareSpace-<version>-macos-arm64.jar
```

The normal executable inludes the full app-image, meaning its fully bundled with the correct configuration and runtime so that its launchable anywhere.

For the Linux binary specifically:
```sh
chmod +x ./ShareSpace
./ShareSpace
```

The submission also includes the entire repo, the javadocs, and the ShareSpace documentaion.

## Tech Stack

| Layer      | Technology                             |
|------------|----------------------------------------|
| Language   | Java 25                                |
| Build      | Maven (shade, javadoc, exec plugins)   |
| Database   | SQLite via JDBC                        |
| Encryption | BCrypt (jbcrypt)                       |
| JSON       | Jackson (asset metadata serialization) |
| Packaging  | JPackage (Packaging script)            |
| UI         | JavaFX (with Swing as an UI extension) |
| CI/CD      | GitHub Actions                         |

**Architecture:** DB Models -> DAOs -> Services -> JavaFX UI

## Development and configuration:

- JDK: use either temurin or OpenJDK. Needs to be version 25

    - Prefer temurin:
    [https://adoptium.net/temurin/]
    > See `Other Downloads` for more specific downloads.

- IDE: use whatever but remember to set the used JDK in the settings
> Always import project as a `Existing Maven project`

> **Recommended: run via Maven in your IDE**  
> This picks up `.mvn/jvm.config` automatically and requires no per-run setup.
>
> **IntelliJ IDEA:** Run -> Edit Configurations -> + -> Maven -> set Working directory to the project root -> set Command line to `exec:java` -> OK. Use this config to run the app.  
> **Eclipse:** Right-click project -> Run As -> Maven build... -> set Goals to `exec:java` -> Apply -> Run. Save it as a named launch config and use it going forward.

> **If you use the green play button instead:**  
> You may see a warning about `java.lang.System::load`. Add this VM argument once to silence it:  
> `--enable-native-access=ALL-UNNAMED`
>
> **IntelliJ:** First run `Main.java` once via right-click -> **Run 'Main.main()'** to generate the config, then Run -> Edit Configurations -> select the `Main` entry -> expand **Modify options** -> tick **Add VM options** -> paste the argument -> Apply  
> **Eclipse:** Run -> Run Configurations -> select your configuration -> **Arguments** tab -> paste into **VM arguments** -> Apply  

If you prefer the terminal:

- Install `Maven` as well
- Usage:

```sh
# Compile with maven and build the jar
mvn compile package

# Run with maven
mvn exec:java

# Package with maven
mvn clean package

# Run the JAR
java -jar /target/ShareSpace-<version>.jar
```

## JavaDoc

Generate and view the project code documentaion JavaDoc HTML:

- CLI / Maven:
```sh
# Build documentaion
mvn javadoc:javdoc
```

- IntelliJ: View -> Tool Windows -> Maven -> Plugins -> javadoc -> double-click `javadoc:javadoc`
- Eclipse: right-click project -> Run As -> Maven build... -> set Goals to `javadoc:javadoc` -> Run

You can then open the index.html inside output directory. Default: `target/reports/apidocs/index.html`

## License

ShareSpace is licensed under the Apache-2.0 license.

See the [LICENSE](LICENSE) files for more details.
