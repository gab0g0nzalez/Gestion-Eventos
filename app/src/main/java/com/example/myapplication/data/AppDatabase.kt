package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UsuarioEntity::class,
        EventoEntity::class,
        OrganizacionEntity::class,
        ContactoEntity::class,
        OportunidadEntity::class,
        SeguimientoEntity::class,
        PropuestaEntity::class,
        HistorialEntity::class,
        ResultadoEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase {
            instancia?.let { return it }
            return synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "crm_activaciones.db"
                )
                    // El MVP aún no se publica: al cambiar el esquema se recrean los datos ficticios.
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { instancia = it }
            }
        }
    }
}
