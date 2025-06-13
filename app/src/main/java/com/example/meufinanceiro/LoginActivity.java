package com.example.meufinanceiro;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.*;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Configuração do Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // do google-services.json
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Botão de login
        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(view -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Erro ao fazer login com Google", Toast.LENGTH_SHORT).show();
                Log.e("LOGIN", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("LOGIN", "Iniciando autenticação com Firebase");
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("LOGIN", "Autenticação com Firebase bem-sucedida");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Log.d("LOGIN", "Usuário obtido: " + user.getUid());
                            Toast.makeText(this, "Bem-vindo " + user.getDisplayName(), Toast.LENGTH_SHORT).show();

                            // Ir para tela principal
                            Log.d("LOGIN", "Iniciando MainActivity");
                            Intent intent = new Intent(LoginActivity.this, com.example.meufinanceiro.MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.e("LOGIN", "Usuário é null após autenticação bem-sucedida");
                        }
                    } else {
                        Log.e("LOGIN", "Falha na autenticação com Firebase", task.getException());
                        Toast.makeText(this, "Erro na autenticação com Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
