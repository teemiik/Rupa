# Market Research Update: Rupa (Exa-исследование)

- **Исполнитель:** Мэри (Business Analyst, `bmm`)
- **Workflow:** `bmad-market-research` + `bmad-domain-research`
- **Дата:** 2026-06-23
- **Метод:** Exa Search API + webfetch. Источники приведены по каждому утверждению.
- **Грейды:** 🟢 первичный (Exa/первоисточник) · 🟡 вторичный · 🔴 гипотеза
- **Связанные артефакты:** `analyst-brief-rupa.md` (этот файл апгрейдит его грейды), `investigation-libgdx-to-godot.md`.

---

## Top-line (пирамида Минто)

Ниша tilt/ball-maze **коммерчески перегрета** (лидер — 10M+ установок, ad+IAP). Но кейс **Rollance** (Azur Games × Ates Games) доказывает: **физика-как-УТП** создаёт лояльную нишу с жизнеспособной экономикой — при условии **объёма контента** и упора на «физический фид». Для Rupa это и есть стратегический путь, если идти не в «коммодити-лабиринт», а в «сочную гибридную физику».

---

## 1. Рынок tilt/ball-maze — конкретика 🟢

| Игра | Установки | Рейтинг | Модель | Источник |
|---|---|---|---|---|
| **Classic Labyrinth 3d Maze** (pictofun) | **10M+** | 4.4★ (142K отзывов) | free + ads + IAP | Google Play (Exa) |
| Classic Labyrinth Maze 3d 2 (сиквел, pictofun) | — | — | free + ads + IAP | Google Play (Exa) |
| Classic Labyrinth (moigner) | 50K+ | 4.23★ (187 отзывов) | free | AppBrain (Exa) |
| Wooden Labyrinth 3D Free | — | — | free + IAP, >5 лет в стор | App Store (Exa) |
| Labyrinth 3D: Maze Ball Puzzle (Daydreamsoft) | — | — | modern competitor | TapTap (Exa) |

**Вывод:** категория живая и денежная, но **доминируют бесплатные ad+IAP-тайтлы с огромной базой**. Новому участнику без UA-бюджета органикой не подняться. Премиум-успехов в выборке нет.

**Апгрейд улики брифа:** «ниша перегрета» 🟡 → **🟢**. Дополнительно: один Exa-поиск дал 6+ живых open-source репозиториев tilt-maze (`rradonic/tilt-ball`, `NDTeegarden/mazemachine`, `tonihintikka/Labyrinth-Game`, `amplitude/Android-Demo`, `mediamonks/tilt-game-android`, `brian-fitzgerald/Simple-Tilt-Maze`) — ещё одно подтверждение коммодитизации.

## 2. Шаблон Rollance (главная находка) 🟢

**Rollance** — мобильная игра с катящимся шаром (Azur Games, разработчик Ates Games). Выделилась в толпе **упором на реалистичную физику как УТП**. Прямой стратегический аналог для Rupa. Источник: PocketGamer.biz, Nikita Tikhomirov (producer, Azur Games), 12.02.2026.

### Что сработало
- **Физика = УТП и ров:** «несколько студий делали конкурентов с большими бюджетами, никто не достиг того же уровня». Основатель Сайт Атеш: *«сделайте глубокую экспертизу в одной области и используйте её как USP — будь то физика, сторителлинг или визуальный стиль»*.
- **Контент — главный рычаг:** после допила уровней **R1 +10 пп, R30 = 4.2%, CPI $0.10 (мир) / $0.67 (США), 15 мин/день**. Стартовали с 20 уровней → сейчас **50+**.
- **Лояльная ниша, не масса:** «привлекла hardcore-игроков за вызовом», сильное long-term retention.
- **Material switching** (разное поведение шара) — variety геймплея.
- **Adaptive graphics** → **+20% LTV**.

### Что НЕ сработало (важно для Rupa)
- **Визуальная монетизация** (скины, трейлы, косметика) — *«игроки пришли за физикой и челленджем, визуальный шум только отвлекал»*.
- **Hyper-casual «выбор типа шара перед уровнем»** — «не зашло».

### Цена физики
- Каждый уровень строился **в 2× дольше** из-за реализма физики. Баланс «реализм без овер-комплексности» — центральный челлендж.

