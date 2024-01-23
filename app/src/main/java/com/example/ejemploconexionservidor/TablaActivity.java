package com.example.ejemploconexionservidor;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class TablaActivity extends AppCompatActivity {
    ListView listView;
    ArrayAdapter<String> adapter;
    ArrayList<String> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabla);

        listView = findViewById(R.id.listView);
        dataList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        cargarDatos();
    }

    private void cargarDatos() {
        new FetchDataFromServer().execute("http://192.168.240.160/ejemplo/getTabla.php");
    }

    private class FetchDataFromServer extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            // Realiza la solicitud HTTP al servidor y obtén la respuesta JSON
            return ""; // Reemplaza con la respuesta JSON real
        }
        
        @Override
        protected void onPostExecute(String result) {
            if (!result.isEmpty()) {
                try {
                    JSONArray jsonArrayResponse = new JSONArray(result);
                    for (int i = 0; i < jsonArrayResponse.length(); i++) {
                        JSONObject jsonObject = jsonArrayResponse.getJSONObject(i);
                        String data = jsonObject.optString("tuCampo", "Valor predeterminado");
                        dataList.add(data);
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

