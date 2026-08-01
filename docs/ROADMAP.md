# Roadmap

## Posição atual

**Fase 4 de 6 - Identidade, evolução pessoal e nutrição (em andamento).**

| Fase | Entrega | Estado |
| --- | --- | --- |
| 0 | Fundação nativa e arquitetura | Concluída |
| 1 | Rotinas, treino ativo e histórico | Concluída |
| 2 | Métricas, calendário, mapas e progresso | Concluída |
| 3 | Integrações Android, Health Connect e importação | Concluída |
| 4 | Perfil, fotos, navegação, evolução geral e nutrição | Em andamento |
| 5 | Conta Google, sincronização e recuperação entre aparelhos | Preparada |
| 6 | Produção, privacidade, monitoramento e Play Store | Planejada |

## Concluído

### Fundação

- [x] Projeto Kotlin, Jetpack Compose e Material 3
- [x] Arquitetura multi-módulo, Hilt, Room e DataStore
- [x] Design System claro, escuro, compacto e com cores configuráveis
- [x] Persistência offline-first e migrações de banco
- [x] Testes locais, instrumentados e verificação com Android Lint

### Treinos

- [x] Catálogo auditado com 126 exercícios, filtros, aliases sem acento e mídia local válida
- [x] Exercícios personalizados e página de detalhes
- [x] Rotinas organizadas em pastas
- [x] Busca de rotinas por nome, exercício ou pasta
- [x] Treino ativo persistente com peso, repetições e aquecimento
- [x] Comparação com desempenho anterior e detecção de PR
- [x] Cronômetro, duração, validações e notificação de treino ativo
- [x] Finalização, descarte e histórico detalhado
- [x] Quantidade planejada de séries normais e de aquecimento por exercício
- [x] Reordenação por pressão longa de pastas, rotinas e exercícios
- [x] Adicionar, trocar e remover exercícios durante a sessão
- [x] Anotações persistentes por exercício para o treino seguinte
- [x] Decisão ao finalizar: manter, atualizar ou copiar a rotina original
- [x] Comparação opcional restrita às rotinas da mesma pasta

### Progresso

- [x] Dashboard compacto
- [x] Histórico pesquisável por treino, exercício, local e data
- [x] Filtros de período e localização
- [x] Métricas, gráficos e evolução por exercício
- [x] Recordes de carga e volume
- [x] Calendário, metas, horários e sequências
- [x] Mapa por treino e mapa geral de locais
- [x] Perfil local, peso corporal e editor circular de avatar
- [x] Avatar persistente com zoom, movimento e rotação por gesto ou botão
- [x] Peso corporal com data editável e bloqueio de datas futuras
- [x] Galeria de fotos com toque para detalhes, seleção por pressão longa e período exato
- [x] Comparação de fotos com divisória arrastável
- [x] Evolução separada por treinos, volume, peso corporal e PRs

### Android nativo

- [x] Permissões contextuais de notificação e localização
- [x] Canais e notificações de descanso, treino ativo e agenda
- [x] Health Connect
- [x] Painel Health Connect com origem Samsung Health, passos e gráfico de 7/30 dias
- [x] Widget e atalhos do sistema
- [x] Compartilhamento de treino em formato story com foto, temas e dados editáveis
- [x] Widgets de treino, nutrição e hidratação com ação rápida de água
- [x] Backup criptografado e transferência entre dispositivos elegíveis
- [x] Ícone adaptativo com variações de cor sem reinicialização imediata
- [x] Barra inferior compacta e menu lateral para áreas secundárias

### Nutrição

- [x] Diário alimentar local por data
- [x] Metas de calorias e macronutrientes
- [x] Foto pela câmera ou galeria
- [x] Cálculo calórico por proteína, carboidrato e gordura
- [x] Meta circular e registro rápido de hidratação
- [x] Lembretes periódicos configuráveis de água, pausa e movimento
- [ ] Análise automática da refeição por foto
- [ ] Banco de alimentos e leitura de código de barras

## Próxima rodada

### Prioridade 1 - Fechar a Fase 4

- [ ] Validar dashboard, perfil, fotos, filtros e rotinas em aparelho real
- [ ] Registrar falhas reproduzíveis no roteiro `docs/VALIDATION.md`
- [ ] Revisar estados vazios, teclado, acessibilidade e telas pequenas
- [ ] Adicionar testes de interface para perfil, histórico e rotinas
- [x] Concluir auditoria visual individual de GIF, nome, músculo e instruções do catálogo
- [ ] Substituir mídias provisórias pelo conjunto final do boneco branco
- [ ] Monitorar consumo de armazenamento das fotos de progresso
- [ ] Editar data, ângulo, peso e observação de cada foto de progresso
- [ ] Unificar peso, fotos, treinos e nutrição em comparações por período
- [ ] Definir fonte nutricional e estratégia da análise por imagem

### Prioridade 2 - Iniciar a Fase 5

- [x] Escolher Firebase e documentar segurança, custos e limites
- [x] Preparar contratos `AuthRepository`, `AccountSession` e `SyncRepository`
- [x] Preparar Credential Manager e dependências Firebase por ambiente
- [ ] Implementar `FirebaseAuthRepository` e sessão opcional
- [ ] Login nativo com Google e opção por e-mail
- [ ] Fila persistente de sincronização com WorkManager
- [ ] Mesclagem explícita entre dados locais e remotos
- [ ] Exportação e exclusão de conta

### Prioridade 3 - Comparações e análises

- [ ] Comparar dois treinos ou dois períodos
- [ ] Tabelas por exercício, músculo, rotina e local
- [ ] Tendências de carga, volume, frequência e duração
- [ ] Relação entre meta, sequência e aderência semanal
- [ ] Exportação de relatórios

### Prioridade 4 - Expandir nutrição

- [x] Definir modelo de metas calóricas e macronutrientes
- [x] Registro simples de refeições com foto
- [ ] Integrar peso, nutrição e evolução fotográfica
- [ ] Avaliar fontes confiáveis para banco de alimentos
- [ ] Ler dados nutricionais compatíveis pelo Health Connect
- [x] Adicionar hidratação e meta diária
- [ ] Adicionar fibras e adesão semanal

## Depois

- [ ] Tutorial guiado
- [ ] Wear OS
- [ ] Ícone e splash finais
- [ ] Assinatura e build de produção
- [ ] Política de privacidade e publicação na Play Store
- [ ] Monitoramento de erros e desempenho
