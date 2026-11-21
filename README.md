# Blacksite Breach - 3D FPS Prototype

A 3D First-Person Shooter prototype game built with **Java 21** and **jMonkeyEngine 3.7.0-stable** for Windows.

---

## 🎮 Game Overview: Operation Blacksite Breach

In this prototype, you are an operative deployed into **Blacksite Sector-7**, a compromised military research installation guarded by autonomous AI combat drones.

### Mission Objectives
1. **Neutralize Rogue Sentries (0/3):** Navigate the facility sectors (Hangar, Security Corridor, and Server Vault) and destroy the 3 patrolling AI combat sentries.
2. **Infiltrate & Hack Data Core:** Breach the Server Core Vault, approach the Central Data Terminal, and hold `[E]` to extract the classified telemetry data.
3. **Evacuate to Extraction Pad:** Return to the Landing Bay and secure extraction on the green landing pad to complete the operation.

---

## 🕹️ Controls

| Action | Key / Input |
|---|---|
| **Move** | `W`, `A`, `S`, `D` |
| **Look / Aim** | `Mouse` (clamped first-person pitch & yaw) |
| **Fire Rifle** | `Left Mouse Button` |
| **Reload** | `R` |
| **Sprint** | `Left Shift` |
| **Jump** | `Spacebar` |
| **Interact / Hack** | `E` (Hold near Terminal) |
| **Restart Game** | `R` (When Mission Complete or Game Over) |

---

## ⚙️ Features & Architecture

- **Engine:** jMonkeyEngine 3.7.0-stable (`jme3-core`, `jme3-desktop`, `jme3-lwjgl3`, `jme3-effects`, `jme3-plugins`)
- **Runtime:** Java 21 (Modern language features: records, pattern matching, switch expressions)
- **AI Character State Machine:**
  - `PATROL`: Drones fly smoothly between waypoints with sinusoidal hover physics and blue optical sensor state.
  - `ALERT`: Upon spotting the player (field-of-view & distance check), the drone turns, sounds an alert chirp, and optic eyes turn yellow.
  - `CHASE`: Pursues the player through the facility.
  - `ATTACK`: Fires glowing red plasma bolts at the player accompanied by laser sound effects.
  - `DEAD`: Explodes with mechanical destruction sound, drops to the floor, extinguishing its sensor eyes.
- **Combat & First-Person Mechanics:**
  - Procedural first-person assault rifle attached to camera viewport with custom recoil animation, muzzle flash, and reload animation.
  - Raycast hitscan detection with visible bullet tracers and red hit markers on impact.
  - Dynamic 2D HUD: Crosshair with hit marker ticks, health bar, ammo counter (`30 / 90`), objective tracker, damage flash vignette, and hack progress bar.
- **Audio Synthesis:**
  - Low-latency procedural sound synthesizer (`SoundEffects`) powered by pure Java Sound (`javax.sound.sampled`). Generates punchy gunshots, laser blasts, hit marker pips, alarms, hacking beeps, explosions, and victory fanfares without external audio file dependencies.

---

## 🚀 How to Run

### Option 1: 1-Click Launch (Windows)
Double-click `run.bat` in the project root directory.

### Option 2: Maven Command Line
```powershell
mvn clean compile exec:java
```

### Option 3: IntelliJ IDEA
1. Open IntelliJ IDEA.
2. Select **File -> Open...** and choose `C:\Users\srina\Downloads\jmonkey-fps-game`.
3. Open `src/main/java/com/blacksite/BlacksiteBreachApp.java` and click **Run**.

---

## 🧪 Testing

Run the automated test suite with:
```powershell
mvn test
```