package com.example.gamebaucua;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    private RewardedAd rewardedAd;
    public static final String TAG = MainActivity.class.getSimpleName();
    Button buttonAds,btnBatDau;
    GridView gridView;
    Custom_BanCoActivity adapter;
    Integer[] dsHinh ={R.drawable.nai, R.drawable.bau, R.drawable.ga, R.drawable.ca, R.drawable.cua, R.drawable.tom};
    AnimationDrawable cdXiNgau1, cdXiNgau2, cdXiNgau3;
    ImageView hinhXiNgau1, hinhXiNgau2, hinhXiNgau3;
    Random randomXiNgau;
    int giaTriXiNgau1, giaTriXiNgau2, giaTriXiNgau3;
    public static Integer[] gtDatCuoc = new Integer[6];
    int tongtiencu, tongtienmoi;
    TextView tvTien, tvThoiGian;
    Timer timer = new Timer();
    Handler handler;
    int tienThuong, kiemtra, id_amthanh;
    SharedPreferences luuTru;
    SoundPool amThanhXiNgau = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
    MediaPlayer nhacnen = new MediaPlayer();
    CheckBox ktAmThanh;
    CountDownTimer demthoigian;
    //Su dung hàm callback de goi lai nhiều lần khi người dùng lắc xí ngầu
    Handler.Callback  callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            // Gọi hàm ramdom dể xách định hình của 3 cục xí ngầu
            RamdomXiNgau1();
            RandomXiNgau2();
            RandomXiNgau3();
            //Lặp lại 6 lần tương ứng với chiều dài của mảng gtDatCuoc
            for (int i = 0; i < gtDatCuoc.length; i++) {
                if (gtDatCuoc[i] != 0) {
                    //So sanh gia tri dặt cược với 3 cục xí ngầu
                    if (i == giaTriXiNgau1) {
                        tienThuong += gtDatCuoc[i];
                    }
                    if (i == giaTriXiNgau2) {
                        tienThuong += gtDatCuoc[i];
                    }
                    if (i == giaTriXiNgau3) {
                        tienThuong += gtDatCuoc[i];
                    }
                    if (i != giaTriXiNgau1 && i != giaTriXiNgau2 && i != giaTriXiNgau3) {
                        tienThuong -= gtDatCuoc[i];
                    }
                }
            }
                    if(tienThuong > 0){
                        Toast.makeText(getApplicationContext(), "Quá dữ bạn trúng được " + tienThuong, Toast.LENGTH_SHORT).show();
                    }else if (tienThuong == 0) {
                        Toast.makeText(getApplicationContext(), "Hên quá mém chết ! " , Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "Ôi xui quá mất " , Toast.LENGTH_SHORT).show();
                    }

                    LuuDuLieuNguoiDung(tienThuong);
                    tvTien.setText(String.valueOf(tongtienmoi));
                    btnBatDau.setEnabled(true);
                    return false;


        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hinhXiNgau1 = (ImageView) findViewById(R.id.xingau1);
        hinhXiNgau2 = (ImageView) findViewById(R.id.xingau2);
        hinhXiNgau3 = (ImageView) findViewById(R.id.xingau3);
        tvTien = (TextView) findViewById(R.id.tvTien);
        ktAmThanh = (CheckBox) findViewById(R.id.checkBox1);
        gridView = (GridView) findViewById(R.id.gvBanCo);
        tvThoiGian = (TextView) findViewById(R.id.tvThoiGian);
        buttonAds = (Button) findViewById(R.id.btnthemXen);
        btnBatDau = (Button) findViewById(R.id.btnLacXiNgau);

        //Đặt các giá trị cọc vào tưng hình trong DS hình
        adapter = new Custom_BanCoActivity(this, R.layout.activity_custom__ban_co2, dsHinh);
        gridView.setAdapter(adapter); //đưa danh sách vào girdView

        luuTru = getSharedPreferences("luutruthongtin", Context.MODE_PRIVATE); // tạo biến lưu trữ
        tongtiencu = luuTru.getInt("TongTien", 1000); // đặt giá trị tiền ban đầu là 1000
        tvTien.setText(String.valueOf(tongtiencu));

        id_amthanh = amThanhXiNgau.load(this, R.raw.lac, 1);
        nhacnen = MediaPlayer.create(this, R.raw.nhacnenbaucua);

        nhacnen.start();

        ktAmThanh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean kt) {
            if(kt){
                // nhấp vào  kt = true thì dừng nhạc nền
                nhacnen.stop();
            }else{
                try {
                    nhacnen.prepare();
                    nhacnen.start(); // ngược lại thì cho bắt đầu lại nhạc khi nhâp vào lần tiếp theo
                } catch (IllegalStateException e){
                    e.printStackTrace();
                } catch (IOException e){
                    //Auto-generated catch block
                    e.printStackTrace();
                }


            }


            }
        });
        demthoigian = new CountDownTimer(180000,1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                long milis = millisUntilFinished;
                long gio = TimeUnit.MILLISECONDS.toHours(milis);
                long phut = TimeUnit.MILLISECONDS.toMinutes(milis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milis));
                long giay = TimeUnit.MILLISECONDS.toSeconds(milis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milis));
                 // định dạng giờ
                String giophutgiay = String.format("%02d:%02d:%02d", gio,phut,giay);
                tvThoiGian.setText(giophutgiay);
            }

            @Override
            public void onFinish() {

            LuuDuLieuNguoiDung(1000);
                tvTien.setText(String.valueOf(tongtienmoi));
                //reset time
                demthoigian.cancel();
                demthoigian.start();
            }
        };

        demthoigian.start();
        handler = new Handler(callback);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

            //Tạo quảng cáo đầu tiên khi load app
        rewardedAd = new RewardedAd(this,
                "ca-app-pub-3940256099942544/5224354917");
        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                Log.e("TAG", "Load ads thanh cong");
            }

            @Override
            public void onRewardedAdFailedToLoad(LoadAdError adError) {
                Log.e("TAG", "Load ads fail");
            }
        };
        rewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);


        //Lắng nghe khi nhấp vào button thêm xèng (xem ads để được 5000 xèng )
    buttonAds.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (rewardedAd.isLoaded()) {
                Activity activityContext = MainActivity.this;

                RewardedAdCallback adCallback = new RewardedAdCallback() {
                    private RewardedAd rewardedAd;

                    @Override
                    public void onRewardedAdOpened() {

                        // Ad opened.
                        nhacnen.pause();
                    }

                    @Override
                    public void onRewardedAdClosed() {

                        // Ad closed.
                        // tải ADS tiếp theo ngay khi quảng cáo đóng
                        onRewardedAdClosedNext();
                        nhacnen.start();

                    }

                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem reward) {
                        nhacnen.start();
                        LuuDuLieuNguoiDung(217300);
                        tvTien.setText(String.valueOf(tongtienmoi));
                        Log.e("TAG", "xem adsxog");
                    }

                    @Override
                    public void onRewardedAdFailedToShow(AdError adError) {
                        adError.getCode();

                    }
                };
                rewardedAd.show(activityContext, adCallback);
            } else {
                Log.d("TAG", "The rewarded ad wasn't loaded yet.");
                Toast.makeText(getApplicationContext(), "Đợi 1 tí ads đang tải nè !!! ", Toast.LENGTH_SHORT).show();
            }
        }
    });


    }

    // Tải tiếp quảng cáo tiếp theo
    public RewardedAd createAndLoadRewardedAd() {
        RewardedAd rewardedAd = new RewardedAd(this,
                "ca-app-pub-3940256099942544/5224354917");
        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                // Ad successfully loaded.
                Log.e("TAG", "Load ads2 thanh cong");
            }

            @Override
            public void onRewardedAdFailedToLoad(LoadAdError adError) {
                // Ad failed to load.
                Log.e("TAG", "fail ads 2");
            }
        };
        rewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);
        return rewardedAd;
    }

    public void onRewardedAdClosedNext() {
        this.rewardedAd = createAndLoadRewardedAd();
    }





