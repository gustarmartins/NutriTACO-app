package com.mekki.taco.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mekki.taco.data.db.entity.Food
import kotlinx.coroutines.flow.Flow

@Dao
interface AlimentoDao {

    /**
     * Insere um único alimento no banco de dados.
     * Se um alimento com o mesmo 'codigoOriginal' já existir, ele será substituído.
     * @param food O alimento a ser inserido.
     * @return O ID da linha (rowId) do alimento inserido.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirAlimento(food: Food): Long

    /**
     * Insere uma lista de alimentos no banco de dados.
     * Se algum alimento com o mesmo 'codigoOriginal' já existir, ele será substituído.
     * Útil para popular o banco de dados inicialmente a partir dos CSVs.
     * @param foods A lista de alimentos a ser inserida.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirAlimentos(foods: List<Food>)

    /**
     * Atualiza um alimento existente no banco de dados.
     * A correspondência é feita pela chave primária do alimento.
     * @param food O alimento a ser atualizado.
     * @return O número de linhas atualizadas (deve ser 1 se o alimento existir).
     */
    @Update
    suspend fun atualizarAlimento(food: Food): Int

    /**
     * Deleta um único alimento do banco de dados.
     * A correspondência é feita pela chave primária do alimento.
     * @param food O alimento a ser deletado.
     */
    @Delete
    suspend fun deletarAlimento(food: Food)

    /**
     * Deleta todos os alimentos da tabela.
     */
    @Query("DELETE FROM alimento")
    suspend fun deletarTodosAlimentos()

    /**
     * Busca um alimento pelo seu ID interno do Room.
     * @param id O ID interno do alimento.
     * @return Um Flow contendo o Alimento ou null se não encontrado.
     */
    @Query("SELECT * FROM alimento WHERE id = :id")
    fun buscarAlimentoPorId(id: Int): Flow<Food?>

    /**
     * Busca um alimento pelo seu código original da Tabela TACO.
     * @param codigoOriginal O código original (IdAlimento do CSV).
     * @return Um Flow contendo o Alimento ou null se não encontrado.
     */
    @Query("SELECT * FROM alimento WHERE codigoOriginal = :codigoOriginal")
    fun buscarAlimentoPorCodigoOriginal(codigoOriginal: String): Flow<Food?>

    /**
     * Busca todos os alimentos cadastrados, ordenados pelo nome.
     * @return Um Flow contendo a lista de todos os alimentos.
     */
    @Query("SELECT * FROM alimento ORDER BY nome ASC")
    fun buscarTodosAlimentos(): Flow<List<Food>>

    /**
     * Busca alimentos cujo nome contenha o termo de pesquisa (ignorando maiúsculas/minúsculas).
     * @param termoBusca O texto a ser procurado no nome dos alimentos.
     * @return Um Flow contendo a lista de alimentos correspondentes.
     */
    @Query("SELECT * FROM alimento WHERE LOWER(nome) LIKE LOWER(:termoBusca) || '%' ORDER BY nome ASC")
    fun buscarAlimentosPorNome(termoBusca: String): Flow<List<Food>>

    /**
     * Busca todos os alimentos de uma categoria específica, ordenados pelo nome.
     * @param categoria A categoria para filtrar os alimentos.
     * @return Um Flow contendo a lista de alimentos da categoria especificada.
     */
    @Query("SELECT * FROM alimento WHERE categoria = :categoria ORDER BY nome ASC")
    fun buscarAlimentosPorCategoria(categoria: String): Flow<List<Food>>

    /**
     * Retorna uma lista de todos os nomes de categorias distintas.
     * @return Um Flow contendo a lista de strings das categorias únicas.
     */
    @Query("SELECT DISTINCT categoria FROM alimento ORDER BY categoria ASC")
    fun buscarTodasCategorias(): Flow<List<String>>

    /**
     * Conta o número total de alimentos na tabela.
     * @return O número total de alimentos.
     */
    @Query("SELECT COUNT(*) FROM alimento")
    suspend fun contarAlimentos(): Int
}