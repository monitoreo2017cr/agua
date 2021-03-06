package com.duran.johan.menu;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import static com.duran.johan.menu.R.string.nombre;

/**
 * Created by kenca on 16/02/2017.
 */

public class MongoRequest extends StringRequest {

    //private static final String REGISTER_REQUEST_URL = "http://192.168.100.12:8081/proyectoJavier/android/registro.php";
    private Map<String, String> params;

    public MongoRequest(Map<String, String> parametros, String REQUEST_URL, Response.Listener<String> listener){
        super(Request.Method.POST, REQUEST_URL, listener, null);
        params = parametros;

    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
