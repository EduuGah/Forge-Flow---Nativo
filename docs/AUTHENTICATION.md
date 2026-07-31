# Autenticação e sincronização do ForgeFlow

## Estado atual

A base técnica está preparada para Google Credential Manager, Firebase Authentication,
Cloud Firestore e Cloud Storage. A conta continuará opcional e o banco local continuará
sendo a fonte principal durante o treino.

Já existem os contratos `AuthRepository`, `AccountSession` e `SyncRepository`. O login só
será ativado depois que o projeto Firebase de desenvolvimento for conectado, para não
exibir uma opção que ainda não pode concluir o fluxo.

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

1. Implementar `FirebaseAuthRepository` e a tela de conta no Perfil.
2. Abrir o Credential Manager e trocar o Google ID token por uma sessão Firebase.
3. Criar regras e emuladores de Auth, Firestore e Storage.
4. Sincronizar primeiro perfil e rotinas.
5. Adicionar histórico, peso, nutrição e fotos em lotes separados.
6. Implementar mesclagem, exclusão de conta e exportação.

## Checklist antes de ativar

- [ ] `app/google-services.json` de desenvolvimento presente
- [ ] SHA-1 e SHA-256 de debug cadastrados
- [ ] provedor Google habilitado
- [ ] Firestore e Storage criados em modo bloqueado
- [ ] regras testadas no Emulator Suite
- [ ] política de privacidade descrevendo saúde, fotos, nutrição e localização
- [ ] cotas e retenção de mídia definidas
