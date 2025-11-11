package com.example.fitnow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TreinoDetalheFragment extends Fragment {

    private static final String ARG_ID = "treinoId";

    public static TreinoDetalheFragment newInstance(String treinoId) {
        Bundle b = new Bundle();
        b.putString(ARG_ID, treinoId);
        TreinoDetalheFragment f = new TreinoDetalheFragment();
        f.setArguments(b);
        return f;
    }

    private TextView tvNome, tvTipo, tvDuracao, tvExerciciosVazio;
    private RecyclerView recyclerExercicios;
    private ExercicioAdapter exercicioAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_treino_detalhe, container, false);

        // Cabeçalho
        tvNome = v.findViewById(R.id.tvNomeTreinoDetalhe);
        tvTipo = v.findViewById(R.id.tvTipoTreinoDetalhe);
        tvDuracao = v.findViewById(R.id.tvDuracaoTreinoDetalhe);

        // Exercícios
        recyclerExercicios = v.findViewById(R.id.recyclerExerciciosDetalhe);
        tvExerciciosVazio = v.findViewById(R.id.tvExerciciosVazio);

        if (recyclerExercicios != null) {
            recyclerExercicios.setLayoutManager(new GridLayoutManager(getContext(), 2));
            recyclerExercicios.setHasFixedSize(true);

            // Construtor pede: List<Exercicio>, OnSelectionChangeListener
            exercicioAdapter = new ExercicioAdapter(
                    new ArrayList<>(),
                    new ExercicioAdapter.OnSelectionChangeListener() {
                        @Override
                        public void onSelectionChanged(List<Exercicio> selecionados) {
                            // no-op no detalhe (não há seleção)
                        }
                    }
            );
            recyclerExercicios.setAdapter(exercicioAdapter);
        }

        String treinoId = getArguments() != null ? getArguments().getString(ARG_ID) : null;
        if (treinoId == null || treinoId.isEmpty()) {
            Toast.makeText(getContext(), "Treino inválido.", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return v;
        }

        FirebaseFirestore.getInstance()
                .collection("treinos")
                .document(treinoId)
                .get()
                .addOnSuccessListener(doc -> {
                    Treino t = doc.toObject(Treino.class);
                    if (t == null) {
                        Toast.makeText(getContext(), "Treino não encontrado.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Preenche UI do cabeçalho
                    tvNome.setText(t.getNome() != null ? t.getNome() : "(Sem nome)");
                    tvTipo.setText(t.getTipo() != null ? t.getTipo() : "(Sem tipo)");
                    tvDuracao.setText(t.getDuracao() > 0 ? (t.getDuracao() + " min") : "(Sem duração)");

                    // ---- Monta a lista de exercícios ----
                    List<Exercicio> lista = new ArrayList<>();
                    if (t.getExercicios() != null) {
                        for (Object obj : t.getExercicios()) {
                            if (obj instanceof Exercicio) {
                                lista.add((Exercicio) obj);
                            } else if (obj instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> m = (Map<String, Object>) obj;
                                String nome = toStr(m.get("nome"), "(sem nome)");
                                String tempo = toStr(m.get("tempo"), "");
                                String dificuldade = toStr(m.get("dificuldade"), "");
                                lista.add(new Exercicio(nome, tempo, dificuldade));
                            }
                        }
                    }

                    // Atualiza a UI da secção exercícios
                    if (recyclerExercicios != null && exercicioAdapter != null) {
                        if (lista.isEmpty()) {
                            recyclerExercicios.setVisibility(View.GONE);
                            if (tvExerciciosVazio != null) tvExerciciosVazio.setVisibility(View.VISIBLE);
                        } else {
                            recyclerExercicios.setVisibility(View.VISIBLE);
                            if (tvExerciciosVazio != null) tvExerciciosVazio.setVisibility(View.GONE);
                            exercicioAdapter.atualizarDados(lista);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Erro ao carregar detalhes.", Toast.LENGTH_SHORT).show()
                );

        return v;
    }

    private static String toStr(Object o, String def) {
        return o == null ? def : String.valueOf(o);
    }
}
