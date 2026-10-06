# Блог-платформа — Backend

Бэкенд-приложение для блог-платформы, разработанное на Java 21 с использованием Spring Web MVC и Spring Data JDBC.

В качестве основной базы данных используется PostgreSQL, а для интеграционного тестирования — встроенная H2 Database (In-Memory).

### Технологический стек

- **Java 21**

- **Spring Framework**

    - Spring MVC

    - Spring Data JDBC

    - Transaction Management

- **Databases**

  - **PostgreSQL** — production / development

  - **H2** — testing

- **Build Tool:** Maven

- **Testing**

    - JUnit 5

     - Mockito

     - Spring Test / MockMvc

- **Server**: Apache Tomcat

## Конфигурация приложения

Перед запуском проекта необходимо настроить параметры подключения к базе данных PostgreSQL.

### 1. Создание конфигурационного файла

Скопируйте шаблон конфигурации:

```bash
cp src/main/resources/application-template.properties \
src/main/resources/application.properties
```

### 2. Настройка подключения к PostgreSQL

Откройте файл:

```bash
src/main/resources/application.properties
```

и укажите актуальные параметры подключения к вашей базе данных PostgreSQL.

**Важно**: файл application.properties с реальными настройками добавлен в .gitignore и не попадает в систему контроля версий.

## Сборка и запуск
### 1. Клонирование репозитория
```bash   
git clone <ссылка-на-репозиторий>
cd ya-blog-backend
```

### 2. Запуск тестов

Для запуска всех модульных и интеграционных тестов с использованием изолированной H2 Database выполните:

```bash
mvn test
```

Файл `application.properties` для `mvn test` не нужен: тесты берут настройки из `src/test/resources/application-test.properties`.

### 3. Сборка WAR-пакета

Для сборки WAR-файла выполните:

```bash
mvn clean package
```


После успешной сборки архив будет доступен по адресу:

```bash
target/ya-blog-backend-1.0-SNAPSHOT.war
```

## Деплой в Apache Tomcat

Для запуска приложения в сервлет-контейнере необходимо установить и настроить Apache Tomcat.

Рекомендуется использовать Tomcat 10.1+, совместимый с Java 21.

### Шаги деплоя

Установите и настройте Apache Tomcat.

Убедитесь, что параметры подключения к PostgreSQL доступны приложению.

Скопируйте собранный WAR-файл:

```bash
target/ya-blog-backend-1.0-SNAPSHOT.war
```


в директорию:

```bash
<TOMCAT_HOME>/webapps/
```

Запустите Tomcat:

Linux / macOS:

```bash
./bin/startup.sh
```

Windows:

```bash
.\bin\startup.bat
```

Tomcat автоматически развернёт приложение из WAR-файла.

## API
### Posts

| Метод    | Endpoint                     | Описание                                      |
|----------|------------------------------|-----------------------------------------------|
| `GET`    | `/api/posts`                 | Получение ленты постов                        |
| `POST`   | `/api/posts`                 | Создание нового поста                         |
| `GET`    | `/api/posts/{id}`             | Получение полной информации о посте          |
| `PUT`    | `/api/posts/{id}`             | Редактирование поста                          |
| `DELETE` | `/api/posts/{id}`             | Удаление поста                                |
| `POST`   | `/api/posts/{id}/likes`       | Добавление лайка посту                        |

#### Параметры `GET /api/posts`

| Параметр     | Описание              |
|--------------|-----------------------|
| `search`     | Слова ищутся как подстрока названия, слова с `#` — как теги (все обязательны) |
| `pageNumber` | Номер страницы        |
| `pageSize`   | Размер страницы       |

### Comments

| Метод    | Endpoint                            | Описание                              |
|----------|-------------------------------------|---------------------------------------|
| `GET`    | `/api/posts/{postId}/comments`      | Получение списка комментариев к посту |
| `POST`   | `/api/posts/{postId}/comments`      | Добавление комментария                |
| `PUT`    | `/api/posts/{postId}/comments/{id}` | Редактирование комментария            |
| `DELETE` | `/api/posts/{postId}/comments/{id}` | Удаление комментария                  |