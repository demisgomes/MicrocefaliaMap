package com.microcefaliamap;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    DatabaseOperations dbOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        dbOperations=new DatabaseOperations(this);
        dbOperations.createDatabase();
        LatLng brasil = new LatLng(-15.05428,  -53.8813);
        //mMap.addMarker(new MarkerOptions().position(recife).title("Recife" + "\n" + "Suspeitos: 455" + "\n" + "Confirmados:284")
          //      .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(recife));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(brasil.latitude, brasil.longitude), (float) 3.4));

        //se o banco está vazio, insere os valores nele recuperados pela task assíncrona
        ArrayList<Case> returnList=dbOperations.returnCases();
        if(returnList==null){
            new MapTask().execute();
        }
        else{
            insertMarkers(returnList);
        }

    }

    public void update(View v){
        new MapTask().execute();
    }

    private class MapTask extends AsyncTask<Void,Integer,ArrayList<Case>>{

        @Override
        protected ArrayList<Case> doInBackground(Void... params) {
            WebService ws=new WebService();

            return ws.getCases();
        }

        @Override
        protected void onPostExecute(ArrayList<Case> cases) {
            super.onPostExecute(cases);
            if(cases!=null){
                if(dbOperations.returnCases()==null){
                    Toast.makeText(getApplicationContext(),"Recuperando dados do servidor",Toast.LENGTH_LONG).show();
                    dbOperations.insertCases(cases);
                }
                else{
                    Toast.makeText(getApplicationContext(),"Atualizando dados",Toast.LENGTH_LONG).show();
                    dbOperations.updateCases(cases);
                }
                mMap.clear();
                insertMarkers(cases);
            }
            else{
                Toast.makeText(getApplicationContext(),"Erro na comunicação com o servidor",Toast.LENGTH_LONG).show();
            }


        }

    }

    public void insertMarkers(ArrayList<Case> cases){
        for(Case newCase:cases){
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(newCase.getLatitude(),newCase.getLongitude()))
                    .title(newCase.getState()+". Suspeitos:"+String.valueOf(newCase.getSuspectedCases())+". Confirmados: "+String.valueOf(newCase.getConfirmedCases()))
                    .icon(BitmapDescriptorFactory.defaultMarker(getMarkerColor(newCase.getSuspectedCases(), newCase.getConfirmedCases()))));

        }
    }

    public float getMarkerColor(float suspectedCases, float confirmedCases){
        float color=BitmapDescriptorFactory.HUE_AZURE;
        if(suspectedCases==0){
            color=BitmapDescriptorFactory.HUE_GREEN;
        }
        else if (suspectedCases>0){
            color=BitmapDescriptorFactory.HUE_YELLOW;
        }
        if (confirmedCases>1){
            color=BitmapDescriptorFactory.HUE_ORANGE;
        }
        if (confirmedCases>20){
            color=BitmapDescriptorFactory.HUE_RED;
        }
        return color;
    }
}
