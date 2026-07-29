# 0003 - UUIDs, tempo e peso inteiro

Status: aceita.

## Decisão

Entidades usam IDs tipados baseados em UUID. Lógica consulta `AppClock`; persistência
armazena instantes em epoch milliseconds. Peso usa gramas inteiras.

## Consequências

IDs podem ser gerados offline e sincronizados depois. Testes controlam o tempo. Cálculos
de peso não acumulam erro de ponto flutuante e a unidade exibida pode mudar no futuro.
