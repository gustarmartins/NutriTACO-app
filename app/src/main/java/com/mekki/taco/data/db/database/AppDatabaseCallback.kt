package com.mekki.taco.data.db.database

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mekki.taco.utils.normalizeForSearch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean


// Handles database initialization and updates for the TACO dataset.
// Ensures user data (isCustom=1) is preserved while official data (isCustom=0) is updated.

class AppDatabaseCallback(
    private val context: Context,
    private val scope: CoroutineScope
) : RoomDatabase.Callback() {

    companion object {
        private const val TAG = "AppDB_Callback"
        private const val USER_DATA_ID_OFFSET = 100000

        // we want to never overwrite user data
        // this ensures that 'taco_preload.sql' can get updated with new data
        private const val CURRENT_TACO_DATA_VERSION = 1
        private const val PREFS_NAME = "taco_db_prefs"
        private const val KEY_TACO_VERSION = "taco_data_version"
        private const val KEY_IS_POPULATED = "is_db_populated"
    }

    // coroutine-safe synchronization
    private val populationMutex = Mutex()

    // We track if onCreate has been triggered in this session toavoid double-runs
    private val isOnCreateTriggered = AtomicBoolean(false)

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        Log.d(TAG, "onCreate CALLED - Marking for population")
        isOnCreateTriggered.set(true)

        scope.launch(Dispatchers.IO) {
            populationMutex.withLock {
                Log.d(TAG, "onCreate: Acquired lock, starting population")
                showToast("Inicializando banco de dados...")
                populateDatabaseFromSqlFile(context, db)
                setUserIdOffset(db)
                saveCurrentVersion()
                Log.d(TAG, "onCreate: Population complete")
            }
        }
    }

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        scope.launch(Dispatchers.IO) {
            populationMutex.withLock {
                Log.d(TAG, "onOpen: Acquired lock")

                // If onCreate was triggered this session, population is already done
                if (isOnCreateTriggered.getAndSet(false)) {
                    Log.d(
                        TAG,
                        "onOpen:  Skipping - onCreate already handled population this session"
                    )
                    return@withLock
                }

                val count = getFoodsCount(db)

                if (count == 0 && !isDatabasePopulated()) {
                    Log.w(TAG, "onOpen: Table empty and not marked as populated - Populating...")
                    showToast("Banco vazio detectado. Populando...")
                    populateDatabaseFromSqlFile(context, db)
                    setUserIdOffset(db)
                    saveCurrentVersion()
                } else {
                    Log.d(TAG, "onOpen: Database has $count rows, checking for updates...")
                    setDatabasePopulated() // Sync prefs with reality
                    checkAndPerformUpdate(db)
                }
            }
        }
    }

    private fun getFoodsCount(db: SupportSQLiteDatabase): Int {
        val cursor = db.query("SELECT COUNT(*) FROM foods")
        val count = if (cursor.moveToFirst()) cursor.getInt(0) else 0
        cursor.close()
        return count
    }

    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getPrefs(): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun isDatabasePopulated(): Boolean {
        return getPrefs().getBoolean(KEY_IS_POPULATED, false)
    }

    private fun setDatabasePopulated() {
        getPrefs().edit { putBoolean(KEY_IS_POPULATED, true) }
    }

    private fun saveCurrentVersion() {
        getPrefs().edit {
            putInt(KEY_TACO_VERSION, CURRENT_TACO_DATA_VERSION)
                .putBoolean(KEY_IS_POPULATED, true) // Also mark as populated
        }
        Log.d(TAG, "TACO data version saved: $CURRENT_TACO_DATA_VERSION")
    }

    private fun checkAndPerformUpdate(db: SupportSQLiteDatabase) {
        val lastVersion = getPrefs().getInt(KEY_TACO_VERSION, 0)
        Log.d(
            TAG,
            "Checking for updates. Stored version: $lastVersion, Current:  $CURRENT_TACO_DATA_VERSION"
        )

        if (lastVersion < CURRENT_TACO_DATA_VERSION) {
            Log.i(TAG, "Update required.  Starting migration...")
            showToast("Atualizando dados da tabela TACO...")
            updateTacoData(db)
            saveCurrentVersion()
            populateFtsTable(db)
        } else {
            Log.d(TAG, "No update required. Checking FTS consistency...")
            ensureFtsPopulated(db)
        }
    }

    private fun ensureFtsPopulated(db: SupportSQLiteDatabase) {
        val cursor = db.query("SELECT COUNT(*) FROM foods_fts")
        val ftsCount = if (cursor.moveToFirst()) cursor.getInt(0) else 0
        cursor.close()

        if (ftsCount == 0) {
            Log.d(TAG, "FTS table empty, populating...")
            populateFtsTable(db)
        }
    }

    private fun setUserIdOffset(db: SupportSQLiteDatabase) {
        try {
            db.execSQL("INSERT OR IGNORE INTO sqlite_sequence (name, seq) VALUES ('foods', $USER_DATA_ID_OFFSET)")
            db.execSQL("UPDATE sqlite_sequence SET seq = MAX(seq, $USER_DATA_ID_OFFSET) WHERE name = 'foods'")
            Log.d(TAG, "User Data ID offset set to $USER_DATA_ID_OFFSET")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set ID offset", e)
        }
    }

    private fun populateDatabaseFromSqlFile(context: Context, db: SupportSQLiteDatabase) {
        // mutex already held at this point but we double-check
        if (isDatabasePopulated()) {
            Log.d(
                TAG,
                "populateDatabaseFromSqlFile: PREFS indicate DB is already populated. Skipping."
            )
            return
        }

        val count = getFoodsCount(db)
        if (count > 0) {
            Log.d(
                TAG,
                "populateDatabaseFromSqlFile: DB has $count rows. Marking as populated and Skipping."
            )
            setDatabasePopulated()
            return
        }

        Log.d(TAG, "populateDatabaseFromSqlFile: Starting real execution.")
        executeSqlFile(context, db, "taco_preload.sql")
        populateFtsTable(db)
        setDatabasePopulated()
        Log.d(TAG, "populateDatabaseFromSqlFile: Complete.")
    }

    private fun updateTacoData(db: SupportSQLiteDatabase) {
        Log.d(TAG, "updateTacoData STARTED.")
        try {
            db.beginTransaction()

            // Create a Staging Table
            db.execSQL("CREATE TEMPORARY TABLE IF NOT EXISTS foods_staging AS SELECT * FROM foods WHERE 0")

            // Load the SQL file into the Staging Table
            executeSqlFile(context, db, "taco_preload.sql", tableNameReplacement = "foods_staging")

            // Update existing official records (isCustom = 0)
            val columnsToUpdate = listOf(
                "name",
                "category",
                "umidade",
                "energiaKcal",
                "energiaKj",
                "proteina",
                "colesterol",
                "carboidratos",
                "fibraAlimentar",
                "cinzas",
                "calcio",
                "magnesio",
                "manganes",
                "fosforo",
                "ferro",
                "sodio",
                "potassio",
                "cobre",
                "zinco",
                "retinol",
                "RE",
                "RAE",
                "tiamina",
                "riboflavina",
                "piridoxina",
                "niacina",
                "vitaminaC",
                "lipidios_total",
                "lipidios_saturados",
                "lipidios_monoinsaturados",
                "lipidios_poliinsaturados",
                "aminoacidos_triptofano",
                "aminoacidos_treonina",
                "aminoacidos_isoleucina",
                "aminoacidos_leucina",
                "aminoacidos_lisina",
                "aminoacidos_metionina",
                "aminoacidos_cistina",
                "aminoacidos_fenilalanina",
                "aminoacidos_tirosina",
                "aminoacidos_valina",
                "aminoacidos_arginina",
                "aminoacidos_histidina",
                "aminoacidos_alanina",
                "aminoacidos_acidoAspartico",
                "aminoacidos_acidoGlutamico",
                "aminoacidos_glicina",
                "aminoacidos_prolina",
                "aminoacidos_serina"
            )

            val setClause = columnsToUpdate.joinToString(", ") { col ->
                "$col = (SELECT $col FROM foods_staging S WHERE S.tacoID = foods.tacoID)"
            }

            val updateSql = """
                UPDATE foods 
                SET $setClause
                WHERE tacoID IN (SELECT tacoID FROM foods_staging) 
                  AND isCustom = 0
            """.trimIndent()

            db.execSQL(updateSql)
            Log.d(TAG, "Existing official foods updated.")

            // Insert NEW records
            val columnsList = "tacoID, ${columnsToUpdate.joinToString(", ")}"

            val insertNewSql = """
                INSERT INTO foods ($columnsList)
                SELECT $columnsList FROM foods_staging
                WHERE tacoID NOT IN (SELECT tacoID FROM foods)
            """.trimIndent()

            db.execSQL(insertNewSql)
            Log.d(TAG, "New foods inserted.")

            // final Clean up
            db.execSQL("DROP TABLE IF EXISTS foods_staging")

            db.setTransactionSuccessful()
            Log.d(TAG, "TACO Update completed successfully.")
            showToast("Dados nutricionais atualizados com sucesso!")

        } catch (e: Exception) {
            Log.e(TAG, "Error updating TACO data", e)
            showToast("Erro ao atualizar dados: ${e.message}")
        } finally {
            db.endTransaction()
        }
    }

    private fun executeSqlFile(
        context: Context,
        db: SupportSQLiteDatabase,
        fileName: String,
        tableNameReplacement: String? = null
    ) {
        try {
            context.assets.open(fileName).bufferedReader(StandardCharsets.UTF_8).useLines { lines ->
                var statementCount = 0
                val statementBuilder = StringBuilder()
                lines.forEach { line ->
                    val trimmedLine = line.trim()
                    if (trimmedLine.isNotEmpty() && !trimmedLine.startsWith("--")) {
                        statementBuilder.append(trimmedLine)
                        if (!trimmedLine.endsWith(";")) {
                            statementBuilder.append(" ")
                        }
                        if (trimmedLine.endsWith(";")) {
                            var sql = statementBuilder.toString()

                            if (tableNameReplacement != null) {
                                sql = sql.replaceFirst(
                                    "(?i)INSERT INTO foods".toRegex(),
                                    "INSERT INTO $tableNameReplacement"
                                )
                            }

                            if (!sql.equals("BEGIN TRANSACTION;", ignoreCase = true) &&
                                !sql.equals("COMMIT;", ignoreCase = true)
                            ) {
                                try {
                                    db.execSQL(sql)
                                    statementCount++
                                } catch (e: Exception) {
                                    Log.e(TAG, "Erro ao executar SQL: '$sql' - ${e.message}", e)
                                }
                            }
                            statementBuilder.clear()
                        }
                    }
                }
                Log.d(TAG, "Executed $statementCount statements from $fileName")
            }
        } catch (e: IOException) {
            Log.e(TAG, "ERRO ao abrir ou ler o arquivo $fileName: ${e.message}", e)
            throw e
        }
    }

    private fun populateFtsTable(db: SupportSQLiteDatabase) {
        Log.e(TAG, "!!! STARTING FTS INDEXING !!!")

        db.beginTransaction()
        var count = 0
        try {
            db.execSQL("DELETE FROM foods_fts")

            val cursor = db.query("SELECT id, name FROM foods")
            val insertStmt =
                db.compileStatement("INSERT INTO foods_fts (rowid, normalized_data) VALUES (?, ?)")

            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val name = cursor.getString(1)

                val normalized = name.normalizeForSearch()

                insertStmt.bindLong(1, id)
                insertStmt.bindString(2, normalized)
                insertStmt.executeInsert()
                insertStmt.clearBindings()
                count++
            }
            cursor.close()
            db.setTransactionSuccessful()
            Log.e(TAG, "!!! FTS INDEXING COMPLETE: $count items !!!")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao popular FTS: ${e.message}", e)
        } finally {
            db.endTransaction()
        }
    }
}