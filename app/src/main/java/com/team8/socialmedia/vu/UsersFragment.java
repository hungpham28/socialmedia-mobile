package com.team8.socialmedia.vu;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ktx.Firebase;
import com.team8.socialmedia.R;

import java.util.ArrayList;
import java.util.List;


public class UsersFragment extends Fragment {

    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    List<ModelUser> userList;

    public UsersFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        recyclerView = view.findViewById(R.id.users_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        userList = new ArrayList<>();
        getAllUsers();

        return view;
    }

    private void getAllUsers() {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        ModelUser modelUser = ds.getValue(ModelUser.class);
                        if (modelUser != null && !modelUser.getUid().equals(fUser.getUid())) {
                            userList.add(modelUser);
                        }
                    } catch (DatabaseException e) {
                        // Log để xem giá trị không chuyển đổi được
                        Log.e("FirebaseError", "Error converting value for " + ds.getKey(), e);
                    }
                }
                adapterUsers = new AdapterUsers(getActivity(), userList);
                recyclerView.setAdapter(adapterUsers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}