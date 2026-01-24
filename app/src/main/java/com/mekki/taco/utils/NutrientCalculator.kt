package com.mekki.taco.utils

import com.mekki.taco.data.db.entity.Aminoacidos
import com.mekki.taco.data.db.entity.Food
import com.mekki.taco.data.db.entity.Lipidios

data class NutrientesPorPorcao(
    val nomeOriginal: String, // Nome do alimento base
    val quantidadeGramas: Double, // Quantidade para a qual os nutrientes foram calculados
    val umidade: Double?, // medido em %
    val energiaKcal: Double?,
    val energiaKj: Double?,
    val proteina: Double?,
    val carboidratos: Double?,
    val lipidios: Lipidios?, // Objeto Lipidios com valores calculados
    val fibraAlimentar: Double?,
    val colesterol: Double?, // mg
    val cinzas: Double?, // g
    val calcio: Double?, // mg
    val magnesio: Double?, // mg
    val manganes: Double?, // mg
    val fosforo: Double?, // mg
    val ferro: Double?, // mg
    val sodio: Double?, // mg
    val potassio: Double?, // mg
    val cobre: Double?, // mg
    val zinco: Double?, // mg
    val retinol: Double?, // µg
    val RE: Double?, // µg
    val RAE: Double?, // µg
    val tiamina: Double?, // mg
    val riboflavina: Double?, // mg
    val piridoxina: Double?, // mg
    val niacina: Double?, // mg
    val vitaminaC: Double?, // mg
    val aminoacidos: Aminoacidos? // Objeto Aminoacidos com valores calculados
)

object NutrientCalculator {

    /**
     * Calcula os valores nutricionais para uma quantidade específica de um alimento.
     * Assume que os valores no objeto Alimento são por 100g.
     */
    fun calcularNutrientesParaPorcao(
        foodBase: Food, // O alimento original com valores por 100g
        quantidadeDesejadaGramas: Double
    ): NutrientesPorPorcao {
        val fator = quantidadeDesejadaGramas / 100.0

        fun calcular(valorPor100g: Double?): Double? {
            return valorPor100g?.let { it * fator }
        }

        val lipidiosCalculados = foodBase.lipidios?.let { lip ->
            Lipidios(
                total = calcular(lip.total),
                saturados = calcular(lip.saturados),
                monoinsaturados = calcular(lip.monoinsaturados),
                poliinsaturados = calcular(lip.poliinsaturados)
            )
        }

        val aminoacidosCalculados = foodBase.aminoacidos?.let { aa ->
            Aminoacidos(
                triptofano = calcular(aa.triptofano), treonina = calcular(aa.treonina),
                isoleucina = calcular(aa.isoleucina), leucina = calcular(aa.leucina),
                lisina = calcular(aa.lisina), metionina = calcular(aa.metionina),
                cistina = calcular(aa.cistina), fenilalanina = calcular(aa.fenilalanina),
                tirosina = calcular(aa.tirosina), valina = calcular(aa.valina),
                arginina = calcular(aa.arginina), histidina = calcular(aa.histidina),
                alanina = calcular(aa.alanina), acidoAspartico = calcular(aa.acidoAspartico),
                acidoGlutamico = calcular(aa.acidoGlutamico), glicina = calcular(aa.glicina),
                prolina = calcular(aa.prolina), serina = calcular(aa.serina)
            )
        }

        return NutrientesPorPorcao(
            nomeOriginal = foodBase.name,
            quantidadeGramas = quantidadeDesejadaGramas,
            umidade = foodBase.umidade,
            energiaKcal = calcular(foodBase.energiaKcal),
            energiaKj = calcular(foodBase.energiaKj),
            proteina = calcular(foodBase.proteina),
            carboidratos = calcular(foodBase.carboidratos),
            lipidios = lipidiosCalculados,
            fibraAlimentar = calcular(foodBase.fibraAlimentar),
            colesterol = calcular(foodBase.colesterol),
            cinzas = calcular(foodBase.cinzas),
            calcio = calcular(foodBase.calcio),
            magnesio = calcular(foodBase.magnesio),
            manganes = calcular(foodBase.manganes),
            fosforo = calcular(foodBase.fosforo),
            ferro = calcular(foodBase.ferro),
            sodio = calcular(foodBase.sodio),
            potassio = calcular(foodBase.potassio),
            cobre = calcular(foodBase.cobre),
            zinco = calcular(foodBase.zinco),
            retinol = calcular(foodBase.retinol),
            RE = calcular(foodBase.RE),
            RAE = calcular(foodBase.RAE),
            tiamina = calcular(foodBase.tiamina),
            riboflavina = calcular(foodBase.riboflavina),
            piridoxina = calcular(foodBase.piridoxina),
            niacina = calcular(foodBase.niacina),
            vitaminaC = calcular(foodBase.vitaminaC),
            aminoacidos = aminoacidosCalculados
        )
    }
}