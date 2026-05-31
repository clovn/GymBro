# Техническое задание: Android-приложение GymBro

**Проект:** GymBro — мобильная экосистема для поиска мест тренировок, партнёров и групповых тренировок  
**Платформа:** Android  
**Язык:** Kotlin  
**UI:** Jetpack Compose  
**Архитектура:** многомодульное приложение, Clean Architecture + Orbit MVI  
**DI:** Koin  
**Сетевой слой:** Retrofit + OkHttp  
**Версия документа:** 2.0  
**Статус:** уточнённое ТЗ для мобильной разработки  

---

## 1. Цель приложения

GymBro помогает пользователю:

- находить спортивные места рядом: залы, площадки, парки, стадионы и пользовательские точки;
- искать партнёров по тренировкам поблизости;
- создавать и посещать групповые тренировки;
- общаться в личных и групповых чатах;
- управлять профилем, приватностью, уведомлениями и историей тренировок.

Главный пользовательский сценарий: пользователь открывает карту, видит доступные места, людей и тренировки рядом, выбирает подходящий объект, присоединяется к тренировке или создаёт свою.

---

## 2. Основные роли пользователей

### 2.1. Гость

Пользователь без авторизации.

Доступно:

- splash screen;
- onboarding;
- вход;
- регистрация;
- восстановление пароля.

Недоступно:

- карта с пользовательскими данными;
- создание тренировок;
- чаты;
- профиль;
- уведомления.

### 2.2. Авторизованный пользователь

Доступно:

- карта;
- поиск мест;
- поиск людей;
- просмотр карточек мест и тренировок;
- создание UGC-точек;
- создание планов тренировок;
- присоединение к тренировкам;
- личные и групповые чаты;
- профиль;
- настройки;
- уведомления.

---

## 3. Технологический стек Android-приложения

| Зона | Технология |
|---|---|
| Язык | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Архитектура UI | Orbit MVI |
| Архитектура проекта | Многомодульная Clean Architecture |
| DI | Koin |
| REST API | Retrofit 2 |
| HTTP-клиент | OkHttp 4 |
| JSON | Kotlinx Serialization или Moshi, выбрать до старта разработки |
| Асинхронность | Kotlin Coroutines, Flow |
| Навигация | Jetpack Navigation Compose |
| Карты | Yandex MapKit SDK |
| Геолокация | FusedLocationProviderClient |
| Real-time | OkHttp WebSocket или Scarlet |
| Push | Firebase Cloud Messaging |
| Локальная БД | Room |
| Настройки | DataStore |
| Защищённое хранение токенов | EncryptedSharedPreferences / AndroidX Security Crypto |
| Изображения | Coil |
| Тесты | JUnit, MockK, Turbine, MockWebServer, Compose UI Testing |
| Min SDK | 26 |
| Target SDK | 34+ |
| Сборка | Gradle Kotlin DSL |

---

## 4. Модульная структура проекта

Приложение должно быть разбито на независимые Gradle-модули. Feature-модули не должны напрямую зависеть друг от друга. Общие сущности, навигационные контракты и UI-компоненты выносятся в core-модули.

### 4.1. Рекомендуемая структура

```text
:app

:core:common
:core:designsystem
:core:network
:core:database
:core:datastore
:core:domain
:core:navigation
:core:location
:core:notifications
:core:map
:core:testing

:feature:auth
:feature:onboarding
:feature:map
:feature:place
:feature:workout
:feature:people-search
:feature:profile
:feature:chat
:feature:notifications
:feature:settings
```

### 4.2. Назначение модулей

| Модуль | Ответственность |
|---|---|
| `:app` | Точка входа, DI-инициализация, NavHost, темы приложения |
| `:core:common` | Result-обёртки, extensions, dispatchers, errors, utils |
| `:core:designsystem` | Тема, цвета, типографика, кнопки, поля, карточки, bottom sheets, chips |
| `:core:network` | Retrofit, OkHttp, interceptors, API DTO, обработка ошибок |
| `:core:database` | Room, DAO, entities, migrations |
| `:core:datastore` | Onboarding flag, user preferences, theme, privacy cache |
| `:core:domain` | Domain-модели, repository interfaces, use cases |
| `:core:navigation` | Типобезопасные routes и deep links |
| `:core:location` | Геолокация, permission flow, координаты пользователя |
| `:core:notifications` | FCM token, обработка push, notification channels |
| `:core:map` | Общие обёртки MapKit, marker renderer, cluster utils |
| `:core:testing` | Test doubles, coroutine rules, fake repositories |
| `:feature:*` | UI, Container/ViewModel, feature use cases, mappers |

### 4.3. Правила зависимостей

```text
feature -> core:designsystem
feature -> core:domain
feature -> core:navigation
feature -> core:common
feature -> core:network только через data-реализацию, если модуль содержит data layer

core:domain не зависит от Android SDK, Retrofit, Room, Compose
core:designsystem может зависеть от Compose и Material 3
app зависит от всех feature-модулей
```

---

## 5. Архитектура: Clean Architecture + Orbit MVI

### 5.1. Слои

