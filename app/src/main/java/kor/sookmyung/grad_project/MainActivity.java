package kor.sookmyung.grad_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.DrawableWrapper;
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

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

        ivMenu = findViewById(R.id.iv_menu);
        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setChecked(true);
                drawerLayout.closeDrawers();

                mFirebaseAuth = FirebaseAuth.getInstance();
                int id = item.getItemId();

                if (id == R.id.menu_first) {
                    Toast.makeText(MainActivity.this,"첫번째 메뉴", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.menu_logout) {
                    Intent intent = new Intent(MainActivity.this, loginActivity.class);
                    mFirebaseAuth.signOut();
                    // 현재 액티비티를 종료 -> mainActivity 종료
                    finish();
                    startActivity(intent);
                } else if (id == R.id.menu_withdraw) {
                    Intent intent = new Intent(MainActivity.this, loginActivity.class);
                    mFirebaseAuth.getCurrentUser().delete();
                    // 현재 액티비를 종료 -> mainActivity 종료
                    finish();
                    startActivity(intent);
                }

                return false;
            }
        });




        // mFirebaseAuth = firebaseAuth.getInstance();

        // drawer navigation 추가

        // 로그아웃 기능 -> 메뉴 onClick()시 불러 올 코드
        // Intent intent = new Intent(MainActivity.this, loginActivity.class);
        // mFirebaseAuth.signOut();
        // startActivity(intent);

        // 탈퇴하기 기능 -> 메뉴 onClick()시 불러 올 코드
        // Intent intent = new Intent(MainActivity.this, loginActivity.class);
        // mFirebaseAuth.getCurrentUser().delete();
        // startActivity(intent);

    }

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