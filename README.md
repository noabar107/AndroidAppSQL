# MyApplication – SQLite via ADB

This app exposes a simple way to run arbitrary SQLite queries on the device from your desktop shell using `adb`.

It uses Android's built-in SQLite 3 (`SQLiteOpenHelper` / `SQLiteDatabase`) and a broadcast receiver that accepts SQL strings.

---

## 1. Build and install

From the project root:

```powershell
.\gradlew assembleDebug
```

After a successful build, install the debug APK:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

> If `adb` is not in your `PATH`, use the full path to `adb.exe`.

---

## 2. Run a non-SELECT query (INSERT/UPDATE/DELETE/DDL)

Example: insert a row into the default `test` table:

```powershell
adb shell "am broadcast -a com.example.myapplication.RUN_SQL -n com.example.myapplication/.SqlBroadcastReceiver --es sql \"INSERT INTO test(value) VALUES('hello from adb');\""
```

What happens:

- The app executes the SQL.
- On success, it stores the text `OK` in `files/last_query_result.txt` and logs it to logcat.

Check the stored result:

```powershell
adb shell run-as com.example.myapplication cat files/last_query_result.txt
```

If `run-as` fails, and you have root, you can instead run:

```powershell
adb shell su -c 'cat /data/data/com.example.myapplication/files/last_query_result.txt'
```

---

## 3. Run a SELECT query and see rows

Example: select all rows from `test`:

```powershell
adb shell "am broadcast -a com.example.myapplication.RUN_SQL -n com.example.myapplication/.SqlBroadcastReceiver --es sql \"SELECT * FROM test;\""
```

Then read the result:

```powershell
adb shell run-as com.example.myapplication cat files/last_query_result.txt
```

You should see output like:

```text
id    value
1     hello from adb
```

The same text is also logged under the tag `SqlBroadcastReceiver`:

```powershell
adb logcat | findstr SqlBroadcastReceiver
```

---

## 4. General usage notes

- Any SQL you pass via the `sql` extra will be executed against the app's `app.db` database.
- Queries starting with `SELECT` or `PRAGMA` are treated as read-queries and their result rows are formatted into a tab-separated text table.
- All other statements (`INSERT`, `UPDATE`, `DELETE`, `CREATE TABLE`, etc.) are executed with `execSQL` and return `OK` on success.
- Errors are caught and written as `ERROR: <type>: <message>` into `last_query_result.txt` and logcat.