| Слой | Содержимое | Ответственность |
|---|---|---|
| Presentation | Compose screens, components, Orbit Container/ViewModel | UI, состояние экрана, действия пользователя, side effects |
| Domain | UseCase, Entity, Repository interface | Бизнес-логика, правила, независимость от Android/API |
| Data | RepositoryImpl, Retrofit API, Room DAO, mappers | Работа с сетью, БД, кэшем, синхронизацией |

### 5.2. MVI-контракт экрана

Для каждого сложного экрана должен быть описан контракт:

```kotlin
data class ScreenState(
    val isLoading: Boolean = false,
    val data: Data? = null,
    val error: UiError? = null
)

sealed interface ScreenSideEffect {
    data class ShowSnackbar(val message: String) : ScreenSideEffect
    data class Navigate(val route: String) : ScreenSideEffect
}

sealed interface ScreenAction {
    data object OnRetryClick : ScreenAction
    data object OnBackClick : ScreenAction
}
```

### 5.3. Orbit MVI требования

- Для каждого экрана используется `ContainerHost<State, SideEffect>`.
- Все пользовательские действия проходят через intent-функции.
- Навигация, Snackbar, Toast, открытие внешних приложений — только через side effects.
- Состояние экрана immutable.
- Ошибки приводятся к единой UI-модели: `UiError.Network`, `UiError.Server`, `UiError.Unauthorized`, `UiError.Validation`, `UiError.Unknown`.
- Для списков с пагинацией состояние должно содержать `isRefreshing`, `isLoadingMore`, `canLoadMore`.

---

## 6. Dependency Injection: Koin

### 6.1. Общие требования

- Использовать Koin вместо Hilt.
- Все зависимости регистрируются по модулям: `networkModule`, `databaseModule`, `repositoryModule`, `useCaseModule`, `featureModule`.
- Feature-модули предоставляют свои Container/ViewModel через `viewModel { ... }`.
- Для Retrofit API использовать `single`.
- Для UseCase можно использовать `factory`, если объект не хранит состояние.

### 6.2. Пример структуры DI

```kotlin
val networkModule = module {
    single { provideOkHttpClient(get(), get()) }
    single { provideRetrofit(get()) }
    single<AuthApi> { get<Retrofit>().create(AuthApi::class.java) }
}

val authModule = module {
    factory { LoginUseCase(get()) }
    factory { RefreshTokenUseCase(get()) }
    viewModel { AuthViewModel(get(), get()) }
}
```

### 6.3. Scope

- Singleton: Retrofit, OkHttpClient, RoomDatabase, DataStore, TokenStorage, WebSocketManager.
- Factory: UseCase, mappers.
- ViewModel: screen state containers.

---

## 7. Дизайн-система

Дизайн приложения основан на макетах GymBro v1. Визуальная концепция: чистый светлый интерфейс, акцентный синий цвет, много воздуха, карточки с закруглениями, нижняя навигация и фокус на карте как главном сценарии.

### 7.1. Общий стиль

- Минималистичный mobile-first интерфейс.
- Белый или очень светлый фон.
- Акцентный синий используется для основных действий, активных табов, выбранных фильтров, FAB и CTA-кнопок.
- Вторичные действия отображаются через outline/button text style.
- Карточки отделяются мягкой тенью или светлой границей.
- Ключевые формы имеют вертикальную структуру: заголовок, поля, основной CTA, вторичное действие.
- Большинство экранов используют верхний app bar с кнопкой назад или меню `...`.

### 7.2. Цветовая палитра

| Токен | Назначение | Примерное значение |
|---|---|---|
| `Primary` | Основные кнопки, активные элементы, FAB | `#2F6BFF` |
| `PrimaryDark` | Pressed/hover state | `#1F55D8` |
| `PrimaryLight` | Фон выбранных chips, иконки в onboarding | `#EAF1FF` |
| `Background` | Основной фон | `#FFFFFF` |
| `Surface` | Карточки, bottom sheets | `#FFFFFF` |
| `SurfaceVariant` | Поля ввода, неактивные блоки, skeleton | `#F4F6FA` |
| `TextPrimary` | Основной текст | `#111827` |
| `TextSecondary` | Подписи, вторичный текст | `#6B7280` |
| `TextTertiary` | Placeholder, metadata | `#9CA3AF` |
| `Divider` | Разделители | `#E5E7EB` |
| `Success` | Успешные статусы | `#22C55E` |
| `Warning` | Предупреждения | `#F59E0B` |
| `Error` | Ошибки, destructive actions | `#EF4444` |

Точные значения должны быть согласованы с дизайн-макетами и вынесены в `GymBroColors` внутри `:core:designsystem`.

### 7.3. Типографика

Использовать Material 3 typography с кастомной настройкой размеров.

| Стиль | Использование |
|---|---|
| `displaySmall` | Крупные заголовки onboarding/splash при необходимости |
| `headlineMedium` | Заголовки главных экранов |
| `titleLarge` | Заголовки карточек: место, тренировка, профиль |
| `titleMedium` | Заголовки секций |
| `bodyLarge` | Основной текст |
| `bodyMedium` | Описания, формы, списки |
| `labelLarge` | Кнопки, chips |
| `labelSmall` | Metadata: время, расстояние, статусы |

