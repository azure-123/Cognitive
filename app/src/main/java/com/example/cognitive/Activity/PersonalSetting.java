package com.example.cognitive.Activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.cognitive.R;
import com.lucasurbas.listitemview.ListItemView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

public class PersonalSetting extends AppCompatActivity {

    private ListItemView avatar;
    private ListItemView user_name;
    private ListItemView user_sex;
    private ListItemView user_birth;
    private SharedPreferences sp;
    private HashMap<String, String> stringHashMap;
    private String userphone;
    private String username;
    private String usersex;
    private String userbirth;
    private Button save_change;
    String TAG = PersonalSetting.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_setting);
        initView();
        stringHashMap = new HashMap<>();
        user_sex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeSexDialog changeSexDialog = new ChangeSexDialog(PersonalSetting.this,new ChangeSexDialog.DataBackListener() {
                    @Override
                    public void getData(String data) {
                        String result = data;
                        usersex = data;
                        user_sex.setSubtitle(result);
                    }
                });
                changeSexDialog.show();
            }
        });
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//添加默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        user_birth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeBirthDialog changeBirthDialog = new ChangeBirthDialog(PersonalSetting.this,new ChangeBirthDialog.DataBackListener() {
                    @Override
                    public void getData(String data) {
                        String result = data;
                        userbirth = data;
                        user_birth.setSubtitle(result);
                    }
                });
                changeBirthDialog.show();
            }
        });
        user_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeNameDialog changeNameDialog = new ChangeNameDialog(PersonalSetting.this,new ChangeNameDialog.DataBackListener() {
                    @Override
                    public void getData(String data) {
                        String result = data;
                        username = data;
                        user_name.setSubtitle(result);
                    }
                });
                changeNameDialog.show();
            }
        });
        save_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 存到SharedPreferences
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("USER_NAME",  username);
                editor.putString("USER_SEX", usersex);
                editor.putString("USER_BIRTH", userbirth);
                editor.commit();
                stringHashMap.put("user_phone", userphone);
                stringHashMap.put("user_name", username);
                stringHashMap.put("user_birth", userbirth);
                stringHashMap.put("user_sex", usersex);
                //上传到数据库
                new Thread(postRun).start();
            }
        });

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
    Runnable postRun = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            requestPost(stringHashMap);
        }
    };
    private void initView() {
        // 注册组件
        avatar=findViewById(R.id.avatar);
        user_name = findViewById(R.id.user_name);
        user_sex = findViewById(R.id.user_sex);
        user_birth = findViewById(R.id.user_birth);
        save_change = findViewById(R.id.save_change);
        sp = PersonalSetting.this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userphone = sp.getString("USER_PHONE", "");
        username = sp.getString("USER_NAME", "");
        usersex = sp.getString("USER_SEX", "");
        userbirth = sp.getString("USER_BIRTH", "");
        if(username!=""){
            user_name.setSubtitle(username);
            user_birth.setSubtitle(userbirth);
            user_sex.setSubtitle(usersex);
        } // 求求你不要退出登录，我真的不想写if else了

    }
    /**
     * post提交数据
     *
     * @param paramsMap
     */
    private void requestPost(HashMap<String, String> paramsMap) {
        int code = 20;
        try {
            String baseUrl = "http://101.132.97.43:8080/ServiceTest/servlet/UpdateInfoServlet";
            //合成参数
            StringBuilder tempParams = new StringBuilder();
            int pos = 0;
            for (String key : paramsMap.keySet()) {
                if (pos >0) {
                    tempParams.append("&");
                }
                tempParams.append(String.format("%s=%s", key, URLEncoder.encode(paramsMap.get(key), "utf-8")));
                pos++;
            }
            String params = tempParams.toString();
            Log.e(TAG,"params--post-->>"+params);
            // 请求的参数转换为byte数组
//            byte[] postData = params.getBytes();
            // 新建一个URL对象
            URL url = new URL(baseUrl);
            // 打开一个HttpURLConnection连接
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            // 设置连接超时时间
            urlConn.setConnectTimeout(5 * 1000);
            //设置从主机读取数据超时
            urlConn.setReadTimeout(5 * 1000);
            // Post请求必须设置允许输出 默认false
            urlConn.setDoOutput(true);
            //设置请求允许输入 默认是true
            urlConn.setDoInput(true);
            // Post请求不能使用缓存
            urlConn.setUseCaches(false);
            // 设置为Post请求
            urlConn.setRequestMethod("POST");
            //设置本次连接是否自动处理重定向
            urlConn.setInstanceFollowRedirects(true);
            //配置请求Content-Type
//            urlConn.setRequestProperty("Content-Type", "application/json");//post请求不能设置这个
            // 开始连接
            urlConn.connect();

            // 发送请求参数
            PrintWriter dos = new PrintWriter(urlConn.getOutputStream());
            dos.write(params);
            dos.flush();
            dos.close();
            // 判断请求是否成功
            if (urlConn.getResponseCode() == 200) {
                // 获取返回的数据
                String result = streamToString(urlConn.getInputStream());
                Log.e(TAG, "Post方式请求成功，result--->" + result);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject != null) {
                        code = jsonObject.optInt("code");
                    }
                    switch (code){
                        case -1 : // 上传失败
                            Looper.prepare();
                            Toast.makeText(PersonalSetting.this,"信息修改失败！", Toast.LENGTH_LONG).show();
                            Looper.loop();
                            break;
                        case 200: // 上传成功
                            Looper.prepare();
                            Toast.makeText(PersonalSetting.this,"信息修改成功！", Toast.LENGTH_LONG).show();
                            Looper.loop();
                            break;
                        default:
                            //Toast.makeText(RegisterActivity.this,"用户名或密码错误，请重新登录", Toast.LENGTH_LONG).show();
                            break;
                    }
                }catch (JSONException e)
                {
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "Post方式请求失败");
            }
            // 关闭连接
            urlConn.disconnect();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
    /**
     * 将输入流转换成字符串
     *
     * @param is 从网络获取的输入流
     * @return
     */
    public String streamToString(InputStream is) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            baos.close();
            is.close();
            byte[] byteArray = baos.toByteArray();
            return new String(byteArray);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }
}