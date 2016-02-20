/* Greek Gods: an Android application which shows the family tree of the Greek Gods.
 * Copyright (C) 2016 Tein van der Lugt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.teinvdlugt.android.greekgods;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private TextView nameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameTextView = (TextView) findViewById(R.id.name_textView);

        fillDatabase();
    }

    public void onClickAllPeople(View view) {
        startActivity(new Intent(this, AllPeopleActivity.class));
    }

    private void fillDatabase() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (checkNotConnected()) return null;
                SQLiteDatabase db = openOrCreateDatabase("data", 0, null);
                db.execSQL("DROP TABLE IF EXISTS `people`;");
                db.execSQL("DROP TABLE IF EXISTS `relations`;");
                db.execSQL("DROP TABLE IF EXISTS `births`;");
                db.execSQL("CREATE TABLE `people` (\n" +
                        "  `personId` int(11) NOT NULL,\n" +
                        "  `name` varchar(30) NOT NULL,\n" +
                        "  `description` varchar(1000) DEFAULT NULL,\n" +
                        "  `shortDescription` varchar(100) DEFAULT NULL)");
                db.execSQL("CREATE TABLE `relations` (\n" +
                        "  `relatiod_id` int(11) NOT NULL,\n" +
                        "  `personId1` int(11) DEFAULT NULL,\n" +
                        "  `personId2` int(11) DEFAULT NULL,\n" +
                        "  `description` varchar(1000) DEFAULT NULL,\n" +
                        "  `relation_type` varchar(10) DEFAULT NULL)");
                db.execSQL("CREATE TABLE `births` (\n" +
                        "  `personId` int(11) NOT NULL,\n" +
                        "  `relationId` int(11) NOT NULL)");

                String peopleSQLStatements = downloadFile("http://teinvdlugt.netai.net/people.sql");
                if (peopleSQLStatements != null) {
                    peopleSQLStatements = peopleSQLStatements.replaceAll("kcv.people", "people");
                    String[] statements = peopleSQLStatements.split("\n");
                    for (String statement : statements)
                        db.execSQL(statement);
                }
                String relationsSQLStatements = downloadFile("http://teinvdlugt.netai.net/relations.sql");
                if (relationsSQLStatements != null) {
                    relationsSQLStatements = relationsSQLStatements.replaceAll("kcv.relations", "relations");
                    String[] statements = relationsSQLStatements.split("\n");
                    for (String statement : statements)
                        db.execSQL(statement);
                }
                String birthsSQLStatements = downloadFile("http://teinvdlugt.netai.net/births.sql");
                if (birthsSQLStatements != null) {
                    birthsSQLStatements = birthsSQLStatements.replaceAll("kcv.births", "births");
                    String[] statements = birthsSQLStatements.split("\n");
                    for (String statement : statements)
                        db.execSQL(statement);
                }

                db.close();
                return null;
            }

            private String downloadFile(String URL) {
                try {
                    URL url = new URL(URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(20000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.connect();
                    int response = conn.getResponseCode();
                    //Log.d("THEVERGE", "Response: " + response);
                    if (response >= 400) return "" + response;
                    InputStream is = conn.getInputStream();
                    return read(is);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            private String read(InputStream inputStream) throws IOException {
                InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(reader);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                return sb.toString();
            }

            private boolean checkNotConnected() {
                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
                return networkInfo == null || !networkInfo.isConnected();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                // For debugging, show name of first person (Chaos)
                SQLiteDatabase db = openOrCreateDatabase("data", 0, null);
                Cursor cursor = db.query("people", new String[]{"name"}, null, null, null, null, null, "1");
                cursor.moveToFirst();
                nameTextView.setText(cursor.getString(cursor.getColumnIndex("name")));
                cursor.close();
                db.close();
            }
        }.execute();
    }
}
