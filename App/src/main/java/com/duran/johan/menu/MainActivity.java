package com.duran.johan.menu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.R.attr.id;
import static android.R.id.message;
import static android.os.Build.VERSION_CODES.M;
import static com.duran.johan.menu.R.id.longitud;
import static com.duran.johan.menu.R.id.map;
import static com.duran.johan.menu.R.layout.maps;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker;

public class MainActivity extends Navigation
        implements OnMapReadyCallback {

    //variables del mapa
    private GoogleMap mMap;//mapa a mostrar
    List<Marker> markers = new ArrayList<Marker>();
    JSONObject marcadoresJson;
    Map<String,String> idColor;
    //Marcadores para aritmetica de puntos
    Marker first;
    Marker second;
    int contadorClics;
    //variables para peticiones al servidor
    MySingleton singleton;

    //control de multidex.
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //se agrega la vista complementaria al menú.
        RelativeLayout item = (RelativeLayout) findViewById(R.id.relative_element);
        View child = getLayoutInflater().inflate(maps, null);
        item.addView(child);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        //evento asociado al boton sobre el mapa
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabFiltrar);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Se inicia la actividad para realizar filtros
                ActivityLauncher.startActivityB(MainActivity.this, ActivityFilter.class, false);
            }
        });

        //inicialización de variable
        contadorClics=0;
        idColor = new HashMap<String,String>();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in CR and move the camera
        LatLng costaRica = new LatLng(10.131581, -84.181927);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(costaRica, 9));
        String file = "getMarkers_busqueda.php"; //temporal solo de ejemplo.
        mMap.getUiSettings().setMapToolbarEnabled(false); //se desabilita redirección a google maps
        mMap.getUiSettings().setMyLocationButtonEnabled(true);//habilita myLocation buttom
        getRequest(file, 1); //peticion para cargar los marcadores
    }


    //Método utilizado para realizar peticiones asincronas al servidor
    public void getRequest(String file, final int num) {
        String dir = getString(R.string.server)+ file;//se crea la dirección completa al servidor

        //inicio de la peticion GET
        JsonArrayRequest jsArrRequest = new JsonArrayRequest
                (Request.Method.GET, dir, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        switch (num) {//Incluir los casos dependiendo de la cantidad de llamados distintos que se puedan hacer.
                            case 1://petición 1 cargar todos los marcadores
                                cargarMarcadores(response);
                                break;
                            case 2:
                                //lo que se desea hacer para la petición 2

                                break;
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        //lo que se desea hacer en caso de error
                    }
                });

        int socketTimeout = 3000;//Tiempo dde espera antes de morir TIMEOUT
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsArrRequest.setRetryPolicy(policy);
        // Access the RequestQueue through your singleton class.
        singleton.getInstance(this).addToRequestQueue(jsArrRequest);
    }

    //Método para cargar los marcadorees sobre le mapa recibe el json de la peticion
    private void cargarMarcadores(JSONArray response) {
        //lo que se desea hacer para la petición 1
        int cantidadMarcadores = 0;
        marcadoresJson = new JSONObject();
        try {
            cantidadMarcadores = response.getJSONObject(0).getJSONObject("results").length();
            marcadoresJson = response.getJSONObject(0).getJSONObject("results");
            //se insertan los marcadores en el mapa
            for (int i = 0; i < cantidadMarcadores; i++) {
                JSONObject location = marcadoresJson.getJSONObject(Integer.toString(i)).getJSONObject("value").getJSONObject("location");
                LatLng position = new LatLng(location.getDouble("lat"), location.getDouble("lng"));
                String color = marcadoresJson.getJSONObject(Integer.toString(i)).getJSONObject("value").getString("color");
                String id = marcadoresJson.getJSONObject(Integer.toString(i)).getJSONObject("value").getString("id");
                String title = marcadoresJson.getJSONObject(Integer.toString(i)).getString("_id");
                BitmapDescriptor iconColor = BitmapDescriptorFactory.defaultMarker(getColor(color));
                Marker marker= mMap.addMarker(new MarkerOptions().position(position).title(title));
                marker.setIcon(iconColor);
                marker.setTag(id);
                idColor.put(id,color);
                //markers.add(Integer.getInteger(id),marker);
            }
            crearEventosMapa();// Si se insertaron se crean los eventos del click.
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //Método para eventos de los marcadores y las ventanas que aparecen al darle clic
    private void crearEventosMapa() {

        //evento asociado al clic sobre el mapa, actualmente para borrar arpoi
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng arg0) {
                // TODO Auto-generated method stub
                //Log.d("arg0", arg0.latitude + "-" + arg0.longitude);
                if(contadorClics>0){
                    contadorClics=0;
                    BitmapDescriptor iconColor1 = BitmapDescriptorFactory.defaultMarker(getColor(idColor.get(first.getTag())));
                    first.setIcon(iconColor1);
                    BitmapDescriptor iconColor2 = BitmapDescriptorFactory.defaultMarker(getColor(idColor.get(second.getTag())));
                    second.setIcon(iconColor2);
                }
            }
        });

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                // TODO Auto-generated method stub
                return null;
            }

            //diseño de la ventana del marcador
            @Override
            public View getInfoContents(Marker marker) {

                View view = getLayoutInflater().inflate(R.layout.map_info_window,null);

                TextView lat = (TextView)view.findViewById(R.id.txtLatitud);
                TextView lng = (TextView)view.findViewById(R.id.txtLongitud);
                TextView est = (TextView)view.findViewById(R.id.txtEstacion);
                lat.setText(String.valueOf(marker.getPosition().latitude));
                lng.setText(String.valueOf(marker.getPosition().longitude));
                est.setText(marker.getTitle());
                TextView btnWindows = (TextView)view.findViewById(R.id.verMas);
                //btnWindows.setText("HOLIII");
                if(!arPOIFlag){
                    btnWindows.setText(String.valueOf("CLIC PARA VER MÁS"));
                    return view;
                }else{
                    if(contadorClics<2){
                        BitmapDescriptor iconColor = BitmapDescriptorFactory.defaultMarker(getColor("rose"));
                        if(contadorClics==0){
                            btnWindows.setText(String.valueOf("Seleccione otro marcador"));
                            first=marker;
                            marker.setIcon(iconColor);
                        }else{//==1
                            if(marker.getTag()!=first.getTag()){
                                btnWindows.setText(String.valueOf("calcular diferencia"));
                                second=marker;
                                marker.setIcon(iconColor);
                            }
                        }
                        contadorClics++;
                        return view;
                    }else{
                        contadorClics=0;
                        BitmapDescriptor iconColor1 = BitmapDescriptorFactory.defaultMarker(getColor(idColor.get(first.getTag())));
                        first.setIcon(iconColor1);
                        BitmapDescriptor iconColor2 = BitmapDescriptorFactory.defaultMarker(getColor(idColor.get(second.getTag())));
                        second.setIcon(iconColor2);
                        return getInfoContents(marker);
                    }
                }
            }
        });
        //evento para control de clic dentro de la ventana
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                //al presionarse clic se lanza la actividad de marcador información.
                if(arPOIFlag){
                    Intent intent = new Intent(MainActivity.this, ActivityAritmetica.class);
                    intent.putExtra("id1", String.valueOf(marker.getTag()));
                    intent.putExtra("id2", String.valueOf(marker.getTag()));
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(MainActivity.this, ActivityMarker.class);
                    intent.putExtra("objId", String.valueOf(marker.getTag()));
                    startActivity(intent);
                }
            }
        });
    }

    //Colores asociados a cada tipo de indice
    private int getColor(String color) {
        switch (color) {
            case "Azul":
                return 240;
            case "Verde":
                return 120;
            case "Amarillo":
                return 60;
            case "Anaranjado":
                return 30;
            case "Rojo":
                return 0;
            case "arpoi":
                return 200;
            default:
                return 300;
        }
    }
}
