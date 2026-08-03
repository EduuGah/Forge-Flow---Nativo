# Autenticação e sincronização do ForgeFlow

## Estado atual

A entrada nativa pelo Google Credential Manager, conta por e-mail e senha, recuperação de senha e
vínculo de senha à conta Google estão implementados. O banco local continua sendo a fonte principal
durante o treino.

Sem `app/google-services.json`, o aplicativo informa que o login não está disponível nesta
instalação. O backup integral no Cloud Firestore está implementado; a sincronização granular com
resolução de conflitos ainda não está ativa.

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
- **Backup remoto:** Cloud Firestore, privado e separado por `uid`.
- **Offline:** Room e DataStore continuam atendendo toda a experiência local.
- **Fila:** WorkManager enviará alterações finalizadas quando houver conta e rede.

## Exportação e restauração atuais

- **Exportação manual:** Ajustes > Dados e backup cria um `.zip` com Room, DataStore, avatar,
  fotos de progresso e fotos de nutrição.
- **Backup Android:** banco e arquivos elegíveis podem ser restaurados pelo Android durante a
  configuração ou transferência de um aparelho, conforme disponibilidade e limites do sistema.
- **Restauração manual:** valida o formato e os limites do `.zip`, pede confirmação e substitui os
  dados somente após reiniciar o aplicativo.
- **Conta ForgeFlow:** salva metadados em `users/{uid}/backups/latest` e o pacote em uma versão
  dividida em blocos no Firestore. Tamanho, dono e SHA-256 são verificados na restauração.
- **Recuperação:** mantém uma cópia temporária para desfazer uma restauração interrompida.

Estrutura remota inicial:

```text
users/{uid}/backups/latest
users/{uid}/backups/latest/versions/{versionId}
users/{uid}/backups/latest/versions/{versionId}/chunks/{chunkId}
entitlements/{uid}
```

As regras do Firestore exigem `request.auth.uid == uid`. Nenhum usuário pode consultar ou escrever
no namespace de outro usuário. O cliente pode ler a própria tag em `entitlements/{uid}`, mas nunca
gravá-la.

## Regras de produto

- Depois da autenticação inicial, é possível treinar sem internet.
- Entrar não restaura nem substitui dados locais automaticamente.
- Backup e restauração são ações explícitas em Ajustes.
- Treino ativo permanece local; só entra na fila depois de finalizado.
- Logout preserva os dados locais por padrão.
- O pacote integral inclui banco, preferências e mídias privadas.
- A sincronização granular futura terá consentimentos separados para fotos, nutrição e localização.

## Próxima implementação

1. Testar as regras no Emulator Suite e em um projeto de homologação.
2. Implementar exclusão remota de conta e backup.
3. Sincronizar perfil e rotinas com resolução explícita de conflitos.
4. Adicionar histórico, peso, nutrição e fotos em lotes separados.
5. Manter o backup integral como recuperação independente da sincronização granular.

## Checklist antes de ativar

- [ ] `app/google-services.json` de desenvolvimento presente
- [ ] SHA-1 e SHA-256 de debug cadastrados
- [ ] provedor Google habilitado
- [ ] Firestore criado em modo bloqueado
- [ ] regras testadas no Emulator Suite
- [ ] política de privacidade descrevendo saúde, fotos, nutrição e localização
- [ ] cotas, tamanho máximo e retenção de backups revisados
