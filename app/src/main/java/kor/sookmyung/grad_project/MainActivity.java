package kor.sookmyung.grad_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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


public class MainActivity extends AppCompatActivity {
    // 로그아웃 기능과 탈퇴 기능을 위한 변수 설정
    private FirebaseAuth mFirebaseAuth;

    // 툴바를 위한 변수 설정
    private ImageView ivMenu;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;

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

        //StorageReference mStorageRef;
        //mStorageRef = FirebaseStorage.getInstance().getReference();



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
//        ImageView imgView1=findViewById(R.id.img_1);
//        FirebaseStorage storage = FirebaseStorage.getInstance("gs://fir-weatherandwear.appspot.com/");
//        StorageReference storageReference = storage.getReference();
//        storageReference.child("musinsa_20220214/image0.jpg").getDownloadUrl()
//                .addOnSuccessListener(new OnSuccessListener<Uri>(){
//                    @Override
//                    public void onSuccess(Uri uri){
//                        Glide.with(getApplicationContext())
//                                .load(uri)
//                                .into(imgView1);
//                    }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(getApplicationContext(), "이미지 불러오기 실패", Toast.LENGTH_SHORT).show();;
//            }
//        });
        ImageView imgView0 = findViewById(R.id.img_0);
        ImageView imgView1=findViewById(R.id.img_1);
        ImageView imgView2=findViewById(R.id.img_2);
        ImageView imgView3=findViewById(R.id.img_3);

        ImageView imgView4=findViewById(R.id.img_4);
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://fir-weatherandwear.appspot.com/");
        StorageReference storageReference = storage.getReference();
        storageReference.child("musinsa_20220214/image0.jpg").getDownloadUrl()
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
            Toast.makeText(getApplicationContext(), "이미지 불러오기 실패", Toast.LENGTH_SHORT).show();;
        }
    });
        storageReference.child("musinsa_20220214/image1.jpg").getDownloadUrl()
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
                Toast.makeText(getApplicationContext(), "이미지 불러오기 실패", Toast.LENGTH_SHORT).show();;
            }
        });
        storageReference.child("musinsa_20220214/image2.jpg").getDownloadUrl()
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
                Toast.makeText(getApplicationContext(), "이미지 불러오기 실패", Toast.LENGTH_SHORT).show();;
            }
        });
        storageReference.child("musinsa_20220214/image3.jpg").getDownloadUrl()
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
                Toast.makeText(getApplicationContext(), "이미지 불러오기 실패", Toast.LENGTH_SHORT).show();;
            }
        });
        storageReference.child("musinsa_20220214/image4.jpg").getDownloadUrl()
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
                Toast.makeText(getApplicationContext(), "이미지 불러오기 실패", Toast.LENGTH_SHORT).show();;
            }
        });



    }

    // 메뉴 중 하나 들어갔다가 뒤로 가기 눌렀을 때 동작하는 부분
    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: { // toolbar 의 back 키 눌렀을 때 동작
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }*/

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



}