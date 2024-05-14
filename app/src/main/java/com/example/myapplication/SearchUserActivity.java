package com.example.myapplication;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.CustomSearchUserRecyclerAdapter;
import com.example.myapplication.model.UserModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchUserActivity extends AppCompatActivity {

    EditText searchInput;
    ImageButton searchButton;
    ImageButton backButton;
    RecyclerView recyclerView;
    CustomSearchUserRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchInput = findViewById(R.id.search_username_input);
        searchButton = findViewById(R.id.search_user_btn);
        backButton = findViewById(R.id.search_back_btn);
        recyclerView = findViewById(R.id.search_user_recycled_view);

        searchInput.requestFocus();

        backButton.setOnClickListener(v -> onBackPressed());

        searchButton.setOnClickListener(v -> {
            String searchTerm = searchInput.getText().toString();
            if (searchTerm.isEmpty() || searchTerm.length() < 3) {
                searchInput.setError("Mínimo 3 caracteres");
                return;
            }
            setupSearchRecycledView(searchTerm);
        });

        adapter = new CustomSearchUserRecyclerAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    void setupSearchRecycledView(String searchTerm) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Query usernameQuery = db.collection("users")
                .whereGreaterThanOrEqualTo("username", searchTerm)
                .whereLessThanOrEqualTo("username", searchTerm + '\uf8ff');

        Query nowUserQuery = db.collection("users")
                .whereGreaterThanOrEqualTo("nowUser", searchTerm)
                .whereLessThanOrEqualTo("nowUser", searchTerm + '\uf8ff');

        List<UserModel> combinedResults = new ArrayList<>();

        usernameQuery.get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                QuerySnapshot usernameSnapshots = task1.getResult();
                if (usernameSnapshots != null) {
                    combinedResults.addAll(usernameSnapshots.toObjects(UserModel.class));
                }

                nowUserQuery.get().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        QuerySnapshot nowUserSnapshots = task2.getResult();
                        if (nowUserSnapshots != null) {
                            combinedResults.addAll(nowUserSnapshots.toObjects(UserModel.class));
                        }

                        // Remove duplicates
                        List<UserModel> finalResults = removeDuplicates(combinedResults);
                        adapter.setUserList(finalResults);
                    }
                });
            }
        });
    }

    private List<UserModel> removeDuplicates(List<UserModel> userList) {
        List<UserModel> uniqueList = new ArrayList<>();
        List<String> userIds = new ArrayList<>();

        for (UserModel user : userList) {
            if (!userIds.contains(user.getUserId())) {
                uniqueList.add(user);
                userIds.add(user.getUserId());
            }
        }
        return uniqueList;
    }
}