Требования:

- Минимальный размер кликабельного текста — 14sp.
- Метаданные не меньше 12sp.
- Заголовки должны быть визуально контрастнее body-текста.
- Для длинных описаний использовать `expandable text` с кнопкой «Показать ещё».

### 7.4. Spacing и размеры

| Токен | Значение | Использование |
|---|---:|---|
| `space_4` | 4dp | Мелкие отступы внутри строк |
| `space_8` | 8dp | Расстояние между близкими элементами |
| `space_12` | 12dp | Внутренние отступы chips/cards |
| `space_16` | 16dp | Основной горизонтальный padding экрана |
| `space_20` | 20dp | Большие блоки формы |
| `space_24` | 24dp | Отступы между секциями |
| `space_32` | 32dp | Hero-секции, onboarding |

Требования:

- Горизонтальные отступы экранов: 16dp.
- Карточки: padding 12-16dp.
- BottomSheet: верхние скругления 24dp.
- Основные кнопки: высота 48-52dp.
- FAB: стандартный Material 3 размер, иконка по центру.
- Минимальная touch target area: 48x48dp.

### 7.5. Скругления

| Токен | Значение | Использование |
|---|---:|---|
| `radiusSmall` | 8dp | Chips, small badges |
| `radiusMedium` | 12dp | Input fields, small cards |
| `radiusLarge` | 16dp | Cards, list items |
| `radiusExtraLarge` | 24dp | Bottom sheets, modal blocks |
| `radiusFull` | 999dp | Avatars, pills |

### 7.6. Компоненты дизайн-системы

В `:core:designsystem` нужно реализовать переиспользуемые компоненты:

- `GymBroButton`
- `GymBroOutlinedButton`
- `GymBroTextButton`
- `GymBroTextField`
- `GymBroPasswordField`
- `GymBroSearchBar`
- `GymBroFilterChip`
- `GymBroAssistChip`
- `GymBroCard`
- `GymBroBottomSheet`
- `GymBroTopBar`
- `GymBroBottomNavigationBar`
- `GymBroAvatar`
- `GymBroRatingBar`
- `GymBroEmptyState`
- `GymBroErrorState`
- `GymBroShimmer`
- `GymBroLoadingButton`

### 7.7. Иконки и графика

- Для интерфейсных иконок использовать Material Symbols / Material Icons.
- Для карты использовать отдельный набор маркеров: место, UGC, тренировка, человек.
- Для onboarding использовать простые line/filled иллюстрации в синем акцентном стиле.
- Для аватаров без фото использовать цветной круг с первой буквой имени.
- Для рейтинга использовать звёзды и числовое значение.

### 7.8. Состояния компонентов

Каждый интерактивный компонент должен иметь состояния:

- default;
- pressed;
- disabled;
- loading;
- error, если применимо;
- selected, если применимо.

Для форм обязательно отображать ошибку под полем, а не только Snackbar.

### 7.9. Тёмная тема

На первом релизе тёмная тема может быть отложена, но архитектура темы должна позволять добавить её без переписывания UI.

Минимальное требование:

- все цвета используются через design tokens;
- запрещено хардкодить цвета внутри feature-экранов;
- системная тема учитывается через `isSystemInDarkTheme()`, даже если dark palette пока повторяет light palette частично.

---

## 8. Навигация приложения

### 8.1. Нижняя навигация

Основной интерфейс после авторизации использует нижний таб-бар с 4 вкладками:

| Таб | Экран | Бейдж |
|---|---|---|
| Explore / Карта | Карта, места, тренировки | Нет |
| Search / Поиск | Поиск мест и людей | Нет |
| Messages / Чаты | Список чатов | Количество непрочитанных |
| Profile / Профиль | Мой профиль | Индикатор новых уведомлений при необходимости |

В дизайне вкладки отображаются внизу экрана, активный пункт выделяется синим цветом.

### 8.2. Стек экранов

```text
Auth flow:
Splash -> Onboarding -> Sign In / Sign Up -> Main
Splash -> Main, если токен валиден
Sign In -> Reset Password

Main tabs:
Map -> Place Info -> All Reviews
Map -> Workout Info -> Chat
Map -> Plan a Workout -> Choose Place -> Create Plan
Map -> Add a Place -> Select Location

Search -> Search Places
Search -> Search People -> People Profile -> Chat

Messages -> Chat -> Profile
Messages -> New Chat

Profile -> Edit Profile
Profile -> My Workout Plans -> Create New Plan
Profile -> My Workouts Upcoming/Past
Profile -> Settings -> About
Profile -> Notifications
```

### 8.3. Deep links

| Deep link | Экран |
|---|---|
| `gymbro://location/{id}` | Карточка места |
| `gymbro://event/{id}` | Карточка тренировки |
| `gymbro://user/{id}` | Профиль пользователя |
| `gymbro://chat/{conversationId}` | Чат |
| `gymbro://notifications` | Уведомления |

---

