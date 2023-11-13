package com.team8.socialmedia.kone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.team8.socialmedia.MainActivity;
import com.team8.socialmedia.R;

import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {
//  View from xml
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv;
    TextView nameTv, userStatusTv;
    EditText messageEt;
    ImageButton sendBtn;
//    Firebase auth
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference userDbRef;


    String hisUid;
    String myUid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

//        init view
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView = findViewById(R.id.chat_recycleView);
        profileIv = findViewById(R.id.chat_profileIv);
        nameTv = findViewById(R.id.chat_nameTv);
        userStatusTv = findViewById(R.id.chat_userStatusTv);
        messageEt =findViewById(R.id.chat_messageEt);
        sendBtn = findViewById(R.id.chat_sendBtn);

//      Firebase auth instance
        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        userDbRef = firebaseDatabase.getReference();

        // Search user to get that user's info
        Query userQuery = userDbRef.orderByChild("uid").equalTo(hisUid);
        // get user picture and name
        userQuery.addValueEventListener(new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check until required info is recived
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String name = "" + ds.child("name").getValue();
                    String image = "" + ds.child("image").getValue();

                    // Set data
                    nameTv.setText(name);
                    try{
                        // image received, set it to imageview in toolbar
                        Picasso.get().load(image).placeholder(R.drawable.ic_default).into(profileIv);
                    }
                    catch (Exception e){
                        // There is excreption getting picture, set default picture
                        Picasso.get().load(R.drawable.ic_default).into(profileIv);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        // Click button to send message
        sendBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //get text from edit text
                String message = messageEt.getText().toString().trim();
                //Check if text is empty or not
                if(TextUtils.isEmpty(message)){
                    //text empty
                    Toast.makeText(ChatActivity.this, "Can't send the empty message...", Toast.LENGTH_SHORT).show();
                }
                else {
                    //text not empty
                    sendMessage(message);
                }
            }
        });


/*          On Clicking user from users list we have passed that user's UID using intent
 *          So get that uid here to get the profile picture, name and start chat with that user   */
        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");
    }

    private void sendMessage(String message) {
        /*"Chats" node will be created that will contain all chats
        * Whenever user sands message it will create new child in "chats" node nad that child will contain
        * the following key values
        * sender: UID of sender
        * receiver: UID if receiver
        * message: the actual message */
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", hisUid);
        hashMap.put("message", message);
        databaseReference.child("Chat").push().setValue(hashMap);
        //Reset edittext after sending message
        messageEt.setText("");
    }

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
            //set email of logged-in user
            myUid = user.getUid(); //currently signed in user's uid
        } else {
            //user not signed in, go to main activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
//      Hide searchview, as we dont need it here
        menu.findItem(R.id.action_search).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}