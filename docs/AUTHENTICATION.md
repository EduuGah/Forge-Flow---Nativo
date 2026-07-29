# Autenticação do ForgeFlow

## Objetivo

Adicionar login por Google e por e-mail sem tornar a conta obrigatória para treinar.
O aplicativo deve continuar totalmente funcional sem rede e associar os dados locais
a uma conta somente quando o usuário escolher sincronizar.

## Limites desta fase

- Nenhum SDK de autenticação foi incluído na Fase 0.
- Nenhuma tela falsa de login foi adicionada.
- Identificadores locais continuam sendo UUIDs independentes do provedor.
- O banco local permanece como fonte principal durante o treino.

## Arquitetura planejada

1. `AuthRepository` expõe sessão, login, logout e exclusão da conta.
2. A interface nativa de credenciais do Android oferece Google e credenciais salvas.
3. Login por e-mail usa links de acesso ou senha, conforme o backend escolhido.
4. `SyncRepository` envia mudanças locais por uma fila persistente.
5. O servidor mantém um identificador interno de usuário e identidades vinculadas.
6. A primeira conexão executa uma mesclagem explícita dos dados locais e remotos.

## Regras de dados

- Treinos nunca dependem de uma conexão ativa para iniciar ou finalizar.
- Localização, mídia e notas possuem consentimento e sincronização independentes.
- Tokens não são armazenados nas tabelas de treino nem nas preferências comuns.
- Logout não apaga dados locais sem confirmação.
- Exclusão da conta separa a remoção remota da limpeza opcional do aparelho.
- Conflitos usam versão e data de atualização; exclusões usam marcadores persistentes.

## Fluxo futuro

1. O usuário abre Perfil e escolhe continuar com Google ou e-mail.
2. A conta é autenticada pelo mecanismo nativo.
3. O ForgeFlow mostra quantos treinos existem no aparelho e na nuvem.
4. O usuário confirma a mesclagem.
5. A sincronização roda em segundo plano e mantém o treino ativo local.

## Próxima decisão

Antes da implementação, escolher o backend e documentar:

- formato dos tokens e rotação;
- política de exclusão e exportação;
- criptografia de dados sensíveis;
- resolução de conflitos;
- cotas para fotos, vídeos e GIFs;
- consentimento separado para histórico de localização.
