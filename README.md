# 🐈 Pusang Gutom
Game by: Jazmine Niña Gular, Yanika Tauro, and Steven Toledo 

(In fulfillment of their CMSC 137 Project Requirement S2 SY 2025-2026)

---

Pusang Gutom is a up-to-4-player, real-time reflex-based game developed in Java using native **javax.swing**, **java.awt**, and standard **TCP Sockets**. Players step into the paws of competing cats positioned at the corners of the screen, racing to slap randomly spawning mouse targets in the central "Pit" area. 

---

## 🛠️ Tech Stack & Prerequisites

Before compiling and running the game, ensure your local development environment meets the following baseline criteria:

* **Language/Runtime:** Java Development Kit (JDK) 21 
* **Build Automation:** Apache Maven 
* **Graphics Framework:** Native Java Swing & AWT (No JavaFX required) 
* **Networking Protocol:** Standard Java Sockets (TCP over Port 4444) 
* **Supported OS:** Windows, macOS, Linux 

---

## 🚀 Installation & Compilation Instructions

### 1. Clone the Repository
Clone the public repository down to your local machine:
```shell
git clone https://github.com/jnbgular04/CMSC137_PusangGutom.git
cd CMSC137_PusangGutom
```
### 2. Compile via Maven
The project uses standard Maven conventions. Build the lifecycle targets and compile source binaries directly:
```shell
mvn clean compile
```
### 3.1. Run via Eclipse
1) Import the root project directory into your workspace using File > Import > Maven > Existing Maven Projects .
2) Verify that your project's Build Path is strictly bound to Java JDK 21 .
3) Locate src/main/java/com/cmsc137/main/Main.java .
4) Right-click the file and select Run As > Java Application.

### 3.2. Run via Terminal
To simulate a multi-process, 4-player networking context locally on one machine, you can launch multiple client frames simultaneously using terminal scripts.

- On Windows Powershell / Command Prompt:
```shell
# Open Instance 1 (Host Player)
mvn exec:java -Dexec.mainClass="com.cmsc137.main.Main"

# Open a separate terminal window for Instance 2 (Guest Player)
mvn exec:java -Dexec.mainClass="com.cmsc137.main.Main"
```