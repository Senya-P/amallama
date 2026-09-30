# amallama

![JDK 25](https://img.shields.io/badge/JDK-25-ED8B00?logo=openjdk&logoColor=white)
![Maven 3.9+](https://img.shields.io/badge/Maven-3.9%2B-C71A37?logo=Apache+Maven&logoColor=white)
![JavaFX 25](https://img.shields.io/badge/JavaFX-25-5382a1)
![Platforms](https://img.shields.io/badge/platform-Windows%20%7C%20macOS%20%7C%20Linux-lightgrey)

A cross-platform desktop app for running local LLMs through
[llamafile](https://github.com/mozilla-ai/llamafile). Pick a model, press **Load**,
chat — everything stays on your machine.

## Features

- **Local by design** — models and runtime state live in `~/.amallama`; nothing is sent anywhere
- **Hardware aware** — probes CPU cores and GPU VRAM, offloads fully to the GPU when the model
  fits and falls back to the CPU when it doesn't
- **Download models** — paste a direct `.gguf` link and it lands in your models directory
- **Live telemetry** — layers offloaded, memory projection and context size, read from the
  runtime's own output
- **OpenAI-compatible API** — while running, llamafile serves `127.0.0.1:8080` for any client

## Requirements

- JDK 25 (the build targets release 25)
- Maven 3.9+
- a llamafile binary — the app does not bundle one

## Quickstart

1. **Get llamafile** from <https://github.com/mozilla-ai/llamafile>. Either a self-contained
   llamafile (model embedded), or a bare `llamafile` binary plus a separate `.gguf` model.
2. **Drop it into `~/.amallama/models/`.** A file named `llamafile` (`llamafile.exe` on Windows)
   is found automatically; anything else can be pointed at with **Browse...** in the Model tab.
3. **Run the app:**

   ```bash
   mvn -pl ui -am javafx:run
   ```

4. In the **Model** tab, select a model and press **Load**. When the status dot goes green,
   the **Chat** tab is live.

The Model tab takes a direct download link — paste a Hugging Face
`resolve/.../<file>.gguf?download=true` URL and it is saved into the models directory.

## Using the model from other apps

While the runtime is RUNNING, llamafile serves an OpenAI-compatible API on
`127.0.0.1:8080`. Any client can talk to it directly:

```bash
curl http://127.0.0.1:8080/v1/models
```

```bash
curl http://127.0.0.1:8080/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{"model":"llamafile","messages":[{"role":"user","content":"Hello"}]}'
```


## Configuration

On every platform the app keeps its state in `~/.amallama/`
(`%USERPROFILE%\.amallama\` on Windows):

```
~/.amallama/
├── config.json     last model + runtime binary
└── models/         model files and the llamafile binary
```

## Build and test

```bash
mvn test                      
mvn -Ppackage package         # jpackage -> target/dist (exe / dmg / app-image)
```

## License

Copyright (c) 2026 the amallama authors.

amallama is free software: you can redistribute it and/or modify it under the terms of the
[GNU General Public License v3.0 or later](LICENSE). 

Any distributed derivative must also be released under the GPL along with its source.

### Third-party components

| Component | License |
| --- | --- |
| [Ioskeley Mono](https://github.com/ahatem/ioskeley-mono) — bundled font | SIL Open Font License 1.1 (`ui/src/main/resources/cz/cuni/mff/ui/fonts/OFL.txt`) |
| JavaFX / OpenJFX | GPLv2 + Classpath Exception |
| Jackson Databind | Apache-2.0 |
| OSHI | MIT |
| JUnit Jupiter — tests only | EPL-2.0 |
| llamafile — launched as a separate process, not linked | MPL-2.0 |