## 9. Экраны и функциональные требования

## 9.1. Splash

### Назначение

Экран запуска приложения. Проверяет состояние авторизации и определяет следующий экран.

### UI

- Синий полноэкранный фон.
- Логотип GymBro по центру.
- Название приложения.
- Короткий tagline.
- Индикатор загрузки.

### Логика

1. Проверить, был ли пройден onboarding.
2. Проверить наличие access token и refresh token.
3. Если access token валиден — перейти на карту.
4. Если access token истёк — выполнить refresh.
5. Если refresh успешен — перейти на карту.
6. Если токенов нет — перейти на onboarding или sign in.

### Состояния

- loading;
- unauthorized;
- token refreshed;
- network error;
- fatal error.

---

## 9.2. Onboarding

### Назначение

Показать ценность приложения перед регистрацией.

### Экраны onboarding

1. **Find Training Spots** — поиск мест для тренировок.
2. **Connect with Buddies** — поиск партнёров.
3. **Join Group Workouts** — групповые тренировки.

### UI

- Белый фон.
- Иконка/иллюстрация в светло-синем круге.
- Заголовок.
- Краткое описание.
- Dots indicator.
- Основная кнопка `Continue` / `Get Started`.
- Кнопка `Skip` справа сверху.

### Логика

- Onboarding показывается только один раз.
- Флаг `onboarding_completed` хранится в DataStore.
- Skip также устанавливает флаг прохождения.

---

## 9.3. Sign In

### Назначение

Авторизация пользователя.

### UI

- Верхний логотип GymBro.
- Заголовок `Welcome Back`.
- Поле email.
- Поле password с toggle показа пароля.
- Ссылка `Forgot Password?`.
- Основная кнопка `Sign In`.
- Разделитель `or`.
- Кнопка `Continue with Google`.
- Ссылка на регистрацию.

### Валидация

- Email обязателен и должен соответствовать email-формату.
- Password обязателен.
- При ошибке авторизации показывать сообщение под формой или Snackbar.

### API

```http
POST /auth/token
```

### Состояния

- default;
- loading;
- validation error;
- invalid credentials;
- no internet;
- server error;
- success.

---

## 9.4. Sign Up

### Назначение

Создание аккаунта.

### UI

- Заголовок `Create Account`.
- Поля: full name, email, password.
- Основная кнопка `Create Account`.
- Ссылка на вход.

### Валидация

- Имя обязательно.
- Email обязателен и валиден.
- Password минимум 8 символов.
- Желательно проверять сложность пароля: буквы + цифры.

### API

```http
POST /auth/register
```

Если регистрация реализована через Keycloak, точный endpoint согласовать с backend-командой.

---

## 9.5. Reset Password

### Назначение

Восстановление доступа к аккаунту.

### UI

- Заголовок `Forgot Password`.
- Текст с объяснением.
- Поле email.
- Кнопка `Send Reset Link`.

### API

```http
POST /auth/password/reset
```

Endpoint требует подтверждения с backend-командой.

---

## 9.6. Карта / Explore

### Назначение

Главный экран приложения. Отображает карту с местами, тренировками и людьми.

### UI

- Полноэкранная Yandex-карта.
- SearchBar сверху.
- Горизонтальные chips: `Spots`, `Workouts`, `People`.
- Маркеры на карте.
- FAB текущей геолокации.
- FAB добавления места или тренировки.
- Bottom navigation.
- BottomSheet при выборе маркера.

### Слои карты

| Слой | Данные | Маркер | Действие |
|---|---|---|---|
| Spots | Места и UGC-точки | Синий marker/icon | Открыть Place Info |
| Workouts | Планы тренировок | Marker с иконкой активности | Открыть Workout Info |
| People | Пользователи рядом | Avatar marker | Открыть People Profile |

### API

```http
GET /geo/locations/nearby?lat={lat}&lon={lon}&radius={radius}&type={type}
GET /geo/events/nearby?lat={lat}&lon={lon}&radius={radius}
GET /users/nearby?lat={lat}&lon={lon}&radius={radius}
```

### Состояния

- loading markers;
- normal;
- empty area;
- no location permission;
- location disabled;
- network error with cached data;
- API error.

### Требования UX

- Карта не должна блокироваться при загрузке данных.
- При смене фильтра обновлять только соответствующий слой.
- При клике на кластер увеличивать zoom.
- BottomSheet должен быть compact, с возможностью перейти в подробный экран.
- Если геолокация запрещена, показывать понятный empty state с ручным выбором города/области.

---

## 9.7. Search Places

### Назначение

Поиск спортивных мест по названию, типу и расстоянию.

### UI

- SearchBar.
- Chips категорий: `Gym`, `Outdoor`, `Yoga`, `Powerlifting`, `HIIT`.
- Список карточек мест.
- На карточке: фото, название, рейтинг, расстояние, тип.

### API

```http
GET /geo/locations/search?query={query}&type={type}&lat={lat}&lon={lon}&page={page}&size={size}
```

Если endpoint отсутствует, использовать `/geo/locations/nearby` с query-параметром после согласования с backend.

---

