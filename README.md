# MySqlApp – SQLite via ADB

This app exposes a simple way to run arbitrary SQLite queries on the device from your desktop shell using `adb`.

It uses Android's built-in SQLite 3 (`SQLiteOpenHelper` / `SQLiteDatabase`) and a broadcast receiver that accepts SQL strings.

---

## 1. Prerequisites (Linux)

- **Java JDK 11+** installed and on your `PATH`:

```bash
java -version
```

- **Android SDK** installed (e.g. under `~/Android/Sdk` or `/opt/android-sdk`).
- `sdk.dir` correctly set in `local.properties` (see below).
- `adb` available on your `PATH` (usually from `platform-tools` in the SDK).

If you cloned this project from Windows, make sure the Gradle wrapper script has Unix line endings:

```bash
cd /path/to/MyApplication
dos2unix gradlew  # if needed
chmod +x gradlew
```

### Configure `local.properties` for Linux

In the project root (`MySqlApp/local.properties`), set your SDK path, for example:

```properties
sdk.dir=/home/youruser/Android/Sdk
```

Adjust the path to match where your SDK is actually installed.

---

## 2. Build and install (Linux)

From the project root:

```bash
cd /path/to/MySqlApp
./gradlew assembleDebug
```

After a successful build, install the debug APK:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 3. Run a non-SELECT query (INSERT/UPDATE/DELETE/DDL)

Example: insert a row into the default `test` table:

```bash
adb shell "am broadcast -a com.example.myapplication.RUN_SQL -n com.example.myapplication/.SqlBroadcastReceiver --es sql \"INSERT INTO test(value) VALUES('hello from adb');\""
```

What happens:

- The app executes the SQL.
- On success, it stores the text `OK` in `files/last_query_result.txt` and logs it to logcat.

Check the stored result:

```bash
adb shell run-as com.example.myapplication cat files/last_query_result.txt
```

If `run-as` fails, and you have root, you can instead run:

```bash
adb shell su -c 'cat /data/data/com.example.myapplication/files/last_query_result.txt'
```

---

## 4. Run a SELECT query and see rows

Example: select all rows from `test`:

```bash
adb shell "am broadcast -a com.example.myapplication.RUN_SQL -n com.example.myapplication/.SqlBroadcastReceiver --es sql \"SELECT * FROM test;\""
```

Then read the result:

```bash
adb shell run-as com.example.myapplication cat files/last_query_result.txt
```

You should see output like:

```text
id    value
1     hello from adb
```

The same text is also logged under the tag `SqlBroadcastReceiver`:

```bash
adb logcat | grep SqlBroadcastReceiver
```

---

## 5. General usage notes

- Any SQL you pass via the `sql` extra will be executed against the app's `app.db` database.
- Queries starting with `SELECT` or `PRAGMA` are treated as read-queries and their result rows are formatted into a tab-separated text table.
- All other statements (`INSERT`, `UPDATE`, `DELETE`, `CREATE TABLE`, etc.) are executed with `execSQL` and return `OK` on success.
- Errors are caught and written as `ERROR: <type>: <message>` into `last_query_result.txt` and logcat.

