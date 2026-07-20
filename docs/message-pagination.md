# Paginación de Mensajes por Cursor

Esta guía explica cómo consumir el endpoint `GET /api/chats/{chatId}/message` desde el frontend para cargar el historial de mensajes de un chat usando **paginación por cursor**, integrada con la llegada de mensajes en tiempo real vía WebSocket.

## ¿Por qué cursor y no páginas?

La paginación clásica con `?page=0&size=50` tiene un problema en un chat: cuando un mensaje nuevo llega por WebSocket, los índices de página se desplazan y el usuario puede ver mensajes duplicados o saltos. Con cursor, cada página referencia un punto fijo en el tiempo (`sentAt`) y es inmune a inserciones.

## Resumen del Flujo

```
FRONTEND                              BACKEND
   |                                     |
1. Usuario abre chat                    |
   |                                     |
2. GET /api/chats/{chatId}/message      |
        ?limit=50                        |
   <-- { data: [...50 mensajes],         |
         metadata: { nextCursor: "..." } |
   |                                     |
3. Usuario hace scroll hacia arriba     |
   |                                     |
4. GET /api/chats/{chatId}/message      |
        ?limit=50&before=<nextCursor>    |
   <-- { data: [...50 más antiguos],     |
         metadata: { nextCursor: "..." } |
   |                                     |
5. Repetir paso 3-4 hasta que           |
   nextCursor === null                  |
   |                                     |
6. (En paralelo) WebSocket entrega      |
   mensajes nuevos → append al final    |
   de la lista renderizada              |
```

> **Punto clave**: el WebSocket y la paginación NO se interfieren. Los mensajes nuevos siempre se anexan al final de la lista (los más recientes), mientras que el cursor solo se usa para traer **histórico antiguo** al hacer scroll hacia arriba.

## Endpoint

### Obtener mensajes de un chat

**`GET /api/chats/{chatId}/message`**

**Query params:**

| Parámetro | Tipo                | Requerido | Default | Descripción                                                                                                              |
| --------- | ------------------- | --------- | ------- | ------------------------------------------------------------------------------------------------------------------------ |
| `chatId`  | string (path)       | Sí        | —       | Identificador de la conversación                                                                                        |
| `limit`   | int                 | No        | `50`    | Cantidad de mensajes a devolver. Entre `1` y `100`                                                                       |
| `before`  | string (ISO-8601)   | No        | `null`  | Cursor: instante de tiempo. Devuelve mensajes **estrictamente anteriores** a este valor. Omitir en la primera carga.     |

**Ejemplo — primera carga:**

```http
GET /api/chats/6a3e157559cddb4b8398f622/message?limit=50
```

**Ejemplo — cargar más antiguos (scroll up):**

```http
GET /api/chats/6a3e157559cddb4b8398f622/message?limit=50&before=2026-07-20T10:30:00Z
```

**Response (200 OK):**

```json
{
  "data": [
    {
      "id": "6a4455d22086f25f7da4dee7",
      "content": "Hola!",
      "chatId": "6a3e157559cddb4b8398f622",
      "senderId": "6a2cabbf7b3f2ebf959d3c52",
      "sendAt": "2026-07-20T10:30:00Z",
      "type": "TEXT",
      "metadata": null
    }
  ],
  "metadata": {
    "nextCursor": "2026-07-20T10:30:00Z"
  }
}
```

> Cuando `metadata.nextCursor` es `null`, significa que **ya no hay más mensajes antiguos** para cargar. Deja de hacer peticiones de paginación.

## Integración con WebSocket

Los mensajes nuevos que llegan por WebSocket **no requieren acción de paginación**: simplemente se anexan al final de la lista renderizada.

```
Lista en memoria del frontend (ordenada: más antiguo → más reciente):

┌─────────────────┬─────────────────┬─────────────────┐
│  msg antiguo 1  │  msg antiguo 2  │  msg reciente N │  ← render invertido (CSS)
└─────────────────┴─────────────────┴─────────────────┘
       ↑ scroll up                 ↑ WebSocket append aquí
       (paginar con cursor)        (no paginar)
```

Recomendaciones:
- Mantén **una sola fuente de verdad** en el estado del frontend: el array de mensajes ordenados por `sendAt`.
- Al recibir un mensaje por WebSocket, busca su posición en el array con base en `sendAt` e insértalo. No lo agregues al final sin verificar — un mensaje puede llegar con `sendAt` anterior al último visible si hay latencia.
- Al cambiar de chat, **resetea el array y `nextCursor`** antes de hacer la primera carga.

## Ejemplo de Consumo desde el Frontend

### TypeScript (con fetch nativo)

```typescript
// Tipos — ajusta según tu frontend
interface Message {
  id: string;
  content: string;
  chatId: string;
  senderId: string;
  sendAt: string;       // ISO-8601
  type: 'TEXT' | 'IMAGE' | 'VIDEO' | 'DOCUMENT';
  metadata: MessageMetadata | null;
}

interface MessageMetadata {
  fileName: string | null;
  sizeBytes: number | null;
  width: number | null;
  height: number | null;
  durationSeconds: number | null;
  mimeType: string | null;
}

interface CursorPage {
  nextCursor: string | null;
}

interface PageResponse {
  data: Message[];
  metadata: CursorPage;
}

// Servicio de mensajes
async function fetchMessages(
  chatId: string,
  before: string | null = null,
  limit = 50
): Promise<PageResponse> {
  const params = new URLSearchParams({ limit: String(limit) });
  if (before) params.set('before', before);

  const response = await fetch(
    `/api/chats/${chatId}/message?${params.toString()}`,
    { credentials: 'include' } // envía la cookie de sesión
  );

  if (!response.ok) {
    throw new Error(`Error al cargar mensajes: ${response.status}`);
  }

  return response.json();
}
```

