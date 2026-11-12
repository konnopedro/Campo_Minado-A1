# Campo Minado — Praça Osório 🌳

Aplicativo Android desenvolvido em Kotlin com Jetpack Compose, inspirado no Campo Minado tradicional e ambientado na Praça Osório, em Curitiba. Os galhos caídos fazem o papel das minas.

Trabalho final (A1) da disciplina de Desenvolvimento Android, feito em grupo de 3 pessoas.

## Demonstração

### Vídeo

<!-- Colocar aqui o link ou o arquivo do vídeo curto do app funcionando -->
_Vídeo em breve._

### Prints

<!-- Colocar aqui os prints, por exemplo: ![Login](docs/login.png) -->

| Login | Níveis | Jogo |
|:-----:|:------:|:----:|
| _(print)_ | _(print)_ | _(print)_ |

| Resultado | Recordes | Histórico |
|:---------:|:--------:|:---------:|
| _(print)_ | _(print)_ | _(print)_ |

| Painel do Admin | Editar nível |
|:---------------:|:------------:|
| _(print)_ | _(print)_ |

## Sobre o jogo

O objetivo é limpar o mapa sem abrir nenhuma casa que esconda um galho. Cada nível tem um mapa quadrado; ao abrir uma casa, o jogador vê quantos galhos existem nas oito casas vizinhas.

- **15 níveis** com mapas de 6×6 até 10×10.
- **Galhos** ocupam o lugar das minas do Campo Minado.
- **Placas de aviso** funcionam como bandeiras: podem ser colocadas pelo botão **Abrir / Placa** ou com um **toque longo** na casa.
- **Primeiro toque seguro:** os galhos são sorteados depois da primeira jogada, fora da casa escolhida e das suas vizinhas.
- **Abertura em cascata:** casas com 0 galhos ao redor abrem automaticamente as vizinhas (busca em largura com fila).
- **Sem vidas:** abrir um galho encerra a partida na hora.
- **Vitória:** abrir todas as casas que não têm galho.
- **Progressão:** vencer um nível desbloqueia o próximo.

### Níveis

| Níveis | Tamanho do mapa | Quantidade de galhos |
|:------:|:---------------:|:--------------------:|
| 1–3 | 6×6 | 4, 5 e 6 |
| 4–6 | 7×7 | 7, 8 e 9 |
| 7–9 | 8×8 | 10, 11 e 12 |
| 10–12 | 9×9 | 13, 14 e 15 |
| 13–15 | 10×10 | 16, 18 e 20 |

Esses valores são o padrão. O administrador pode alterar a quantidade de galhos de cada nível (de 1 até 25% das casas do mapa). O tamanho do mapa de cada nível é fixo.

## Como funciona

### Perfis

O app tem dois perfis, escolhidos na tela de Login. O que aparece na tela e na barra de navegação depende do perfil guardado no `SessaoViewModel`.

| | Jogador | Administrador |
|---|---|---|
| Entrada | Nome (novo ou um dos jogadores salvos no aparelho) | Usuário e senha simulados |
| Barra inferior | Níveis, Recordes, Histórico | Níveis (Admin), Recordes |
| Pode | Jogar os níveis desbloqueados, ver recordes, ver e excluir partidas, renomear e excluir o próprio perfil | Editar galhos e ativar/desativar níveis (Firestore), excluir recordes |

Acesso do administrador (simulado, só para a demonstração): usuário `admin`, senha `admin123`.

### Telas

1. **Login** — escolhe Jogador ou Administrador.
2. **Níveis** — grade com os 15 níveis agrupados por tamanho de mapa, com cadeado e melhor tempo.
3. **Jogo** — mapa N×N, galhos restantes, cronômetro e botão Abrir/Placa.
4. **Resultado** — vitória ou derrota, mapa revelado, jogar de novo ou próximo nível.
5. **Recordes** — melhores tempos, filtrados por tamanho de mapa e ordenados por tempo.
6. **Histórico e Perfil** — partidas do jogador (filtro de vitórias/derrotas), renomear e excluir perfil.
7. **Painel do Admin** — níveis vindos do Firestore, com filtro por tamanho de mapa.
8. **Editar nível** (Admin) — ajusta galhos e estado ativo e salva no Firestore.

Jogo e Resultado ficam fora da barra de navegação.

### Dados

- **Room (local):**
  - `JogadorEntity`: nome, nível máximo desbloqueado e data de criação.
  - `PartidaEntity`: nível, tamanho do mapa, galhos, casas abertas, venceu, tempo e data. Tem chave estrangeira para o jogador com `CASCADE`.
  - As duas têm CRUD completo, e as listagens saem como `Flow<List<...>>` coletado pelos ViewModels.

| Operação | `JogadorEntity` | `PartidaEntity` |
|---|---|---|
| **Create** | Login com um nome novo cadastra o jogador | Ao fim da partida, o resultado é salvo |
| **Read** (`Flow`) | Lista de jogadores salvos no Login | Histórico do jogador, Recordes e melhores tempos |
| **Update** | Renomear perfil; nível desbloqueado ao vencer | Anotar / editar a nota de uma partida no Histórico |
| **Delete** | Excluir perfil (apaga as partidas, `CASCADE`) | Excluir partida no Histórico; o Admin exclui nos Recordes |
- **Firebase Firestore (remoto):** a coleção `niveis` guarda a configuração dos 15 níveis. O jogador lê e o administrador edita.
- **Sem internet:** o Repository usa os níveis padrão da tabela acima e o aviso de erro aparece na tela. O jogo continua funcionando.

### Tratamento de erros

O `JogoRepository` é a única ponte entre os ViewModels e as fontes de dados. Ele usa `try-catch` para isolar as falhas do Room e do Firestore e devolve o resultado (sucesso ou erro) para os ViewModels, que colocam a mensagem no `UiState` da tela.

## Arquitetura (MVVM)

```
com.example.campominado
├── data
│   ├── local        AppDatabase, entity/ (JogadorEntity, PartidaEntity), dao/ (JogadorDao, PartidaDao)
│   ├── remote       acesso ao Firestore (coleção "niveis")
│   └── repository   JogoRepository (ponte entre ViewModels, Room e Firestore)
├── logic            regras do jogo em Kotlin puro (sorteio, vizinhos, cascata, vitória, níveis padrão)
└── ui
    ├── navigation   NavHost e Bottom Navigation (diferente para cada perfil)
    ├── screens      telas em Jetpack Compose
    ├── theme        cores e tema claro
    └── viewmodel    SessaoViewModel, JogoViewModel, HistoricoViewModel, AdminViewModel e ViewModelFactory
```

- Cada ViewModel tem o seu próprio `data class UiState`.
- A `ViewModelFactory` injeta o `JogoRepository` nos ViewModels.
- Filtros e ordenação das listas são feitos nos ViewModels com `filter`, `map` e `sortedBy`.
- As listas usam `LazyColumn` e `LazyVerticalGrid`.

## Tecnologias

- Kotlin
- Android e Jetpack Compose
- Material 3
- Navigation Compose
- Room (persistência local)
- Firebase Firestore (configuração remota dos níveis)
- Arquitetura MVVM com Repository

## Como executar

1. Clone este repositório.
2. Abra a pasta do projeto no Android Studio.
3. Aguarde a sincronização do Gradle.
4. Execute o app em um emulador ou dispositivo Android (o arquivo `app/google-services.json` já está no repositório).

Os testes unitários das regras do jogo ficam em `app/src/test` e podem ser executados pelo Android Studio.

## Equipe

- Pedro Konno
- Daniel Sallesse
- Daniel Marciniuk