## 9.8. Search People

### Назначение

Поиск партнёров для тренировок.

### UI

- SearchBar.
- Tabs/Chips: `People`.
- Список пользователей.
- Карточка: аватар, имя, цель, уровень, расстояние, короткое описание.
- Кнопка фильтров.

### Фильтры

- тип тренировки;
- уровень;
- пол;
- возраст;
- расстояние;
- рейтинг/GymBro Score.

### API

```http
GET /users/nearby?lat={lat}&lon={lon}&radius={radius}&goal={goal}&level={level}&gender={gender}&ageMin={ageMin}&ageMax={ageMax}&page={page}&size={size}
```

---

## 9.9. Filters

### Назначение

Фильтрация результатов поиска.

### UI

- Chips для workout type.
- Chips для goal.
- Slider или range для distance.
- Rating filter.
- Кнопка `Apply filters`.
- Кнопка сброса.

### Логика

- Фильтры должны сохраняться в состоянии текущего поиска.
- После применения пользователь возвращается на предыдущий экран.
- Сброс очищает все значения до дефолтных.

---

## 9.10. Place Info

### Назначение

Подробная карточка места.

### UI

- Большое фото/галерея сверху.
- Кнопка назад.
- Меню `...`.
- Название места.
- Рейтинг и количество отзывов.
- Адрес и расстояние.
- Информация о графике работы.
- Список тегов/оборудования.
- Кнопка `Get Directions`.
- Кнопка `Plan a workout`.
- Блок reviews.
- Кнопка просмотра всех отзывов.

### API

```http
GET /geo/locations/{id}
GET /geo/locations/{id}/reviews?page={page}&size={size}
POST /geo/locations/{id}/review
```

### Состояния

- loading;
- normal;
- no reviews;
- error;
- review sending;
- review success.

---

## 9.11. All Reviews

### Назначение

Полный список отзывов о месте или пользователе.

### UI

- Верхний app bar.
- Средний рейтинг.
- Распределение рейтингов по звёздам.
- Список отзывов.
- Pull-to-refresh.
- Lazy loading.

### API

```http
GET /geo/locations/{id}/reviews?page={page}&size={size}
GET /users/{id}/reviews?page={page}&size={size}
```

---

## 9.12. Plan a Workout / Create Plan

### Назначение

Создание плана групповой тренировки.

### UI

- Step indicator или секции формы.
- Выбор места.
- Дата.
- Время.
- Тип тренировки.
- Уровень.
- Количество участников.
- Описание.
- Кнопка `Create plan`.

### API

```http
POST /geo/events
```

### Body

```json
{
  "locationId": "string",
  "dateTime": "2026-03-18T18:00:00Z",
  "activityType": "STRENGTH",
  "level": "INTERMEDIATE",
  "description": "string",
  "maxParticipants": 5
}
```

### Валидация

- Место обязательно.
- Дата и время не могут быть в прошлом.
- Количество участников: 1-20.
- Описание: до 300 символов.

---

## 9.13. Choose Place

### Назначение

Выбор места при создании тренировки.

### UI

- SearchBar.
- Переключение list/map view.
- Список найденных мест.
- Карта с маркерами мест.
- Кнопка `Select`.

### API

```http
GET /geo/locations/search?query={query}&lat={lat}&lon={lon}
```

---

## 9.14. Add a Place

### Назначение

Создание пользовательской точки на карте.

### UI

- Step indicator.
- Поле названия.
- Категория.
- Operating hours.
- Описание.
- Tags/equipment.
- Добавление фото.
- Выбор локации.
- Кнопка `Publish place`.

### API

```http
POST /geo/locations
POST /geo/locations/{id}/photos
```

### Валидация

- Название обязательно, до 100 символов.
- Тип обязателен.
- Описание до 500 символов.
- Фото: максимум 5.
- Координаты обязательны.

---

## 9.15. Select Location

### Назначение

Ручной выбор координат для UGC-точки.

### UI

- Карта.
- Маркер/crosshair по центру.
- Кнопка текущей геолокации.
- Кнопка `Confirm location`.

### Логика

- При движении карты обновлять координаты выбранной точки.
- После подтверждения возвращать координаты на экран Add a Place.

---

## 9.16. Workout Info

### Назначение

Подробная карточка тренировки.

### UI

- Фото или hero-блок.
- Название тренировки.
- Дата и время.
- Локация.
- Автор.
- Участники.
- Описание.
- Уровень и тип тренировки.
- Кнопка `Join workout` / `Leave workout`.
- Кнопка открытия чата для участников.

### API

```http
GET /geo/events/{id}
POST /geo/events/{id}/join
DELETE /geo/events/{id}/join
DELETE /geo/events/{id}
```

### Состояния

- пользователь не участник;
- пользователь участник;
- пользователь автор;
- мест нет;
- тренировка завершена;
- тренировка отменена.

---

## 9.17. People Profile

### Назначение

Просмотр профиля другого пользователя.

### UI

- Avatar.
- Имя.
- Goal, level, score.
- Статистика: workouts, reviews, distance.
- Описание.
- Кнопка `Message`.
- Кнопка `Plan workout together`.
- Меню `...`: report/block/share.

