# SlotSense (Java) — Slot Game Launch System

**SlotSense** is a Java project built to showcase real-world **slot engine** skills: reproducible RNG, a step-based pipeline (Step flow), typed output contracts (POJOs → JSON), Monte Carlo simulation, and a “launcher” CLI to run spins and large-scale simulations.

> Goal: a **reusable framework** to launch slot games (config per game/version) with production-grade output plus math/validation tooling.

---

## What’s inside (high level)

- **Core Engine**
    - Reproducible RNG (seeded)
    - `SpinContext` (spin state)
    - `Step` pipeline (each step mutates context + emits structured events)
    - Base engine + concrete engines
- **Official Output Contract (Schema)**
    - `SpinResult` / `SessionResult` as POJOs (no `Map<String,Object>`)
    - Clean JSON serialization
- **Standard feature event system**
    - `bonusInfo[]` as a structured event stream per step/feature
- **Math & simulation**
    - Monte Carlo: RTP, hit rate, payout percentiles, rough volatility
    - Statistical tests with tolerances
- **Launcher / CLI**
    - `spin` and `simulate` commands
    - Config loading via `--game` and `--version`
- **CI / Reports**
    - Unit tests + quick simulation in GitHub Actions to catch regressions

---

## Proposed package/module layout

```text
core/    (RNG, SpinContext, Step, BaseEngine)
math/    (RTP, hit rate, volatility, simulators)
games/   (game configs and concrete engines)
runner/  (CLI for spins/sim)
tests/   (unit + statistical)
```
## Roadmap — Slot Game Launch System

### Phase 0 — Solid foundation (1–2 days)

- **Package/module structure**
    - `core/` (RNG, SpinContext, Step, BaseEngine)
    - `math/` (RTP, hit rate, volatility, simulators)
    - `games/` (game configs + concrete engines)
    - `runner/` (CLI for spins/sim)
    - `tests/` (unit + statistical)

- **Official output contract (output schema)**
    - Define a stable JSON schema for `SpinResult` / `SessionResult`
    - This schema is the source of truth for the entire project

- **Clean serialization with POJOs**
    - Move from `Map<String,Object>` to typed classes like `SpinResult`, `BonusInfo`, etc.
    - Portfolio signal: “clean architecture / typed output”

---

### Phase 1 — Production-grade output (current priority)

- **Standard `bonusInfo / feature events` system**
    - Each `Step` can emit a structured block:
        - `bonusName`
        - `payload` (step-specific data)
        - `creditsWon` (incremental)
        - optional: `newReelLayout`, `cascadeList`, etc.
    - Keep `events` (human-readable logs) optional; the main contract is `bonusInfo`

- **Single spin mode + session mode**
    - **Single**: returns a list with 1 object `{ type: "spin", ... }`
    - **Session**: `baseSpin + freeSpins[]` (can also be wrapped in a list for server uniformity)

---

### Phase 2 — Real math (prove “I can build engines”)

- **Monte Carlo simulator**
    - `simulate(nSpins, config, seed)` → metrics:
        - Total RTP + RTP by feature
        - Hit rate (any win)
        - Bonus hit rate
        - Payout distribution (p50/p90/p99/max)
        - Rough volatility (std dev / optional skew)

- **Statistical (tolerance-based) tests**
    - RTP within expected range (e.g. `96% ± 0.3`)
    - Bonus hit rate ~ config (± tolerance)
    - Reproducibility: same seed → same results

---

### Phase 3 — Game “launch system”

- **Game Registry + Config Loader**
    - Load JSON/YAML configs by game name + version
    - `GameDefinition`: reels, symbols, paytable, enabled features, etc.

- **CLI Runner / Launcher**
    - `./gradlew run --args="spin --game demo --bet 1 --seed 123"`
    - `./gradlew run --args="simulate --spins 1000000 --report out.json"`

- **CI + Reports**
    - GitHub Actions: unit tests + “quick sim” (100k spins) to detect regressions




