package com.example.location

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.location.ui.theme.LocationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: LocationViewModel = viewModel()
            LocationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp(viewModel)

                }
            }
        }
    }
}

@Composable
fun MyApp(viewModel: LocationViewModel){
    val context = LocalContext.current
    val locationUtils = LocationUtils(context)
    LocationDisplay(locationUtils = locationUtils,viewModel, context = context)
}

@Composable
fun LocationDisplay(locationUtils: LocationUtils,
                    viewModel: LocationViewModel,
                      context: Context){
    val location = viewModel.location.value
    val address = location?.let{
        locationUtils.reverseGeocodeLocation(location)
    }
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {
            permissions ->
            if(permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                &&
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION]== true
                ){
                locationUtils.requestLocationUpdates(viewModel = viewModel)
                // I have Access to location
            }
            else{
                // Ask for Permission
                val rationalRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

                if(rationalRequired){
                    Toast.makeText(context, "Location permission is required for this feature to work",Toast.LENGTH_LONG)
                        .show()
                }
                else
                {
                    Toast.makeText(context, "Location permission is required. Please Enable it in Android Setting",Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    )

    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {

        if(location != null){
            Text(text = "Address: ${location.latitude} ${location.longitude} \n $address")
        }
        else
        {
            Text(text = "Location not Available")

        }


        Button(onClick = {
            if(locationUtils.hasLocationPermission(context)){
                locationUtils.requestLocationUpdates(viewModel)
                // Permission Already granted
            }
            else{
                // Request location Permission
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION
                        ,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }) {

            Text(text = "Get Location")

        }

    }

}