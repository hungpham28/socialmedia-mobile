package com.team8.socialmedia.hung;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.team8.socialmedia.R;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class GroupCreateActivity extends AppCompatActivity {

    //permisstions constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    //image pick constants
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    //permission arrays
    private String[] cameraPermissions;
    private String[] storagePermissions;
    //picked image uri
    private Uri image_uri = null;

    //actionbar
    private ActionBar actionBar;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //UI views
    private ImageView groupIconIv;
    private EditText groupTitleEt, groupDescriptionEt;
    private FloatingActionButton createGroupBtn;

    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create);

        actionBar= getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Create Group");

        //init UI views
        groupIconIv = findViewById(R.id.groupIconIv);
        groupTitleEt = findViewById(R.id.groupTitleEt);
        groupDescriptionEt = findViewById(R.id.groupDescriptionEt);
        createGroupBtn = findViewById(R.id.createGroupBtn);

        //init permissions arrays
        cameraPermissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = firebaseAuth.getInstance();
        checkUser();

        //pick image
        groupIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        //handle click event
        createGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCreatingGroup();
            }
        });
    }

    private void startCreatingGroup() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating Group");

        //input title,description
        String groupTitle = groupTitleEt.getText().toString().trim();
        String groupDescription = groupDescriptionEt.getText().toString().trim();
        //validation
        if(TextUtils.isEmpty(groupTitle)){
            Toast.makeText(this, "Please enter group Title...", Toast.LENGTH_SHORT).show();
            return;//dont procede further
        }
        progressDialog.show();

        //timestamp: for groupicon image, groupId, timeCreated etc
        String g_timestamp = ""+System.currentTimeMillis();
        if(image_uri==null){
            //creating group without icon image
            createGroup(g_timestamp,
                    groupTitle,
                    groupDescription,
                    "");
        }else{
            //creating group with icon image
            //upload image
            //image name and path
            String fileNameAndPath = "Group_Imgs/"+"image"+g_timestamp;

            StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileNameAndPath);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image uploaded, get url
                            Task<Uri> p_uriTask= taskSnapshot.getStorage().getDownloadUrl();
                            while (!p_uriTask.isSuccessful());
                            Uri p_downloadUri= p_uriTask.getResult();
                            if(p_uriTask.isSuccessful()){
                                createGroup(g_timestamp,
                                        groupTitle,
                                        groupDescription,
                                        ""+p_downloadUri);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed uploading image
                            progressDialog.dismiss();
                            Toast.makeText(GroupCreateActivity.this, ""+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void createGroup(String g_timestamp,String groupTitle,String groupDescription,String groupIcon) {
        //setup info of group
        HashMap<String,String> hashMap=new HashMap<>();
        hashMap.put("groupId",g_timestamp);
        hashMap.put("groupTitle",groupTitle);
        hashMap.put("groupDescription",groupDescription);
        hashMap.put("groupIcon",groupIcon);
        hashMap.put("timestamp",g_timestamp);
        hashMap.put("createdBy", ""+firebaseAuth.getUid());

        //create group
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(g_timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //created successfully

                        //setup member inf (add current user in group's participants list
                        HashMap<String,String> mem=new HashMap<>();
                        mem.put("uid",firebaseAuth.getUid());
                        mem.put("role","creator");
                        mem.put("timestamp", g_timestamp);

                        DatabaseReference ref1=FirebaseDatabase.getInstance().getReference("Groups");
                        ref1.child(g_timestamp).child("Participants").child(firebaseAuth.getUid())
                                .setValue(mem)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        //participant added
                                        progressDialog.dismiss();
                                        Toast.makeText(GroupCreateActivity.this, "Group created successfully...",Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(GroupCreateActivity.this, ""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                        Toast.makeText(GroupCreateActivity.this, ""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showImagePickDialog() {
        String[] options = {"Camera", "Gallery"};
        TextView textView = new TextView(this);
        textView.setText("Pick Image From");
        textView.setPadding(20, 30, 20, 30);
        textView.setTextSize(20F);
        textView.setBackgroundColor(Color.WHITE);
        textView.setTextColor(Color.BLACK);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCustomTitle(textView);
        builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            System.out.println("Camera");
                            //camera clicked
                            if (!checkCameraPermission()) {
                                requestCameraPermission();
                            } else {
                                pickFromCamera();
                            }
                        }
                        if (which == 1) {
                            System.out.println("Gallery");
//                    if (!checkStoragePermission()){
                            requestStoragePermission();
//                    }else{
                            pickFromGallery();
//                    }
                        }
                    }
                }).show();
    }
    private void pickFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }
    private void pickFromCamera(){
        ContentValues cv=new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Group Image Icon Title");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Group Image Icon Description");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);

    }

    private boolean checkStoragePermissions(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }


    private void requestStoragePermission() {
        //request runtime storage permission
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);

    }

    private boolean checkCameraPermission() {
        //check if camera permission is enabled or not
        //return true if enabled
        //return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestCameraPermission() {
        //request runtime camera permission
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);

    }
    private void checkUser() {
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user!=null){
            actionBar.setSubtitle(user.getEmail());
        }
    }
    //handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {

        //this method is called when user press Allow or deny from permission request dialog
        //here we will handle permission cases (allowed and denied)
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {

                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//                     boolean storageAccepted= grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted) {
                        //both permission are granted
                        pickFromCamera();
                    }
                } else {
                    //camera or gallery or both permissions are denied
                    Toast.makeText(this, "Camera & storage both permissions are neccessary", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case STORAGE_REQUEST_CODE: {
                System.out.println("STORAGE_REQUEST_CODE");
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        //storage permission granted
                        pickFromGallery();
                    } else {
                        //camera or gallery or both permissions were denied
                        Toast.makeText(this, "Storage permissions | neccessary...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        //this method will be called after picking image from camera or gallery
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //image is picked from gallery, get uri of image
                image_uri = data.getData();

                //set to imageview
                groupIconIv.setImageURI(image_uri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //image is picked from camer, get uri of image

                groupIconIv.setImageURI(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}