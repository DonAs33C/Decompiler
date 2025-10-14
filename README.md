## Java Decompiler Utility (jd-cli Wrapper)

This project provides a command-line tool to **decompile non-common Java applications** (not available on Maven Repository), entirely contained within `.jar` files.

---

## 🧩 Main Features
- Recursively scans directories for `.jar` files.
- Automatically decompiles JARs using [`jd-cli`](https://github.com/kwart/jd-cli).
- Cleans up `.class` files after decompilation.
- Checks [mvnrepository.com](https://mvnrepository.com) to determine whether the JAR is public or private.
- Handles file operations and dedicated directories for output.
- Simple command-line interface for input and feedback.

---

## ⚙️ Requirements
- **Java 8+**
- `jd-cli.jar` file located in the `lib/` folder
- Dependency: [JSoup](https://jsoup.org/)

---

## 🚀 Usage
Compile and run:

```bash
javac -cp .;lib/jsoup.jar it/decompiler/Decompiler.java
java -cp .;lib/jsoup.jar it.decompiler.Decompiler
```

Then, enter the **path** to the folder containing your `.jar` files.

---

## 🔍 How it works internally
The program uses `jd-cli` as an external Java decompiler, executed via system commands.  
Example command on Windows:

```bash
java -jar jd-cli.jar -g OFF CheckHashElement.class > CheckHashElement.java
```

In Java, the command is dynamically built:

```java
String fileJava = file.toString().replace(".class", ".java");
String command = "cmd /c java -jar src\\jd-cli\\jd-cli.jar -g OFF \"" + file + "\" > \"" + fileJava + "\"";
```

In short:
1. The `.class` extension is replaced with `.java`.
2. `jd-cli` is executed to generate the source code.
3. The process repeats for all `.class` files inside the `.jar`.

This enables the **complete decompilation of private Java applications**, even when the original source code is unavailable.

---

## 🧠 About
This code was created during the author’s **early professional experience**, and therefore the **code quality is quite low, IMO**.  
Nevertheless, it is a valuable example for understanding:
- the use of system commands in Java (`ProcessBuilder`);
- integration with external tools (`jd-cli`);
- HTML scraping using `Jsoup`;
- file and directory manipulation.

---

## 🔧 Future Improvements
- Implement **multi-threaded** decompilation.
- Add a **logging system** for better traceability.
- Improve **exception and I/O handling**.
- Develop a **graphical interface (GUI)** for easier use.
- Add **cross-platform** support (Linux/macOS without “cmd /c”).

---

## 📜 License
Open-source project for educational and learning purposes.
