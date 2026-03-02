package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.util.Log
import java.io.File

class SqlBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val sql = intent.getStringExtra(EXTRA_SQL)?.trim()

        if (sql.isNullOrEmpty()) {
            logAndStoreResult(context, "No SQL provided.")
            return
        }

        val dbHelper = AppDatabaseHelper(context.applicationContext)
        val db = dbHelper.writableDatabase

        val result = try {
            if (isSelectQuery(sql)) {
                runSelect(db.rawQuery(sql, null))
            } else {
                db.execSQL(sql)
                "OK"
            }
        } catch (t: Throwable) {
            "ERROR: ${t::class.java.simpleName}: ${t.message}"
        }

        logAndStoreResult(context, result)
    }

    private fun isSelectQuery(sql: String): Boolean {
        val trimmed = sql.trim().lowercase()
        return trimmed.startsWith("select") || trimmed.startsWith("pragma")
    }

    private fun runSelect(cursor: Cursor): String {
        cursor.use { c ->
            if (c.columnCount == 0) {
                return "No columns."
            }

            val header = buildString {
                for (i in 0 until c.columnCount) {
                    if (i > 0) append('\t')
                    append(c.getColumnName(i))
                }
            }

            val rows = mutableListOf<String>()
            rows.add(header)

            while (c.moveToNext()) {
                val row = buildString {
                    for (i in 0 until c.columnCount) {
                        if (i > 0) append('\t')
                        append(c.getString(i) ?: "NULL")
                    }
                }
                rows.add(row)
            }

            return if (rows.size == 1) {
                "$header\n(0 rows)"
            } else {
                rows.joinToString(separator = "\n")
            }
        }
    }

    private fun logAndStoreResult(context: Context, result: String) {
        Log.d(TAG, "SQL result:\n$result")

        runCatching {
            val file = File(context.filesDir, RESULT_FILE_NAME)
            file.writeText(result)
        }
    }

    companion object {
        private const val TAG = "SqlBroadcastReceiver"
        const val EXTRA_SQL = "sql"
        const val RESULT_FILE_NAME = "last_query_result.txt"
    }
}

