# DreamBot
Dreambot scripts

## Run the drop table demo locally (Windows CMD)

From your JDK `bin` folder, compile and run with your own paths:

```bat
cd C:\Downloads\jdk-26.0.1\bin
javac -cp C:\Downloads\snakeyaml-2.2.jar -d C:\Downloads\cb-classes C:\Downloads\CyberBot\DropEntry.java C:\Downloads\CyberBot\DropTable.java C:\Downloads\CyberBot\DropTableEngine.java C:\Downloads\CyberBot\DropTableDemo.java C:\Downloads\CyberBot\Item.java C:\Downloads\CyberBot\Npc.java C:\Downloads\CyberBot\QuantityRange.java C:\Downloads\CyberBot\Rates.java
java -cp C:\Downloads\cb-classes;C:\Downloads\snakeyaml-2.2.jar com.cyberscape.rsps317.DropTableDemo --data-dir=C:\Downloads\CyberBot Vorkath 1000
```

To boost rare drops for a run, add:

```bat
--rare-drop-multiplier=25
```

To boost coin drops heavily (for example 100x up to 100000000x+), add:

```bat
--gp-multiplier=100
```

Note: very large multipliers can overflow integer quantities for extreme cases. Use practical values for normal testing.

## All-in-one GUI mode

You can launch a local GUI with simulation controls plus SDN/git setup fields:

```bat
java -cp C:\Downloads\cb-classes;C:\Downloads\snakeyaml-2.2.jar com.cyberscape.rsps317.DropTableDemo --gui --data-dir=C:\Downloads\CyberBot
```

GUI includes:
- Script Repo
- Script Module
- SDN Parameters (Optional)
- Drop Rate / Rare Drop / Gold (Coins) multipliers
