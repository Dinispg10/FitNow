package com.example.fitnow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FragmentTreino extends Fragment {

    private RecyclerView recyclerView;
    private TreinoAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_treino, container, false);

        recyclerView = view.findViewById(R.id.recyclerTreinos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TreinoAdapter();
        recyclerView.setAdapter(adapter);

        carregarTreinosDoUsuario();

        return view;
    }

    private void carregarTreinosDoUsuario() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("treinos")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Treino> listaTreinos = querySnapshot.toObjects(Treino.class);
                    adapter.setTreinos(listaTreinos);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erro ao carregar treinos.", Toast.LENGTH_SHORT).show();
                });
    }
}
