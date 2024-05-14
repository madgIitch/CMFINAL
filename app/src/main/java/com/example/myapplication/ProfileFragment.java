package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.model.UserModel;
import com.example.myapplication.utils.AndroidUtil;
import com.example.myapplication.utils.FirebaseUtil;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.UploadTask;

import kotlin.Unit;
import android.app.Activity;

public class ProfileFragment extends Fragment {

    private ImageView profilePic;
    private EditText usernameInput, emailInput;
    private Button updateProfileBtn;
    private ProgressBar progressBar;
    private TextView logoutBtn;

    private UserModel currentUserModel;
    private ActivityResultLauncher<Intent> imagePickLauncher;
    private Uri selectImageUri;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            selectImageUri = data.getData();
                            AndroidUtil.setProfilePic(getContext(), selectImageUri, profilePic);
                        }
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profilePic = view.findViewById(R.id.profile_image_view);
        usernameInput = view.findViewById(R.id.profile_username);
        emailInput = view.findViewById(R.id.profile_email);
        updateProfileBtn = view.findViewById(R.id.profile_update_btn);
        progressBar = view.findViewById(R.id.profile_progressbar);
        logoutBtn = view.findViewById(R.id.logout_btn);

        profilePic.setOnClickListener(v -> {
            // Trigger the image picker when the profile image is clicked
            ImagePicker.with(this)
                    .cropSquare() // Crop the image to a square
                    .compress(512) // Compress the image to a maximum of 512 KB
                    .maxResultSize(512, 512) // Scale the image to max 512x512 pixels
                    .createIntent(intent -> {
                        imagePickLauncher.launch(intent);
                        return Unit.INSTANCE;
                    });
        });

        getUserData();
        setupListeners();

        return view;
    }

    private void setupListeners() {
        updateProfileBtn.setOnClickListener(v -> updateBtnClick());
        logoutBtn.setOnClickListener(v -> logoutUser());
    }

    private void updateBtnClick() {
        if (currentUserModel == null) {
            Toast.makeText(getContext(), "Error al cargar los datos del usuario. Inténtalo de nuevo.", Toast.LENGTH_SHORT).show();
            return;
        }

        String newUsername = usernameInput.getText().toString().trim();
        if (newUsername.isEmpty() || newUsername.length() < 3) {
            usernameInput.setError("El nombre de usuario debe tener más de 3 caracteres");
            return;
        }
        currentUserModel.setUsername(newUsername);
        setInProgress(true);

        if (selectImageUri != null) {
            FirebaseUtil.getCurrentProfilePicStorageRef().putFile(selectImageUri).addOnCompleteListener(task -> updateToFirestore());
        } else {
            updateToFirestore();
        }
    }

    private void logoutUser() {
        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUtil.logout();
                Intent intent = new Intent(getContext(), SplashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private void updateToFirestore() {
        if (currentUserModel == null) {
            Toast.makeText(getContext(), "Error al cargar los datos del usuario. Inténtalo de nuevo.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUtil.currentUserDetails().set(currentUserModel).addOnCompleteListener(task -> {
            setInProgress(false);
            if (task.isSuccessful()) {
                AndroidUtil.showToast(getContext(), "Los cambios se han guardado correctamente");
            } else {
                AndroidUtil.showToast(getContext(), "Ha ocurrido un error, prueba de nuevo");
            }
        });
    }

    private void getUserData() {
        setInProgress(true);

        FirebaseUtil.getCurrentProfilePicStorageRef().getDownloadUrl().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri uri = task.getResult();
                AndroidUtil.setProfilePic(getContext(), uri, profilePic);
            } else {
                // Handle the error when the file does not exist
                AndroidUtil.showToast(getContext(), "No se encontró la imagen de perfil.");
            }
        });

        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            setInProgress(false);
            if (task.isSuccessful() && task.getResult() != null) {
                currentUserModel = task.getResult().toObject(UserModel.class);
                if (currentUserModel != null) {
                    usernameInput.setText(currentUserModel.getUsername());
                    emailInput.setText(currentUserModel.getEmail());
                } else {
                    Toast.makeText(getContext(), "Error al cargar los datos del usuario. Inténtalo de nuevo.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Error al cargar los datos del usuario. Inténtalo de nuevo.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setInProgress(boolean inProgress) {
        progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        updateProfileBtn.setVisibility(inProgress ? View.GONE : View.VISIBLE);
    }
}
