package com.mekki.taco.data.db.dao

import androidx.room.*
import com.mekki.taco.data.db.entity.ItemDieta
import kotlinx.coroutines.flow.Flow
import com.mekki.taco.data.model.DietItemWithFood

@Dao
interface ItemDietaDao {

    /**
     * Insere um novo item (alimento) em uma dieta.
     * @param itemDieta O item da dieta a ser inserido.
     * @return O ID da linha (rowId) do item inserido.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirItemDieta(itemDieta: ItemDieta): Long

    /**
     * Insere uma lista de itens de dieta.
     * @param itensDieta A lista de itens de dieta a serem inseridos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirItensDieta(itensDieta: List<ItemDieta>)

    /**
     * Atualiza um item de dieta existente.
     * @param itemDieta O item da dieta a ser atualizado.
     */
    @Update
    suspend fun atualizarItemDieta(itemDieta: ItemDieta)

    /**
     * Deleta um item de dieta específico.
     * @param itemDieta O item da dieta a ser deletado.
     */
    @Delete
    suspend fun deletarItemDieta(itemDieta: ItemDieta)

    /**
     * Busca todos os itens de uma dieta específica, ordenados talvez pelo tipo de refeição ou ID.
     * @param idDieta O ID da dieta cujos itens devem ser buscados.
     * @return Um Flow contendo a lista de ItemDieta para a dieta especificada.
     */
    @Query("SELECT * FROM itens_dieta WHERE dietaId = :idDieta ORDER BY tipoRefeicao ASC, id ASC")
    fun buscarItensPorDietaId(idDieta: Int): Flow<List<ItemDieta>>

    /**
     * Busca um item de dieta específico pelo seu ID.
     * @param itemId O ID do ItemDieta.
     * @return Um Flow contendo o ItemDieta ou null.
     */
    @Query("SELECT * FROM itens_dieta WHERE id = :itemId")
    fun buscarItemDietaPorId(itemId: Int): Flow<ItemDieta?>


    /**
     * Deleta todos os itens de uma dieta específica.
     * Útil se você quiser limpar uma dieta sem deletar a dieta em si.
     * @param idDieta O ID da dieta cujos itens serão deletados.
     */
    @Query("DELETE FROM itens_dieta WHERE dietaId = :idDieta")
    suspend fun deletarTodosItensDeUmaDieta(idDieta: Int)

    /**
     * Busca todos os itens de uma dieta específica, trazendo também os dados completos
     * do Alimento associado a cada item.
     * @Transaction é crucial para garantir que a query de relação seja executada atomicamente.
     */
    @Transaction
    @Query("SELECT * FROM itens_dieta WHERE dietaId = :idDieta")
    fun buscarItensComAlimentoPorDietaId(idDieta: Int): Flow<List<DietItemWithFood>>

    @Query("UPDATE itens_dieta SET quantidadeGramas = :novaQuantidade WHERE id = :itemId")
    suspend fun atualizarQuantidadeItem(itemId: Int, novaQuantidade: Double)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemDieta>)
}