# Sound effect sources

All runtime SFX in `src/game/sound/assets/` were trimmed/normalized from reusable source audio. The game code does not require attribution for the CC0 items below, but the sources are recorded here for provenance.

## CC0 / public-domain sources

- **Golf club / ball strike** — `golf ball hits stereo.wav` by `fluctuating_frequency`, Freesound, CC0: https://freesound.org/s/622415/
- **Grass impact** — `Landing on grass` by `onehugeeye`, Freesound, CC0: https://freesound.org/people/onehugeeye/sounds/511318/
- **Ice impact** — `ICEImpt_ImpactBreak01_InMotionAudio_FREESampleSunday` by `InMotionAudio`, Freesound, CC0: https://freesound.org/people/InMotionAudio/sounds/719169/
- **Golf ball entering hole** — `Golf ball in hole.wav` by `Scottrex05`, Freesound, CC0: https://freesound.org/people/Scottrex05/sounds/593482/
- **Water splash / sand texture** — `Water Splash and sand footsteps` by Peludo, OpenGameArt, CC0: https://opengameart.org/content/water-splash-and-sand-footsteps
- **Menu select / combo accent** — `Various Sound Effects` by Spring Spring, OpenGameArt, CC0: https://opengameart.org/content/various-sound-effects-0

## Runtime processing

The checked-in `.wav` files are short PCM 16-bit clips derived from the sources above. Long acquisition files are intentionally not kept in the repository. Multiple variants are used for club, grass, ice, sand, water, and cup impacts so repeated collisions do not sound identical.