### Hook de React (ejemplo completo)

```typescript
function useChatMessages(chatId: string) {
  const [messages, setMessages] = useState<Message[]>([]);
  const [nextCursor, setNextCursor] = useState<string | null>(null);
  const [loadingMore, setLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(true);

  // Reset al cambiar de chat
  useEffect(() => {
    setMessages([]);
    setNextCursor(null);
    setHasMore(true);
    loadInitial();
  }, [chatId]);

  async function loadInitial() {
    const page = await fetchMessages(chatId, null, 50);
    setMessages(page.data);
    setNextCursor(page.metadata.nextCursor);
    setHasMore(page.metadata.nextCursor !== null);
  }

  async function loadMore() {
    if (!hasMore || loadingMore || !nextCursor) return;
    setLoadingMore(true);
    try {
      const page = await fetchMessages(chatId, nextCursor, 50);
      // PREPEND: mensajes más antiguos van ARRIBA de la lista actual
      setMessages(prev => [...page.data, ...prev]);
      setNextCursor(page.metadata.nextCursor);
      setHasMore(page.metadata.nextCursor !== null);
    } finally {
      setLoadingMore(false);
    }
  }

  // Llamar a loadMore() desde tu IntersectionObserver o scroll listener
  // cuando el usuario se acerca al tope de la lista.

  return { messages, loadMore, loadingMore, hasMore };
}
```

### JavaScript puro (scroll listener)

```javascript
let messages = [];
let nextCursor = null;
let hasMore = true;
let loadingMore = false;

async function loadInitial(chatId) {
  const page = await fetchMessages(chatId, null, 50);
  messages = page.data;
  nextCursor = page.metadata.nextCursor;
  hasMore = nextCursor !== null;
  render();
}

async function loadMore(chatId) {
  if (!hasMore || loadingMore || !nextCursor) return;
  loadingMore = true;
  renderLoadingIndicator();
  try {
    const page = await fetchMessages(chatId, nextCursor, 50);
    // PREPEND: los antiguos van al inicio del array
    messages = [...page.data, ...messages];
    nextCursor = page.metadata.nextCursor;
    hasMore = nextCursor !== null;
  } finally {
    loadingMore = false;
    render();
  }
}

// Detectar scroll hacia arriba
const listEl = document.querySelector('#chat-messages');
listEl.addEventListener('scroll', () => {
  if (listEl.scrollTop < 100) { // cerca del tope
    loadMore(currentChatId);
  }
});
```

## Manejo de Errores

| Código | Causa probable                                  | Acción recomendada                                    |
| ------ | ----------------------------------------------- | ----------------------------------------------------- |
| 400    | `limit` fuera de rango (1–100) o `before` malformado | Mostrar mensaje y no reintentar                   |
| 401    | Sesión expirada o sin autenticación             | Redirigir a login                                     |
| 404    | Chat no existe                                   | Mostrar error y volver a la lista de chats            |

Las respuestas de error usan formato RFC 7807:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "limit: must be greater than or equal to 1",
  "instance": "/api/chats/6a3e157559cddb4b8398f622/message"
}
```

## Detección de Fin de Historial

Hay tres formas de saber que ya no hay más mensajes para cargar:

1. **`nextCursor === null`** en la respuesta (camino recomendado)
2. **La respuesta trae menos mensajes que `limit`** — por ejemplo, pediste 50 y devolvió 12
3. **La respuesta viene vacía** (`data.length === 0`)

Cuando detectes cualquiera de estas tres condiciones, deja de hacer peticiones y oculta el indicador de "cargando más".

## Errores Comunes a Evitar

| ❌ No hagas                                                          | ✅ Haz en su lugar                                                          |
| ------------------------------------------------------------------- | --------------------------------------------------------------------------- |
| Pasar `page` o `size` en la URL                                     | Usar solo `limit` y `before`                                                |
| Concatenar mensajes al **final** del array en cada paginación       | Hacer **prepend** (insertar al inicio) — son los más antiguos              |
| Reutilizar el mismo `nextCursor` tras recibir un mensaje por WS     | `nextCursor` solo cambia cuando recibes una respuesta paginada             |
| Seguir pidiendo cuando `nextCursor === null`                        | Detener el infinite scroll                                                  |
| Confiar en `data.length === limit` para saber si hay más             | Usar `nextCursor === null` como señal canónica                             |
| Cachear `nextCursor` en `localStorage` para reanudar al recargar    | Pedir siempre desde el principio al cambiar de chat (es lo más simple)      |

## Notas de Implementación

- El backend **fuerza el orden** `sentAt DESC` server-side. El cliente no puede alterarlo.
- El cursor es el `sentAt` del mensaje más antiguo de cada página, serializado como ISO-8601.
- Para evitar un `count()` extra en MongoDB, el backend pide `limit + 1` mensajes internamente y usa el resultado para decidir si hay siguiente página.
- El endpoint **no requiere** que el cliente envíe headers especiales más allá de la autenticación de sesión (cookie/token).
- La latencia entre un mensaje nuevo llegando por WebSocket y el siguiente `loadMore` no causa duplicados: el cursor filtra con `sent_at < before`, así que los mensajes nuevos (con `sentAt` posterior) nunca aparecen en páginas de histórico.
