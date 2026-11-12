package com.example.campominado.data.repository

import com.example.campominado.data.local.dao.JogadorDao
import com.example.campominado.data.local.dao.PartidaDao
import com.example.campominado.data.local.entity.JogadorEntity
import com.example.campominado.data.local.entity.PartidaEntity
import com.example.campominado.data.remote.NivelRemoto
import com.example.campominado.data.remote.NiveisRemoteDataSource
import com.example.campominado.logic.NiveisPadrao
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout

/**
 * Única ponte entre os ViewModels e os dados. Os ViewModels NUNCA falam com
 * o Room (DAOs) diretamente: pedem tudo para cá.
 *
 * Regra de falhas (exigida pelo professor): toda chamada ao banco fica dentro
 * de try-catch. Em vez de estourar uma exceção, devolvemos Resultado.Erro com
 * uma mensagem, e o ViewModel põe essa mensagem no UiState.
 *
 * Room guarda jogadores e partidas (no aparelho). Firestore guarda a configuração dos níveis (na nuvem).
 */
class JogoRepository(
    private val jogadorDao: JogadorDao,
    private val partidaDao: PartidaDao,
    private val niveisRemoto: NiveisRemoteDataSource
) {

    companion object {
        const val NIVEL_MAXIMO = 15

        // Quanto tempo esperamos o Firestore antes de desistir e avisar o erro.
        // Sem esse limite, sem internet (ou com o Firestore desligado) o app ficava "travado" esperando.
        const val TEMPO_LIMITE_MS = 8_000L
    }

    // ---------------------------------------------------------------
    // NÍVEIS (Firestore, com valores padrão como reserva)
    // ---------------------------------------------------------------

    /**
     * Os 15 níveis SEMPRE chegam completos. Se o Firestore falhar (sem internet,
     * Firebase não configurado, demorou demais), usamos os valores padrão e preenchemos "erro",
     * que o ViewModel mostra na tela. A lista nunca vem vazia.
     */
    data class NiveisCarregados(val niveis: List<NivelRemoto>, val erro: String? = null)

    private val niveisPadrao: List<NivelRemoto>
        get() = NiveisPadrao.todos.map { NivelRemoto(it.nivel, it.tamanhoMapa, it.galhos) }

    // Última lista que veio do servidor, guardada em memória.
    // Assim, começar uma partida não precisa esperar o Firestore de novo.
    private var niveisEmCache: List<NivelRemoto>? = null

    /**
     * Busca os níveis no Firestore.
     * criarSeVazio = true (só o Admin usa): se a coleção ainda estiver vazia, grava os 15 níveis padrão.
     */
    suspend fun carregarNiveis(criarSeVazio: Boolean = false): NiveisCarregados =
        try {
            withTimeout(TEMPO_LIMITE_MS) {
                var remotos = niveisRemoto.buscarNiveis()
                if (remotos.isEmpty() && criarSeVazio) {
                    niveisRemoto.criarNiveisPadrao()
                    remotos = niveisPadrao
                }
                val niveis = juntarComPadrao(remotos)
                niveisEmCache = niveis
                NiveisCarregados(niveis)
            }
        } catch (e: TimeoutCancellationException) {
            NiveisCarregados(niveisEmCache ?: niveisPadrao, "O servidor demorou para responder. Usando os níveis padrão.")
        } catch (e: Exception) {
            NiveisCarregados(niveisEmCache ?: niveisPadrao, "Sem conexão com o servidor. Usando os níveis padrão.")
        }

    /** Para começar uma partida: usa a lista em memória, se houver; senão, busca no Firestore. */
    suspend fun niveisParaJogar(): NiveisCarregados =
        niveisEmCache?.let { NiveisCarregados(it) } ?: carregarNiveis()

    // Para cada um dos 15 níveis, vale o do Firestore; se faltar, vale o padrão.
    // O tamanho do mapa é fixo, então sempre vem do padrão.
    private fun juntarComPadrao(remotos: List<NivelRemoto>): List<NivelRemoto> {
        val porNivel = remotos.associateBy { it.nivel }
        return niveisPadrao.map { p ->
            val r = porNivel[p.nivel]
            if (r == null) p else p.copy(galhos = r.galhos, ativo = r.ativo)
        }
    }

    // Admin: salva um nível. Valida antes: de 1 galho até 25% das casas do mapa.
    suspend fun salvarNivel(nivel: NivelRemoto): Resultado<Unit> {
        val padrao = NiveisPadrao.buscar(nivel.nivel)
            ?: return Resultado.Erro("Nível inválido.")
        val maximo = NiveisPadrao.maximoDeGalhos(padrao.tamanhoMapa)
        if (nivel.galhos !in 1..maximo) {
            return Resultado.Erro("Use de 1 a $maximo galhos neste nível.")
        }
        // Força o tamanho padrão: o tamanho do mapa de cada nível não muda.
        val corrigido = nivel.copy(tamanhoMapa = padrao.tamanhoMapa)
        return try {
            withTimeout(TEMPO_LIMITE_MS) { niveisRemoto.salvarNivel(corrigido) }
            // Atualiza também a lista em memória, para a próxima partida já usar o valor novo.
            niveisEmCache = niveisEmCache?.map { if (it.nivel == corrigido.nivel) corrigido else it }
            Resultado.Sucesso(Unit)
        } catch (e: TimeoutCancellationException) {
            Resultado.Erro("O servidor demorou para responder. O nível não foi salvo.")
        } catch (e: Exception) {
            Resultado.Erro("Não foi possível salvar o nível. Verifique a conexão.")
        }
    }

    // ---------------------------------------------------------------
    // JOGADORES (CRUD completo)
    // ---------------------------------------------------------------

    // READ reativo. O Flow emite a lista de novo sempre que a tabela muda.
    // O .catch pega falhas durante a leitura e as transforma em Resultado.Erro.
    fun listarJogadores(): Flow<Resultado<List<JogadorEntity>>> =
        jogadorDao.listarTodos()
            .map<List<JogadorEntity>, Resultado<List<JogadorEntity>>> { Resultado.Sucesso(it) }
            .catch { emit(Resultado.Erro("Não foi possível carregar os jogadores.")) }

    fun observarJogador(id: Long): Flow<Resultado<JogadorEntity?>> =
        jogadorDao.observarPorId(id)
            .map<JogadorEntity?, Resultado<JogadorEntity?>> { Resultado.Sucesso(it) }
            .catch { emit(Resultado.Erro("Não foi possível carregar o jogador.")) }

    // CREATE: usado no Login. Se o nome já existe, reaproveita o jogador salvo
    // (assim "entrar" e "cadastrar" são o mesmo gesto). Devolve o id do jogador.
    suspend fun entrarOuCadastrar(nome: String): Resultado<Long> {
        val nomeLimpo = nome.trim()
        if (nomeLimpo.isEmpty()) return Resultado.Erro("Digite um nome para jogar.")
        return try {
            val existente = jogadorDao.buscarPorNome(nomeLimpo)
            Resultado.Sucesso(existente?.id ?: jogadorDao.inserir(JogadorEntity(nome = nomeLimpo)))
        } catch (e: Exception) {
            Resultado.Erro("Não foi possível entrar com esse nome.")
        }
    }

    // UPDATE: renomear. Impede nome vazio ou nome que já é de outro jogador.
    suspend fun renomearJogador(jogador: JogadorEntity, novoNome: String): Resultado<Unit> {
        val nomeLimpo = novoNome.trim()
        if (nomeLimpo.isEmpty()) return Resultado.Erro("O nome não pode ficar vazio.")
        return try {
            val outro = jogadorDao.buscarPorNome(nomeLimpo)
            if (outro != null && outro.id != jogador.id) {
                Resultado.Erro("Já existe um jogador com esse nome.")
            } else {
                jogadorDao.atualizar(jogador.copy(nome = nomeLimpo))
                Resultado.Sucesso(Unit)
            }
        } catch (e: Exception) {
            Resultado.Erro("Não foi possível renomear o jogador.")
        }
    }

    // DELETE: as partidas do jogador somem junto (CASCADE definido na PartidaEntity).
    suspend fun excluirJogador(jogador: JogadorEntity): Resultado<Unit> =
        try {
            jogadorDao.excluir(jogador)
            Resultado.Sucesso(Unit)
        } catch (e: Exception) {
            Resultado.Erro("Não foi possível excluir o perfil.")
        }

    // ---------------------------------------------------------------
    // PARTIDAS
    // ---------------------------------------------------------------

    // Histórico de um jogador (mais recentes primeiro).
    fun listarPartidasDoJogador(jogadorId: Long): Flow<Resultado<List<PartidaEntity>>> =
        partidaDao.listarPorJogador(jogadorId)
            .map<List<PartidaEntity>, Resultado<List<PartidaEntity>>> { Resultado.Sucesso(it) }
            .catch { emit(Resultado.Erro("Não foi possível carregar o histórico.")) }

    // Todas as vitórias de todos os jogadores (base dos Recordes).
    // Filtrar por tamanho de mapa e ordenar por tempo é trabalho do ViewModel.
    fun listarVitorias(): Flow<Resultado<List<PartidaEntity>>> =
        partidaDao.listarVitorias()
            .map<List<PartidaEntity>, Resultado<List<PartidaEntity>>> { Resultado.Sucesso(it) }
            .catch { emit(Resultado.Erro("Não foi possível carregar os recordes.")) }

    // Salva o fim de uma partida. Se o jogador VENCEU o seu nível mais alto
    // liberado, desbloqueia o próximo (até o 15).
    suspend fun registrarPartida(partida: PartidaEntity): Resultado<Unit> =
        try {
            partidaDao.inserir(partida)
            if (partida.venceu) desbloquearProximoNivel(partida.jogadorId, partida.nivel)
            Resultado.Sucesso(Unit)
        } catch (e: Exception) {
            Resultado.Erro("Não foi possível salvar a partida.")
        }

    // Excluir uma partida (Histórico do jogador ou Recordes, pelo Admin).
    suspend fun excluirPartida(partida: PartidaEntity): Resultado<Unit> =
        try {
            partidaDao.excluir(partida)
            Resultado.Sucesso(Unit)
        } catch (e: Exception) {
            Resultado.Erro("Não foi possível excluir a partida.")
        }

    // Só desbloqueia se o nível vencido for justamente o último liberado.
    // Vencer de novo um nível antigo não muda nada.
    // Sem try-catch aqui: quem chama (registrarPartida) já captura.
    private suspend fun desbloquearProximoNivel(jogadorId: Long, nivelVencido: Int) {
        val jogador = jogadorDao.observarPorId(jogadorId).first() ?: return
        if (nivelVencido == jogador.nivelMaximoDesbloqueado && nivelVencido < NIVEL_MAXIMO) {
            jogadorDao.atualizar(jogador.copy(nivelMaximoDesbloqueado = nivelVencido + 1))
        }
    }
}
