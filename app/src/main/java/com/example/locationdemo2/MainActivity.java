package com.example.locationdemo2;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 1;

    //location demo with fused location provider client
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private static final int UPDATE_INTERVAL = 5000; // 5 seconds
    private static final int FASTEST_INTERVAL = 3000; // 3 seconds

    private List<String> permissionsToRequest;
    private List<String> permissions = new ArrayList<>();
    private List<String> permissionsRejected = new ArrayList<>();

    private TextView txtLocation;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtLocation = findViewById(R.id.textViewLocation);

        //instantiate the fusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //add permissions
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionsToRequest = permissionsToRequest(permissions);
        if (permissionsToRequest.size() > 0){
            requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), REQUEST_CODE);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private List<String> permissionsToRequest(List<String> permissions) {

        ArrayList<String> results = new ArrayList<>();
        for (String perm : permissions) {
            if (!hasPermission(perm))

                results.add(perm);
        }
        return results;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean hasPermission(String perm) {

        return checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        //this is a proper place to check the google play services availability
        int errorCode = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(this);

        if (errorCode != ConnectionResult.SUCCESS) {
            Dialog errorDialog = GoogleApiAvailability.getInstance()
                    .getErrorDialog(this, errorCode, errorCode, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            Toast.makeText(MainActivity.this, "No Google play services", Toast.LENGTH_SHORT).show();
                        }
                    });

            errorDialog.show();

        } else {
            Log.i(TAG, "onPostResume: ");
            findLocation();
        }
    }

    private void findLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        txtLocation.setText(String.format("Lat : %s , Long : %s", location.getLatitude(), location.getLongitude()));
                    }
                }
            });
        }
        startUpdateLocation();
    }

    private void startUpdateLocation() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    txtLocation.setText(String.format("Lat : %s , Long : %s", location.getLatitude(), location.getLongitude()));

                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull final String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE){
            for(String perm : permissions){
                if(!hasPermission(perm)){
                    permissionsRejected.add(perm);
                }
            }

            if (permissionsRejected.size() > 0){
                if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))){
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("The Location permission is mandatory")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), REQUEST_CODE);
                                    }
                                }
                            }).setNegativeButton("Cancel", null)
                            .create()
                            .show();
                }
            }

        }
    }
}