# 0001 - Arquitetura multi-módulo

Status: aceita.

## Decisão

Usar módulos `app`, `core` por responsabilidade e `feature` por fluxo de usuário.
Configurações Android repetidas ficam em convention plugins dentro de `build-logic`.

## Consequências

Fronteiras públicas ficam explícitas e features podem crescer isoladamente. O custo é
uma configuração inicial maior e builds com mais projetos, compensado por plugins de
convenção e número controlado de módulos.
