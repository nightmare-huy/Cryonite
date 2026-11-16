# Сборка и выпуск релиза

## Сборка мода

```bash
./gradlew build
```

Запускает:
1. Генерацию данных (`runData`) — языковые файлы,loot tables, рецепты, текстуры блоков
2. Компиляцию Java-кода
3. Сборку JAR-мода
4. Запуск GameTest'ов

## Выпуск релиза

Процесс выпуска релиза автоматизирован через GitHub Actions:

1. Коммит и push в ветку `arena/9d8f978f-cryonite`
2. Ожидание 175 секунд (требование регламента)
3. Проверка CI через `gh api .../check-runs`
4. При зелёном CI:
   - JAR переопубликовывается в релизе `v1.0.1`
   - `src/generated` коммитится автоматически (не править вручную!)

## CIチェック

```bash
SHA=$(git rev-parse HEAD)
gh api "repos/nightmare-huy/Cryonite/commits/$SHA/check-runs?per_page=100" \
  --jq '.check_runs[] | [.name, .status, .conclusion] | @csv'
```

## Ограничения

- Java/Gradle не запускаются в sandbox — компиляция только через CI
- `gh release download` не работает из sandbox — использовать `gh release view`
- Never edit `src/generated` by hand — только через CI auto-commit
- Mock players в тестах должны использовать `mockPlayer(helper)`
