# Roteiro de validação

Use este arquivo para registrar problemas encontrados no APK sem precisar iniciar
uma rodada de implementação.

## Como registrar um problema

```text
Tela:
Ação realizada:
Resultado atual:
Resultado esperado:
Frequência: sempre / às vezes / uma vez
Modo: claro / escuro
Dispositivo e Android:
Print ou vídeo:
```

Prioridades:

- `P0`: perda de dados, falha ao abrir ou travamento recorrente.
- `P1`: fluxo principal bloqueado, treino ou histórico incorreto.
- `P2`: comportamento confuso ou problema visual relevante.
- `P3`: acabamento, texto ou sugestão.

## Checklist rápido

### Dashboard

- [ ] Abrir sem treino ativo
- [ ] Abrir com treino ativo e continuar a sessão
- [ ] Conferir meta semanal, sequência, volume e próximo treino
- [ ] Abrir calendário, evolução, biblioteca e rotinas
- [ ] Validar nome do perfil no cabeçalho

### Perfil e fotos

- [ ] Salvar nome, nascimento, altura, peso, objetivo e experiência
- [ ] Alternar entre kg e lb sem alterar o valor real
- [ ] Adicionar uma foto pelo seletor do Android
- [ ] Comparar duas ou mais fotos
- [ ] Abrir detalhes com toque, selecionar com pressão longa e filtrar período exato
- [ ] Arrastar a divisória entre duas fotos na comparação
- [ ] Navegar entre registros e excluir uma foto
- [ ] Reiniciar o aplicativo e confirmar a persistência

### Rotinas e biblioteca

- [ ] Buscar rotina por nome, exercício e pasta
- [ ] Expandir e recolher pastas
- [ ] Criar, editar, mover e excluir uma rotina
- [ ] Copiar uma rotina e uma pasta; reordenar ambas por pressão longa
- [ ] Definir séries normais, aquecimentos e observação por exercício
- [ ] Buscar e filtrar exercícios por músculo
- [ ] Buscar por apelidos comuns (`serrote`, `hip thrust`, `preacher curl`) e com espaço final
- [ ] Abrir detalhes de um exercício
- [ ] Conferir GIF e instruções dos nove exercícios corrigidos nesta rodada
- [ ] Criar e excluir exercício personalizado

### Treino ativo

- [ ] Iniciar rotina e treino vazio
- [ ] Registrar aquecimento e séries normais
- [ ] Confirmar peso anterior, PR de carga e PR de volume
- [ ] Adicionar e excluir série
- [ ] Reordenar, adicionar, trocar e remover exercício durante o treino
- [ ] Salvar uma anotação e confirmá-la no treino seguinte
- [ ] Usar cronômetro em primeiro e segundo plano
- [ ] Tentar finalizar sem séries válidas
- [ ] Finalizar com nome e localização
- [ ] Finalizar mantendo a rotina, atualizando a original e criando uma cópia
- [ ] Conferir notificação e barra de treino ativo

### Histórico e evolução

- [ ] Pesquisar por treino, exercício, local e data
- [ ] Combinar período e filtro de localização
- [ ] Limpar todos os filtros
- [ ] Expandir treino e conferir séries, volume e PRs
- [ ] Excluir treino com confirmação
- [ ] Abrir mapa do treino e mapa geral
- [ ] Tocar nos gráficos e conferir o valor selecionado

### Android

- [ ] Alternar tema, modo compacto e cor principal
- [ ] Confirmar que a troca de cor não fecha o aplicativo
- [ ] Verificar ícone adaptativo após sair do app
- [ ] Conectar, sincronizar e revogar Health Connect
- [ ] Conferir origem Samsung Health e histórico de passos em 7 e 30 dias
- [ ] Registrar água e validar lembretes de bem-estar
- [ ] Testar widget e atalhos
- [ ] Reiniciar o aparelho com treino ou cronômetro ativo

## Resultado da rodada

```text
APK/commit:
Dispositivo:
Android:
Data:
P0:
P1:
P2:
P3:
Observações:
```
