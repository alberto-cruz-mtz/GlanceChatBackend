# Glance Chat — Backend

Backend de **Glance Chat**, una aplicación de mensajería instantánea pensada como una alternativa a WhatsApp y Signal. Este repositorio contiene únicamente el **servidor (API REST + WebSocket)**; el frontend y la app móvil viven en repositorios separados.

---

## 🤔 ¿Por qué existe este proyecto?

En México, desde hace algunos años, el registro de líneas telefónicas está vinculado a la identidad personal mediante la **CURP** (Clave Única de Registro de Población). Esto significa que, para activar una línea, es obligatorio presentar (y validar) un documento de identidad oficial — un requisito que no todas las personas pueden o quieren cumplir.

La consecuencia práctica es clara: **una parte de la población queda excluida de las apps de mensajería que exigen un número celular** (WhatsApp, Signal, Telegram, etc.), ya sea porque:

- No tienen acceso a una línea telefónica propia.
- El proceso de registro/portabilidad es burocrático o lento.
- Prefieren no vincular su identidad oficial a una cuenta de mensajería.

**Glance Chat elimina esa barrera**: en lugar de pedir un número telefónico, usa un **nombre de usuario único** como identificador. Cualquier persona puede crear una cuenta en segundos, sin necesidad de SIM, sin CURP y sin verificación conyuntural con operadores telefónicos.

> Si Signal/WhatsApp te dicen _"necesitas un número de teléfono"_, Glance Chat te dice _"elige un nombre de usuario"_.

---

## ✨ Características

- **Registro con nombre de usuario único** (5–25 caracteres, letras/números/guión bajo).
- **Public ID** generado automáticamente al registrarse — un identificador corto de 8 caracteres (ej. `4A-7X-9M-2D`) que puedes compartir para que otras personas te agreguen sin revelar tu username.
- **Mensajería en tiempo real** vía WebSocket (STOMP) entre dos usuarios.
- **Conversaciones 1-a-1** con persistencia de historial en MongoDB.
- **Paginación por cursor** del historial de mensajes (`before` + `limit`), inmune a inserciones en vivo.
- **Subida de archivos multimedia** mediante URLs prefirmadas de S3 (avatares y archivos de chat) — el binario **nunca** pasa por el backend.
- **Doble factor de autenticación (2FA)** con TOTP (compatible con Google Authenticator, Authy, 1Password, etc.).
- **Código OTP por correo electrónico** para verificación.
- **Device Authorization Flow** (estilo OAuth device code) para autorizar nuevos dispositivos.
- **Avatares personalizados** subidos a un bucket S3 / R2 / MinIO.
- **API documentada** con Swagger / OpenAPI.
- **Manejo de errores** estandarizado con `ProblemDetail` (RFC 7807).
- **Health check** en `GET /health`.
- **Docker** listo para producción con multi-stage (`Dockerfile` + `docker-compose.yml`).

---

## 🧱 Stack tecnológico

| Capa                      | Tecnología                                               |
| ------------------------- | -------------------------------------------------------- |
| Lenguaje / framework      | Java 25 + Spring Boot 4.0.6 (WAR)                        |
| Persistencia              | MongoDB (Spring Data MongoDB)                            |
| Mensajería en tiempo real | WebSocket + STOMP                                        |
| Seguridad                 | Spring Security + JWT (`java-jwt` de Auth0)              |
| Hash de contraseñas       | `BCryptPasswordEncoder` (vía Spring Security)            |
| 2FA                       | TOTP (`dev.samstevens.totp`)                             |
| Almacenamiento de objetos | AWS SDK v2 (S3-compatible: AWS S3, MinIO, Cloudflare R2) |
| Correo electrónico        | Spring Mail (SMTP)                                       |
| Documentación de API      | springdoc-openapi (Swagger UI)                           |
| Build                     | Gradle                                                   |
| Containerización          | Docker + Docker Compose                                  |

---

## 📁 Estructura del proyecto

```
src/main/java/alberto/cruz/mtz/glance/chat/backend/
├── GlanceChatBackendApplication.java   # Entry point de Spring Boot
├── ServletInitializer.java             # Bootstrap para WAR en Tomcat externo
├── configuration/                      # Beans y configuración (Security, WebSocket, Mail, S3)
│   └── security/
├── controller/
│   ├── rest/                           # Endpoints REST
│   │   ├── AuthenticationController.java
│   │   ├── ChatController.java
│   │   ├── DeviceAuthorizationController.java
│   │   ├── HealthController.java
│   │   ├── ProfileController.java
│   │   ├── TwoFactorAuthenticationController.java
│   │   └── UploadController.java
│   └── message/                        # Controladores WebSocket (STOMP)
│       └── ChatMessageController.java
├── dto/                                # Objetos de request/response
├── exception/                          # Excepciones de dominio + handler global
│   └── handler/
├── model/                              # Documentos de MongoDB (User, Message, Conversation, Session)
├── service/                             # Lógica de negocio
└── util/                               # JWT, TOTP, PublicId, Email, AvatarStorage
```

---

## 🔐 Modelo de datos (MongoDB)

| Colección       | Propósito                                                                             |
| --------------- | ------------------------------------------------------------------------------------- |
| `users`         | Usuarios, perfil, estado, flag de 2FA, publicId, secret de TOTP                       |
| `conversations` | Una conversación por par de usuarios; guarda el último mensaje para la lista de chats |
| `messages`      | Mensajes persistidos; índice compuesto `(conversation_id, sent_at DESC)`              |
| `sessions`      | Dispositivos autorizados por usuario (huella + activo/inactivo)                       |

> El esquema detallado está documentado en [`docs/schema.md`](docs/schema.md).

---

## 🌐 Endpoints REST (resumen)

