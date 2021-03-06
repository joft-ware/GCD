package edu.skku.gcd;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    final private static int REQUEST_IMAGE_CAPTURE = 1;
    final private static int RESULT_LOAD_IMAGE = 2;
    final private static int REQUEST_CAMERA_PERMISSION_GRANTED = 3;
    final private static int REQUEST_STORAGE_PERMISSION_GRANTED = 4;

    private ImageView imagePreview;
    private TextView uploadTextView, ocrResultText, noResultText, resultText;
    private EditText nameTextView;
    private Button regButton, scanImage, resultPage;
    private Bitmap image;
    private static String imageFilePath;
    private Button cameraButton, galleryImageButton;
    private String imageFilePath2;
    private String okpath;
    private Uri pu;

    //메뉴
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu1, menu);
        return super.onCreateOptionsMenu(menu);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.quit:
                System.exit(0);
                break;

            case R.id.dev:
                Intent intent = new Intent(this, PopActivity.class);
                intent.putExtra("data", "2019-2 성균관대학교 글로벌캡스톤디자인 3조\n류호성 | 이은혜 | 이태주 | 이혜린 | 조재훈\nPlanB");
                intent.putExtra("title", "개발진");
                startActivity(intent);
                break;

            case R.id.ask:
                Intent intent2 = new Intent(this, PopActivity.class);
                intent2.putExtra("data", "문의사항이 있으시면\nzxc91911003@gmail.com으로 메일 주세요.\nPlanB");
                intent2.putExtra("title", "문의사항");
                startActivity(intent2);
                break;

            case R.id.ver:
                Intent intent3 = new Intent(this, PopActivity.class);
                intent3.putExtra("data", "Ver1.0 \nPlanB");
                intent3.putExtra("title", "버전");
                startActivity(intent3);
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    //이미지 파일 Path 구하기
    static String getRealPathFromURI(Context context, Uri uri2) {
        String result = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri2, proj, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(proj[0]);
                result = cursor.getString(column_index);
            }
            cursor.close();
        }
        if (result == null) {
            result = "Not found";
        }
        return result;
    }

    //이미지 파일 방향 세팅
    static Bitmap getCorrectOrientedImage(String imageFilePath) {
        ExifInterface exifInterface = null;

        Matrix matrix = new Matrix();
        try {
            exifInterface = new ExifInterface(imageFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(270);
                    break;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    // 이미지 파일 생성
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,      /* prefix */
                ".jpg",         /* suffix */
                storageDir          /* directory */
        );
        imageFilePath2 = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, LoadingActivity.class);
        startActivity(intent);
        askPermission();

        cameraButton = (Button) findViewById(R.id.cameraButton);
        galleryImageButton = (Button) findViewById(R.id.galleryImageButton);
        imagePreview = (ImageView) findViewById(R.id.imagePreview);
        uploadTextView = (TextView) findViewById(R.id.uploadImageTextView);
        nameTextView = (EditText) findViewById(R.id.editText);
        ocrResultText = (TextView) findViewById(R.id.ocrResultText);
        regButton = (Button) findViewById(R.id.regButton);
        scanImage = (Button) findViewById(R.id.scanImage);
        noResultText = (TextView) findViewById(R.id.noResultText);
        resultPage = (Button) findViewById(R.id.resultPage);
        resultText = (TextView) findViewById(R.id.uploadImageTextView);



        galleryImageButton.setText("GALLERY");
        cameraButton.setText("CAMERA");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);

        galleryImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromAlbum();
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askPermission();
                sendTakePhotoIntent();
            }
        });

    }

    // 이미지 불러오기
    private void getImageFromAlbum() {
        try {
            Intent pickPhotoIntent =
                    new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhotoIntent, RESULT_LOAD_IMAGE);
        } catch (Exception e) {
            makeToastText("Gallery load failed");
        }
    }


    // 권한 설정
    private void askPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION_GRANTED);
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MainActivity.REQUEST_CAMERA_PERMISSION_GRANTED);
        }
    }

    // 이미지 불러오기
    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case (REQUEST_IMAGE_CAPTURE):
                    imageFilePath = imageFilePath2;
                    try {
                        image = getCorrectOrientedImage(imageFilePath);
                        makeToastText(imageFilePath);
                    } catch (Exception e) {
                        e.printStackTrace();
                        makeToastText(imageFilePath);
                    }
                    break;

                case (RESULT_LOAD_IMAGE):
                    Uri imageUri = data.getData();
                    imageFilePath = getRealPathFromURI(this, imageUri);
                    try {
                        image = getCorrectOrientedImage(imageFilePath);
                        makeToastText(imageFilePath);
                    } catch (Exception e) {
                        e.printStackTrace();
                        makeToastText(imageFilePath);
                    }

                    break;
            }
            imagePreview.setVisibility(View.VISIBLE);
            imagePreview.setImageBitmap(image);
            scanImage.setVisibility(View.VISIBLE);
            resultText.setVisibility(View.INVISIBLE);

            cameraButton.setVisibility(View.INVISIBLE);
            galleryImageButton.setVisibility(View.INVISIBLE);



            final Intent intent4 = new Intent(this, NextActivity.class);
            scanImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    intent4.putExtra("data", imageFilePath);
                    startActivity(intent4);
                }
            });
        }
    }

    // 토스트
    public void makeToastText(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void sendTakePhotoIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                pu = FileProvider.getUriForFile(this, getPackageName(), photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, pu);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }


}