package kor.sookmyung.grad_project;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import org.w3c.dom.Text;

import java.io.File;
import java.io.InputStream;
import java.util.Date;

public class Fragment2 extends Fragment {
    private static final String TAG = "Fragment2";

    int weatherIndex = 0;


    Context context;
    OnTabItemSelectedListener listener;
    OnRequestListener requestListener;

    ImageView weatherIcon;
    TextView dateTextView;
    TextView locationTextView;
    TextView nowTempTextView;
    TextView maxTempTextView;



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.context = context;

        if (context instanceof OnTabItemSelectedListener) {
            listener = (OnTabItemSelectedListener) context;
        }

        if (context instanceof OnRequestListener) {
            requestListener = (OnRequestListener) context;
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();

        if (context != null) {
            context = null;
            listener = null;
            requestListener = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment2, container, false);

        initUI(rootView);

        // check current location
        if (requestListener != null) {
            requestListener.onRequest("getCurrentLocation");
        }

        //applyItem();
        return rootView;
    }

    private void initUI(ViewGroup rootView) {

        weatherIcon = rootView.findViewById(R.id.weatherIcon);
        dateTextView = rootView.findViewById(R.id.dateTextView);
        locationTextView = rootView.findViewById(R.id.locationTextView);
        nowTempTextView = rootView.findViewById(R.id.nowTempTextView);
        maxTempTextView = rootView.findViewById(R.id.maxTempTextView);
    }

    public void setAddress(String data) {
        locationTextView.setText(data);
    }

    public void setDateString(String dateString) {
        dateTextView.setText(dateString);
    }

    public void setNowTempTextView(String nowTempString){ nowTempTextView.setText(nowTempString); }

    public void setMaxTempTextView(String maxTempString){ maxTempTextView.setText(maxTempString); }



    /**
     * ① 맑음
     * ② 구름 조금
     * ③ 구름 많음
     * ④ 흐림
     * ⑤ 비
     * ⑥ 눈/비
     * ⑦ 눈
     *
     */
    public void setWeather(String data) {
        AppConstants.println("setWeather called : " + data);

        if (data != null) {
            if (data.equals("맑음")) {
                weatherIcon.setImageResource(R.drawable.weather_1);
                weatherIndex = 0;
            } else if (data.equals("구름 조금")) {
                weatherIcon.setImageResource(R.drawable.weather_2);
                weatherIndex = 1;
            } else if (data.equals("구름 많음")) {
                weatherIcon.setImageResource(R.drawable.weather_3);
                weatherIndex = 2;
            } else if (data.equals("흐림")) {
                weatherIcon.setImageResource(R.drawable.weather_4);
                weatherIndex = 3;
            } else if (data.equals("비")) {
                weatherIcon.setImageResource(R.drawable.weather_5);
                weatherIndex = 4;
            } else if (data.equals("눈/비")) {
                weatherIcon.setImageResource(R.drawable.weather_6);
                weatherIndex = 5;
            } else if (data.equals("눈")) {
                weatherIcon.setImageResource(R.drawable.weather_7);
                weatherIndex = 6;
            } else {
                Log.d("Fragment2", "Unknown weather string : " + data);
            }
        }
    }



}
