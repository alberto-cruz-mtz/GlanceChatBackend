# Endpoint de Subida de Archivos (S3 Presigned URL)

Esta guía explica cómo consumir el endpoint `POST /api/upload/presigned-url` desde el frontend para subir archivos (imágenes, videos, documentos) directamente a un bucket de S3 sin que el archivo pase por el backend.

## Resumen del Flujo

```
FRONTEND                                BACKEND                              S3
   |                                       |                                  |
1. POST /api/upload/presigned-url          |                                  |
   body: { fileName, fileType }            |                                  |
   <-- 200: { uploadUrl, publicUrl, key }  |                                  |
   |                                       |                                  |
2. PUT <uploadUrl>  ─────────────────────────────────────────────────────────>|
   header: Content-Type: <fileType>        |                                  |
   body: <archivo en binario>               |                                  |
   <-- 200 OK                              |                                  |
   |                                       |                                  |
3. [El frontend usa publicUrl/key para     |                                  |
    referenciar el archivo en mensajes]    |                                  |
```

> **Punto clave**: el archivo NUNCA pasa por el backend. El backend solo genera una URL temporal firmada para que el frontend suba directo a S3.

## Endpoint

### Solicitar URL Prefirmada

**`POST /api/upload/presigned-url`**

**Request:**

```http
POST /api/upload/presigned-url
Content-Type: application/json

{
  "fileName": "foto-playa.jpg",
  "fileType": "image/jpeg"
}
```

| Campo      | Tipo   | Requerido | Descripción                                              |
| ---------- | ------ | --------- | -------------------------------------------------------- |
| `fileName` | string | Sí        | Nombre original del archivo (se usa para formar la key) |
| `fileType` | string | Sí        | MIME type del archivo (ej. `image/png`, `video/mp4`)     |

**Response (200 OK):**

```json
{
  "uploadUrl": "https://tu-bucket.s3.amazonaws.com/chat-media/1721401200000-foto-playa.jpg?X-Amz-Algorithm=...",
  "publicUrl": "https://tu-dominio-publico.com/chat-media/1721401200000-foto-playa.jpg",
  "key": "chat-media/1721401200000-foto-playa.jpg"
}
```

| Campo       | Tipo   | Descripción                                                                                |
| ----------- | ------ | ------------------------------------------------------------------------------------------ |
| `uploadUrl` | string | URL temporal (válida **1 minuto**) donde el frontend debe hacer `PUT` con el archivo       |
| `publicUrl` | string | URL pública final para acceder al archivo una vez subido (úsala en mensajes, avatares, etc.) |
| `key`       | string | Identificador único del archivo en S3. Tiene el formato `chat-media/{timestamp}-{fileName}` |

> ⚠️ La `uploadUrl` expira en **60 segundos**. Solicítala justo antes de iniciar la subida.

## Ejemplo de Consumo desde el Frontend

### JavaScript (fetch + PUT binario)

```javascript
// 1. Solicitar la URL prefirmada al backend
async function uploadFile(file) {
  const presignResponse = await fetch('/api/upload/presigned-url', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      fileName: file.name,
      fileType: file.type,
    }),
  });

  if (!presignResponse.ok) {
    throw new Error('No se pudo obtener la URL de subida');
  }

  const { uploadUrl, publicUrl, key } = await presignResponse.json();

  // 2. Subir el archivo DIRECTO a S3 usando PUT
  const uploadResponse = await fetch(uploadUrl, {
    method: 'PUT',
    headers: {
      'Content-Type': file.type, // DEBE coincidir con el fileType enviado en el paso 1
    },
    body: file, // el archivo en binario (Blob/File)
  });

  if (!uploadResponse.ok) {
    throw new Error('Error al subir el archivo a S3');
  }

  // 3. Devolver la URL pública para usarla en mensajes
  return { publicUrl, key };
}

// Uso típico al enviar un mensaje con archivo
const fileInput = document.querySelector('#file-input');
fileInput.addEventListener('change', async (e) => {
  const file = e.target.files[0];
  const { publicUrl, key } = await uploadFile(file);

  // Enviar el mensaje con la referencia al archivo
  await fetch('/api/messages', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      content: publicUrl,
      fileKey: key,
      fileType: file.type,
    }),
  });
});
```

### Importante al hacer el PUT a S3

- **Método**: debe ser `PUT` (no `POST`)
- **Header obligatorio**: `Content-Type` con el mismo MIME type enviado en el paso 1
- **Body**: el archivo en binario (no JSON, no FormData, no base64)
- **No envíes headers extra** (Authorization, etc.) — la URL ya trae la firma necesaria

## Manejo de Errores

Las respuestas de error usan el formato RFC 7807 Problem Details:

```json
{
  "type": "about:blank",
  "title": "Internal Server Error",
  "status": 500,
  "detail": "Error al generar la URL prefirmada",
  "instance": "/api/upload/presigned-url"
}
```

| Código | Causa probable                                                  |
| ------ | --------------------------------------------------------------- |
| 400    | Body inválido (faltan `fileName` o `fileType`)                  |
| 500    | Error al firmar la URL (problema con credenciales AWS o S3)    |

## Configuración Requerida en el Backend

El endpoint lee dos propiedades desde `application.yaml`:

```yaml
aws:
  s3:
    bucket-name-chat: nombre-de-tu-bucket
    public-url-chat: https://tu-dominio-publico.com
```

- `bucket-name-chat`: bucket destino donde se guardan los archivos
- `public-url-chat`: dominio base que se concatena con la `key` para formar la `publicUrl`

## Notas de Implementación

- La `key` generada incluye un **timestamp en milisegundos** como prefijo para evitar colisiones cuando dos usuarios suben archivos con el mismo nombre
- Todos los archivos se guardan bajo el prefijo `chat-media/` en el bucket
- La URL prefirmada usa el servicio AWS S3 SDK (`S3Presigner`) y es válida únicamente para `PUT`
- El backend **no valida** el `fileType` ni el tamaño del archivo — esas restricciones deben implementarse en el frontend antes de llamar al endpoint, o en una capa de validación adicional (Lambda de S3, CloudFront, etc.) si se requiere
- La `publicUrl` asume que el bucket u objeto está configurado para acceso público (o detrás de un CDN)