> La documentación completa, con ejemplos de request/response, está disponible en Swagger UI cuando el servidor está corriendo: <http://localhost:8080/swagger-ui.html>

| Método | Endpoint                         | Descripción                                            |
| ------ | -------------------------------- | ------------------------------------------------------ |
| `POST` | `/api/auth/signup`               | Registrar usuario nuevo                                |
| `POST` | `/api/auth/login`                | Iniciar sesión                                         |
| `POST` | `/api/auth/devices/request-code` | Solicitar device code (flujo estilo OAuth device flow) |
| `POST` | `/api/auth/devices/authorize`    | Autorizar un device code con tu cuenta                 |
| `POST` | `/api/auth/devices/check-status` | Sondear el estado de un device code                    |
| `GET`  | `/health`                        | Health check                                           |
| `POST` | `/api/profiles`                  | Personalizar perfil (display name + avatar opcional)   |
| `GET`  | `/api/profiles/{username}`       | Obtener perfil público de un usuario                   |
| `POST` | `/api/2fa/setup`                 | Iniciar setup de 2FA                                   |
| `POST` | `/api/2fa/verify`                | Verificar código TOTP                                  |
| `POST` | `/api/chats`                     | Crear conversación con un usuario                      |
| `GET`  | `/api/chats`                     | Listar mis conversaciones                              |
| `GET`  | `/api/chats/{chatId}`            | Obtener una conversación específica                    |
| `GET`  | `/api/chats/{chatId}/message`    | Historial paginado por cursor                          |
| `POST` | `/api/upload/presigned-url`      | Generar URL prefirmada para subir archivos a S3        |

### WebSocket

- **Endpoint**: `ws://<host>/ws`
- **Broker**: `/queue`
- **Prefijo de aplicación**: `/app`
- **Prefijo de usuario**: `/user`
- **Destino de envío (cliente → servidor)**: `/app/chat.message.send`
- **Cola privada (servidor → cliente)**: `/user/queue/private.messages`

Los mensajes llegan al cliente en su cola personal, identificada por su `userId`.

---

## 🚀 Cómo correrlo localmente

### Opción 1 — Docker Compose (recomendado)

Levanta el backend **y** una instancia de MongoDB ya configurada:

```bash
docker compose up --build
```

El servicio queda expuesto en <http://localhost:8080>. La UI de Swagger estará disponible en <http://localhost:8080/swagger-ui.html>.

> **Antes de subir a producción**, edita `docker-compose.yml` y reemplaza las credenciales y secretos por valores seguros. Las claves que vienen por defecto son únicamente para desarrollo local.

### Opción 2 — Sin Docker

Requisitos: **JDK 25**, **Gradle** y un **MongoDB 7+** corriendo en `localhost:27017`.

```bash
# 1) Asegúrate de que MongoDB esté corriendo
# 2) Define las variables mínimas (o usa application-local.yaml)
export MONGO_HOST=localhost
export MONGO_PORT=27017
export MONGO_DATABASE=glance_chat
export JWT_SECRET=cambia-esto-por-algo-seguro
export MAIL_HOST=smtp.example.com
export MAIL_USERNAME=tu-usuario
export MAIL_PASSWORD=tu-password

# 3) Levanta el servidor con el perfil "local"
./gradlew bootRun --args='--spring.profiles.active=local'
```

---

## ⚙️ Variables de entorno

El backend se configura casi enteramente desde variables de entorno, lo que lo hace 12-factor friendly:

| Variable                  | Default                       | Descripción                                         |
| ------------------------- | ----------------------------- | --------------------------------------------------- |
| `MONGO_HOST`              | `localhost`                   | Host de MongoDB                                     |
| `MONGO_PORT`              | `27017`                       | Puerto de MongoDB                                   |
| `MONGO_USERNAME`          | `root`                        | Usuario de MongoDB                                  |
| `MONGO_PASSWORD`          | `1234`                        | Password de MongoDB                                 |
| `MONGO_DATABASE`          | `glance_chat`                 | Nombre de la base                                   |
| `JWT_SECRET`              | `your_jwt_secret_key`         | Secreto para firmar JWT — **cambiar en producción** |
| `JWT_EXPIRATION`          | `7200000`                     | Expiración del access token en ms (2h por defecto)  |
| `JWT_ISSUER`              | `glance_chat_backend`         | Issuer del JWT                                      |
| `MAIL_HOST`               | `smtp.example.com`            | Host SMTP                                           |
| `MAIL_PORT`               | `587`                         | Puerto SMTP                                         |
| `MAIL_USERNAME`           | `username`                    | Usuario SMTP                                        |
| `MAIL_PASSWORD`           | `password`                    | Password SMTP                                       |
| `AWS_ACCESS_KEY`          | —                             | Access key de S3                                    |
| `AWS_SECRET_KEY`          | —                             | Secret key de S3                                    |
| `AWS_REGION`              | `auto`                        | Región de S3 (Cloudflare R2 usa `auto`)             |
| `AWS_S3_BUCKET_NAME`      | `avatars`                     | Bucket para avatares                                |
| `AWS_S3_BUCKET_NAME_CHAT` | `messages-chat`               | Bucket para archivos de chat                        |
| `AWS_S3_URL`              | —                             | Endpoint S3 interno                                 |
| `AWS_S3_PUBLIC_URL`       | —                             | CDN pública de avatares                             |
| `AWS_S3_PUBLIC_URL_CHAT`  | —                             | CDN pública de archivos de chat                     |
| `ERROR_URL`               | `http://localhost:8080/error` | URL base para los `type` de ProblemDetail           |

> En producción, **Cloudflare R2** funciona perfecto: solo cambia los defaults a tus URLs R2. Para desarrollo local, **MinIO** es un drop-in replacement.
