package kor.sookmyung.grad_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth; // 파이어베이스 인증
    private DatabaseReference mDatabaseRef; // 실시간 데이터베이스
    private EditText mEtNickname, mEtEmail, mEtPwd;  // 회원가입 입력 필드
    private Button mBtnRegister; // 회원가입 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("firebase-WeatherAndWear");

        mEtNickname = (EditText) findViewById(R.id.register_nickname);
        mEtEmail = (EditText) findViewById(R.id.register_email);
        mEtPwd = (EditText) findViewById(R.id.register_password);

        mBtnRegister = (Button) findViewById(R.id.register_button2);
        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //회원가입 처리 시작
                String strNickname = mEtNickname.getText().toString().trim();
                String strEmail = mEtEmail.getText().toString().trim();
                String strPwd = mEtPwd.getText().toString().trim();

                if (!strNickname.equals("") && !strEmail.equals("") && !strPwd.equals("")) {
                    // 모든 항목이 공백이 아닌 경우
                    // Firebase Auth 진행
                    mFirebaseAuth.createUserWithEmailAndPassword(strEmail,strPwd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                                UserInfo info = new UserInfo();
                                info.setIdToken(firebaseUser.getUid());
                                info.setNickname(strNickname);
                                info.setEmailId(firebaseUser.getEmail());
                                info.setPassword(strPwd);

                                // setValue : database 에 insert(삽입)
                                mDatabaseRef.child("UserInfo").child(firebaseUser.getUid()).setValue(info);
                                Toast.makeText(RegisterActivity.this,"회원가입에 성공하셨습니다.",Toast.LENGTH_SHORT).show();
                                finish();

                                // 가입이 이루어졌을 시 main
                                Intent intent = new Intent(RegisterActivity.this, loginActivity.class);
                                startActivity(intent);
                            }
                            else {
                                Toast.makeText(RegisterActivity.this,"회원가입에 실패하셨습니다.",Toast.LENGTH_SHORT).show();

                            }

                        }
                    });

                } else {
                    // 항목 중 하나라도 공백인 경우
                    Toast.makeText(RegisterActivity.this, "모든 항목을 입력하세요", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

}