package com.example.ejemploconexionservidor;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Inicio extends Activity {
    EditText user;
    EditText pass;
    Button validar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inicio);

        user = (EditText) findViewById(R.id.txtUsuario);
        pass = (EditText) findViewById(R.id.txtPass);
        validar = (Button) findViewById(R.id.btnValidar);

        validar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> parametros = new ArrayList<>();
                parametros.add("usuario");
                parametros.add(user.getText().toString());
                parametros.add("contrasena");
                parametros.add(pass.getText().toString());

                new PostDataTask(parametros).execute("http://192.168.240.160/ejemplo/login.php");
            }
        });
    }

    private class PostDataTask extends AsyncTask<String, Void, String> {
        private ArrayList<String> parametros;

        public PostDataTask(ArrayList<String> parametros) {
            this.parametros = parametros;
        }

        @Override
        protected String doInBackground(String... urls) {
            String urlString = urls[0];
            StringBuilder sb = new StringBuilder();

            try {
                String queryString = "?";
                for (int i = 0; i < parametros.size() - 1; i += 2) {
                    queryString += parametros.get(i) + "=" + Uri.encode(parametros.get(i + 1)) + "&";
                }
                if (queryString.endsWith("&")) {
                    queryString = queryString.substring(0, queryString.length() - 1);
                }
                URL url = new URL(urlString + queryString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null || result.trim().isEmpty()) {
                Toast.makeText(Inicio.this, "La respuesta está vacía o es nula.", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                JSONArray jsonArrayResponse = new JSONArray(result);
                if (jsonArrayResponse.length() > 0) {
                    JSONObject userData = jsonArrayResponse.getJSONObject(0);
                    int userId = userData.optInt("ID_USUARIO", -1);
                    String userName = userData.optString("USER", "No proporcionado");
                    if (userId > 0) {
                        String info = "ID: " + userId + "\nUsuario: " + userName;
                        Toast.makeText(Inicio.this, "Usuario correcto" , Toast.LENGTH_LONG).show();
                        Toast.makeText(Inicio.this,  info, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(Inicio.this, "Usuario incorrecto.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMessage = jsonArrayResponse.getJSONObject(0).optString("error", "Error desconocido.");
                    Toast.makeText(Inicio.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(Inicio.this, "Datos incorrectos", Toast.LENGTH_LONG).show();
            }
        }
    }
}