### API

```http
GET /users/{id}
POST /chat/conversations
POST /users/{id}/review
```

---

## 9.18. My Profile

### Назначение

Профиль текущего пользователя.

### UI

- Avatar.
- Имя.
- Статистика: score, workouts, reviews.
- Кнопка `Edit profile`.
- Блок `My Plans`.
- Пункты меню: My workouts, Achievements, Notifications, Settings.

### API

```http
GET /users/profile
```

---

## 9.19. Edit Profile

### Назначение

Редактирование профиля.

### UI

- Выбор аватара.
- Name.
- Bio.
- Fitness goals.
- Fitness level.
- Preferred workout types.
- Кнопка `Save changes`.

### API

```http
PUT /users/profile
PUT /users/profile/avatar
```

---

## 9.20. My Workout Plans

### Назначение

Список созданных пользователем планов.

### UI

- Upcoming/Past tabs.
- Карточки тренировок.
- Кнопка создания нового плана.

### API

```http
GET /users/profile/events?status=upcoming
GET /users/profile/events?status=past
```

Endpoints требуют подтверждения.

---

## 9.21. My Workouts

### Назначение

История тренировок пользователя.

### UI

- Tabs: upcoming/past.
- Карточки тренировок.
- Для upcoming: дата, место, участники, status.
- Для past: результат, возможность оставить отзыв.

### API

```http
GET /users/profile/workouts?status=upcoming
GET /users/profile/workouts?status=past
```

Endpoints требуют подтверждения.

---

## 9.22. Chats

### Назначение

Список личных и групповых диалогов.

### UI

- Search / title.
- Список чатов.
- Avatar.
- Имя/название группы.
- Последнее сообщение.
- Время.
- Unread badge.
- FAB или кнопка нового чата.

### API

```http
GET /chat/conversations?page={page}&size={size}
```

### Real-time

Через WebSocket обновлять:

- последнее сообщение;
- unread count;
- порядок диалогов;
- online/typing status при необходимости.

---

## 9.23. Chat

### Назначение

Экран переписки.

### UI

- Top bar с аватаром, именем и статусом.
- Сообщения в bubbles.
- Свои сообщения справа, чужие слева.
- Время сообщения.
- Статус отправки/доставки/прочтения.
- Поле ввода.
- Кнопка отправки.
- Возможность отправить фото, если это есть в backend scope.
- Меню `...`: view profile, mute, block, clear history, report.

### API

```http
GET /chat/conversations/{id}/messages?page={page}&size={size}
```

### WebSocket protocol

```json
{ "type": "message", "conversationId": "string", "text": "string" }
{ "type": "typing", "conversationId": "string" }
{ "type": "read", "conversationId": "string", "messageId": "string" }
```

### Offline

- Новое сообщение сразу появляется в UI с pending status.
- Pending-сообщения сохраняются в Room.
- После восстановления сети сообщения отправляются автоматически.
- При ошибке отправки показывается retry action.

---

## 9.24. Notifications

### Назначение

Список уведомлений пользователя.

### UI

- Top bar.
- Список уведомлений.
- Avatar/icon события.
- Текст.
- Время.
- Статус прочитано/не прочитано.
- Pull-to-refresh.

### API

```http
GET /notifications?page={page}&size={size}
PUT /notifications/{id}/read
PUT /notifications/read-all
```

---

## 9.25. Settings

### Назначение

Настройки аккаунта и приложения.

### UI

- Edit profile.
- Notifications.
- Privacy.
- Appearance.
- Language.
- About.
- Log out.
- Delete account.

### API

```http
GET /users/profile/privacy
PUT /users/profile/privacy
DELETE /users/profile
```

---

## 9.26. About

### Назначение

Информация о приложении.

### UI

- Логотип.
- Версия приложения.
- Текст о проекте.
- Terms of Service.
- Privacy Policy.
- Contact support.

---

## 10. Интеграция с backend

### 10.1. Базовые параметры

| Параметр | Значение |
|---|---|
| Base URL | `https://api.gymbro.ru/api/v1/` |
| Auth | `Authorization: Bearer <access_token>` |
| Формат | JSON |
| Пагинация | `page`, `size` |
| Ошибки | 400, 401, 403, 404, 409, 422, 429, 500 |

### 10.2. Обработка ошибок

| Код | UI-поведение |
|---|---|
| 400/422 | Показать ошибки валидации у полей |
| 401 | Попытаться обновить токен, при неуспехе разлогинить |
| 403 | Показать «Недостаточно прав» |
| 404 | Показать empty/error state |
| 409 | Показать конфликт: уже присоединился, мест нет и т.д. |
| 429 | Показать сообщение о лимите и retry позже |
| 500 | Snackbar «Ошибка сервера, попробуйте позже» |

### 10.3. Token refresh

- Access token добавляется через OkHttp interceptor.
- При 401 OkHttp Authenticator выполняет refresh.
- Если refresh успешен, исходный запрос повторяется.
- Если refresh неуспешен, очищаются токены и открывается Sign In.
- Refresh должен быть потокобезопасным, чтобы несколько запросов не обновляли токен одновременно.

