# Banco de dados

## Implementado na versão 1

Tabela `exercises`:

| Coluna | Tipo | Regra |
| --- | --- | --- |
| `id` | TEXT | chave primária UUID |
| `name` | TEXT | índice único |
| `primary_muscle_group` | TEXT | índice |
| `secondary_muscle_groups` | TEXT | nomes de enum separados |
| `equipment` | TEXT | nome do enum |
| `instructions` | TEXT | instrução do exercício |
| `is_custom` | INTEGER | booleano |
| `created_at_epoch_millis` | INTEGER | instante UTC |
| `updated_at_epoch_millis` | INTEGER | instante UTC |

O schema do Room é exportado para `core/database/schemas`. O seed usa UUIDs fixos,
índice único por nome e `OnConflictStrategy.IGNORE`; abrir o aplicativo repetidamente
não duplica os cinco exercícios iniciais.

## Entidades planejadas

- `workout_routines`
- `routine_exercises`
- `workout_sessions`
- `workout_session_exercises`
- `workout_sets`

Os modelos de domínio correspondentes já existem em `:core:model`, mas suas tabelas e
DAOs serão adicionados apenas quando houver uma fatia vertical que os utilize.

## Relações planejadas

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
- Peso é `Weight` no domínio e será persistido em gramas como INTEGER.
- Formatação em quilogramas pertence à apresentação, não ao banco.

## Migrations

A versão inicial é 1. Toda mudança de schema deve:

1. aumentar a versão;
2. adicionar uma `Migration` explícita ao `MigrationRegistry`;
3. atualizar o schema exportado;
4. incluir teste da migration.

Produção não usa `fallbackToDestructiveMigration`.

## Treino ativo

A implementação futura persistirá a sessão antes de abrir a tela e cada série logo após
uma alteração. Uma restrição transacional, apoiada por índice parcial quando necessário,
garantirá no máximo uma sessão comum `ACTIVE`. Sessões `TUTORIAL` não participarão dessa
restrição.

A Home observará uma consulta de sessão ativa por Flow. Assim, rotação, encerramento do
app e morte do processo não perderão o treino.

## Dados de tutorial

Sessões de demonstração terão status `TUTORIAL`. Consultas de histórico comum filtrarão
esse status obrigatoriamente. Ao concluir ou abandonar o tutorial, uma transação removerá
a sessão e seus filhos em cascata. Preferências do tutorial ficarão no DataStore, não nas
tabelas de treino.
