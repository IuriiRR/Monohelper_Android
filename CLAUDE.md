# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test

```bash
./gradlew :app:assembleDebug           # build APK
./gradlew :app:testDebugUnitTest       # all unit tests (no emulator needed)
./gradlew :app:testDebugUnitTest --tests "com.monohelper.ui.dashboard.*"  # single class/package
```

`local.properties` (not committed) must contain: `sdk.dir=/path/to/Android/sdk`

## Architecture

MVVM + clean layering — **no Android types in `domain`/`core`** (pure-JVM testable):

```
core/    → ApiConfig, Money/DateFmt formatters, AppResult<T>
data/    → Retrofit ApiService, DTOs, BaseUrlInterceptor, 4 repositories + Mappers
domain/  → UI-facing models, 6 use cases (thin wrappers over repositories)
ui/      → 4 screens (Dashboard/Accounts/Transactions/Sync), nav, shared components
di/      → 3 Hilt modules (network, config, repo bindings)
```

- ViewModels expose `StateFlow<UiState>` sealed classes; screens collect with `collectAsStateWithLifecycle`
- `AppResult<T>` (Success/Error) flows from repo → use case → ViewModel; never throw across layers
- DTOs are confined to `data/`; domain `Models.kt` holds the UI-facing types; `Mappers.kt` converts
- No offline DB — repositories cache in `StateFlow`, network-only

## Key Invariants

- **Money**: API values are integers (minor units). Render only via `Money.format` / `MoneyText`. No inline `/100` math anywhere.
- **Time**: API `time` fields are Unix **seconds**. Format only via `DateFmt`.
- **Base URL**: Dual-mode (direct `http://pi:8088` or gateway `http://gw:8888/cloudapi`). `BaseUrlInterceptor` rewrites at call time — never hardcode `/cloudapi` in routes. User-editable via gear icon, persisted in SharedPreferences (`PrefsApiConfig`).
- **Sync polling**: `PollTask` polls every 1.5 s, tolerates ≤3 consecutive transient failures before surfacing error.

## Testing Conventions

- Fake repositories live in `testutil/Fakes.kt`; `TestData.kt` holds shared test fixtures
- `MainDispatcherRule` replaces `Dispatchers.Main` with `UnconfinedTestDispatcher` — add to every ViewModel test
- `ApiServiceTest` uses MockWebServer and tests both direct and gateway URL modes
- Robolectric Compose tests cover per-screen happy paths (`*ScreenTest.kt`)
- Turbine used for `StateFlow` assertions in ViewModel tests

## API Contract

Backend API reference: `../Monohelper_Local/api_docs/API.md`. `data/api/Dtos.kt` maps it 1:1.
