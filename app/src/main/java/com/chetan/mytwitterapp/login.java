package com.chetan.mytwitterapp;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class login extends AppCompatActivity {
    EditText etName;
    EditText etEmail;
    EditText etPassword;
    ImageView ivUserImage;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    public String TAG="hjjjhere";
    public String useringiding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etName=(EditText)findViewById(R.id.etName);
        etEmail=(EditText)findViewById(R.id.etEmail);
        etPassword=(EditText)findViewById(R.id.etPassword);
        ivUserImage=(ImageView) findViewById(R.id.ivUserImage);
        ivUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckUserPermsions();
            }
        });
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
        Toast.makeText(getApplicationContext(),"just logged"+SaveSettings.UserID,Toast.LENGTH_LONG).show();
    }

    public void buLogin(View view) {
        showProgressDialog();
        FirebaseStorage storage=FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();
        DateFormat df = new SimpleDateFormat("ddMMyyHHmmss");
        Date dateobj = new Date();
        final String ImagePath= df.format(dateobj) +".jpg";
        StorageReference mountainsRef = storageRef.child("images/"+ ImagePath);
        ivUserImage.setDrawingCacheEnabled(true);
        ivUserImage.buildDrawingCache();
        // Bitmap bitmap = imageView.getDrawingCache();
        BitmapDrawable drawable=(BitmapDrawable)ivUserImage.getDrawable();
        Bitmap bitmap =drawable.getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                //Toast.makeText(getApplicationContext(),)
                String downloadUrl = taskSnapshot.getUploadSessionUri().toString();
                String name="";
                try {
                    //for space with name
                    name = java.net.URLEncoder.encode( etName.getText().toString() , "UTF-8");
                    downloadUrl= java.net.URLEncoder.encode(downloadUrl , "UTF-8");
                } catch (UnsupportedEncodingException e) {

                }
                //TODO:  login and register
                String url="http://10.0.2.2:8000/register.php?first_name="+name+"&email="+etEmail.getText().toString()
                        +"&password="+etPassword.getText().toString()
                        +"&picture_path="+downloadUrl;
                new MyAsyncTaskgetNews().execute(url);
                //hideProgressDialog();

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        signInAnonymously();
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        hideProgressDialog();
    }
    private void signInAnonymously() {
        // [START signin_anonymously]
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInAnonymously", task.getException());
                        }
                    }
                });
    }

    void CheckUserPermsions(){
        if ( Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED  ){
                requestPermissions(new String[]{
                                android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return ;
            }
        }
        //LoadImage();
        chooseImage();
    }
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    chooseImage();// init the contact list
                } else {
                    // Permission Denied
                    Toast.makeText( this,"your message" , Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    int RESULT_LOAD_IMAGE=346;
    int PICK_IMAGE_REQUEST=346;
    void LoadImage(){
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }
    private Uri filePath=null;
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                ivUserImage.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }


    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            filePath=selectedImage;
            Bitmap selecte = null;
            try {
                selecte = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ivUserImage.setImageBitmap(selecte);
        }
    }
*/
    // loading display

    @VisibleForTesting
    public ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("loading");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    // get news from server
    public class MyAsyncTaskgetNews extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
        }
        @Override
        protected String  doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                String NewsData;
                URL url = new URL(params[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(7000);
                Log.d(TAG,"proginprogress");
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    Operations operations=new Operations(getApplicationContext());
                    NewsData = operations.ConvertInputToStringNoChange(in);
                    Log.d(TAG,"inprogress");
                    publishProgress(NewsData);
                } finally {
                    urlConnection.disconnect();
                }
            }catch (Exception ex){
                //Toast.makeText(getApplicationContext(),"h1234ere",Toast.LENGTH_LONG).show();
            }
            return null;
        }
        protected void onProgressUpdate(String... progress) {

            try {
                JSONObject json= new JSONObject(progress[0]);
                //display response data
                //Log.d(TAG,"inprogress");
                if (json.getString("msg")==null)
                    return;
                if (json.getString("msg").equals("user is added")) {
                    Toast.makeText(getApplicationContext(), json.getString("msg"), Toast.LENGTH_LONG).show();
//login
                    Log.d(TAG,"url");
                    String url="http://10.0.2.2:8000/login.php?email="+etEmail.getText().toString()+"&password="+etPassword.getText().toString();
                    //Toast.makeText(getApplicationContext(),"here4",Toast.LENGTH_LONG).show();
                    //Log.d(TAG,url);
                    new MyAsyncTaskgetNews().execute(url);
                }
                //Toast.makeText(getApplicationContext(),"here5",Toast.LENGTH_LONG).show();
                if (json.getString("msg").equalsIgnoreCase("Pass Login")) {
                    JSONArray UserInfo=new JSONArray( json.getString("info"));
                    JSONObject UserCreintal= UserInfo.getJSONObject(0);
                    Log.d(TAG,"checkhere");
                    hideProgressDialog();
                    SaveSettings saveSettings= new SaveSettings(getApplicationContext());
                    Log.d(TAG,UserCreintal.getString("user_id"));
                    saveSettings.SaveData(UserCreintal.getString("user_id"));
                    //Toast.makeText(getApplicationContext(),UserCreintal.getString("user_id")+"from",Toast.LENGTH_LONG).show();
                    finish(); //close this activity
                }
            } catch (Exception ex) {
                Log.d("er",  ex.getMessage());
            }
        }
        protected void onPostExecute(String  result2){
        }
    }
}


