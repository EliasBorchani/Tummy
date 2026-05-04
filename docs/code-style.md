# Code style

Project-wide Kotlin conventions, applied beyond what stock formatting catches.
For *what to import*, *which dispatcher to avoid*, etc., see
[kmp-gotchas.md](./kmp-gotchas.md).

## Naming

### Read methods on repos / DAOs use `get`, not `observe` / `fetch` / `load`

When the same method covers several cases (range, single date, all rows),
prefer one generic function with default arguments over multiple specialized
ones.

```kotlin
// good
fun getStandard(start: String? = null, endInclusive: String? = null): Flow<...>

// avoid
fun observeStandardRange(start: String, endInclusive: String): Flow<...>
fun observeStandardAtDate(date: String): Flow<...>
```

## Function bodies

### Single-expression bodies are reserved for true one-liners

Multi-line bodies use a regular block with explicit `return`, not
`fun foo() = bigExpression.run { ... }`.

```kotlin
// good — one liner
override fun getAtDate(date: LocalDate): Flow<List<X>> =
    get(start = date, endInclusive = date)

// good — multi-line uses { ... } with return
override fun get(start: LocalDate?, endInclusive: LocalDate?): Flow<List<X>> {
    return combine(daoStandard, daoCustom) { standards, customs ->
        (standards + customs).sortedBy { it.date }
    }
}

// avoid — multi-line single-expression body
override fun get(...) = combine(daoStandard, daoCustom) {
    ...
}
```

## Use cases

### `suspend fun invoke(...)`, never `suspend operator fun invoke(...)`

The `operator` modifier breaks Android Studio's "Find Usages" — references to
`useCase(...)` aren't tracked back to the declaration. Drop the keyword and
call sites use the explicit form `useCase.invoke(...)`. The verbosity is
worth the navigability.

```kotlin
// good
class UpsertIngredientLogUseCase(...) {
    suspend fun invoke(date: LocalDate, ingredient: Ingredient) { ... }
}
// call site
upsertIngredientLog.invoke(date, ingredient)

// avoid
suspend operator fun invoke(date: LocalDate, ingredient: Ingredient) { ... }
upsertIngredientLog(date, ingredient)
```

## MVI / Presentation

### State is reactive, never mutable

`utilities:presentation` exposes `BaseViewModel<State, Intent, Event>`:

- `abstract val state: StateFlow<State>` — subclass overrides, builds via
  `combine(...).stateIn(...)`. **No `updateState` reducer.** Imperative bits
  (text input, ephemeral toggles) live in private `MutableStateFlow`s
  combined into the public state flow.
- `abstract fun onIntent(intent: Intent)` — user input dispatch.
- `events: SharedFlow<Event>` — one-shot effects (navigation, toast).

Reactive state shape:

```kotlin
class XxxViewModel(
    private val repo: XxxRepository,
) : BaseViewModel<XxxState, XxxIntent, XxxEvent>() {

    private val isRefreshing = MutableStateFlow(false)

    override val state: StateFlow<XxxState> = combine(
        repo.get(),
        isRefreshing,
    ) { items, refreshing ->
        XxxState(items = items, refreshing = refreshing)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = XxxState(),
    )

    override fun onIntent(intent: XxxIntent) { ... }
}
```

For loading-aware flows, wrap with `AsyncData<T>` from `utilities:kotlin-ext`
(`Flow<T>.toAsyncDataFlow()` emits `Loading` first, then `Ready(data)`). Use
sum-type state shapes (`sealed interface XxxState { Loading; Ready(...) }`)
when the loading distinction matters at the UI level.

### Feature module shape

A feature module is commonMain-only. Exactly four file kinds:

- `XyzState.kt` — one `data class`.
- `XyzIntent.kt` — one `sealed interface`.
- `XyzEvent.kt` — one `sealed interface`.
- `XyzViewModel.kt` — extends `BaseViewModel<XyzState, XyzIntent, XyzEvent>`,
  declares dependencies via constructor, wires use cases.

The VM knows nothing about platform or navigation. It emits a navigation
`Event`; the platform-side UI observes `events` and pushes routes.

Register the VM in two places:
- `umbrella/TummyKoin.kt` — `factory { XyzViewModel(get(), ...) }` in
  `viewModelsModule` (iOS).
- `composeApp/.../di/AppModule.kt` — `viewModelOf(::XyzViewModel)` (Android,
  required for `koinViewModel()` and `NavBackStackEntry`-scoped storage).

### What about parameters?

When a VM needs a value that isn't available from the graph (e.g. a date for
a per-day log screen), inject via `parametersOf(...)` at the call site:

- Android: `koinViewModel<MyVM> { parametersOf(date) }`.
- iOS: `TummyDI.shared.myViewModel(date: date)` (the typed façade in umbrella
  unpacks `parametersOf` for you).

`viewModelsModule` then declares `factory { params -> MyVM(params.get(), get(), ...) }`.
