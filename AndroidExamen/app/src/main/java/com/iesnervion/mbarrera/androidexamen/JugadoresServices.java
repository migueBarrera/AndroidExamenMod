package com.iesnervion.mbarrera.androidexamen;

import android.support.v4.media.session.MediaSessionCompat;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by Migue-w10 on 27/12/2016.
 */

public interface JugadoresServices {

    @GET("jugadores")
    Call<List<Jugador>> obtenerJugadores();
}
