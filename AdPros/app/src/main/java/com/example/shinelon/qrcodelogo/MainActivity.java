package com.example.shinelon.qrcodelogo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.content)
    EditText content;
    @BindView(R.id.width)
    EditText width;
    @BindView(R.id.height)
    EditText height;
    @BindView(R.id.rongcuo)
    RadioGroup rongcuo;
    @BindView(R.id.code)
    ImageView code;
    @BindView(R.id.create)
    Button create;
    @BindView(R.id.logoed)
    CheckBox logoed;
    @BindView(R.id.choose)
    Button choose;
    @BindView(R.id.color_front)
    ImageView front;
    @BindView(R.id.color_back)
    ImageView back;
    private String rclv="L";//容错率
    private Bitmap code_bitmap;
    private Bitmap logo;
    private int frontColor = Color.BLACK;
    private int backColor = Color.WHITE;
    public static final int TAKE_PHOTO = 1;//拍照
    public static final int CHOOSE_PHOTO = 2;//从相册选择图片
    private Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestAllPower();
        }
        initListener();
    }
    public void requestAllPower() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
        }
    }

    private void initListener() {
        rongcuo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.radio1:
                        rclv = "L";
                        break;
                    case R.id.radio2:
                        rclv = "M";
                        break;
                    case R.id.radio3:
                        rclv = "Q";
                        break;
                    case R.id.radio4:
                        rclv = "H";
                        break;
                }
                Log.e("childId",checkedId+""+rclv);
            }
        });
        logoed.setOnClickListener(this);
        create.setOnClickListener(this);
        back.setOnClickListener(this);
        front.setOnClickListener(this);
        choose.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.logoed:
                if(logoed.isChecked()){
                    choose.setVisibility(View.VISIBLE);
                }else{
                    choose.setVisibility(View.GONE);
                }
                break;
            case R.id.color_back:
                startActivity(1);
                break;
            case R.id.color_front:
                startActivity(2);
                break;
            case R.id.create:
                createCode();
                break;
            case R.id.choose:
                showLogoDialog();
                break;
        }
    }
    private void showLogoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder
                .setTitle("选择LOGO")
                .setSingleChoiceItems(new String[]{"拍照上传", "从相册选择"}, -1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0://拍照
                                        takePhoto();
                                        break;
                                    case 1:// 从相册选择
                                        choosePhotoFromAlbum();
                                        break;
                                    default:
                                        break;
                                }
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        builder.create();
        builder.show();
    }
    /**
     * 拍照
     */
    private void takePhoto() {
        // 创建File对象，用于存储拍照后的图片
        File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT < 24) {
            imageUri = Uri.fromFile(outputImage);
        } else {
            imageUri = FileProvider.getUriForFile(MainActivity.this, "com.example.shinelon.qrcodelogo.fileprovider", outputImage);
        }
        // 启动相机程序
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    /**
     * 从相册选取图片
     */
    private void choosePhotoFromAlbum() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            openAlbum();
        }
    }

    /**
     * /打开相册
     */
    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    private void createCode() {
        int w,h;//生成二维码的宽高
        String str_content = content.getText().toString();
        String str_width = width.getText().toString();
        String str_height = height.getText().toString();
        if (str_width.length() <= 0 || str_height.length() <= 0) {
            w = 600;
            h = 600;
        } else {
            w = Integer.parseInt(str_width);
            h = Integer.parseInt(str_height);
        }

        if (str_content.length() <= 0) {
            Toast.makeText(this, "你没有输入二维码内容哟！", Toast.LENGTH_SHORT).show();
            return;
        }
        if(logoed.isChecked()) {
            code_bitmap = QrCodeUtils.createQRCodeBitmap(str_content, w, h, "UTF-8",
                    rclv, "2", frontColor, backColor, logo, 0.2F, null);
        }else{
            code_bitmap = QrCodeUtils.createQRCodeBitmap(str_content, w, h, "UTF-8",
                    rclv, "2", frontColor, backColor, null, 0.2F, null);
        }
        code.setImageBitmap(code_bitmap);
    }

    private void startActivity(int i) {
        Intent intent = new Intent(MainActivity.this,ColorPicActivity.class);
        startActivityForResult(intent,i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==101) {
            int r = data.getIntExtra("r", -1);
            int g = data.getIntExtra("g", -1);
            int b = data.getIntExtra("b", -1);
            int a = data.getIntExtra("a", -1);
            if (r == -1 || g == -1 || b == -1 || a == -1) {
                Toast.makeText(MainActivity.this, "颜色获取失败，请重试", Toast.LENGTH_SHORT).show();
            } else {
                if(requestCode==1){
                    back.setBackgroundColor(Color.argb(a,r,g,b));
                    backColor=Color.argb(a,r,g,b);
                }else{
                    front.setBackgroundColor(Color.argb(a,r,g,b));
                    frontColor=Color.argb(a,r,g,b);
                }
            }
        }else{
            switch (requestCode) {
                case TAKE_PHOTO:
                    if (resultCode == RESULT_OK) {
                        try {
                            logo = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case CHOOSE_PHOTO:
                    if (resultCode == RESULT_OK) {
                        // 判断手机系统版本号
                        if (Build.VERSION.SDK_INT >= 19) {
                            // 4.4及以上系统使用这个方法处理图片
                            handleImageOnKitKat(data);
                        } else {
                            // 4.4以下系统使用这个方法处理图片
                            handleImageBeforeKitKat(data);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }
    /**
     * 4.4以后
     *
     * @param data
     */
    @SuppressLint("NewApi")
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        Log.d("TAG", "handleImageOnKitKat: uri is " + uri);
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        logo = BitmapFactory.decodeFile(imagePath);
    }
    /**
     * 4.4版本以前，直接获取真实路径
     *
     * @param data
     */
    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        logo = BitmapFactory.decodeFile(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
}
