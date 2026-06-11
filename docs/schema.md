## Table structure

### users

| Name             | Type         | Settings         | References | Note |
| ---------------- | ------------ | ---------------- | ---------- | ---- |
| **id**           | TEXT         | 🔑 PK, not null  |            |      |
| **public_id**    | VARCHAR(255) | not null, unique |            |      |
| **email**        | VARCHAR(255) | not null, unique |            |      |
| **display_name** | VARCHAR(255) | not null         |            |      |
| **avatar_url**   | TEXT         | not null         |            |      |
| **status**       | VARCHAR(255) | not null         |            |      |
| **created_at**   | TIMESTAMPTZ  | not null         |            |      |
| **updated_at**   | TIMESTAMPTZ  | not null         |            |      |

### conversations

| Name                | Type        | Settings        | References | Note |
| ------------------- | ----------- | --------------- | ---------- | ---- |
| **id**              | TEXT        | 🔑 PK, not null |            |      |
| **participants**    | VECTOR      | not null        |            |      |
| **last_message**    | TEXT        | not null        |            |      |
| **last_message_at** | TIMESTAMPTZ | not null        |            |      |
| **created_at**      | TIMESTAMPTZ | not null        |            |      |

### messages

| Name                | Type        | Settings        | References                                | Note |
| ------------------- | ----------- | --------------- | ----------------------------------------- | ---- |
| **id**              | TEXT        | 🔑 PK, not null |                                           |      |
| **sender_id**       | TEXT        | null            | fk_messages_sender_id_users               |      |
| **conversation_id** | TEXT        | null            | fk_messages_conversation_id_conversations |      |
| **content**         | TEXT        | null            |                                           |      |
| **status**          | TEXT        | null            |                                           |      |
| **delivered_at**    | TIMESTAMPTZ | null            |                                           |      |
| **sent_at**         | TIMESTAMPTZ | null            |                                           |      |
| **deleted_at**      | TIMESTAMPTZ | null            |                                           |      |

### sessions

| Name                   | Type        | Settings        | References                | Note |
| ---------------------- | ----------- | --------------- | ------------------------- | ---- |
| **id**                 | TEXT        | 🔑 PK, not null |                           |      |
| **device_name**        | TEXT        | null            |                           |      |
| **device_os**          | TEXT        | null            |                           |      |
| **device_fingerprint** | TEXT        | null            |                           |      |
| **is_active**          | BOOLEAN     | null            |                           |      |
| **created_at**         | TIMESTAMPTZ | null            |                           |      |
| **user_id**            | TEXT        | null            | fk_sessions_user_id_users |      |
