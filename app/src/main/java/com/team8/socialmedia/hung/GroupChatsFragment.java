package com.team8.socialmedia.hung;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.team8.socialmedia.R;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class GroupChatsFragment extends Fragment {


    public GroupChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_chats, container, false);
    }
}