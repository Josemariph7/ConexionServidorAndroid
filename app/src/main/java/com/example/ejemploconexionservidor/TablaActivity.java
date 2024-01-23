package com.example.ejemploconexionservidor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class TablaActivity extends AppCompatActivity {
    ListView listView;
    ArrayAdapter<String> adapter;
    ArrayList<String> dataList;
    Button cambiarContrasenaButton;
    Button cerrarSesionButton;
    Button insertarUsuarioButton;
    Button eliminarUsuarioButton;

    int selectedItemPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabla);

        listView = findViewById(R.id.listView);
        dataList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position == selectedItemPosition) {
                    view.setBackgroundResource(R.drawable.list_item_selector);
                } else {
                    view.setBackgroundColor(getResources().getColor(android.R.color.transparent)); // Se asegura de que la transparencia se aplique correctamente
                }
                return view;
            }
        };
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            // Actualiza la posición seleccionada
            selectedItemPosition = position;

            // Actualiza la vista para el estado seleccionado
            for (int i = 0; i < parent.getChildCount(); i++) {
                View childView = parent.getChildAt(i);
                if (i == position) {
                    childView.setSelected(true); // Esto debería mantener el estado seleccionado.
                    childView.setBackgroundResource(R.drawable.list_item_selector);
                } else {
                    childView.setSelected(false); // Desmarca cualquier estado seleccionado anterior.
                    childView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                }
            }

            // Notifica al adaptador del cambio y redibuja la lista
            adapter.notifyDataSetChanged();
        });

        // Inicializa los botones
        cambiarContrasenaButton = findViewById(R.id.btnCerrarSesion4);
        cerrarSesionButton = findViewById(R.id.btnCerrarSesion);
        insertarUsuarioButton = findViewById(R.id.btnCerrarSesion2);
        eliminarUsuarioButton = findViewById(R.id.btnCerrarSesion3);

        cambiarContrasenaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(TablaActivity.this);
                builder.setTitle("Cambiar Contraseña");
                final EditText newPasswordEditText = new EditText(TablaActivity.this);
                newPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(newPasswordEditText);

                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newPassword = newPasswordEditText.getText().toString();

                        new ChangePasswordTask().execute(newPassword);
                    }
                });

                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });


        cerrarSesionButton.setOnClickListener(v -> {
            Toast.makeText(TablaActivity.this, "Cerrando sesión...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(TablaActivity.this, Inicio.class);
            startActivity(intent);
        });

        insertarUsuarioButton.setOnClickListener(v -> {
            // Implementa la lógica para insertar un nuevo usuario
        });

        eliminarUsuarioButton.setOnClickListener(v -> {
            if (selectedItemPosition != -1) {
                String selectedItem = dataList.get(selectedItemPosition);
                // Implementa la lógica para eliminar el usuario seleccionado
                // Por ejemplo, puedes extraer el ID del usuario del selectedItem y enviar una solicitud para eliminarlo
            } else {
                Toast.makeText(TablaActivity.this, "Selecciona un usuario para eliminar", Toast.LENGTH_SHORT).show();
            }
        });

        cargarDatos();
    }

    private void cargarDatos() {
        new FetchDataFromServer().execute("http://192.168.56.1/ejemplo/getTabla.php");
    }

    private class FetchDataFromServer extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            StringBuilder sb = new StringBuilder();
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "Error de conexión: " + e.getMessage();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result.startsWith("Error")) {
                Toast.makeText(TablaActivity.this, result, Toast.LENGTH_LONG).show();
            } else {
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject user = jsonArray.getJSONObject(i);
                        String userData = "Usuario: " + user.getString("USER") + ", Contraseña: " + user.getString("PASSWORD");
                        dataList.add(userData);
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Toast.makeText(TablaActivity.this, "Error al parsear los datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    private class ChangePasswordTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String newPassword = params[0];
            String serverUrl = "http://192.168.56.1/ejemplo/cambiarPass.php";

            try {
                // Realiza la solicitud HTTP al servidor para cambiar la contraseña
                HttpURLConnection connection = (HttpURLConnection) new URL(serverUrl).openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Envía la nueva contraseña como parámetro
                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                String postData = "new_password=" + URLEncoder.encode(newPassword, "UTF-8");
                writer.write(postData);
                writer.flush();
                writer.close();
                os.close();

                // Lee la respuesta del servidor
                InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                is.close();

                // Devuelve la respuesta del servidor
                return response.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return "Error al cambiar la contraseña: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // Maneja el resultado de la operación
            if (result.startsWith("Error")) {
                // Muestra un mensaje de error al usuario
                Toast.makeText(TablaActivity.this, result, Toast.LENGTH_LONG).show();
            } else {
                // Muestra un mensaje de éxito al usuario
                Toast.makeText(TablaActivity.this, "Contraseña cambiada con éxito.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}


