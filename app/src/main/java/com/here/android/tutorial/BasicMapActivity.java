/*
 * Copyright (c) 2011-2018 HERE Global B.V. and its affiliate(s).
 * All rights reserved.
 * The use of this software is conditional upon having a separate agreement
 * with a HERE company for the use or utilization of this software. In the
 * absence of such agreement, the use of the software is not allowed.
 */
package com.here.android.tutorial;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;
import android.util.Log;
import android.view.Menu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BasicMapActivity extends Activity {
    private static final String LOG_TAG = BasicMapActivity.class.getSimpleName();
    // permissions request code
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    ArrayList<Position> positions = new ArrayList<>();

    /**
     * Permissions that need to be explicitly requested from end user.
     */
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    // map embedded in the map fragment
    private Map map = null;

    // map fragment embedded in this activity
    private MapFragment mapFragment = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();
    }

    ArrayList<MapObject> mapMarkers;

    private void initialize() {
        setContentView(R.layout.activity_main);
        mapMarkers = new ArrayList<>();

        // Search for the map fragment to finish setup by calling init().
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapfragment);
        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {
                if (error == OnEngineInitListener.Error.NONE) {
                    getJson();
                    // retrieve a reference of the map from the map fragment
                    map = mapFragment.getMap();
                    // Set the map center to the Vancouver region (no animation)
                    map.setCenter(new GeoCoordinate(52.54258081047913, 19.69306172150562, 0.0),
                            Map.Animation.NONE);
                    // Set the zoom level to the average between min and max
                    map.setZoomLevel((map.getMaxZoomLevel() + map.getMinZoomLevel()));
                    drawMarkers(null);

                } else {
                    Log.e(LOG_TAG, "Cannot initialize MapFragment (" + error + ")");
                }
            }
        });


    }


    public void drawMarkers(View view) {
        Image prywatny = new Image();
        Image platny = new Image();
        Image darmowy = new Image();
        try {
            prywatny.setImageResource(R.drawable.prywatnyznacznik);
            platny.setImageResource(R.drawable.platnyznacznik);
            darmowy.setImageResource(R.drawable.darmowyznacznik);
        } catch (IOException e) {
            e.printStackTrace();
        }

        map.removeMapObjects(mapMarkers);
        mapMarkers.clear();
        for (Position pos : positions) {

            MapMarker mapMarker = new MapMarker();
            GeoCoordinate geoCoordinate = new GeoCoordinate(pos.getX(), pos.getY());
            mapMarker.setCoordinate(geoCoordinate);

            if (pos.getTyp().equals("prywatny")) {
                mapMarker.setIcon(prywatny);
            } else if (pos.getTyp().equals("platny")) {
                mapMarker.setIcon(platny);
            } else if (pos.getTyp().equals("darmowy")) {
                mapMarker.setIcon(darmowy);
            }

            mapMarkers.add(mapMarker);
            map.addMapObject(mapMarker);
        }
    }

    /**
     * Checks the dynamically controlled permissions and requests missing permissions from end user.
     */
    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<String>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                // all permissions were granted
                initialize();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void getJson() {

        String json;

        try {
            InputStream is = getAssets().open("Parkomaty_point_geojson.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            json = new String(buffer, "UTF-8");
            JSONObject jsonObject = new JSONObject(json);

            JSONArray jsonArray = jsonObject.getJSONArray("features");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                JSONObject arra = obj.getJSONObject("geometry");
                JSONArray coordinates = arra.getJSONArray("coordinates");
                Position position = new Position();
                position.setX(coordinates.getDouble(1));
                position.setY(coordinates.getDouble(0));
                position.setName(arra.getString("nazwa"));
                position.setTyp(arra.getString("rodzaj"));
                positions.add(position);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onClickPrywatny(View view) {
        Image prywatny = new Image();
        Image platny = new Image();
        Image darmowy = new Image();
        Position pos2 = new Position();
        ClusterLayer clusterLayer = new ClusterLayer();
        try {
            prywatny.setImageResource(R.drawable.prywatnyznacznik);
            platny.setImageResource(R.drawable.platnyznacznik);
            darmowy.setImageResource(R.drawable.darmowyznacznik);
        } catch (IOException e) {
            e.printStackTrace();
        }
        pos2.setPrywatne(false);
        if (pos2.getPrywatne())
            pos2.setPrywatne(false);
        else
            pos2.setPrywatne(true);
        List<MapObject> temp = new ArrayList<>();

        if (pos2.getPrywatne()) {
            for (Position pos : positions) {

                MapMarker mapMarker = new MapMarker();
                GeoCoordinate geoCoordinate = new GeoCoordinate(pos.getX(), pos.getY());
                mapMarker.setCoordinate(geoCoordinate);
                mapMarker.setIcon(prywatny);
                if (pos.getTyp().equals("prywatny")) {
                    clusterLayer.addMarker(mapMarker);
                    map.addClusterLayer(clusterLayer);
                }
            }
        }
        else {for (Position pos : positions) {

            MapMarker mapMarker = new MapMarker();
            GeoCoordinate geoCoordinate = new GeoCoordinate(pos.getX(), pos.getY());
            mapMarker.setCoordinate(geoCoordinate);
            mapMarker.setIcon(prywatny);
            if (pos.getTyp().equals("prywatny")) {
                map.removeClusterLayer(clusterLayer);
        }
        }
            map.removeMapObjects(temp);
        }
    }

    public void onClickPlatny(View view) {

    }

    public void onClickDarmo(View view) {

    }
}