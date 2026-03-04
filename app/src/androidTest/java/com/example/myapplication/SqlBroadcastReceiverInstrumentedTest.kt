package com.example.myapplication

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SqlBroadcastReceiverInstrumentedTest {

    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun clearState() {
        context.deleteDatabase("app.db")
        context.deleteFile(SqlBroadcastReceiver.RESULT_FILE_NAME)
    }

    @Test
    fun insertAndSelectViaReceiver_writesExpectedResult() {
        val receiver = SqlBroadcastReceiver()

        // Run an INSERT statement
        val insertIntent = Intent(context, SqlBroadcastReceiver::class.java).apply {
            action = "com.example.myapplication.RUN_SQL"
            putExtra(SqlBroadcastReceiver.EXTRA_SQL, "INSERT INTO test(value) VALUES('from test');")
        }
        receiver.onReceive(context, insertIntent)

        val insertResult = readResultFile().trim()
        assertEquals("OK", insertResult)

        // Run a SELECT statement to verify row content
        val selectIntent = Intent(context, SqlBroadcastReceiver::class.java).apply {
            action = "com.example.myapplication.RUN_SQL"
            putExtra(SqlBroadcastReceiver.EXTRA_SQL, "SELECT * FROM test;")
        }
        receiver.onReceive(context, selectIntent)

        val selectResult = readResultFile()
        val lines = selectResult.lines().filter { it.isNotBlank() }

        assertTrue("Expected at least header and one data row", lines.size >= 2)
        val header = lines.first()
        assertTrue(header.contains("id"))
        assertTrue(header.contains("value"))
        assertTrue(lines.any { it.contains("from test") })
    }

    private fun readResultFile(): String {
        return context.openFileInput(SqlBroadcastReceiver.RESULT_FILE_NAME)
            .bufferedReader()
            .use { it.readText() }
    }
}

