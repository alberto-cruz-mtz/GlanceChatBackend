# Device Authorization Flow

This guide explains how to authorize a new device using the `/api/auth/devices/**` endpoints. The flow follows a Device Authorization Grant pattern similar to OAuth 2.0.

## Overview

```
NEW DEVICE (unauthenticated)                 USER (authenticated)
         |                                         |
  1.  POST /devices/request-code                   |
     body: { deviceName, osVersion }               |
     <-- 200: { deviceCode: "048291",              |
               expiresIn: 300 }                    |
         |                                         |
     [Device shows "048291" to user]               |
         |                                         |
  2.  (polling loop)                     POST /devices/authorize
         |                              body: { deviceCode: "048291" }
         |                              header: Bearer <JWT>
         |                              <-- 204 No Content
         |                                         |
  3.  POST /devices/checked                       |
     body: { deviceCode: "048291" }                |
     <-- 200: { id, publicId, accessToken, ... }   |
     (or 202 Accepted if still pending)            |
         |                                         |
  4.  [New device stores JWT,                     |
       proceeds as authenticated user]            |
```

## Endpoints

### 1. Request Authorization Code

**`POST /api/auth/devices/request-code`**

No authentication required. The new device calls this to get a 6-digit code.

**Request:**

```http
POST /api/auth/devices/request-code
Content-Type: application/json

{
  "deviceName": "iPhone 15",
  "osVersion": "iOS 17.4"
}
```

| Field        | Type   | Required | Description            |
|--------------|--------|----------|------------------------|
| `deviceName` | string | Yes      | Name of the device     |
| `osVersion`  | string | Yes      | OS version of device   |

**Response (200 OK):**

```json
{
  "deviceCode": "048291",
  "expiresIn": 300
}
```

> The code expires in **5 minutes**. Display this code to the user so they can enter it on an already-authenticated device.

---

### 2. Authorize Device

**`POST /api/auth/devices/authorize`**

Requires authentication. The logged-in user approves the device by providing the code.

**Request:**

```http
POST /api/auth/devices/authorize
Content-Type: application/json
Authorization: Bearer <JWT>

{
  "deviceCode": "048291"
}
```

| Field        | Type   | Required | Description                              |
|--------------|--------|----------|------------------------------------------|
| `deviceCode` | string | Yes      | 6-digit code shown on the new device     |

**Validation:**
- `deviceCode` must be exactly 6 digits (`^[0-9]{6}$`)

**Responses:**

| Status | Description                                     |
|--------|-------------------------------------------------|
| 204    | Device authorized successfully                  |
| 400    | Invalid or expired code                         |
| 404    | Authenticated user not found                    |
| 409    | Code has already been used by another user       |

---

### 3. Check Authorization Status

**`POST /api/auth/devices/checked`**

No authentication required. The new device polls this endpoint to check if a user has authorized its code.

**Request:**

```http
POST /api/auth/devices/checked
Content-Type: application/json

{
  "deviceCode": "048291"
}
```

**Responses:**

| Status | Description                                           |
|--------|-------------------------------------------------------|
| 200    | Device authorized — returns full `AuthenticationResponse` |
| 202    | Still pending — no user has authorized the code yet    |
| 400    | Invalid or expired code                                |

**Success response (200 OK):**

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

> Store the `accessToken` on the new device — it is the JWT used for authenticated requests.

## Implementation Notes

- Device codes are stored **in-memory** — they are lost on server restart
- Code generation uses `SecureRandom` (zero-padded 6-digit numeric)
- On successful authorization, a `Session` document is created in MongoDB
- If 2FA is enabled for the user, `requiredAuthenticateFor2FA` will be `true` and further authentication steps are required

## Error Handling

All error responses use RFC 7807 Problem Details format:

```json
{
  "type": "about:blank",
  "title": "Invalid or Expired Device Code",
  "status": 400,
  "detail": "The provided device code is invalid or has expired.",
  "instance": "/api/auth/devices/authorize"
}
```

Validation errors include a `fieldErrors` map:

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
