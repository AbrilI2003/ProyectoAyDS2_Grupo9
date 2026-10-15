# demo

Proyecto base con Spring Boot, JPA y MySQL. Sirve como punto de partida común: cada integrante hace un fork y trabaja desde el mismo estado inicial.

- **Group / Artifact:** `com.aydsii` / `demo`
- **Versión:** `0.0.1-SNAPSHOT`
- **Paquete base:** `com.aydsii.demo`

## Requerimientos

| Herramienta | Versión | Notas |
|---|---|---|
| JDK | 25 | Definido en `java.version` del `pom.xml` |
| Maven | 3.9+ | No hace falta instalarlo: se usa el wrapper (`mvnw` / `mvnw.cmd`) |
| Docker o Podman (opcional) | Cualquiera reciente | Para levantar MySQL con `compose.yaml` |
| Git | Cualquiera reciente | Para el fork |

## Dependencias incluidas

| Dependencia | Versión | Uso |
|---|---|---|
| Spring Boot (parent) | 4.1.1 | Gestiona las versiones de los starters |
| `spring-boot-starter-data-jpa` | 4.1.1 | JPA, Hibernate y HikariCP |
| `spring-boot-starter-webmvc` | 4.1.1 | API REST |
| `spring-boot-starter-validation` | 4.1.1 | Validación de datos (`@Valid`, `@NotNull`, etc.) |
| `spring-boot-starter-log4j2` | 4.1.1 | Logging con Log4j2 (Log4j 2.25.5) |
| `springdoc-openapi-starter-webmvc-ui` | 3.1.0 | Documentación OpenAPI y Swagger UI |
| `mysql-connector-j` | 9.7.0 | Driver de MySQL (runtime) |
| `lombok` | 1.18.46 | Reduce código repetitivo (opcional) |
| `spring-boot-devtools` | 4.1.1 | Recarga automática en desarrollo (runtime, opcional) |
| `spring-boot-starter-data-jpa-test` | 4.1.1 | Tests (scope test) |
| `spring-boot-starter-webmvc-test` | 4.1.1 | Tests (scope test) |

> Logback está excluido de los starters porque se usa Log4j2. Si agregás otro starter de Spring Boot, excluí también `spring-boot-starter-logging`.

## Estructura

```
.
├── compose.yaml                         # MySQL para desarrollo (Docker / Podman)
├── pom.xml
└── src
    ├── main
    │   ├── java/com/aydsii/demo         # Código de la aplicación
    │   └── resources
    │       ├── application.properties   # Configuración (datasource, JPA)
    │       └── log4j2-spring.xml        # Configuración de logging
    └── test
```

## Puesta en marcha

### 1. Levantar MySQL

Si usas XAMPP o Laragon, podes saltar este paso.

```bash
# Docker
docker compose up -d

# Podman
podman compose up -d
```

Valores por defecto (se pueden cambiar con variables de entorno o un archivo `.env`):

| Variable | Default |
|---|---|
| `MYSQL_DATABASE` | `demo` |
| `MYSQL_USER` | `demo` |
| `MYSQL_PASSWORD` | `demo` |
| `MYSQL_ROOT_PASSWORD` | `root` |
| `MYSQL_PORT` | `3306` |

Son credenciales solo para desarrollo local. Para borrar la base y empezar de cero: `docker compose down -v` (o `podman compose down -v`).

### 2. Configurar la aplicación

Las propiedades de [application.properties](src/main/resources/application.properties) están en blanco a propósito. Completalas con los datos de tu base. Con los valores por defecto del compose:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/demo
spring.datasource.username=demo
spring.datasource.password=demo
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

Las propiedades que no uses, borralas: una propiedad vacía puede pisar el valor por defecto de Spring.

> No subas contraseñas reales al repositorio.

### 3. Ejecutar

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

### 4. Tests

```bash
./mvnw test
```

### Swagger UI

Con la app corriendo: http://localhost:8080/swagger-ui.html

## Logging

La configuración está en [log4j2-spring.xml](src/main/resources/log4j2-spring.xml):

- Consola y archivo rotativo en `logs/demo.log` (rota por día o a los 10 MB, conserva 30 días).
- Nivel `debug` para `com.aydsii`, `info` para el resto.

## Fork: mismo punto de partida para todos

1. **Hacer el fork:** desde la página del repositorio original, botón **Fork** y elegir tu cuenta.
2. **Clonar tu fork:**
   ```bash
   git clone https://github.com/<tu-usuario>/<repositorio>.git
   cd <repositorio>
   ```
3. **Completar la configuración local** (ver "Puesta en marcha"). Los cambios en `application.properties` con tus credenciales son locales: no los subas.

A partir de acá, cada fork es un proyecto independiente: no se sincroniza con el original ni con los forks de los demás.

Reemplazá `<tu-usuario>` y `<repositorio>` por los datos reales.
