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
import com.example.myapplication.model.ChatroomModel;
import com.example.myapplication.model.UserModel;
import com.example.myapplication.utils.AndroidUtil;
import com.example.myapplication.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class RecentChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatroomViewHolder> {

    private Context context;

    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatroomModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatroomViewHolder holder, int position, @NonNull ChatroomModel chatroomModel) {
        String otherUserId = chatroomModel.getUserIds().stream()
                .filter(id -> !id.equals(FirebaseUtil.currentUserId()))
                .findFirst()
                .orElse(null);

        if (otherUserId != null) {
            FirebaseUtil.allUserCollectionReference().document(otherUserId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    UserModel userModel = task.getResult().toObject(UserModel.class);
                    if (userModel != null) {
                        holder.usernameTextView.setText(userModel.getUsername());
                        FirebaseUtil.getOtherProfilePicStorageRef(userModel.getUserId()).getDownloadUrl().addOnCompleteListener(t -> {
                            if (t.isSuccessful()) {
                                Uri uri = t.getResult();
                                AndroidUtil.setProfilePic(context, uri, holder.profilePic);
                            }
                        });
                    }
                }
            });
        }

        holder.lastMessageTextView.setText(chatroomModel.getLastMessage());
        holder.lastMessageTimeTextView.setText(FirebaseUtil.timestampToString(chatroomModel.getLastMessageTimestamp()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            UserModel tempUserModel = new UserModel(otherUserId, null, null, null);
            AndroidUtil.passUserModelAsIntent(intent, tempUserModel);
            context.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public ChatroomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row, parent, false);
        return new ChatroomViewHolder(view);
    }

    class ChatroomViewHolder extends RecyclerView.ViewHolder {

        TextView usernameTextView;
        TextView lastMessageTextView;
        TextView lastMessageTimeTextView;
        ImageView profilePic;

        public ChatroomViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.profile_pic_imageView);
            usernameTextView = itemView.findViewById(R.id.user_name_text);
            lastMessageTextView = itemView.findViewById(R.id.last_message_text);
            lastMessageTimeTextView = itemView.findViewById(R.id.last_message_time_text);
        }
    }
}
