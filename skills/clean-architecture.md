# Clean Architecture

## Структура слоёв (зависимости ТОЛЬКО внутрь)
src/
├── domain/             # Ядро — не зависит ни от чего
│   ├── entities/       # Бизнес-сущности
│   ├── interfaces/     # Контракты репозиториев и сервисов
│   └── exceptions/     # Доменные исключения
├── application/        # Use Cases — зависит только от domain
│   ├── use-cases/      # Один файл = один Use Case
│   └── dto/            # Data Transfer Objects
├── infrastructure/     # Реализации — зависит от domain + application
│   ├── repositories/   # Реализации интерфейсов репозиториев
│   └── services/       # Реализации внешних сервисов
└── presentation/       # HTTP слой — зависит только от application
    ├── controllers/
    └── routes/

## Жёсткие правила
- domain НЕ импортирует из других слоёв — никогда
- application импортирует ТОЛЬКО из domain
- Use Case принимает DTO на вход, возвращает DTO на выход
- ЗАПРЕЩЕНО вызывать БД напрямую из presentation или domain
- ЗАПРЕЩЕНО создавать new ConcreteClass() внутри бизнес-логики
- Каждый внешний сервис/репозиторий имеет интерфейс в domain/interfaces/

## Применение в Spring Boot (текущий проект)
- domain/entities/ → Java-классы без аннотаций JPA (чистые POJO)
- domain/interfaces/ → Java-интерфейсы (Repository, Service)
- application/use-cases/ → @Service-классы с одним публичным методом
- infrastructure/repositories/ → @Repository, реализуют интерфейсы из domain
- presentation/controllers/ → @RestController, вызывают только use-cases
- DTO-классы находятся в application/dto/ (не Entity напрямую)
