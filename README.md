# Fiscalização em Campo — Android em Java (MVP + RxJava)

App para **agentes de vigilância sanitária** registrarem fiscalizações em estabelecimentos **sem depender de sinal**: os dados são salvos no aparelho com geolocalização e sincronizados depois, com retentativas inteligentes.

> Segmento: **Setor Público**

## Stack
**Java 17** • Android Views (XML) + **ViewBinding** • Material Components • **MVP** (Model-View-Presenter) • **RxJava 3** + RxAndroid • **Room** (`room-rxjava3`) • RecyclerView com `ListAdapter`/`DiffUtil` • LocationManager (sem Google Play Services) • JUnit 4 • Mockito • `TestScheduler`

## Arquitetura MVP

```
 Activity (View passiva) ◄──► Contract ◄──► Presenter (Java puro, testável na JVM)
                                                │
                                   InspectionRepository ──► Room (fonte local)
                                                │
                                   SyncManager ──► InspectionApi (idempotente por UUID)
```
- **Views passivas**: as Activities só desenham; validação, estados e decisões ficam no Presenter.
- **`attach`/`detach`** com `CompositeDisposable`: sem vazamento de Activity e sem callback em tela destruída.
- **Schedulers injetados** (`io`, `ui`, `timer`): nos testes viram `trampoline()`/`TestScheduler`, e os testes rodam síncronos e determinísticos.

## Offline-first e sincronização
- Cada fiscalização nasce com **UUID gerado no cliente**: o reenvio não duplica registros no servidor.
- `SyncManager` envia **um a um** (`concatMapCompletable`) com **backoff exponencial** (1 s → 2 s → 4 s) **apenas para erros de rede**. Erros de negócio (ex.: HTTP 422) não são retentados.
- Um registro problemático é marcado como `FAILED` e **não bloqueia o lote**.
- Os testes verificam o backoff com **tempo virtual** (`TestScheduler`), sem `sleep`.

## Regras de negócio
- CNPJ validado por dígito verificador.
- "Aprovado com ressalvas" ou "Interditado" **exigem descrição** das irregularidades (exigência legal).
- Localização GPS obrigatória como prova de presença no local.

## Como executar
```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```
A `FlakyFakeApi` simula uma conexão 3G instável (cerca de 40% de falhas) para demonstrar a sincronização resiliente.
