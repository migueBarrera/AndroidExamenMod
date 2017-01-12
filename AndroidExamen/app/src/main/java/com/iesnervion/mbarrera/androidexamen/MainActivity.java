package com.iesnervion.mbarrera.androidexamen;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.iesnervion.mbarrera.androidexamen.Contrato.Jugador_DB;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity{

    //SQLITE
    String consultaJugadores = "SELECT * FROM "+Jugador_DB.JUGADOR_DB_TABLE_NAME+";";
    JugadorDatabaseHelper manejadoraJugadores ;

    ListView miLista;
    public MyArrayAdapterTotal<Jugador> miArrayAdapter;
    List<Jugador> jugadores;
    public Jugador[] jugadoresArray=new Jugador[]{new Jugador()};
    Context miContexto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        miContexto=this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //BOTON FLOTANTE AÑADIR
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.botonFlotanteADD);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent para ir ala actividad Formulario
                Intent i = new Intent(getApplicationContext(),FormularioActivity.class);
                startActivity(i);
            }
        });
        //Inicializacion
        miLista = (ListView) findViewById(R.id.miLista);
        //Listenner para la lista
        miLista.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3)
            {
                showPopupMenu(v,position);
            }
        });
        //Obtener Datos
        cargarLista();
    }

    public void cargarLista(){
        //OkClient
        OkHttpClient okClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request request = chain.request().newBuilder()
                                .addHeader("Accept","Application/JSON").build();
                        return chain.proceed(request);
                    }
                }).build();

        //RETROFIT
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://apijugadores.mbarrera.ciclo.iesnervion.es/").client(okClient)
                .addConverterFactory(GsonConverterFactory.create()).build();


        JugadoresServices servicio = retrofit.create(JugadoresServices.class);

        Call<List<Jugador>> call = servicio.obtenerJugadores();
        Log.d(" etiquetaaaa","Antes del eneuque");



         call.enqueue(new Callback<List<Jugador>>() {
            @Override
            public void onResponse(Call<List<Jugador>> call, Response<List<Jugador>> response) {
                Log.d(" etiquetaaaa","EN ONRESPONSE");
                if(response.isSuccessful()){
                    Log.d(" etiquetaaaa","DENTRO DEL IF DE ONRESPONSE");

                    jugadores = response.body();
                    jugadoresArray =  jugadores.toArray(new Jugador[0]);

                    miArrayAdapter = new MyArrayAdapterTotal<>(getApplicationContext(),R.layout.fila_personalizada,R.id.tvNombre, jugadoresArray);
                    miLista.setAdapter(miArrayAdapter);

                }
            }

            @Override
            public void onFailure(Call<List<Jugador>> call, Throwable t) {
                Log.d(" etiquetaaaa","EN ONFAILURE");
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showPopupMenu(final View view, final int position)
    {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_pop, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                ListView milista = (ListView) view.getParent();

                switch (item.getItemId()){
                    case R.id.borrarItem:
                        //BorrarJugador
                        //((Datos) MainActivity.this.getApplication()).borrarJugador(position);
                        borrarJugador(position);
                        //Obtener la lista actualizada
                        //jugadores =  ((Datos) MainActivity.this.getApplication()).getJugadores();
                        //jugadores = obtenerListado();
                        Jugador[] jugadoresArray =  jugadores.toArray(new Jugador[0]);
                        //Crear adapter con la lista actualizada
                        miArrayAdapter = new MyArrayAdapterTotal<Jugador>(getApplicationContext(),R.layout.fila_personalizada,R.id.tvNombre, jugadoresArray);
                        //EnviarAdapter
                        MainActivity.this.miLista.setAdapter(miArrayAdapter);

                        //Notificar al usuario
                        Toast.makeText(getApplicationContext(),"Se ha borrado un jugador",Toast.LENGTH_LONG).show();

                        break;


                    case R.id.editarItem:


                        Intent i = new Intent(getApplicationContext(),FormularioActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("playerAEditar",(Jugador)milista.getItemAtPosition(position));
                        i.putExtras(bundle);
                        startActivity(i);
                        break;

                }
                return true;
            }
        });
        //dont forget to show the menu
        popupMenu.show();
    }

    private void borrarJugador(int position) {
        Jugador j = jugadores.get(position);
        String cadena = "DELETE FROM "+Jugador_DB.JUGADOR_DB_TABLE_NAME+" WHERE "+Jugador_DB.JUGADOR_DB_NOMBRE+" = '"+j.getNombre()+"' ;";
        SQLiteDatabase db = manejadoraJugadores.getWritableDatabase();
        db.execSQL(cadena);
        db.close();
    }

    /*public ArrayList<Jugador> obtenerListado(){
        ArrayList<Jugador> js = new ArrayList<Jugador>();
        manejadoraJugadores = new JugadorDatabaseHelper(this);
        SQLiteDatabase db = manejadoraJugadores.getReadableDatabase();

        Cursor cursor = db.rawQuery(consultaJugadores,null);

        if(cursor.moveToFirst()){
            do{
                String nombre = cursor.getString(cursor.getColumnIndex(Jugador_DB.JUGADOR_DB_NOMBRE));
                String posicion = cursor.getString(cursor.getColumnIndex(Jugador_DB.JUGADOR_DB_POSICION));
                double altura = cursor.getDouble(cursor.getColumnIndex(Jugador_DB.JUGADOR_DB_ALTURA));
                double peso = cursor.getDouble(cursor.getColumnIndex(Jugador_DB.JUGADOR_DB_PESO));
                int img = cursor.getInt(cursor.getColumnIndex(Jugador_DB.JUGADOR_DB_IMAGEN));

                Jugador j = new Jugador(nombre,img,altura,peso,posicion);
                js.add(j);
            }while (cursor.moveToNext());
        }

        db.close();

        return (js);
    }*/


}
