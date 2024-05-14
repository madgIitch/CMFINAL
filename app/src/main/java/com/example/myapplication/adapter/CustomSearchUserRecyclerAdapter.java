package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ChatActivity;
import com.example.myapplication.R;
import com.example.myapplication.model.UserModel;
import com.example.myapplication.utils.AndroidUtil;
import com.example.myapplication.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;

public class CustomSearchUserRecyclerAdapter extends RecyclerView.Adapter<CustomSearchUserRecyclerAdapter.UserModelViewHolder> {

    private Context context;
    private List<UserModel> userList = new ArrayList<>();

    public CustomSearchUserRecyclerAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_user_recycler_row, parent, false);
        return new UserModelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserModelViewHolder holder, int position) {
        UserModel userModel = userList.get(position);
        holder.usernameTextView.setText(userModel.getUsername());
        holder.nowUserTextView.setText(userModel.getNowUser());
        if (userModel.getUserId().equals(FirebaseUtil.currentUserId())) {
            holder.usernameTextView.setText(userModel.getUsername() + " (Tú)");
        }

        FirebaseUtil.getOtherProfilePicStorageRef(userModel.getUserId()).getDownloadUrl().addOnCompleteListener(t -> {
            if (t.isSuccessful()) {
                Uri uri = t.getResult();
                AndroidUtil.setProfilePic(context, uri, holder.profilePic);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            AndroidUtil.passUserModelAsIntent(intent, userModel);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void setUserList(List<UserModel> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    static class UserModelViewHolder extends RecyclerView.ViewHolder {

        TextView usernameTextView;
        TextView nowUserTextView;
        ImageView profilePic;

        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.profile_pic_imageView);
            usernameTextView = itemView.findViewById(R.id.user_name_text);
            nowUserTextView = itemView.findViewById(R.id.user_now_user);
        }
    }
}
