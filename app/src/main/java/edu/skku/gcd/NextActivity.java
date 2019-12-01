package edu.skku.gcd;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class NextActivity extends AppCompatActivity {

    private ImageView imagePreview;
    private Bitmap image;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu1, menu);
        return super.onCreateOptionsMenu(menu);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
        imagePreview = (ImageView) findViewById(R.id.imagePreview2);

        Intent intent = getIntent();
        String imageFilePath = (String)intent.getStringExtra("data");

        image = getCorrectOrientedImage(imageFilePath);
        imagePreview.setImageBitmap(image);

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
                intent2.putExtra("title", "문의");
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
}
