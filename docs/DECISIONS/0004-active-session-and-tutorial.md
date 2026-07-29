# 0004 - Sessão ativa e tutorial

Status: aceita para implementação futura.

## Decisão

Treino ativo será persistido no Room a cada mudança e haverá apenas uma sessão comum
ativa. Tutorial usa status `TUTORIAL`, preferências separadas e dados removíveis.

Elementos guiados terão tags estáveis e âncoras medidas. Avanço e scroll dependerão do
estado real e da presença do componente, sem delays fixos.

## Consequências

Treinos sobrevivem à morte do processo. Dados de demonstração não contaminam histórico
nem bloqueiam uma sessão real. O overlay do tutorial precisará viver acima do conteúdo
raiz e posicionar mensagens conforme o espaço disponível.
