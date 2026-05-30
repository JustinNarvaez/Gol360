# ⚽ Gol 360 - Backend (PJG360)

Plataforma digital informativa y social para el seguimiento del **Mundial FIFA 2026**. Centraliza partidos, estadísticas, pollas futboleras, álbum digital y gestión de entradas en un solo lugar.

## Stack Tecnológico

- **Java 21 + Spring Boot** - API REST
- **Spring Security** - Autenticación y control de acceso por roles
- **Maven** - Gestión de dependencias

## Módulos implementados

- **Autenticación** - Registro, login y logout de usuarios (Local Fan / Visit Fan)
- **Usuarios** - Consulta, actualización de perfil y cambio de contraseña
- **Partidos** - Calendario, detalle y actualización de estado
- **Eventos de partido** - Registro de goles, tarjetas y sustituciones en tiempo real
- **Preferencias** - Configuración de selecciones, estadios y notificaciones favoritas

## Cómo correr el proyecto

```bash
# Clonar el repositorio
git clone https://github.com/JustinNarvaez/PJG360.git
cd backend

# Compilar sin ejecutar pruebas
mvn clean install -DskipTests

# Correr la aplicación
mvn spring-boot:run
```

## Pruebas unitarias del Backend

Las pruebas están en `src/test/java/com/pjg360/PJG360/services/impl/` y cubren `AuthServiceImpl`, `UserServiceImpl` y `MatchEventServiceImpl` usando JUnit 5 y Mockito.

```bash
mvn test
```

## Equipo

| Nombre | Rol |
|---|---|
| Jhonatan Lara Gómez | Project Manager |
| María Paula Marín Soler | Desarrolladora |
| Justin Felipe Narváez Gutiérrez | Desarrollador |
| Julieth Dayana Serrano Castañeda | Scrum Master |

Universidad El Bosque - Ingeniería de Sistemas — 2026