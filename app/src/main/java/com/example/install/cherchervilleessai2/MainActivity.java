package com.example.install.cherchervilleessai2;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private TextView est_latitude;
    private TextView est_longitude;
    private  String latitude, longitude, ville;
    private WebView mWebView;
    public String city="";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //liaison des composants avec la vue
        est_latitude = findViewById(R.id.txtLatitude);
        est_longitude = findViewById(R.id.txtLongitude);

        //recuperation du resultat de la latitude longitude
    }

    public void clickAffichageCarte(View v)
    {
        if(verifConnection(v)) {

            //Recuperation des données dans la vue
            latitude = est_latitude.getText().toString();
            longitude = est_longitude.getText().toString();

            //test pour ne pas remplir uniquement un "." dans les champs
            if (latitude.equals(".") || longitude.equals(".")) {
                Toast.makeText(this, "latitude ou longitude non valide", Toast.LENGTH_SHORT).show();

                //si tout ce passe bien on transforme les String en double
            } else {
                Double lat = Double.parseDouble(latitude);
                Double longi = Double.parseDouble(longitude);

                if (lat <= -90 || lat >= 90) {
                    Toast.makeText(this, "latitude ou longitude non valide", Toast.LENGTH_SHORT).show();
                } else {
                    MaTache tache = new MaTache(longitude, latitude, this);
                    // Déclare mWebView à activity_main (le layout)
                    mWebView = (WebView) findViewById(R.id.webviewCarte);
                    //indique l'url vers laquelle on souhaite naviguer
                    String url = "http://maps.google.fr/maps?q="+lat+","+longi+"&iwloc=A&hl=fr";
                    mWebView.loadUrl(url);
                }
            }
        }

    }

    public void oneClick(View v)
    {
        if(verifConnection(v)) {

            //Recuperation des données dans la vue
            latitude = est_latitude.getText().toString();
            longitude = est_longitude.getText().toString();

            //test pour ne pas remplir uniquement un "." dans les champs
            if (latitude.equals(".") || longitude.equals(".")) {
                Toast.makeText(this, "latitude ou longitude non valide", Toast.LENGTH_SHORT).show();

                //si tout ce passe bien on transforme les String en double
            } else {
                Double lat = Double.parseDouble(latitude);
                Double longi = Double.parseDouble(longitude);

                if (lat <= -90 || lat >= 90) {
                    Toast.makeText(this, "latitude ou longitude non valide", Toast.LENGTH_SHORT).show();
                } else {
                    MaTache tache = new MaTache(longitude, latitude, this);

                    try {
                        tache.execute().get();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }



    private WebView affichageImage()
    {
        // Déclare mWebView à activity_main (le layout)
        mWebView = (WebView) findViewById(R.id.activity_main_webview);
        //indique l'url vers laquelle on souhaite naviguer
        String url = "http://www.google.fr/search?hl=fr&tbm=isch&sa=1&q="+city;
        mWebView.loadUrl(url);

        //pour rester dans l'intent courant
        //mWebView.setWebViewClient(new WebViewClient());
        // Configure la webview pour l'utilisation du javascript
        //WebSettings webSettings = mWebView.getSettings();
        // Permet l'ouverture des fenêtres
        //webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        return mWebView;
    }


    private boolean verifConnection(View v)
    {
        ConnectivityManager conect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conect.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

class MaTache extends AsyncTask<String, Void, String> {
    private String lon = "";
    private String lat = "";
    private Context context;

    public MaTache(String lon, String lat, Context context) {
        this.lat = lat;
        this.lon = lon;
        this.context = context;
    }

    @Override
    public void onPreExecute() {
        super.onPreExecute();
        Log.i("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ", "je suis dans le onExecute");
    }

    @Override
    protected String doInBackground(String... strings) {


        try {
            URL url = new URL("https://api-adresse.data.gouv.fr/reverse/?lon=" + lon + "&lat=" + lat);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            String result = getStringFromInputStream(in);
            return result;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(String result) {

        JSONObject obj = null;
        //traitement du retour Json
        try {
            Log.i("QQQQQQQQQQQQQQQQQQQQQQQQQQQQQ", result);
            // On récupère le JSON complet
            obj = new JSONObject(result);

            if (obj != null) {
                // On récupère le tableau d'objets qui nous concernent
                JSONArray features = obj.getJSONArray("features");

                if (features != null) {
                    JSONObject zero = features.getJSONObject(0);
                    Log.i("zero", zero.toString());

                    if (zero.length() >= 1) {
                        JSONObject properties = zero.getJSONObject("properties");

                        if (properties != null) {
                            city = properties.getString("city");
                            //Log.i("city§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§", city.toString());
                            Toast.makeText(context, city.toString(), Toast.LENGTH_SHORT).show();
                            affichageImage();
                        }
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void onProgressUpdate(Integer... values) {

    }

    private String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();

    }
}
}










// http://maps.googleapis.com/maps/api/geocode/json?latlng="+lati+","+longi+"&sensor=false"
// "https://maps.googleapis.com/maps/api/geocode/json?latlng="+latLong
