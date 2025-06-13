package com.example.meufinanceiro.utils;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UsuarioFirebase {

    public static FirebaseUser getUsuarioAtual() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static String getNomeUsuario() {
        FirebaseUser user = getUsuarioAtual();
        return user != null ? user.getDisplayName() : "";
    }

    public static Uri getFotoUsuario() {
        FirebaseUser user = getUsuarioAtual();
        return user != null ? user.getPhotoUrl() : null;
    }

    public static String getEmailUsuario() {
        FirebaseUser user = getUsuarioAtual();
        return user != null ? user.getEmail() : "";
    }

    public static String getUidUsuario() {
        FirebaseUser user = getUsuarioAtual();
        return user != null ? user.getUid() : "";
    }
}
