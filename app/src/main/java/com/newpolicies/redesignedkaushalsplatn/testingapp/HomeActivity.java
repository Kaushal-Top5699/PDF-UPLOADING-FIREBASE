package com.newpolicies.redesignedkaushalsplatn.testingapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class HomeActivity extends AppCompatActivity {

    private Button btnSelect, btnupload;

    private TextView mFileStatus;

    private FirebaseDatabase mDatabase;

    private FirebaseStorage mStorage;

    private Uri pdfUri;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnSelect = findViewById(R.id.btnSelect);

        btnupload = findViewById(R.id.btnUpload);

        mFileStatus = findViewById(R.id.fileStatus);

        mDatabase = FirebaseDatabase.getInstance();

        mStorage = FirebaseStorage.getInstance();


        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {

                    selectPDF();

                } else {

                    ActivityCompat.requestPermissions(HomeActivity.this, new String[]
                            {Manifest.permission.READ_EXTERNAL_STORAGE}, 5699);

                }

            }
        });

        btnupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (pdfUri != null) {

                    uploadPDF(pdfUri);

                } else {

                    Toast.makeText(HomeActivity.this, "Please select a file!", Toast.LENGTH_SHORT).show();

                }

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 5699 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            selectPDF();

        } else {

            Toast.makeText(this, "Please allow external storage reading!", Toast.LENGTH_SHORT).show();

        }

    }

    private void selectPDF() {

        Intent intent = new Intent();

        intent.setType("application/pdf");

        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(intent, 9965);

    }

    private void uploadPDF(Uri pdfUri) {

        progressDialog = new ProgressDialog(HomeActivity.this);

        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        progressDialog.setTitle("Uploading File...");

        progressDialog.setProgress(0);

        progressDialog.show();

        final String fileName = System.currentTimeMillis()+" ";

        StorageReference mySto = mStorage.getReference();

        mySto.child("Uploads").child(fileName).putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();

                        task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                String url = uri.toString();

                                DatabaseReference myRef = mDatabase.getReference();

                                myRef.child(fileName).setValue(url)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {

                                                    Toast.makeText(HomeActivity.this, "File is uploaded", Toast.LENGTH_SHORT).show();

                                                    progressDialog.dismiss();

                                                } else {

                                                    Toast.makeText(HomeActivity.this, "File upload failed", Toast.LENGTH_SHORT).show();

                                                    progressDialog.dismiss();

                                                }

                                            }
                                        });

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(HomeActivity.this, "ERROR! Something went wrong try again.", Toast.LENGTH_SHORT).show();

                progressDialog.dismiss();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                int currentProgress = (int) (100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());

                progressDialog.setProgress(currentProgress);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 9965 && resultCode == RESULT_OK && data != null) {

            pdfUri = data.getData();

            mFileStatus.setText("Selected File: "+ data.getData().getLastPathSegment());

        } else {

            Toast.makeText(this, "Please select a file!", Toast.LENGTH_SHORT).show();

        }

    }
}
