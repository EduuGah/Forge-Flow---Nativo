# Autenticação e sincronização do ForgeFlow

## Estado atual

A entrada nativa pelo Google Credential Manager e a sessão do Firebase Authentication estão
implementadas no Perfil. A conta continua opcional e o banco local continua sendo a fonte
principal durante o treino.

Sem `app/google-services.json`, a seção de conta permanece visível para orientar a configuração,
mas o botão de entrada fica desativado. A sincronização de dados com Cloud Firestore e Cloud
Storage ainda não está ativa; entrar identifica o usuário, mas não envia dados locais.

## Onde colocar a configuração do Google

Não coloque uma chave manualmente em um arquivo Kotlin, `local.properties` ou recurso XML.

1. Crie um projeto no [Firebase Console](https://console.firebase.google.com/).
2. Adicione um aplicativo Android com o package exato `com.forgeflow.app`.
3. Na raiz do projeto, execute:

   ```powershell
   .\gradlew.bat signingReport
   ```

4. Copie os valores SHA-1 e SHA-256 do variant `debug` para **Project settings > Your apps**.
5. Em **Authentication > Sign-in method**, habilite Google.
6. Baixe novamente o arquivo chamado exatamente `google-services.json`.
7. Coloque-o neste caminho:

   ```text
   C:\Projetos\ForgeFlow-Android\app\google-services.json
   ```

O Gradle detecta esse arquivo e ativa o plugin do Google Services automaticamente. O arquivo
está ignorado pelo Git para permitir projetos separados de desenvolvimento e produção.

O `google-services.json` contém identificadores do aplicativo, não uma credencial de servidor.
Mesmo assim, chaves de conta de serviço, Firebase Admin SDK e segredos de backend nunca podem
ser colocados no APK ou no repositório.

## Arquitetura escolhida

- **Entrada:** Credential Manager com Sign in with Google.
- **Identidade:** Firebase Authentication.
- **Dados sincronizados:** Cloud Firestore, separados por `uid`.
- **Fotos:** Cloud Storage, privadas por `uid` e por categoria.
- **Offline:** Room e DataStore continuam atendendo toda a experiência local.
- **Fila:** WorkManager enviará alterações finalizadas quando houver conta e rede.

## Exportação e restauração atuais

- **Exportação manual:** Ajustes > Dados e backup cria um `.zip` com Room, DataStore, avatar,
  fotos de progresso e fotos de nutrição.
- **Backup Android:** banco e arquivos elegíveis podem ser restaurados pelo Android durante a
  configuração ou transferência de um aparelho, conforme disponibilidade e limites do sistema.
- **Conta Google:** autentica o usuário, mas ainda não é uma cópia remota dos dados do ForgeFlow.
- **Importação do `.zip`:** o pacote já inclui versão de formato; o fluxo seguro de restauração
  será implementado junto da mesclagem da Fase 5.

Estrutura remota inicial:

```text
users/{uid}
users/{uid}/routines/{routineId}
users/{uid}/workouts/{workoutId}
users/{uid}/bodyWeights/{entryId}
users/{uid}/nutritionMeals/{mealId}
users/{uid}/progressPhotos/{photoId}
```

As regras do Firestore e Storage devem exigir `request.auth.uid == uid`. Nenhum usuário pode
consultar ou escrever no namespace de outro usuário.

## Regras de produto

- É possível treinar sem conta e sem internet.
- Entrar não substitui dados locais automaticamente.
- A primeira conexão mostra uma etapa explícita de mesclagem.
- Treino ativo permanece local; só entra na fila depois de finalizado.
- Logout preserva os dados locais por padrão.
- Fotos, nutrição e localização possuem consentimentos de sincronização separados.
- Exclusões são sincronizadas com marcadores persistentes para não reaparecerem em outro aparelho.
- A conta oferece exportação e exclusão dos dados remotos.

## Próxima implementação

1. Criar regras e emuladores de Auth, Firestore e Storage.
2. Sincronizar primeiro perfil e rotinas.
3. Adicionar histórico, peso, nutrição e fotos em lotes separados.
4. Implementar mesclagem e exclusão remota de conta.
5. Implementar importação validada dos pacotes exportados.

## Checklist antes de ativar

- [ ] `app/google-services.json` de desenvolvimento presente
- [ ] SHA-1 e SHA-256 de debug cadastrados
- [ ] provedor Google habilitado
- [ ] Firestore e Storage criados em modo bloqueado
- [ ] regras testadas no Emulator Suite
- [ ] política de privacidade descrevendo saúde, fotos, nutrição e localização
- [ ] cotas e retenção de mídia definidas
