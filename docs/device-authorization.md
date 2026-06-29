# Flujo de Autorización de Dispositivos

Esta guía explica cómo autorizar un nuevo dispositivo utilizando los endpoints `/api/auth/devices/**`. El flujo sigue un patrón de Device Authorization Grant similar a OAuth 2.0.

## Resumen del Flujo

```
DISPOSITIVO NUEVO (sin autenticar)          USUARIO (autenticado)
         |                                         |
  1.  POST /devices/request-code                   |
     body: { deviceName, osVersion }               |
     <-- 200: { deviceCode: "048291",              |
               expiresIn: 300 }                    |
         |                                         |
     [El dispositivo muestra "048291" al usuario]  |
         |                                         |
  2.  (bucle de polling)               POST /devices/authorize
         |                              body: { deviceCode: "048291" }
         |                              header: Bearer <JWT>
         |                              <-- 204 No Content
         |                                         |
  3.  POST /devices/checked                       |
     body: { deviceCode: "048291" }                |
     <-- 200: { id, publicId, accessToken, ... }   |
     (o 202 Accepted si aún está pendiente)        |
         |                                         |
  4.  [El nuevo dispositivo almacena el JWT,      |
       procede como usuario autenticado]          |
```

## Endpoints

### 1. Solicitar Código de Autorización

**`POST /api/auth/devices/request-code`**

No requiere autenticación. El nuevo dispositivo llama a este endpoint para obtener un código de 6 dígitos.

**Request:**

```http
POST /api/auth/devices/request-code
Content-Type: application/json

{
  "deviceName": "iPhone 15",
  "osVersion": "iOS 17.4"
}
```

| Campo        | Tipo   | Requerido | Descripción              |
| ------------ | ------ | --------- | ------------------------ |
| `deviceName` | string | Sí        | Nombre del dispositivo   |
| `osVersion`  | string | Sí        | Versión del SO del disp. |

**Response (200 OK):**

```json
{
  "deviceCode": "048291",
  "expiresIn": 300
}
```

> El código expira en **5 minutos**. Muestra este código al usuario para que lo ingrese en un dispositivo ya autenticado.

---

### 2. Autorizar Dispositivo

**`POST /api/auth/devices/authorize`**

Requiere autenticación. El usuario que ya tiene sesión autoriza el dispositivo proporcionando el código.

**Request:**

```http
POST /api/auth/devices/authorize
Content-Type: application/json
Authorization: Bearer <JWT>

{
  "deviceCode": "048291"
}
```

| Campo        | Tipo   | Requerido | Descripción                                          |
| ------------ | ------ | --------- | ---------------------------------------------------- |
| `deviceCode` | string | Sí        | Código de 6 dígitos mostrado en el nuevo dispositivo |

**Validación:**

- `deviceCode` debe ser exactamente 6 dígitos (`^[0-9]{6}$`)

**Respuestas:**

| Estado | Descripción                                 |
| ------ | ------------------------------------------- |
| 204    | Dispositivo autorizado exitosamente         |
| 400    | Código inválido o expirado                  |
| 404    | Usuario autenticado no encontrado           |
| 409    | El código ya fue utilizado por otro usuario |

---

### 3. Verificar Estado de Autorización

**`POST /api/auth/devices/checked`**

No requiere autenticación. El nuevo dispositivo hace polling a este endpoint para verificar si un usuario autorizó su código.

**Request:**

```http
POST /api/auth/devices/checked
Content-Type: application/json

{
  "deviceCode": "048291"
}
```

**Respuestas:**

| Estado | Descripción                                                        |
| ------ | ------------------------------------------------------------------ |
| 200    | Dispositivo autorizado — retorna `AuthenticationResponse` completa |
| 202    | Aún pendiente — ningún usuario ha autorizado el código aún         |
| 400    | Código inválido o expirado                                         |

**Response exitoso (200 OK):**

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "publicId": "A1B2-C3D4",
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "avatar": "https://example.com/avatar.jpg",
  "username": "john_doe",
  "hasSetUpProfile": true,
  "requiredAuthenticateFor2FA": false
}
```

> Almacena el `accessToken` en el nuevo dispositivo — es el JWT que se usa para solicitudes autenticadas.

## Notas de Implementación

- Los códigos de dispositivo se almacenan **en memoria** — se pierden al reiniciar el servidor
- La generación de códigos usa `SecureRandom` (numérico de 6 dígitos con ceros a la izquierda)
- Al autorizar exitosamente, se crea un documento `Session` en MongoDB
- Si el usuario tiene habilitado 2FA, `requiredAuthenticateFor2FA` será `true` y se requieren pasos adicionales de autenticación

## Manejo de Errores

Todas las respuestas de error usan el formato RFC 7807 Problem Details:

```json
{
  "type": "about:blank",
  "title": "Invalid or Expired Device Code",
  "status": 400,
  "detail": "The provided device code is invalid or has expired.",
  "instance": "/api/auth/devices/authorize"
}
```

Los errores de validación incluyen un mapa `fieldErrors`:

```json
{
  "type": "about:blank",
  "title": "Validation Failed",
  "status": 400,
  "fieldErrors": {
    "deviceCode": "must not be empty"
  }
}
```
