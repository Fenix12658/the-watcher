# The Watcher - mod horror per Minecraft Java (Fabric)

Un'entità ti perseguita nel buio, suoni inquietanti ti accompagnano e ogni tanto parte un **jumpscare** a schermo intero con urlo.

## Requisiti (per giocare)

| Cosa | Versione |
|---|---|
| Minecraft Java | **26.3** |
| Fabric Loader | 0.20.1 o superiore |
| [Fabric API](https://modrinth.com/mod/fabric-api) | 0.165.0+26.3 |
| Java | 25 (incluso nel launcher ufficiale) |

> **Nota sulla 26.4:** al momento esiste solo come snapshot (26.4-snapshot-6), quindi la mod e' scritta per la 26.3 stabile. Quando uscira' la 26.4 basterà cambiare tre valori in `gradle.properties` (vedi sotto).

La mod serve **sia sul client che sul server**.

## Come compilarla

Serve un JDK 25 (es. Temurin 25).

```bash
./gradlew build          # Windows: gradlew.bat build
```

Il file pronto e' in `build/libs/thewatcher-1.0.0.jar`: copialo nella cartella `mods` insieme a Fabric API.

Per provarla direttamente da sviluppo: `./gradlew runClient`.

## Cosa succede in gioco

- **Nell'Overworld**, dopo 45 secondi dall'ingresso, un "regista" decide a intervalli casuali:
  - **Atmosfera**: sussurri nel buio e battiti cardiaci.
  - **L'Osservatore** compare a 20-30 blocchi, fuori dalla tua visuale. Se lo guardi **si blocca e ti fissa**; appena distogli lo sguardo **ti corre incontro**. Se ti raggiunge, o se lo fissi da vicino per 5 secondi, scatta il jumpscare. Se lo fissi da lontano per 2,5 secondi, svanisce.
  - **Accordi dissonanti** improvvisi (falso allarme).
  - **Jumpscare a sorpresa** (solo se sei al buio).
- **Jumpscare**: flash bianco, immagine a schermo intero che trema, urlo, lampi rossi, poi 4 secondi di cecita'.

## Comandi di prova

Agiscono **solo su chi li digita** (non si puo' spaventare gli altri):

| Comando | Effetto |
|---|---|
| `/watcher jumpscare` | Jumpscare immediato |
| `/watcher stalker` | Fa comparire l'Osservatore |
| `/watcher stinger` / `heartbeat` / `ambient` | Solo il suono |
| `/watcher on` / `off` | Attiva o disattiva gli eventi casuali per te |
| `/watcher intensity <1-5>` | Frequenza degli eventi (default 2) |

Le impostazioni dei comandi si azzerano al riavvio del server.

## Personalizzare

| Cosa | Dove |
|---|---|
| Frequenza e probabilita' degli eventi | `HorrorDirector.java` (`WAIT_FACTOR`, `runEvent`, `scaled(...)`) |
| Comportamento e velocita' del mostro | `StalkerEntity.java` |
| Immagini del jumpscare | `assets/thewatcher/textures/gui/jumpscare_1.png`, `jumpscare_2.png` (qualsiasi PNG 16:9, ideale 640x360) |
| Skin del mostro | `assets/thewatcher/textures/entity/stalker.png` (64x64, layout skin standard) |
| Suoni | `assets/thewatcher/sounds/*.ogg` (OGG Vorbis mono) |

Per usare un'altra immagine basta sostituire i PNG mantenendo lo stesso nome. Per aggiungerne una terza, aggiungila all'array `FACES` in `JumpscareOverlay.java`.

## Passare alla 26.4 (quando sara' uscita)

In `gradle.properties` aggiorna `minecraft_version`, `loader_version` e `fabric_api_version` con i valori raccomandati su <https://fabricmc.net/develop>, e in `fabric.mod.json` cambia `"minecraft": "~26.3"` in `"~26.4"`. Se Mojang ha rinominato qualche classe, il compilatore ti indichera' dove.
