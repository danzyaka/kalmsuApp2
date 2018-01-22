package com.example.danzy.kalmsuapp;

import android.app.AlertDialog;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.customtabs.CustomTabsIntent;
import android.content.Intent;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.methods.VKApiWall;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.api.model.VKScopes;
import com.vk.sdk.util.VKUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity

    implements NavigationView.OnNavigationItemSelectedListener {

    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        this.setTitle("Новости");
        getGivePost();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_news) {
            url = "";
            getGivePost();
        } else if (id == R.id.nav_rasp) {
            url = "http://www.it-institut.ru/SearchString/Index/14";

        } else if (id == R.id.nav_3d) {
            url = "http://www.kalmsu.ru/files/3d/";

        } else if (id == R.id.nav_calc) {
            url = "http://kalmsu.ru/files/calculator/index.html";

        } else if (id == R.id.nav_info) {
            url = "";
            getInfo();
        }
        getBrawse();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void getBrawse() {
        if  (!url.equals("")) {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
            customTabsIntent.launchUrl(this, Uri.parse(url));
        }
    }
    public void getInfo() {
        Intent intent = new Intent(this, InfoActivity.class);
        startActivity(intent);
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private String[] scope = new String[] {VKScope.WALL, VKScope.FRIENDS};

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                getGivePost();
            }
            @Override
            public void onError(VKError error) {
                getError(error.errorMessage);
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    public void getGivePost() {

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.vk.com/method/newsfeed.get?filters=post&count=100&source_ids=-143651669&start_time=1485037387" +
                "&photo_sizes=1&access_token=254cb221782ee11f645aff99c186a4967b476621ed05c63f75327ed9fa607b4e0601bb4fdc5252bf0125e&v=5.71";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        CoordinatorLayout llayot = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
                        ScrollView scroll = new ScrollView(getApplicationContext());
                        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        LinearLayout mainLnr = new LinearLayout(getApplicationContext());
                        mainLnr.setOrientation(LinearLayout.VERTICAL);
                        mainLnr.setGravity(Gravity.CENTER);
                        mainLnr.setBackgroundResource(R.color.vk_grey_color);
                        LinearLayout.LayoutParams mainLnrParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        mainLnrParams.setMargins(0,findViewById(R.id.AppBar).getHeight(),0,0);
                        LinearLayout.LayoutParams inLnrParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        inLnrParams.setMargins(20,20,20,0);

                        try {
                            JSONObject resp = response.getJSONObject("response");
                            JSONArray items = resp.getJSONArray("items");
                            for (int i=0;i<items.length();i++) {
                                JSONObject item = items.getJSONObject(i);
                                mainLnr.addView(getCreateElementList(item),inLnrParams);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        scroll.addView (mainLnr, mainLnrParams);
                        llayot.addView(scroll, scrollParams);
                        findViewById(R.id.progressBar3).setVisibility(View.GONE);

                        /*
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Response")
                                .setMessage(w);
                        AlertDialog alert = builder.create();
                        alert.show();*/
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        error.printStackTrace();
                    }
                });

// Access the RequestQueue through your singleton class.
        queue.add(jsObjRequest);

    }

    public void getError(String error) {
        TextView errtext = new TextView(getApplicationContext());
        ViewGroup.LayoutParams lParams = new ViewGroup.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ConstraintLayout llayot = (ConstraintLayout)findViewById(R.id.Constlay);
        llayot.removeAllViews();
        errtext.setText("Необходимо авторизоваться");
        llayot.addView(errtext, lParams);
    }

    public LinearLayout getCreateElementList(JSONObject item) {

        LinearLayout inLnr = new LinearLayout(getApplicationContext());

        try {
            TextView txt = new TextView(getApplicationContext());
            LinearLayout.LayoutParams txtParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            txtParams.setMargins(20,20,20,0);
            txt.setText(item.getString("text"));
            inLnr.addView(txt, txtParams);



            if (item.has("attachments")) {
                JSONArray attachs = item.getJSONArray("attachments");
                ArrayList<String> imgList = new ArrayList<String>(attachs.length());
                LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                for (int j = 0; j < attachs.length(); j++) {
                    JSONObject att = attachs.getJSONObject(j);
                    if (att.has("photo")) {
                        JSONObject photo = att.getJSONObject("photo");
                        JSONArray sizes = photo.getJSONArray("sizes");
                        JSONObject size = sizes.getJSONObject(sizes.length() - 1);
                        imgList.add(size.getString("src"));
                    }
                    if (att.has("video")) {
                        JSONObject video = att.getJSONObject("video");
                        String videoCover = video.getString("photo_320");
                        ImageView videoImg = new ImageView(getApplicationContext());
                    }
                }
                for (String imgURL:imgList) {
                    ImageView img = new ImageView(getApplicationContext());
                    getBtmThread getbtmthread = new getBtmThread();
                    getbtmthread.execute(imgURL);
                    try {
                        Bitmap btm = getbtmthread.get();
                        img.setImageBitmap(btm);
                    }
                    catch (InterruptedException e) {e.printStackTrace();}
                    catch (ExecutionException e) {e.printStackTrace();}
                    inLnr.addView(img, imgParams);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        inLnr.setOrientation(LinearLayout.VERTICAL);
        inLnr.setBackgroundResource(R.drawable.round_corners);
        return inLnr;
    }

    static class getBtmThread extends AsyncTask<String, String, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... param) {
            try {
                return getBitmapFromURL(param[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
        }

        private Bitmap getBitmapFromURL(String stringurl) {
            try {
                URL url = new URL(stringurl);
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setDoInput(true);
                http.connect();
                InputStream input = http.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
   /* */

}
