package com.example.ejemploconexionservidor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
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
    Button cambiarContrasenaButton, cerrarSesionButton, insertarUsuarioButton, eliminarUsuarioButton;

    int selectedItemPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabla);

        listView = findViewById(R.id.listView);
        dataList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> selectedItemPosition = position);

        cambiarContrasenaButton = findViewById(R.id.btnCerrarSesion4);
        cerrarSesionButton = findViewById(R.id.btnCerrarSesion);
        insertarUsuarioButton = findViewById(R.id.btnCerrarSesion2);
        eliminarUsuarioButton = findViewById(R.id.btnCerrarSesion3);

        cambiarContrasenaButton.setOnClickListener(v -> mostrarDialogoCambiarContrasena());
        cerrarSesionButton.setOnClickListener(v -> cerrarSesion());
        insertarUsuarioButton.setOnClickListener(v -> mostrarDialogoInsertarUsuario());
        eliminarUsuarioButton.setOnClickListener(v -> mostrarDialogoEliminarUsuario());

        cargarDatos();
    }

    private void cargarDatos() {
        new FetchDataFromServer().execute("http://192.168.240.160/ejemplo/getTabla.php");
    }

    private void actualizarDatos() {
        dataList.clear();
        cargarDatos();
    }

    private void mostrarDialogoInsertarUsuario() {
        AlertDialog.Builder builder = new AlertDialog.Builder(TablaActivity.this);
        builder.setTitle("Insertar nuevo usuario");

        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_insertar_usuario, null);
        builder.setView(customLayout);

        builder.setPositiveButton("Insertar", (dialog, which) -> {
            EditText editTextUsuario = customLayout.findViewById(R.id.editTextUsuario);
            EditText editTextPassword = customLayout.findViewById(R.id.editTextPassword);

            String usuario = editTextUsuario.getText().toString();
            String password = editTextPassword.getText().toString();

            insertarUsuario(usuario, password);
            actualizarDatos();
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    public void insertarUsuario(String usuario, String contrasena) {
        AsyncTask.execute(() -> {
            try {
                URL url = new URL("http://192.168.240.160/ejemplo/insertarUsuario.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                String data = URLEncoder.encode("usuario", "UTF-8") + "=" + URLEncoder.encode(usuario, "UTF-8") + "&" +
                        URLEncoder.encode("contrasena", "UTF-8") + "=" + URLEncoder.encode(contrasena, "UTF-8");
                writer.write(data);
                writer.flush();
                writer.close();
                os.close();
                conn.getInputStream();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void mostrarDialogoEliminarUsuario() {
        AlertDialog.Builder builder = new AlertDialog.Builder(TablaActivity.this);
        builder.setTitle("Eliminar Usuario");

        // Configura el EditText para ingresar el nombre del usuario
        final EditText input = new EditText(TablaActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Configura los botones de acción
        builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nombreUsuario = input.getText().toString();
                eliminarUsuario(nombreUsuario);
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


    private void eliminarUsuario(String usuario) {
        new EliminarUsuarioTask().execute(usuario);
    }


    private class EliminarUsuarioTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String usuario = params[0];
            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://192.168.240.160/ejemplo/eliminarUsuario.php");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                String data = URLEncoder.encode("usuario", "UTF-8") + "=" + URLEncoder.encode(usuario, "UTF-8");
                writer.write(data);
                writer.flush();
                writer.close();
                os.close();

                // Leer la respuesta del servidor
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString(); // Retorna la respuesta real del servidor
            } catch (Exception e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(TablaActivity.this, result, Toast.LENGTH_SHORT).show(); // Muestra la respuesta del servidor
            actualizarDatos();
        }
    }

    private void mostrarDialogoCambiarContrasena() {
        AlertDialog.Builder builder = new AlertDialog.Builder(TablaActivity.this);
        builder.setTitle("Cambiar Contraseña");

        final EditText inputUsuario = new EditText(TablaActivity.this);
        inputUsuario.setInputType(InputType.TYPE_CLASS_TEXT);
        inputUsuario.setHint("Nombre de usuario");
        builder.setView(inputUsuario);

        builder.setPositiveButton("Siguiente", (dialog, which) -> {
            String nombreUsuario = inputUsuario.getText().toString();
            mostrarDialogoCambiarContrasenaDetalle(nombreUsuario);
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void mostrarDialogoCambiarContrasenaDetalle(String nombreUsuario) {
        AlertDialog.Builder builder = new AlertDialog.Builder(TablaActivity.this);
        builder.setTitle("Cambiar Contraseña para " + nombreUsuario);

        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_cambiar_contrasena, null);
        builder.setView(customLayout);

        builder.setPositiveButton("Cambiar", (dialog, which) -> {
            EditText editTextContrasenaActual = customLayout.findViewById(R.id.editTextContrasenaActual);
            EditText editTextContrasenaNueva = customLayout.findViewById(R.id.editTextContrasenaNueva);

            String contrasenaActual = editTextContrasenaActual.getText().toString();
            String contrasenaNueva = editTextContrasenaNueva.getText().toString();

            cambiarContrasena(nombreUsuario, contrasenaActual, contrasenaNueva);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void cambiarContrasena(String usuario, String contrasenaActual, String contrasenaNueva) {
        new CambiarContrasenaTask().execute(usuario, contrasenaActual, contrasenaNueva);
    }

    private class CambiarContrasenaTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String usuario = params[0];
            String contrasenaActual = params[1];
            String contrasenaNueva = params[2];
            HttpURLConnection conn = null;

            try {
                URL url = new URL("http://192.168.240.160/ejemplo/cambiarPass.php");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                String data = URLEncoder.encode("usuario", "UTF-8") + "=" + URLEncoder.encode(usuario, "UTF-8") +
                        "&" + URLEncoder.encode("contrasenaActual", "UTF-8") + "=" + URLEncoder.encode(contrasenaActual, "UTF-8") +
                        "&" + URLEncoder.encode("contrasenaNueva", "UTF-8") + "=" + URLEncoder.encode(contrasenaNueva, "UTF-8");
                writer.write(data);
                writer.flush();
                writer.close();
                os.close();

                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(TablaActivity.this, result, Toast.LENGTH_LONG).show();
            actualizarDatos();
        }
    }


    private void cerrarSesion() {
        startActivity(new Intent(TablaActivity.this, Inicio.class));
        finish();
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
            if (!result.startsWith("Error")) {
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
}
