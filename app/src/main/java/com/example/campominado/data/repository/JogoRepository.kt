package com.example.campominado.data.repository

import com.example.campominado.data.local.dao.JogadorDao
import com.example.campominado.data.local.dao.PartidaDao
import com.example.campominado.data.local.entity.JogadorEntity
import com.example.campominado.data.local.entity.PartidaEntity
import com.example.campominado.data.remote.NivelRemoto
import com.example.campominado.data.remote.NiveisRemoteDataSource
import com.example.campominado.logic.NiveisPadrao
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout

class JogoRepository(
    private val jogadorDao: JogadorDao,
    private val partidaDao: PartidaDao,
    private val niveisRemoto: NiveisRemoteDataSource
) {

    companion object {
        const val NIVEL_MAXIMO = 15

        const val TEMPO_LIMITE_MS = 8_000L

        private const val TAG = "JogoRepository"
    }

    private fun motivoDoErro(e: Exception): String {
        Log.e(TAG, "Falha no Firestore", e)
        return when ((e as? FirebaseFirestoreException)?.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                "O Firestore recusou o acesso (verifique as regras e se o banco foi criado)."
            FirebaseFirestoreException.Code.NOT_FOUND ->
                "O banco Firestore ainda não foi criado no projeto."
            FirebaseFirestoreException.Code.UNAVAILABLE ->
                "Sem conexão com o servidor."
            else -> "Falha ao falar com o servidor."
        }
    }

    data class NiveisCarregados(val niveis: List<NivelRemoto>, val erro: String? = null)

    private val niveisPadrao: List<NivelRemoto>
        get() = NiveisPadrao.todos.map { NivelRemoto(it.nivel, it.tamanhoMapa, it.galhos) }

    private var niveisEmCache: List<NivelRemoto>? = null

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
            NiveisCarregados(niveisEmCache ?: niveisPadrao, "${motivoDoErro(e)} Usando os níveis padrão.")
        }

    suspend fun niveisParaJogar(): NiveisCarregados =
        niveisEmCache?.let { NiveisCarregados(it) } ?: carregarNiveis()

    private fun juntarComPadrao(remotos: List<NivelRemoto>): List<NivelRemoto> {
        val porNivel = remotos.associateBy { it.nivel }
        return niveisPadrao.map { p ->
            val r = porNivel[p.nivel]
            if (r == null) p else p.copy(galhos = r.galhos, ativo = r.ativo)
        }
    }

    suspend fun salvarNivel(nivel: NivelRemoto): Resultado<Unit> {
        val padrao = NiveisPadrao.buscar(nivel.nivel)
            ?: return Resultado.Erro("Nível inválido.")
        val maximo = NiveisPadrao.maximoDeGalhos(padrao.tamanhoMapa)
        if (nivel.galhos !in 1..maximo) {
            return Resultado.Erro("Use de 1 a $maximo galhos neste nível.")
        }

        val corrigido = nivel.copy(tamanhoMapa = padrao.tamanhoMapa)
        return try {
            withTimeout(TEMPO_LIMITE_MS) { niveisRemoto.salvarNivel(corrigido) }

            niveisEmCache = niveisEmCache?.map { if (it.nivel == corrigido.nivel) corrigido else it }
            Resultado.Sucesso(Unit)
        } catch (e: TimeoutCancellationException) {
            Resultado.Erro("O servidor demorou para responder. O nível não foi salvo.")
        } catch (e: Exception) {
            Resultado.Erro("Não foi possível salvar o nível. ${motivoDoErro(e)}")
        }
    }

    fun listarJogadores(): Flow<Resultado<List<JogadorEntity>>> =
        jogadorDao.listarTodos()
            .map<List<JogadorEntity>, Resultado<List<JogadorEntity>>> { Resultado.Sucesso(it) }
            .catch { emit(Resultado.Erro("Não foi possível carregar os jogadores.")) }

    fun observarJogador(id: Long): Flow<Resultado<JogadorEntity?>> =
        jogadorDao.observarPorId(id)
            .map<JogadorEntity?, Resultado<JogadorEntity?>> { Resultado.Sucesso(it) }
            .catch { emit(Resultado.Erro("Não foi possível carregar o jogador.")) }

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

    suspend fun excluirJogador(jogador: JogadorEntity): Resultado<Unit> =
        try {
            jogadorDao.excluir(jogador)
            Resultado.Sucesso(Unit)
        } catch (e: Exception) {
            Resultado.Erro("Não foi possível excluir o perfil.")
        }

    fun listarPartidasDoJogador(jogadorId: Long): Flow<Resultado<List<PartidaEntity>>> =
        partidaDao.listarPorJogador(jogadorId)
            .map<List<PartidaEntity>, Resultado<List<PartidaEntity>>> { Resultado.Sucesso(it) }
            .catch { emit(Resultado.Erro("Não foi possível carregar o histórico.")) }

    fun listarVitorias(): Flow<Resultado<List<PartidaEntity>>> =
        partidaDao.listarVitorias()
            .map<List<PartidaEntity>, Resultado<List<PartidaEntity>>> { Resultado.Sucesso(it) }
            .catch { emit(Resultado.Erro("Não foi possível carregar os recordes.")) }

    suspend fun registrarPartida(partida: PartidaEntity): Resultado<Unit> =
        try {
            partidaDao.inserir(partida)
            if (partida.venceu) desbloquearProximoNivel(partida.jogadorId, partida.nivel)
            Resultado.Sucesso(Unit)
        } catch (e: Exception) {
            Resultado.Erro("Não foi possível salvar a partida.")
        }

    suspend fun atualizarPartida(partida: PartidaEntity): Resultado<Unit> =
        try {
            partidaDao.atualizar(partida)
            Resultado.Sucesso(Unit)
        } catch (e: Exception) {
            Resultado.Erro("Não foi possível atualizar a partida.")
        }

    suspend fun excluirPartida(partida: PartidaEntity): Resultado<Unit> =
        try {
            partidaDao.excluir(partida)
            Resultado.Sucesso(Unit)
        } catch (e: Exception) {
            Resultado.Erro("Não foi possível excluir a partida.")
        }

    private suspend fun desbloquearProximoNivel(jogadorId: Long, nivelVencido: Int) {
        val jogador = jogadorDao.observarPorId(jogadorId).first() ?: return
        if (nivelVencido == jogador.nivelMaximoDesbloqueado && nivelVencido < NIVEL_MAXIMO) {
            jogadorDao.atualizar(jogador.copy(nivelMaximoDesbloqueado = nivelVencido + 1))
        }
    }
}
