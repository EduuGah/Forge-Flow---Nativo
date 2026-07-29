# Banco de dados

## Schema atual: versão 4

Tabela `exercises`:

| Coluna | Tipo | Regra |
| --- | --- | --- |
| `id` | TEXT | chave primária UUID |
| `name` | TEXT | índice único |
| `primary_muscle_group` | TEXT | índice |
| `secondary_muscle_groups` | TEXT | nomes de enum separados |
| `equipment` | TEXT | nome do enum |
| `instructions` | TEXT | instrução do exercício |
| `media_uri` | TEXT? | origem opcional de imagem ou GIF |
| `media_type` | TEXT? | tipo da mídia |
| `media_thumbnail_uri` | TEXT? | miniatura opcional |
| `is_custom` | INTEGER | booleano |
| `created_at_epoch_millis` | INTEGER | instante UTC |
| `updated_at_epoch_millis` | INTEGER | instante UTC |

O schema do Room é exportado para `core/database/schemas`. O seed usa UUIDs fixos,
índice único por nome e `OnConflictStrategy.IGNORE`; abrir o aplicativo repetidamente
não duplica os cinco exercícios iniciais.

Também estão implementadas:

- `routines`
- `routine_exercises`
- `workout_sessions`
- `workout_session_exercises`
- `workout_sets`

Sessões guardam status, horários, notas e uma localização opcional capturada na
finalização. Exercícios da sessão preservam snapshots de nome, grupo muscular e mídia.
Séries persistem tipo, peso em gramas, repetições, RPE e estado de conclusão.

## Relações

- `routine_exercises.routine_id -> workout_routines.id`: `CASCADE`.
- `routine_exercises.exercise_id -> exercises.id`: `RESTRICT` enquanto estiver em uso.
- `workout_sessions.routine_id -> workout_routines.id`: `SET NULL`.
- `workout_session_exercises.session_id -> workout_sessions.id`: `CASCADE`.
- `workout_session_exercises.exercise_id -> exercises.id`: `SET NULL`; snapshots
  preservam nome e grupo muscular.
- `workout_sets.session_exercise_id -> workout_session_exercises.id`: `CASCADE`.

Posições e chaves estrangeiras receberão índices. Alterações que envolvam sessão,
exercícios e séries serão transacionais.

## IDs, datas e peso

- IDs são UUIDs armazenados de forma consistente como TEXT.
- Datas são `Instant` no domínio e epoch milliseconds UTC no Room.
- Peso é `Weight` no domínio e é persistido em gramas como INTEGER.
- Formatação em quilogramas ou libras pertence à apresentação, não ao banco.
- Localização usa latitude/longitude, precisão, horário da captura e rótulo opcional.

## Migrations

A versão inicial é 1. As migrações existentes são:

- `1 -> 2`: rotinas, sessões, exercícios da sessão e séries.
- `2 -> 3`: mídia opcional dos exercícios e seus snapshots.
- `3 -> 4`: localização opcional da sessão.

Toda mudança de schema deve:

1. aumentar a versão;
2. adicionar uma `Migration` explícita ao `MigrationRegistry`;
3. atualizar o schema exportado;
4. incluir teste da migration.

Produção não usa `fallbackToDestructiveMigration`.

## Treino ativo

A sessão é persistida antes da abertura da tela. Séries adicionadas, concluídas ou
editadas são gravadas no Room, e a Home observa a sessão ativa por Flow. Assim, rotação,
encerramento do app e morte do processo não perdem o treino.

Ao finalizar, a localização só é capturada quando o usuário ativou essa opção e concedeu
a permissão. A falta de posição válida não impede a conclusão do treino.

## Dados de tutorial

Sessões de demonstração terão status `TUTORIAL`. Consultas de histórico comum filtrarão
esse status obrigatoriamente. Ao concluir ou abandonar o tutorial, uma transação removerá
a sessão e seus filhos em cascata. Preferências do tutorial ficarão no DataStore, não nas
tabelas de treino.