## 3. Миграция libGDX→Godot — есть зрелые прецеденты 🟢
- **Simon Dalvai** («Porting my libGDX games to Godot», 2023) — переносит Sn4ke/WhatColor/ColorShooter. Мотивация 1-в-1 с Rupa: ад с gradle/robovm, Apple удаляет необновляемые 2–3 года приложения, Godot экспортирует Android/iOS/web/десктоп «из коробки». В порт кладёт звуки/уровни/туториалы — ровно пробелы Rupa.
- **AntzGames** — side-by-side ремейк Raid on Bungeling Bay (libGDX vs Godot).

**Вывод:** перенос Rupa — не эксклюзивная затея, а **повторяющийся паттерн**. Усиливает сценарии 1/2 брифа.

## 4. Гибридная механика Rupa — редкая 🟢
Поиск «paddle bounce ball to goal avoid holes» вернул **только Breakout/Pong-туториалы** (mcoorlim, Chaduke, Spooky Bricks, godot-demo-projects/pong), а **не** игру в стиле Rupa. Т.е. «подбрасывающая площадка + лузы-хазы» — **не** распространённый паттерн.

**Апгрейд улики брифа:** гипотеза №1 «своя механика = возможное УТП» 🔴 → **🟢**. Самый обнадёживающий сигнал для продукта.

---

## Импликации для Rupa

| Факт из research | Импликация |
|---|---|
| 10M+ у лидера, free+ads+IAP | Сценарий 3 (коммерция) — только через дифференциацию, не «ещё один лабиринт» |
| Rollance: физика = УТП + ров | Подтверждает: гибридная «площадка+лузы» может стать острой специализацией |
| Контент = главный рычаг; 20→50 уровней | Rupa с **10 уровнями критически мало** даже для бесплатной |
| Rollance: уровень в 2× дольше из-за физики | Объясняет мало уровней в Rupa. Процедурная/полу-проц. генерация = ключ к объёму |
| Косметика не продаётся у физ-игр | Не делать ставку на скины; монетизация — rewarded ads + «продолжить» |
| Adaptive graphics +20% LTV | При расширении — добавить масштабирование качества под FPS |

---

## Апгрейд улик `analyst-brief-rupa.md`

| Улика | Было | Стало | Основание |
|---|---|---|---|
| Ниша перегрета | 🟡 | **🟢** | Classic Labyrinth 3D 10M+; 6+ open-source репо |
| Гибридная механика = УТП | 🔴 | **🟢** | Rollance (физика-УТП); нет прямых paddle-to-goal аналогов |
| 10 уровней мало | 🟡 | **🟢** | Rollance: метрики прыгнули на 20→50 уровнях |
| Перенос на Godot осмыслен | 🟢 | 🟢 (усилено) | Dalvai/AntzGames — зрелые прецеденты |

**Новый УТП-вектор (добавить в бриф):** физика-feel как острая специализация (не визуал, не нарратив) — по шаблону Rollance.

## Рекомендации Мэри (обновлённые)

1. Если развивать — путь Rollance: **упор на сочный/уникальный физический фид** (не «ещё tilt-maze»). Сначала — починить и отполировать физику (F2 и т.п.).
2. **Объём контента — критично:** приоритет на процедурную/полу-процедурную генерацию, цель — 30–50+ уровней.
3. Монетизация (если коммерция): rewarded ads + «продолжить», **не** косметика.
4. Сценарий 3 теперь имеет доказанный темплейт — но требует ресурса на контент и UA; без них — сценарий 1/2.

---

## Источники (Exa + webfetch, 2026-06-23)

- PocketGamer.biz — «How doubling down on physics expertise helped Rollance stand out in a crowded market», N. Tikhomirov, 12.02.2026.
- Google Play — Classic Labyrinth 3d Maze (de.pictofun.labyrinthone), Classic Labyrinth (com.moigner.labyrinth).
- AppBrain — Classic Labyrinth stats. App Store — Wooden Labyrinth 3D Free.
- simondalvai.org — «Porting my libGDX games to Godot» (2023); «Godot Engine vs libGDX».
- antzgames.itch.io — «Godot vs. libGDX 2D Remake Showdown».
- GameDeveloper.com, Mobile Game Doctor, GDevelop blog, Deconstructor of Fun — design patterns (для возможного следующего шага по F2P level design).
- GitHub (Exa) — 6+ open-source tilt-maze репозиториев.