//Luu lai du lieu tien cua nguoi dung tránh bị mất
    private void LuuDuLieuNguoiDung(int tienthuong){
        SharedPreferences.Editor edit = luuTru.edit();
        tongtiencu = luuTru.getInt("TongTien", 1000);
        tongtienmoi = tongtiencu + tienthuong;
        edit.putInt("TongTien", tongtienmoi);
        tongtiencu = tongtienmoi;
        edit.apply();
        // lua trang thai

    }

    public void LacXiNgau(View v) {
        //Đặt hình xí ngầu tự động lắc
        hinhXiNgau1.setImageResource(R.drawable.hinhdongxingau);
        hinhXiNgau2.setImageResource(R.drawable.hinhdongxingau);
        hinhXiNgau3.setImageResource(R.drawable.hinhdongxingau);

        //tạo hình động
        cdXiNgau1 = (AnimationDrawable) hinhXiNgau1.getDrawable();
        cdXiNgau2 = (AnimationDrawable) hinhXiNgau2.getDrawable();
        cdXiNgau3 = (AnimationDrawable) hinhXiNgau3.getDrawable();
        kiemtra = 0;

        for (int i = 0; i < gtDatCuoc.length; i++) {
            kiemtra += gtDatCuoc[i];
        }
        tongtiencu = luuTru.getInt("TongTien", 1000);
        //Kiểm tra xem người dùng có đặt cược chưa
        if(kiemtra == 0){
            Toast.makeText(getApplicationContext(), "Bạn vui lòng đặt cược ! ", Toast.LENGTH_SHORT).show();
        }else{
            if(kiemtra > tongtiencu ){
                Toast.makeText(getApplicationContext(), "Bạn không đủ tiền để đặt cược ! ", Toast.LENGTH_SHORT).show();
            }else{
                //bắt đầu âm thanh khi lắc
                amThanhXiNgau.play(id_amthanh, 1.0f, 1.0f, 1, 0, 1.0f);
                cdXiNgau1.start();
                cdXiNgau2.start();
                cdXiNgau3.start();
                // bắt đâu hiệu ứng của xí ngầu
                tienThuong = 0;
                //Lập lịch bắt đầu lắc xi ngâù trong 1s
                timer.schedule(new LacXiNgau(), 1000);
                btnBatDau.setEnabled(false);  // không cho click vào lắc liên tục

            }

        }



    }
    class LacXiNgau extends TimerTask {

        @Override
        public void run() {
            handler.sendEmptyMessage(0);
        }
    }

    //3 Hàm ramdom  ngẫu nhiên 6 giá trị từ 0-6 tương ứng với mỗi hình
    // trong danh sách hình bầu cua.
    private void RamdomXiNgau1(){
    randomXiNgau = new Random();
    int rd = randomXiNgau.nextInt(6);
    switch (rd){
        case 0:
            hinhXiNgau1.setImageResource(dsHinh[0]);
            giaTriXiNgau1 = rd;
            break;
        case 1:
            hinhXiNgau1.setImageResource(dsHinh[1]);
            giaTriXiNgau1 = rd;
            break;

        case 2:
            hinhXiNgau1.setImageResource(dsHinh[2]);
            giaTriXiNgau1 = rd;
            break;

        case 3:
            hinhXiNgau1.setImageResource(dsHinh[3]);
            giaTriXiNgau1 = rd;
            break;

        case 4:
            hinhXiNgau1.setImageResource(dsHinh[4]);
            giaTriXiNgau1 = rd;
            break;

        case 5:
            hinhXiNgau1.setImageResource(dsHinh[5]);
            giaTriXiNgau1 = rd;
            break;

    }
    }

    private void RandomXiNgau2() {

        randomXiNgau = new Random();
        int rd = randomXiNgau.nextInt(6);
        switch (rd) {

            case 0:
                hinhXiNgau2.setImageResource(dsHinh[0]);
                giaTriXiNgau2 = rd;
                break;

            case 1:
                hinhXiNgau2.setImageResource(dsHinh[1]);
                giaTriXiNgau2 = rd;
                break;

            case 2:
                hinhXiNgau2.setImageResource(dsHinh[2]);
                giaTriXiNgau2 = rd;
                break;

            case 3:
                hinhXiNgau2.setImageResource(dsHinh[3]);
                giaTriXiNgau2 = rd;
                break;

            case 4:
                hinhXiNgau2.setImageResource(dsHinh[4]);
                giaTriXiNgau2 = rd;
                break;

            case 5:
                hinhXiNgau2.setImageResource(dsHinh[5]);
                giaTriXiNgau2 = rd;
                break;

        }
    }

    private void RandomXiNgau3() {

        randomXiNgau = new Random();
        int rd = randomXiNgau.nextInt(6);
        switch (rd) {

            case 0:
                hinhXiNgau3.setImageResource(dsHinh[0]);
                giaTriXiNgau3 = rd;
                break;

            case 1:
                hinhXiNgau3.setImageResource(dsHinh[1]);
                giaTriXiNgau3 = rd;
                break;

            case 2:
                hinhXiNgau3.setImageResource(dsHinh[2]);
                giaTriXiNgau3 = rd;
                break;

            case 3:
                hinhXiNgau3.setImageResource(dsHinh[3]);
                giaTriXiNgau3 = rd;
                break;

            case 4:
                hinhXiNgau3.setImageResource(dsHinh[4]);
                giaTriXiNgau3 = rd;
                break;

            case 5:
                hinhXiNgau3.setImageResource(dsHinh[5]);
                giaTriXiNgau3 = rd;
                break;

        }
    }

//dừng nhạc và phát lại khi tắt mở app
    @Override
    public void onPause() {
        super.onPause();
        nhacnen.pause();

    }
    @Override
    public void onResume() {
        super.onResume();
        nhacnen.start();

    }

}
