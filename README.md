# Monohelper Android

Native Android client for the [Monohelper Local](../Monohelper_Local) backend — a
Raspberry Pi FastAPI monolith that syncs Monobank accounts/transactions into SQLite.
The app mirrors the React web frontend: **Dashboard** (monthly budget report +
balance chart), **Accounts**, **Transactions** (account drill-down), and **Sync**
(enqueue a background task, poll to completion), plus a worker-health badge.

## Stack

- Kotlin + Jetpack Compose, Material 3
- MVVM + clean layering: `core` / `data` / `domain` / `ui` / `di` (one ViewModel per screen)
- Coroutines + Flow; ViewModels expose `StateFlow<UiState>`
- Retrofit + kotlinx.serialization, OkHttp
- Hilt DI
- Chart: plain Compose `Canvas` (no chart dependency)
- **No offline DB** — network-only, repositories cache in `StateFlow`

## Package layout

```
com.monohelper
├── core/        config (ApiConfig), format (Money, DateFmt), result (AppResult)
├── data/        api (Retrofit + DTOs + BaseUrlInterceptor), config, repo (4 repositories + mappers)
├── domain/      model (UI-facing models), usecase (ListAccounts, ListTransactions,
│                GetMonthlyReport, EnqueueSync, PollTask, GetWorkerHealth)
├── ui/          dashboard / accounts / transactions / sync, nav, components, theme
└── di/          Hilt modules (network, config, repo bindings)
```

DTOs never leave `data`; `domain`/`core` have no Android framework types (pure-JVM testable).

## Base URL (dual-mode)

The backend runs two ways; the app holds **one configurable base URL** (gear icon in
the app bar, persisted in SharedPreferences):

| Mode | Base URL |
|---|---|
| Direct | `http://<pi-host>:8088` |
| Gateway | `http://<gateway-host>:8888/cloudapi` |

`BaseUrlInterceptor` rewrites every request at call time (scheme/host/port + path
prefix), so `/cloudapi` is never hardcoded. Default for fresh installs comes from
`DEFAULT_BASE_URL` in `app/build.gradle.kts`. Cleartext HTTP is enabled (local network,
no auth — matching the backend).

## Conventions

- **Money**: all API values are integers in minor units; rendering goes through
  `Money.format` / `MoneyText` only — no inline `/100` math.
- **Time**: API `time` fields are Unix **seconds**; formatting goes through `DateFmt`.
- Sync endpoints return `202 {task_id}`; `PollTask` polls `GET /tasks/{id}` every 1.5 s
  until `success`/`error` (tolerates up to 3 consecutive transient failures).

## Build & test

```bash
./gradlew :app:assembleDebug    # build APK (needs Android SDK; see local.properties)
./gradlew :app:testDebugUnitTest  # all tests — pure-JVM + Robolectric Compose tests
```

`local.properties` (not committed): `sdk.dir=/path/to/Android/sdk`.

Tests: JUnit + Turbine for `StateFlow`, MockWebServer for the Retrofit layer (covers
both direct and gateway base-URL modes), fake repositories for ViewModels, Robolectric
for per-screen Compose happy-path tests. No emulator needed.

## API contract

The backend API reference lives in the backend repo:
`Monohelper_Local/api_docs/API.md`. DTOs in `data/api/Dtos.kt` map it 1:1.
