# DreamBot

Dreambot scripts plus a local drop-table simulator.

## Build and run local drop simulator

From the repository root:

```bash
mvn -q -DskipTests package
java -jar target/Launcer.exe Vorkath 10000
```

### Rare drop rate changer

You can raise rare drop chance locally by overriding `rare_drop_multiplier` at launch:

```bash
java -Drare_drop_multiplier=10 -jar target/Launcer.exe Vorkath 10000
```

Other optional overrides:
- `-Ddrop_rate_multiplier=<number>`
- `-Dgp_multiplier=<number>`
- `-Dxp_multiplier=<number>`
