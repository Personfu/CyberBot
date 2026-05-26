<div align="center">
  <img src="assets/master/banner.svg" width="100%" alt="CyberBot banner"/>
</div>

<div align="center">

**Full F2P account builder** — Tutorial Island → 23 quests → route-to-99 all skills → Grand Exchange → repeat.

</div>

---

<div align="center">
  <img src="assets/master/architecture.svg" width="100%" alt="Architecture overview"/>
</div>

---

## Features

### Quest Engine (23 F2P Quests)

| Quest | QP | Notes |
|---|---|---|
| Cook's Assistant | 1 | First meal for the Lumbridge Cook |
| Sheep Shearer | 1 | 20 balls of wool for Fred |
| Rune Mysteries | 1 | Unlocks Runecrafting |
| Restless Ghost | 1 | Prayer XP reward |
| Witch's Potion | 1 | - |
| Imp Catcher | 1 | Magic XP reward |
| **Ides of Milk** | 1 | **Unlocks Brutus combat farming (2025)** |
| Below Ice Mountain | 1 | Unlocks Ruins of Camdozaal (2021) |
| Romeo and Juliet | 5 | - |
| Ernest the Chicken | 4 | - |
| Goblin Diplomacy | 5 | - |
| Vampyre Slayer | 3 | - |
| The Corsair Curse | 2 | 1000 XP lamp reward |
| Misthalin Mystery | 1 | - |
| X Marks the Spot | 1 | - |
| Doric's Quest | 1 | Free pickaxe |
| Pirate's Treasure | 2 | - |
| Prince Ali Rescue | 3 | Toll gate access |
| Black Knights' Fortress | 3 | Needs 12 QP |
| The Knight's Sword | 1 | 12,725 Smithing XP |
| Demon Slayer | 3 | - |
| Shield of Arrav | 1 | Co-op stub |
| Dragon Slayer I | 2 | Rune platebody unlock |

### Combat Training - Brutus (Primary F2P Method)

**Brutus** is the giant cow boss from *Ides of Milk*. After completing the quest, he can be rechallenged indefinitely:

- **Location**: Lumbridge cow field, east of the castle (~Tile 3259, 3267)
- **Drops**: Cowhide (~180gp) + Raw beef (free food) + Bones
- **No supplies needed** - cooks beef drops on a fire for free food
- **Falls back** to regular cows while Brutus is on respawn cooldown
- **Banks cowhide** at Lumbridge bank then sell at Grand Exchange (~54,000gp/hr)
- Trains Attack, Strength, Defence, and Hitpoints simultaneously

### Skill Modules (Route to 99 Everything)

<img src="assets/master/build-plan-flow.svg" width="100%" alt="Build plan flow"/>

| Skill | Training Method |
|---|---|
| Attack / Strength / Defence / HP | Brutus > cows > goblins > ogresses |
| Prayer | Bury bones; altar at Edgeville |
| Mining | Tin/Copper > Iron > Coal > Mithril |
| Smithing | Knight's Sword boost > Anvil loop |
| Woodcutting | Trees > Oaks > Willows > Yews |
| Firemaking | Burn every log batch immediately |
| Fishing | Shrimp > Trout > Lobster (F2P) |
| Cooking | Cook every fish catch before banking |
| Crafting | Leather > Gold > Gems |
| Fletching | Arrow shafts > Bows > Bolts |
| Runecrafting | Air runes > Body > Cosmic |
| Thieving | Men > Cake stall > Silk stall |

### Grand Exchange Integration

- Auto-sells Cowhide, Feathers, Logs, Ores, Fish, Crafted items
- Restocks consumables (food, arrows, runes) via GE buy orders
- Mule support: trade excess GP to a designated mule account

<img src="assets/master/gui-mockup.svg" width="100%" alt="GUI mockup"/>

### Antiban System

<img src="assets/master/antiban-curve.svg" width="100%" alt="Antiban human mouse curve"/>

| Feature | Description |
|---|---|
| Human Mouse | Bezier-curve mouse paths with per-account speed variance |
| Camera Jitter | Random camera angle micro-adjustments between actions |
| AFK Drift | Simulates idle periods |
| Break Manager | Scheduled breaks every 45-90 minutes, 5-20 min duration |
| Night Sleep | Automatic offline window 00:00-07:00 local time |
| Per-Account Seed | Username hash seeds all behavioral RNG - each account behaves uniquely |
| World Hopping | Hops to avoid detection |
| Fatigue Model | Reduces reaction speed after 4+ hrs; recovers after breaks |

---

## Build

```powershell
cd DreambotMasterAIO
.\build.ps1
```

---

<img src="assets/master/feature-matrix.svg" width="100%" alt="Feature comparison matrix"/>

## Legal Notice

For **educational purposes only**. Botting violates OSRS ToS and may result in bans.
