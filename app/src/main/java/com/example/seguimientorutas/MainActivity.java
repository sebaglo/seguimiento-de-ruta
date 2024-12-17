package com.example.seguimientorutas;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationRequest;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private LatLng startLocation = null; // Ubicación inicial
    private LatLng endLocation = null;   // Ubicación final
    private Polyline currentRoute = null; // Línea de la ruta
    private FusedLocationProviderClient fusedLocationProviderClient; // Cliente de ubicación
    private LocationCallback locationCallback; // Callback para recibir la ubicación
    private LatLng currentLocation; // Ubicación actual
    private Spinner mapTypeSpinner; // Spinner para cambiar tipo de mapa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar el mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Inicializar el cliente de ubicación
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        startLocationUpdates();

        // Inicializar el Spinner para seleccionar el tipo de mapa
        mapTypeSpinner = findViewById(R.id.mapTypeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.map_types_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mapTypeSpinner.setAdapter(adapter);

        // Establecer el listener para el Spinner
        mapTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
                String selectedItem = (String) parentView.getItemAtPosition(position); // Hacer un cast explícito a String
                changeMapType(selectedItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Aquí puedes definir un comportamiento si no se selecciona nada
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Establecer el tipo de mapa predeterminado
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Establecer el evento de clic en el mapa
        mMap.setOnMapClickListener(this);

        // Centrar el mapa en una ubicación predeterminada (puedes cambiarla por la ubicación de inicio)
        LatLng defaultLocation = new LatLng(-30.5986, -71.2006); // Ovalle, Chile
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15));
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        if (startLocation == null) {
            // Si no hay una ubicación de inicio, se marca la primera ubicación como el inicio
            startLocation = latLng;
            mMap.addMarker(new MarkerOptions().position(startLocation).title("Inicio de la ruta"));
            Toast.makeText(this, "Ruta iniciada", Toast.LENGTH_SHORT).show();
        } else if (endLocation == null) {
            // Si ya hay un inicio, se marca la segunda ubicación como el final
            endLocation = latLng;
            mMap.addMarker(new MarkerOptions().position(endLocation).title("Final de la ruta"));
            Toast.makeText(this, "Ruta finalizada", Toast.LENGTH_SHORT).show();

            // Dibujar la ruta entre los puntos
            drawRoute();
        } else {
            // Si ya hay una ruta completa, reiniciamos la selección de ruta
            resetRoute();
        }
    }

    private void drawRoute() {
        if (startLocation != null && endLocation != null) {
            // Crear una nueva línea (ruta) entre los dos puntos
            if (currentRoute != null) {
                currentRoute.remove(); // Eliminar la línea anterior si existe
            }

            PolylineOptions options = new PolylineOptions()
                    .add(startLocation)
                    .add(endLocation)
                    .width(5)
                    .color(0xFF0000FF); // Color azul

            currentRoute = mMap.addPolyline(options); // Dibujar la línea en el mapa
        }
    }

    private void resetRoute() {
        // Reiniciar la ruta, eliminando las marcas y la línea
        startLocation = null;
        endLocation = null;
        if (currentRoute != null) {
            currentRoute.remove();
        }
        mMap.clear();
        Toast.makeText(this, "Ruta reiniciada", Toast.LENGTH_SHORT).show();
    }

    private void startLocationUpdates() {
        // Crear la solicitud de ubicación
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000); // Actualización cada 1 segundo
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // Prioridad alta

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    for (android.location.Location location : locationResult.getLocations()) {
                        if (location != null) {
                            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            if (mMap != null) {
                                // Actualizar la cámara del mapa con la nueva ubicación
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                                mMap.addMarker(new MarkerOptions().position(currentLocation).title("Ubicación actual"));
                            }
                        }
                    }
                }
            }
        };

        // Solicitar actualizaciones de ubicación
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void changeMapType(String mapType) {
        switch (mapType) {
            case "Normal":
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case "Satelital":
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case "Terreno":
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            default:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Detener las actualizaciones de ubicación al detener la actividad
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }
}
