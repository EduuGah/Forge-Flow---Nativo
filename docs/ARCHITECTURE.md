# Arquitetura

## Objetivos

O ForgeFlow é offline-first. A interface não conhece detalhes de Room, DataStore ou de
uma futura API. A organização favorece entregas verticais pequenas sem criar camadas vazias.

## Camadas

```text
Compose UI -> ViewModel -> UseCase (quando houver regra real) -> Repository -> DataSource
Room/DataStore -> Flow -> Repository -> ViewModel -> UiState -> Compose
```

- UI: rotas conectam ViewModels; telas de conteúdo recebem estado e callbacks.
- Domain: modelos e regras independentes da UI e da persistência. Use cases só serão
  adicionados quando houver lógica de negócio reutilizável.
- Data: contratos e implementações de repositories, fontes locais e mapeamento de erros.
- Persistence: Room guarda dados relacionais; DataStore guarda preferências pequenas.

`DataResult` impede que exceções do Room atravessem o contrato do repository. Os estados
de tela são imutáveis e as ações da interface são explícitas.

## Responsabilidade dos módulos

- `:app`: Application, Activity, tema raiz, barra inferior e composição do grafo.
- `:core:common`: relógio, dispatchers, escopo da aplicação e erros compartilhados.
- `:core:model`: modelos de domínio, IDs tipados, unidades e enums.
- `:core:designsystem`: tema, tokens, componentes e tags de teste.
- `:core:database`: Room, entidades, DAOs, mappers e migrations.
- `:core:data`: repositories, fontes locais, seed e DataStore.
- `:core:navigation`: destinos serializáveis compartilhados.
- `:core:testing`: fakes, builders e regras de corrotinas.
- `:feature:*`: navegação e apresentação de cada funcionalidade.

## Navegação

O aplicativo usa uma única `MainActivity` e destinos tipados. Home, Rotinas, Histórico e
Configurações são destinos de topo. A navegação entre abas usa `saveState`, `restoreState`
e `launchSingleTop`.

Exercícios e Treino Ativo ficam fora da barra inferior. O botão voltar remove esses
destinos e retorna ao ponto de origem. Deep links não são declarados até existirem URLs
e contratos reais.

## Injeção

Hilt cria e conecta banco, DAO, fontes locais, repositories, DataStore, relógio e escopos
de corrotinas. Dependências Android usam `@ApplicationContext`; o domínio não recebe
`Context`.

O escopo de aplicação é explícito e supervisionado. Não há `GlobalScope`, singleton
manual nem Service Locator.

## Estado Compose

- ViewModels expõem `StateFlow<UiState>`.
- Rotas usam `collectAsStateWithLifecycle`.
- Componentes menores não recebem ViewModels.
- Listas usam IDs estáveis como chave.
- Previews chamam apenas composables de conteúdo.
- Dados de negócio permanecem no ViewModel/repository/Room, não em `remember`.

## Offline-first

Room é a fonte de verdade para exercícios e será a fonte de verdade para rotinas,
sessões e histórico. A futura fonte remota será combinada dentro dos repositories; as
telas e ViewModels não precisarão mudar de origem de dados.

Escritas relacionais são `suspend`. Observações são `Flow`. Sincronização e WorkManager
estão fora da Fase 0; apenas a dependência está catalogada.

## Tutorial futuro

`ForgeFlowTestTags` centraliza identificadores estáveis. A implementação futura deve:

1. registrar âncoras por identificador e coordenadas de layout;
2. observar o estado real e a presença da âncora com Flow/snapshot state;
3. usar `BringIntoViewRequester` antes de medir;
4. calcular a posição da caixa pelo espaço disponível;
5. desenhar overlay e recorte acima de toda a árvore;
6. avançar por eventos reais, nunca por delays fixos;
7. persistir o progresso separadamente no DataStore;
8. usar sessão `TUTORIAL`, isolada do histórico comum.

## Criar uma feature

1. Crie `:feature:nome` e aplique `forgeflow.android.feature`.
2. Adicione somente dependências de domínio usadas pela feature.
3. Defina estado e ações na apresentação.
4. Crie ViewModel apenas se houver estado ou lógica.
5. Separe rota com ViewModel do composable de conteúdo.
6. Exponha uma função de `NavGraphBuilder` e mantenha o destino em `:core:navigation`.
7. Adicione previews e testes de lógica/UI.
8. Integre o destino em `:app`.

Não crie `BaseViewModel`, `BaseRepository`, `Manager` genérico ou camada `domain` vazia.
