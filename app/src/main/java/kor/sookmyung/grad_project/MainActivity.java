package kor.sookmyung.grad_project;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;



public class MainActivity extends AppCompatActivity {
    // 로그아웃 기능과 탈퇴 기능을 위한 변수 설정
    private FirebaseAuth mFirebaseAuth;

    // 툴바를 위한 변수 설정
    private ImageView ivMenu;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;

    //날씨 api
    private String[] weather ;
    private String x = "", y = "", address = "";
    private double latitude, longitude;
    private String date ="", time = "";
    private GpsTracker gpsTracker;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 네비게이션 뷰 정의
        NavigationView navigationView = findViewById(R.id.navigationView);

        // 네비게이션 메뉴 헤더 동적 추가(사용자 정보 텍스트로 받아오는 부분)
        LinearLayout ll_navigation_container = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.navigation_item,null);
        ll_navigation_container.setBackground(getResources().getDrawable(R.color.header));
        ll_navigation_container.setPadding(30,70,30,50);
        ll_navigation_container.setOrientation(LinearLayout.VERTICAL);
        ll_navigation_container.setGravity(Gravity.BOTTOM);
        ll_navigation_container.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        ImageView iv_userPicture = new ImageView(this);
        iv_userPicture.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_account_circle_24));

        final TextView tv_userNickname = new TextView(this);
        tv_userNickname.setTextColor(getResources().getColor(R.color.white));
        tv_userNickname.setTextSize(17);

        final TextView tv_userEmail = new TextView(this);
        tv_userEmail.setTextColor(getResources().getColor(R.color.white));
        tv_userEmail.setTextSize(14);


        // 이메일과 닉네임 가져오기
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = firebaseUser.getUid();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.getReference("firebase-WeatherAndWear").child("UserInfo").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.getKey().equals("nickname")) {
                        String userNickname = dataSnapshot.getValue().toString();
                        tv_userNickname.setText(userNickname + " 님");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        tv_userEmail.setText(firebaseUser.getEmail());

        // ll_navigation_container 에 만든 요소들을 담음
        ll_navigation_container.addView(iv_userPicture);
        ll_navigation_container.addView(tv_userNickname);
        ll_navigation_container.addView(tv_userEmail);

        // activity_Main.xml  navigation view 안에 app:headerLayout="@layout/navi_header"
        navigationView.addHeaderView(ll_navigation_container);

        // 메뉴 이미지 눌렀을 때 메뉴가 나오는 부분
        ivMenu = findViewById(R.id.iv_menu);
        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbar);

        // 툴바 생성
        setSupportActionBar(toolbar);

        ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // 네비게이션 메뉴 눌렀을 때 동작 하는 부분
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                mFirebaseAuth = FirebaseAuth.getInstance();
                int id = item.getItemId();

                if (id == R.id.menu_first) { // My LookBook 기능 추가 필요
                    Intent intent = new Intent(MainActivity.this, MyLookBook.class);
                    startActivity(intent);
                    item.setEnabled(false);
                    //Toast.makeText(MainActivity.this, "첫번째 메뉴", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.menu_logout) { // 로그아웃 기능
                    Intent intent = new Intent(MainActivity.this, loginActivity.class);
                    mFirebaseAuth.signOut();
                    // 현재 액티비티를 종료 -> mainActivity 종료
                    finish();
                    startActivity(intent);
                } else if (id == R.id.menu_withdraw) { // 탈퇴하기 기능
                    Intent intent = new Intent(MainActivity.this, loginActivity.class);
                    mFirebaseAuth.getCurrentUser().delete();
                    // 현재 액티비를 종료 -> mainActivity 종료
                    finish();
                    startActivity(intent);
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });


        //이미지 불러오기
        ImageView imgView0 = findViewById(R.id.img_0);
        ImageView imgView1=findViewById(R.id.img_1);
        ImageView imgView2=findViewById(R.id.img_2);
        ImageView imgView3=findViewById(R.id.img_3);

        ImageView imgView4=findViewById(R.id.img_4);
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://fb-ww-bucket/");
        StorageReference storageReference = storage.getReference();
        storageReference.child("20220223_0.jpg").getDownloadUrl()
            .addOnSuccessListener(new OnSuccessListener<Uri>(){
                @Override
                public void onSuccess(Uri uri){
                    Glide.with(getApplicationContext())
                            .load(uri)
                            .into(imgView0);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "이미지 불러오기 실패", Toast.LENGTH_SHORT).show();
                }
            });
        storageReference.child("20220223_1.jpg").getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>(){
                    @Override
                    public void onSuccess(Uri uri){
                        Glide.with(getApplicationContext())
                                .load(uri)
                                .into(imgView1);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "이미지 불러오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
        storageReference.child("20220223_2.jpg").getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>(){
                    @Override
                    public void onSuccess(Uri uri){
                        Glide.with(getApplicationContext())
                                .load(uri)
                                .into(imgView2);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "이미지 불러오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
        storageReference.child("20220223_3.jpg").getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>(){
                    @Override
                    public void onSuccess(Uri uri){
                        Glide.with(getApplicationContext())
                                .load(uri)
                                .into(imgView3);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "이미지 불러오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
        storageReference.child("20220223_4.jpg").getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>(){
                    @Override
                    public void onSuccess(Uri uri){
                        Glide.with(getApplicationContext())
                                .load(uri)
                                .into(imgView4);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "이미지 불러오기 실패", Toast.LENGTH_SHORT).show();
            }
        });

        //날씨
        if(!checkLocationServicesStatus()){
            showDialogForLocationServiceSetting();
        }
        else{
            checkRunTimePermission();
        }
        final TextView textView_date = (TextView) findViewById(R.id.dateTextView);
        final TextView textView_address = (TextView) findViewById(R.id.locationTextView);
        Button ShowLocationButton = (Button)findViewById(R.id.refreshButton);
        ShowLocationButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                gpsTracker = new GpsTracker(MainActivity.this);
                latitude = gpsTracker.getLatitude();
                longitude = gpsTracker.getLongitude();

                String address = getCurrentAddress(latitude, longitude);
                String[] local = address.split(" ");
                if(address!=null){
                    String localName = local[1]; //~~구 받아오기
                    readExcel(localName);//행정시 이름으로 격자값구하기
                }

                WeatherData wd = new WeatherData();

                //date, time 값넣기
                Date mDate = new Date();
                SimpleDateFormat mFormat = new SimpleDateFormat("yyyyMMdd");
                date = mFormat.format(mDate);
                SimpleDateFormat mFormat2 = new SimpleDateFormat("HH00");
                time = mFormat2.format(mDate);
                x = Integer.toString((int)Math.round(latitude));
                y = Integer.toString((int)Math.round(longitude));

                new Thread(()->{
                    try {
                        weather = wd.lookUpWeather(date, time, x, y).clone();
                        if(weather==null)
                            Toast.makeText(MainActivity.this, "야 널이다", Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }).start();

                TextView weatherIcon = (TextView) findViewById(R.id.weatherIcon);
                TextView nowTemp = (TextView) findViewById(R.id.nowTempTextView);
                TextView minMaxTemp = (TextView)findViewById(R.id.minMaxTempTextView);
                if(weather!=null){
                    if(weather[0].equals("0")){  // 비/눈 안옴
                        weatherIcon.setText(weather[1]);
                    }else { // 비/눈 온다
                        weatherIcon.setText(weather[0]);
                    }
                    nowTemp.setText(weather[2]);
                    minMaxTemp.setText(weather[3]+"/"+weather[4]);
                }else{
                    ;
                }



                textView_address.setText(address);
                SimpleDateFormat month = new SimpleDateFormat("M");
                SimpleDateFormat day =new SimpleDateFormat("dd");
                textView_date.setText(month.format(mDate)+"월"+day.format(mDate)+"일");

            }
        });



    } /// onCreate


    // 메뉴가 열려 있을 때 백버튼 메인으로 돌아간다. 메인화면에서 백버튼 2회 눌렀을 때 종료
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    @Override
    public void onBackPressed() {
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);

        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // 아래 코드 추가
            long tempTime = System.currentTimeMillis();
            long intervalTime = tempTime - backPressedTime;

            if(0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
                finish();
            } else {
                backPressedTime = tempTime;
                Toast.makeText(getApplicationContext(),"한번 더 누르면 종료합니다",Toast.LENGTH_SHORT).show();
            }
        }

    }

    //////////////////////////////////////////////////////
    public void readExcel(String localName){
        try{
            InputStream is = getBaseContext().getResources().getAssets().open("local_name.xls");
            Workbook wb = Workbook.getWorkbook(is);

            if (wb != null) {
                Sheet sheet = wb.getSheet(0);   // 시트 불러오기
                if (sheet != null) {
                    int colTotal = sheet.getColumns();    // 전체 컬럼
                    int rowIndexStart = 1;                  // row 인덱스 시작
                    int rowTotal = sheet.getColumn(colTotal - 1).length;

                    for (int row = rowIndexStart; row < rowTotal; row++) {
                        String contents = sheet.getCell(0, row).getContents();
                        if (contents.contains(localName)) {
                            x = sheet.getCell(1, row).getContents();
                            y = sheet.getCell(2, row).getContents();
                            row = rowTotal;
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.i("READ_EXCEL1", e.getMessage());
            e.printStackTrace();
        } catch (BiffException e) {
            Log.i("READ_EXCEL1", e.getMessage());
            e.printStackTrace();
        }
        Log.i("격자값", "x = " + x + "  y = " + y);

    }
    //날씨 위치
    //ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if (check_result) {

                //위치 값을 가져올 수 있음
                ;
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                } else {

                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음



        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }

    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }



        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }

    //여기서부터는 GPS 활성화를 위한 메소드
    ActivityResultLauncher<Intent> startActivityResult= registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode()==RESULT_OK){
                        //사용자가 gps 활성시켰는지 검사
                        if(checkLocationServicesStatus()){
                            if(checkLocationServicesStatus()){
                                Log.d("@@@","onActivityResult : GPS활성화 되어있음");
                                checkRunTimePermission();
                                return;
                            }
                        }
                    }
                }
            }
    );
    private void showDialogForLocationServiceSetting(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                            +"위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent callGPSSettingIntent
                        = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityResult.launch(callGPSSettingIntent);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.create().show();

    }
    public boolean checkLocationServicesStatus(){
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}