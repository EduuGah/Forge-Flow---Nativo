# 0002 - Persistência offline-first

Status: aceita.

## Decisão

Room é a fonte de verdade para dados relacionais. DataStore guarda somente preferências
pequenas. Repositories escondem a origem e expõem Flow/operadores suspend.

## Consequências

O aplicativo funciona sem rede e sobrevive à morte do processo. Uma futura API deverá
sincronizar com Room em vez de alimentar diretamente a interface.