---

## 11. Карта и геолокация

### 11.1. Yandex MapKit

Требования:

- Инициализировать MapKit в Application.
- MapView использовать через `AndroidView`.
- Корректно прокидывать lifecycle `onStart/onStop`.
- Маркеры должны обновляться без пересоздания всей карты.
- Для большого количества точек использовать кластеризацию.

### 11.2. Геолокация

- Запрашивать `ACCESS_FINE_LOCATION` и `ACCESS_COARSE_LOCATION`.
- Перед системным permission dialog показывать объяснение ценности геолокации.
- При отказе дать возможность пользоваться приложением вручную.
- Координаты пользователя отправлять на backend не чаще одного раза в 5 минут и только если включена настройка `showOnMap`.

### 11.3. Кэш карты

| Данные | Кэш |
|---|---|
| Locations | Room TTL 30 минут |
| Events | Room TTL 5 минут |
| Users nearby | Не кэшировать или хранить кратковременно в памяти |
| Map tiles | SDK cache |

---

## 12. Real-time и push

### 12.1. WebSocket

- URL production: `wss://api.gymbro.ru/chat/connect`.
- Авторизация через Bearer token в handshake header или query token, согласовать с backend.
- Reconnect: exponential backoff 1s, 2s, 4s, 8s, 16s, максимум 30s.
- Heartbeat: ping каждые 30 секунд.
- При восстановлении соединения догружать сообщения через REST по `since` или cursor.

### 12.2. Push-уведомления

При старте и обновлении FCM token:

```http
POST /users/profile/fcm-token
```

Типы push:

| Событие | Действие по нажатию |
|---|---|
| Новое сообщение | Открыть чат |
| Пользователь присоединился к тренировке | Открыть тренировку |
| Новая тренировка рядом | Открыть тренировку |
| Новый отзыв | Открыть профиль или отзывы |
| Напоминание о тренировке | Открыть тренировку |
| Отмена тренировки | Открыть детали или показать уведомление |

Foreground:

- если открыт текущий чат — системный push не показывать;
- показывать in-app banner/snackbar для других событий.

Background:

- показывать системный push через NotificationChannel.

---

## 13. Локальное хранение и offline

### 13.1. Room

Кэшировать:

- locations;
- events;
- conversations;
- messages;
- notifications;
- profile snapshot.

### 13.2. DataStore

Хранить:

- `onboarding_completed`;
- настройки темы;
- выбранный язык;
- локальные privacy preferences;
- последние выбранные фильтры.

### 13.3. Offline UX

- При отсутствии сети показывать cached data, где возможно.
- Поверх экрана показывать компактный offline banner.
- Для действий, которые требуют сеть, показывать понятную ошибку.
- Сообщения в чатах отправлять через локальную очередь.
- После восстановления сети синхронизировать очередь.

---

## 14. Безопасность и приватность

- Access/refresh tokens хранить только в защищённом хранилище.
- Не логировать токены, email, персональные данные и координаты в release-сборке.
- Для debug logging маскировать Authorization header.
- Настройка `showOnMap` должна реально запрещать отправку/отображение пользователя на карте.
- При удалении аккаунта очищать локальные данные.
- Для фото использовать безопасный upload с ограничением размера и типа файла.
- Для внешних intent проверять наличие приложения/обработчика.

---

## 15. Аналитика и события

На первом этапе можно добавить абстрактный интерфейс аналитики без подключения конкретного SDK.

События:

- onboarding_completed;
- sign_in_success;
- sign_up_success;
- map_layer_changed;
- place_opened;
- workout_created;
- workout_joined;
- message_sent;
- profile_updated;
- notification_opened.

Запрещено отправлять чувствительные данные: текст сообщений, точные координаты, токены.

---

## 16. Доступность

- Все кликабельные элементы имеют touch target минимум 48dp.
- Все иконки без текста имеют contentDescription.
- Поля форм имеют label и error text.
- Цвет не является единственным способом передачи состояния.
- Поддержать системный font scale минимум до 1.3 без критичных поломок UI.
- Для изображений мест и аватаров предусмотреть fallback.

---

## 17. Производительность

- LazyColumn/LazyRow для длинных списков.
- Пагинация для чатов, уведомлений, отзывов, поиска.
- Изображения грузить через Coil с placeholder/error.
- Не выполнять тяжёлые операции на Main thread.
- Использовать debounce для поиска: 300-500 мс.
- Для карты ограничивать частоту запросов при перемещении камеры.
- WebSocket должен жить только когда пользователь авторизован.

---

## 18. Тестирование

### 18.1. Unit tests

Покрыть:

- UseCase;
- Repository mapping;
- Orbit Container/ViewModel intents;
- validation rules;
- token refresh logic;
- offline message queue.

### 18.2. Integration tests

Инструменты:

- MockWebServer;
- Room in-memory database;
- fake DataStore;
- fake WebSocket для чатов.

### 18.3. UI tests

Покрыть smoke-сценарии:

