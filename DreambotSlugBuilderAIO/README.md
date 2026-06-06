# Slug Builder AIO Premium v1.2

A task-based all-in-one DreamBot script. The engine (`AIOScript`) drives a
`TaskManager`; every module is a self-contained `Task`, so new skills/methods
drop in without touching the engine.

## Features

- **Money modules** (unusual money makers):
  - Jade → Trading Sticks (Tai Bwo Wannai hop-and-shop)
  - Soul Rune shop buyer (Magic Guild / Baba Yaga)
  - Apple Mush (apple press)
  - Fire Shades (Temple Trekking Route 3, first encounter)
- **Bossing module** (name-based, no server-specific IDs required): Brutus,
  Varrock Sewers (axe adds), King Black Dragon, Giant Mole, Chaos Elemental,
  Ivar King of Bones. Handles travel-to-arena, eat-at-HP%, optional Protect
  from Melee flicking, attacking boss + adds, valuable ground looting, and
  bank-restock when out of food. Set the boss, food, and an optional NPC-name
  override in the **Bossing** tab; adjust anchor tiles in `BossRegistry` if the
  CyberScape 317 coordinates differ.
- **Antiban** – probability-gated idle behaviours (camera, tab switches, mouse
  moves, reaction pauses)
- **Discord webhooks** – periodic progress embeds, fail-soft on a daemon thread
- **RuneGuard** – optional runtime signing via the official
  `com.runeguard.client.Runeguard` client (fail-soft wrapper)
- **Live profit tracking** – GE-priced profit and GP/hr on the paint overlay

## Building locally

The DreamBot client jar is **not** redistributable and is not in this repo. The
launcher downloads it after login to:

| OS      | Path                                   |
|---------|----------------------------------------|
| Windows | `%USERPROFILE%\DreamBot\BotData\client.jar` |
| macOS   | `~/DreamBot/BotData/client.jar`        |
| Linux   | `~/DreamBot/BotData/client.jar`        |

Then:

```bash
cd DreambotSlugBuilderAIO
./build.sh                                  # auto-detects ~/DreamBot/BotData/client.jar
# or, point at an explicit client jar:
DREAMBOT_CLIENT="/c/Users/pfuru/DreamBot/BotData/client.jar" ./build.sh
```

On Windows without bash, compile directly:

```bat
javac --release 11 ^
  -cp "%USERPROFILE%\DreamBot\BotData\client.jar;lib\runeguardjavaclient-0.1.0.jar" ^
  -d build\classes (dir /s /b src\*.java)
```

The build produces `build/SlugBuilderAIO.jar` (script classes + the RuneGuard
client merged in so it loads at runtime). Copy it into your DreamBot `Scripts`
folder and select **Slug Builder AIO** in the client.

## RuneGuard notes

The published `runeguardjavaclient-0.1.0.jar` exposes:

```java
new Runeguard(String signingKey, Consumer<String> log)
new Runeguard(String signingKey, Duration timeout, Consumer<String> log)
void start(String, String, String) throws RuneguardSessionException
void stop();  void close();
```

The three `start(...)` arguments are not self-describing in the compiled jar.
This wrapper passes them as `(scriptToken, scriptName, scriptVersion)`. If your
RuneGuard dashboard documents a different order, change only
`RuneGuardClient.start()`.

Supply the signing key (PEM) and script token in the **RuneGuard** tab of the
GUI. With no key supplied the script runs normally without RuneGuard.

## Extending

Add a class extending `nezz.dreambot.aio.task.Task` (or `money.MoneyTask`),
register it in `AIOScript.buildModule()` / `TaskManager`, and it participates in
the priority loop. The DreamBot/RuneLite ID references collected in the project
notes are useful when wiring new skills.
```
