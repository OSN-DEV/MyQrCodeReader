package tenpokei.java_conf.gr.jp.myqrcodereader.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.lang.StringBuilder

/**
 * Database utility
 */
class AppDatabase(context: Context) : SQLiteOpenHelper(context, "app_data.db", null, 1) {

    //==============================================================================================
    // SQLiteOpenHelper
    //==============================================================================================
    override fun onCreate(database: SQLiteDatabase) {
        val sql = StringBuilder()
        sql.append("CREATE TABLE read_histories(")
        sql.append(" id INTEGER PRIMARY KEY AUTOINCREMENT")
        sql.append(",site_name TEXT")
        sql.append(",display_value TEXT")
        sql.append(",read_date INTEGER")
        database.execSQL(sql.toString())
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // NOP
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // NOP
    }


    //==============================================================================================
    // Public Method
    //==============================================================================================
    /**
     * insert data and delete old data if numbers of record is over 30.
     */
    fun createHistory(siteName: String, displayValue: String) {
        val database = writableDatabase
        try {
            database.beginTransaction()

            val sql = StringBuilder()
            sql.append("INSERT INTO read_histories(")
            sql.append(" site_name")
            sql.append(",display_value")
            sql.append(",read_date")
            sql.append(") values (?,?,?)")
            database.execSQL(sql.toString(), arrayOf(siteName, displayValue, System.currentTimeMillis()))

            sql.setLength(0)
            sql.append("DELETE FROM read_histories ")
            sql.append("WHERE id not in (")
            sql.append("  SELECT id FROM read_histories")
            sql.append("  ORDER BY read_date DESC LIMIT 30)")
            database.execSQL(sql.toString())

            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }
}
