package kor.sookmyung.grad_project;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class StorageActivity {
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    StorageReference mountainsRef = storageRef.child("mountains.jpg");
    StorageReference mountainImageRef = storageRef.child("images/mountains.jpg");

    InputStream stream;

    {
        try {
            stream = new FileInputStream(new File("/Users/soo/Downloads/image1.jpg"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
