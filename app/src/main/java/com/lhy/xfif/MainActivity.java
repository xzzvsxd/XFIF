package com.lhy.xfif;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    @SuppressLint("SdCardPath") public static String ScorePath = "/sdcard/Android/.xfif_config.lhy", PassPath = "/sdcard/Android/.xfif_autopass";
    public TextView scoreTipLeftTextView, scoreTipTextView;
    private EditText scoreEditText;
    public CheckBox checkBox;
    private Button okButton;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"SdCardPath", "SetTextI18n", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 申请权限
        initPermissions();
        // 初始化视图
        initView();

        File mmFile = new File(ScorePath);
        if (mmFile.exists() && ReadText(ScorePath).contains("~")) {
            scoreTipTextView.setText(ReadText(ScorePath));
        } else {
            WriteText(ScorePath, "80~95");
            scoreTipTextView.setText("80~95");
        }
        modifyEnable();

        // 按钮点击自动过关
        mmFile = new File(PassPath);
        checkBox.setChecked(mmFile.exists());
        File finalMmFile = mmFile;
        checkBox.setOnClickListener(view -> {
            if (checkBox.isChecked()) {
                try {
                    finalMmFile.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else
                finalMmFile.delete();
        });

        // 按钮点击修改分数
        okButton.setOnClickListener(view -> {
            String s = scoreEditText.getText().toString();
            if (!TextUtils.isEmpty(s)) {
                if (s.contains("～")) {
                    s = s.replace("～", "~");
                    scoreEditText.setText(s);
                }
                if (!s.contains("~")) {
                    scoreEditText.setText("80~95");
                    Toast.makeText(MainActivity.this, "成绩输入不是一个范围,请重新输入!", Toast.LENGTH_SHORT).show();
                }
                try {
                    String[] Range = s.split("~");
                    if (Integer.parseInt(Range[0]) < 0 || Integer.parseInt(Range[1]) > 100) {
                        // scoreEditText.setText("80");
                        Toast.makeText(MainActivity.this, "成绩不能大于100或小于0,请重新输入!", Toast.LENGTH_SHORT).show();
                    }
                    // 保存文件
                    WriteText(ScorePath, s);
                    // 修改页面显示
                    scoreTipTextView.setText(s);
                    // 重新调用函数
                    modifyEnable();
                } catch (Exception e) {
                    scoreEditText.setText("80~95");
                    // 重新调用函数
                    modifyEnable();
                }
            } else {
                Toast.makeText(MainActivity.this, "不能为空", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O) public static String ReadText(String strFilePath) {
        StringBuilder content = new StringBuilder(); // 文件内容字符串
        // 打开文件
        File file = new File(strFilePath);
        // 如果path是传递过来的参数，可以做一个非目录的判断
        if (!file.isDirectory()) {
            try {
                InputStream instream = Files.newInputStream(file.toPath());
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line;
                // 分行读取
                while ((line = buffreader.readLine()) != null)
                    content.append(line);
                instream.close();
            } catch (IOException ignored) { }
        }
        return content.toString();
    }

    public static void WriteText(String path, String txt) {
        byte[] sourceByte = txt.getBytes();
        if (null != sourceByte) {
            try {
                File file = new File(path);
                if (!file.exists()) {
                    File dir = new File(Objects.requireNonNull(file.getParent()));
                    dir.mkdirs();
                    file.createNewFile();
                }
                FileOutputStream outStream = new FileOutputStream(file);
                outStream.write(sourceByte);
                outStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 申请读写内存卡权限
     */
    @SuppressLint("CheckResult")
    private void initPermissions() {
        if (Build.VERSION.SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
                // return;
            }
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // 没有授权，编写申请权限代码
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // 没有授权，编写申请权限代码
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
    }

    /**
     * 修改状态
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"SetTextI18n", "SdCardPath"})
    private void modifyEnable() {
        scoreTipLeftTextView.setText("当前分数范围已被修改为：");
        String[] RangeText = ReadText(ScorePath).split("~");
        scoreTipTextView.setText(RangeText[0]+"~"+RangeText[1]);
        scoreEditText.setEnabled(true);
        okButton.setEnabled(true);
    }

    public void dialog(String title, String text) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        dialog.setTitle(title);
        dialog.setMessage(text);
        dialog.show();
    }

    /**
     * 初始化视图
     */
    @SuppressLint("SetTextI18n")
    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        scoreEditText = findViewById(R.id.score_text);
        scoreTipTextView = findViewById(R.id.score_tip);
        okButton = findViewById(R.id.ok_btn);
        scoreTipLeftTextView = findViewById(R.id.score_tip_left);
        // 是否自动过关
        checkBox = findViewById(R.id.checkBox);
        setSupportActionBar(toolbar);
    }

    @SuppressLint("NonConstantResourceId") @Override public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.toolbar_action_update) {
            dialog("更新日志", getString(R.string.update_log));
            return true;
        } else if (itemId == R.id.toolbar_action_github) {
            Uri uri = Uri.parse("https://github.com/1595901624/XFIF");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.toolbar_action_about) {
            dialog("关于", getString(R.string.about));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}