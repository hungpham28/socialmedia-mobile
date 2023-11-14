package com.team8.socialmedia.kone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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

    //For checking if use has seen message or not
    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;
    List<ModelChat> chatList;
    AdapterChat adapterChat;


    String hisUid;
    String myUid;
    String hisImage;


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
        messageEt = findViewById(R.id.chat_messageEt);
        sendBtn = findViewById(R.id.chat_sendBtn);

        //Layout (LinearLayout) for RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        //Recyclerview properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        /*          On Clicking user from users list we have passed that user's UID using intent
         *          So get that uid here to get the profile picture, name and start chat with that user   */
        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");

//      Firebase auth instance
        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        userDbRef = firebaseDatabase.getReference("Users");

        // Search user to get that user's info
        Query userQuery = userDbRef.orderByChild("uid").equalTo(hisUid);
        // get user picture and name
        userQuery.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check until required info is recived
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data
                    String name = "" + ds.child("name").getValue();
                    hisImage = "" + ds.child("image").getValue();
                    String typingStatus = "" + ds.child("typingTo").getValue();

                    //Check typing status
                    if (typingStatus.equals(myUid)){
                        userStatusTv.setText("typing...");
                    }
                    else{
                        //Get value of onlinestatus
                        String onlineStatus = "" + ds.child("onlineStatus").getValue();
                        if (onlineStatus.equals("online")){
                            userStatusTv.setText(onlineStatus);
                        }
                        else{
                            //convert timestamp to proper time data
                            //convert time stamp to dd/mm/yyyy hh:mm am/pm
                            Calendar cal = Calendar.getInstance(Locale.US);
                            cal.setTimeInMillis(Long.parseLong(onlineStatus));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
                            userStatusTv.setText("Lat seen at: "+ dateTime);

                        }
                    }


                    // Set data
                    nameTv.setText(name);
                    try {
                        // image received, set it to imageview in toolbar
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_default).into(profileIv);
                    } catch (Exception e) {
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
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get text from edit text
                String message = messageEt.getText().toString().trim();
                //Check if text is empty or not
                if (TextUtils.isEmpty(message)) {
                    //text empty
                    Toast.makeText(ChatActivity.this, "Can't send the empty message...", Toast.LENGTH_SHORT).show();
                }
                else {
                    //text not empty
                    sendMessage(message);
                }
            }
        });
        //Check edit text change listener
        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length() ==0){
                    checkTypingStatus(hisUid);//uid of receiver
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        readMessages();

        seenMessage();
    }

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                for (DataSnapshot ds : datasnapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)) {
                        HashMap<String, Object> hasSeenHasMap = new HashMap<>();
                        hasSeenHasMap.put("isSeen", true);
                        ds.getRef().updateChildren(hasSeenHasMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessages() {
        chatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid) ||
                            chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid)) {
                        chatList.add(chat);
                    }
                    //Adapter
                    adapterChat = new AdapterChat(ChatActivity.this, chatList, hisImage);
                    adapterChat.notifyDataSetChanged();
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message) {
        /*"Chats" node will be created that will contain all chats
         * Whenever user sands message it will create new child in "chats" node nad that child will contain
         * the following key values
         * sender: UID of sender
         * receiver: UID if receiver
         * message: the actual message */
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", hisUid);
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("isSeen", false);
        databaseReference.child("Chats").push().setValue(hashMap);
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
    private void checkOnlineStatus(String status){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);

        //Updata value od onlineStatus of current user
        dbRef.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);

        //Updata value od typingStatus of current user
        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        //Set online
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Get timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());

        //set ofline with last seen time stamp
        checkOnlineStatus(timestamp);
        checkTypingStatus("noOne");

        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        //Set online
        checkOnlineStatus("online");
        super.onResume();
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