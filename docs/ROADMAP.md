# Roadmap

## Concluído

### Fundação

- [x] Projeto Kotlin, Jetpack Compose e Material 3
- [x] Arquitetura multi-módulo, Hilt, Room e DataStore
- [x] Design System claro, escuro, compacto e com cores configuráveis
- [x] Persistência offline-first e migrações de banco
- [x] Testes locais, instrumentados e verificação com Android Lint

### Treinos

- [x] Catálogo amplo de exercícios com busca, filtros e suporte a mídia
- [x] Exercícios personalizados e página de detalhes
- [x] Rotinas organizadas em pastas
- [x] Busca de rotinas por nome, exercício ou pasta
- [x] Treino ativo persistente com peso, repetições e aquecimento
- [x] Comparação com desempenho anterior e detecção de PR
- [x] Cronômetro, duração, validações e notificação de treino ativo
- [x] Finalização, descarte e histórico detalhado

### Progresso

- [x] Dashboard compacto
- [x] Histórico pesquisável por treino, exercício, local e data
- [x] Filtros de período e localização
- [x] Métricas, gráficos e evolução por exercício
- [x] Recordes de carga e volume
- [x] Calendário, metas, horários e sequências
- [x] Mapa por treino e mapa geral de locais
- [x] Perfil local e comparação de fotos de progresso

### Android nativo

- [x] Permissões contextuais de notificação e localização
- [x] Canais e notificações de descanso, treino ativo e agenda
- [x] Health Connect
- [x] Widget e atalhos do sistema
- [x] Backup criptografado e transferência entre dispositivos elegíveis
- [x] Ícone adaptativo com variações de cor sem reinicialização imediata

## Próxima rodada

### Prioridade 1 - Estabilidade e experiência

- [ ] Validar dashboard, perfil, fotos, filtros e rotinas em aparelho real
- [ ] Registrar falhas reproduzíveis no roteiro `docs/VALIDATION.md`
- [ ] Revisar estados vazios, teclado, acessibilidade e telas pequenas
- [ ] Adicionar testes de interface para perfil, histórico e rotinas
- [ ] Monitorar consumo de armazenamento das fotos de progresso

### Prioridade 2 - Conta e sincronização

- [ ] Escolher backend e documentar segurança, custos e limites
- [ ] Implementar `AuthRepository` e sessão opcional
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

### Prioridade 4 - Nutrição

- [ ] Definir modelo de metas calóricas e macronutrientes
- [ ] Registro simples de refeições e peso corporal
- [ ] Integração com perfil e evolução fotográfica
- [ ] Avaliar fontes confiáveis para banco de alimentos
- [ ] Definir integração compatível com Health Connect

## Depois

- [ ] Tutorial guiado
- [ ] Wear OS
- [ ] Ícone e splash finais
- [ ] Assinatura e build de produção
- [ ] Política de privacidade e publicação na Play Store
- [ ] Monitoramento de erros e desempenho