1. Первый запуск -> onboarding -> sign in.
2. Sign in happy path.
3. Открытие карты и смена слоёв.
4. Создание тренировки.
5. Поиск людей.
6. Открытие чата и отправка сообщения.
7. Редактирование профиля.

### 18.4. Минимальные критерии качества

- Все ViewModel/Container покрыты unit-тестами на основные состояния.
- Все UseCase покрыты unit-тестами.
- Для сетевого слоя есть тесты успешных и ошибочных ответов.
- Для навигации есть smoke UI-test.

---

## 19. Сборки и окружения

### 19.1. Build types

| Build type | Назначение |
|---|---|
| debug | Локальная разработка, logging включён |
| staging | Интеграция с тестовым backend |
| release | Production |

### 19.2. Flavors

```text
dev
stage
prod
```

### 19.3. Конфигурация

- Base URL задаётся через BuildConfig.
- Yandex MapKit key — через local properties / CI secrets.
- Firebase config разделяется по окружениям.
- Logging interceptor включён только в debug/stage.

---

## 20. Acceptance criteria

Приложение считается готовым к релизу, если:

- реализованы все экраны MVP;
- авторизация работает end-to-end;
- token refresh работает автоматически;
- карта отображает минимум места и тренировки;
- пользователь может создать тренировку и присоединиться к тренировке;
- пользователь может создать UGC-точку;
- профиль можно просмотреть и отредактировать;
- список чатов и экран чата работают с REST + WebSocket;
- push-уведомления открывают нужные экраны через deep links;
- offline-состояния обработаны без крашей;
- основные сценарии покрыты тестами;
- UI соответствует дизайн-системе и макетам GymBro v1;
- release-сборка не содержит debug logs с персональными данными.

---

## 21. Вопросы для согласования с backend/design

### Backend

- Точный контракт регистрации через Keycloak: `/auth/register` или внешний flow.
- Endpoint для восстановления пароля.
- Endpoint для поиска мест по query.
- Endpoint для моих тренировок и моих планов.
- Формат ошибок валидации.
- Формат WebSocket-событий.
- Поддержка upload фото: один endpoint или отдельный.
- Поддержка блокировки/репорта пользователя.

### Design

- Финальные значения цветовой палитры.
- Финальный шрифт: системный Roboto или кастомный.
- Dark theme: входит в MVP или post-MVP.
- Пустые состояния для всех списков.
- Иллюстрации onboarding и empty states.
- Иконки маркеров карты.

---

## 22. MVP scope

### Обязательно в MVP

- Splash / onboarding.
- Sign in / sign up / reset password.
- Карта с местами и тренировками.
- Поиск мест и людей.
- Карточка места.
- Создание UGC-точки.
- Создание тренировки.
- Карточка тренировки.
- Присоединение/выход из тренировки.
- Профиль пользователя.
- Редактирование профиля.
- Чаты: список + экран переписки.
- Push-уведомления.
- Настройки приватности.

### Можно вынести после MVP

- Достижения.
- Расширенная аналитика.
- Темная тема, если не хватает сроков.
- Сложные рекомендации партнёров.
- Медиа-сообщения в чатах.
- Расширенная модерация UGC.
- Социальные OAuth-провайдеры кроме Google.

---

## 23. Рекомендуемый порядок разработки

1. Создать многомодульную структуру проекта.
2. Подключить Compose, Koin, Retrofit, Room, DataStore, Navigation.
3. Реализовать design system.
4. Реализовать auth flow и token storage.
5. Реализовать главный NavHost и bottom navigation.
6. Реализовать карту и базовые слои.
7. Реализовать места: поиск, карточка, отзывы, UGC.
8. Реализовать тренировки: создание, карточка, join/leave.
9. Реализовать профиль и редактирование.
10. Реализовать поиск людей и фильтры.
11. Реализовать чаты и WebSocket.
12. Реализовать push и deep links.
13. Реализовать settings/privacy/notifications.
14. Добавить offline-кэш и синхронизацию.
15. Покрыть основные сценарии тестами.
16. Провести QA и UX-polish.

---

## 24. Definition of Done для экрана

Экран считается завершённым, если:

- UI соответствует макету и design tokens;
- есть все состояния: loading, content, empty, error;
- есть обработка retry;
- все actions проходят через MVI intent;
- side effects не хранятся в state;
- API интеграция работает;
- ошибки backend корректно отображаются;
- есть preview для ключевых UI-состояний;
- есть unit-тест Container/ViewModel;
- нет хардкода строк — строки вынесены в resources;
- нет хардкода цветов — используются design tokens;
- экран не крашится при отсутствии сети.

---

## 25. Definition of Done для релиза

Релизная сборка готова, если:

- собрана release APK/AAB;
- включена minification, если согласовано;
- нет debug logging персональных данных;
- проверены permissions;
- проверены push deep links;
- проверены основные сценарии на реальном устройстве;
- проверена работа при плохой сети;
- проверен logout и очистка локальных данных;
- приложение проходит smoke UI-test;
- backend endpoints соответствуют контрактам;
- дизайн расхождения зафиксированы или исправлены.
