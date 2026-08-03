# Firebase, e-mail e monetização

## Configuração validada

O arquivo `app/google-services.json` possui o package `com.forgeflow.app`, cliente Android,
cliente Web e o SHA-1 de debug esperado. Ele permanece ignorado pelo Git. Não coloque chaves de
conta de serviço, senha SMTP ou credenciais do Firebase Admin no aplicativo.

No Firebase Console, ainda é necessário confirmar:

1. Em **Authentication > Sign-in method**, habilitar **Google** e **E-mail/senha**.
2. Em **Authentication > Templates**, revisar redefinição de senha e alteração de e-mail.
3. Em **Firestore**, criar o banco em modo bloqueado e publicar `firestore.rules`.

Para publicar as regras:

```powershell
firebase login
firebase deploy --only firestore:rules --project SEU_PROJECT_ID
```

O backup atual não usa Cloud Storage. O pacote é dividido em documentos menores e salvo no
Firestore, mantendo compatibilidade com o plano Spark dentro das cotas do projeto. Exportação e
restauração manual por ZIP continuam independentes do Firebase.

## Domínio e SMTP

O ForgeFlow entra automaticamente depois do cadastro e não exige verificação do endereço. Para a
redefinição de senha, use o modelo padrão do Firebase Authentication; não é necessário contratar
SMTP. O melhor acabamento opcional é:

- domínio de links: `auth.forgeflow.app`;
- remetente: `noreply@forgeflow.app`;
- resposta de suporte: `suporte@forgeflow.app`.

O domínio precisa ser seu. Configure SPF, DKIM e DMARC com os registros exibidos pelo Firebase ou
pelo provedor de e-mail. Para e-mails transacionais adicionais, a recomendação inicial é Brevo:
oferece relay SMTP e um plano gratuito adequado aos testes. Resend também funciona, mas o plano
gratuito tem limite diário menor.

No Brevo, use `smtp-relay.brevo.com`, porta `587` com TLS, o login SMTP fornecido pelo painel e a
chave SMTP como senha. A chave deve ficar apenas na configuração protegida do Firebase ou backend.

A extensão **Trigger Email** do Firebase usa SMTP e uma coleção do Firestore. Ela serve para
mensagens do produto, recibos e alertas; os modelos de verificação e senha continuam sendo geridos
em Authentication. Nunca permita que o cliente grave destinatário e HTML arbitrários na coleção
de e-mails.

## Apoiadores e administração

O aplicativo lê `entitlements/{uid}` e mostra a tag no Perfil, na barra lateral e na tela
**Apoiar o ForgeFlow**. O documento aceita:

```text
supporterTier: "ADMIN" | "SUPPORTER" | "PRO" | "FOUNDER" | "LIFETIME"
source: "PLAY_BILLING" | "MANUAL"
grantedAt: timestamp
grantedBy: uid do administrador
```

As regras bloqueiam escrita pelo aplicativo. Durante os testes, uma concessão manual pode ser feita
diretamente no Firebase Console. Para produção, use uma Cloud Function com Firebase Admin SDK e
exija a custom claim `admin: true`. Uma página administrativa é útil, mas só é segura quando chama
esse backend; esconder a página ou verificar `admin` apenas no Android não protege nada.

### Conceder uma tag manualmente para teste

1. Abra **Firebase Console > Authentication > Users** e copie o `UID` da conta.
2. Abra **Firestore Database > Data**.
3. Crie a coleção `entitlements`, caso ainda não exista.
4. Adicione um documento usando exatamente o `UID` como identificador do documento.
5. Adicione o campo de texto `supporterTier` com `ADMIN`, `SUPPORTER`, `PRO`, `FOUNDER` ou
   `LIFETIME`. Use `ADMIN` apenas na conta administrativa e `FOUNDER` para concessões manuais.
6. Opcionalmente, adicione `source` como `MANUAL`, `grantedAt` como timestamp e `grantedBy` com o
   UID administrativo.

A mudança aparece em tempo real no Perfil enquanto a conta está conectada. Não adicione uma chave
de administrador ao APK e não permita escrita direta nessa coleção pelas regras do cliente.

## Doações e Google Play Billing

Se o pagamento liberar tag, remoção de anúncios, nuvem ampliada ou qualquer benefício digital,
trate-o como produto digital. Crie no Play Console um produto não consumível, por exemplo
`forgeflow_supporter_lifetime`, e siga este fluxo:

1. O app inicia a compra com Google Play Billing e associa o `uid` ofuscado.
2. Uma Cloud Function recebe o token e consulta a Google Play Developer API.
3. O backend confirma estado `PURCHASED`, impede reutilização do token e grava o entitlement.
4. O backend reconhece a compra e processa cancelamentos/reembolsos.

Não conceda a tag apenas com o retorno recebido pelo APK. Para apoio recorrente, use uma assinatura;
para um selo permanente, use produto não consumível.

## Anúncios

AdMob pode monetizar o plano gratuito, mas só deve ser ativado depois de criar o aplicativo e as
unidades no painel. Antes disso, adicionar um ID fictício de produção ao Manifest pode causar falha
na inicialização. A estratégia recomendada é:

- anúncio nativo discreto no Dashboard ou Histórico;
- nenhum intersticial durante treino ativo, cronômetro ou finalização;
- anúncio recompensado apenas para um benefício opcional e explícito;
- apoiadores sem anúncios;
- IDs de teste durante desenvolvimento;
- UMP antes de solicitar anúncios e opção permanente de rever privacidade em Ajustes.

Quando os IDs do AdMob e o produto do Play Console existirem, eles devem entrar por propriedades de
build por ambiente. IDs públicos podem ficar no APK; credenciais de servidor e chaves privadas não.
