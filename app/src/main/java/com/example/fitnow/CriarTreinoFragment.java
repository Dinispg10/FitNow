package com.example.fitnow;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CriarTreinoFragment extends Fragment {

    private Spinner spinnerTipoTreino, spinnerParteCorpo;
    private RecyclerView recyclerExercicios;
    private EditText editNomeTreino, editDuracao;
    private Button btnSalvarTreino;

    private String tipoSelecionado = "";
    private String parteSelecionada = "";

    private ExercicioAdapter adapterExercicios;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_criar_treino, container, false);

        spinnerTipoTreino = view.findViewById(R.id.spinnerTipoTreino);
        spinnerParteCorpo = view.findViewById(R.id.spinnerParteCorpo);
        recyclerExercicios = view.findViewById(R.id.recyclerExercicios);
        editNomeTreino = view.findViewById(R.id.editNomeTreino);
        editDuracao = view.findViewById(R.id.editDuracao);
        btnSalvarTreino = view.findViewById(R.id.btnSalvarTreino);

        recyclerExercicios.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerExercicios.setHasFixedSize(true);

        String[] tiposTreino = {"Cardio", "Força", "Mobilidade", "Alongamento"};
        String[] partesCorpo = {"Peito", "Pernas", "Braços", "Ombros", "Costas"};

        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, tiposTreino);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoTreino.setAdapter(adapterTipo);

        spinnerTipoTreino.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                tipoSelecionado = parent.getItemAtPosition(position).toString();

                spinnerParteCorpo.setVisibility(View.GONE);
                recyclerExercicios.setVisibility(View.GONE);
                adapterExercicios = null;

                if (tipoSelecionado.equals("Cardio")) {
                    List<Exercicio> cardioExercicios = new ArrayList<>();
                    cardioExercicios.add(new Exercicio("Passadeira", "15 min", "Médio"));
                    cardioExercicios.add(new Exercicio("Escadas", "10 min", "Médio"));
                    cardioExercicios.add(new Exercicio("Bicicleta", "20 min", "Fácil"));
                    mostrarExercicios(cardioExercicios);
                } else if (tipoSelecionado.equals("Força")) {
                    spinnerParteCorpo.setVisibility(View.VISIBLE);

                    ArrayAdapter<String> adapterParte = new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_spinner_item, partesCorpo);
                    adapterParte.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerParteCorpo.setAdapter(adapterParte);

                    spinnerParteCorpo.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(android.widget.AdapterView<?> parent2, View view2, int posParte, long idParte) {
                            parteSelecionada = parent2.getItemAtPosition(posParte).toString();
                            List<Exercicio> exerciciosForca = new ArrayList<>();
                            switch (parteSelecionada) {
                                case "Peito":
                                    exerciciosForca.add(new Exercicio("Supino", "20 min", "Médio"));
                                    exerciciosForca.add(new Exercicio("Flexão", "15 min", "Fácil"));
                                    break;
                                case "Pernas":
                                    exerciciosForca.add(new Exercicio("Agachamento", "25 min", "Difícil"));
                                    exerciciosForca.add(new Exercicio("Leg Press", "20 min", "Médio"));
                                    break;
                                case "Braços":
                                    exerciciosForca.add(new Exercicio("Bíceps", "15 min", "Médio"));
                                    exerciciosForca.add(new Exercicio("Tríceps", "15 min", "Fácil"));
                                    break;
                                case "Ombros":
                                    exerciciosForca.add(new Exercicio("Elevação Lateral", "10 min", "Fácil"));
                                    exerciciosForca.add(new Exercicio("Desenvolvimento", "20 min", "Médio"));
                                    break;
                                case "Costas":
                                    exerciciosForca.add(new Exercicio("Puxada", "20 min", "Médio"));
                                    exerciciosForca.add(new Exercicio("Remada", "25 min", "Difícil"));
                                    break;
                            }
                            mostrarExercicios(exerciciosForca);
                        }

                        @Override
                        public void onNothingSelected(android.widget.AdapterView<?> parent2) {
                            recyclerExercicios.setVisibility(View.GONE);
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                recyclerExercicios.setVisibility(View.GONE);
            }
        });

        btnSalvarTreino.setOnClickListener(v -> {
            String nome = editNomeTreino.getText().toString().trim();
            String duracaoStr = editDuracao.getText().toString().trim();

            if (nome.isEmpty()) {
                Toast.makeText(getContext(), "Preencha o nome do treino", Toast.LENGTH_SHORT).show();
                return;
            }
            if (duracaoStr.isEmpty()) {
                Toast.makeText(getContext(), "Preencha a duração", Toast.LENGTH_SHORT).show();
                return;
            }
            if (adapterExercicios == null || adapterExercicios.getExerciciosSelecionados().isEmpty()) {
                Toast.makeText(getContext(), "Selecione pelo menos um exercício", Toast.LENGTH_SHORT).show();
                return;
            }

            int duracao = Integer.parseInt(duracaoStr);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(getContext(), "Usuário não autenticado", Toast.LENGTH_SHORT).show();
                return;
            }

            Treino treino = new Treino();
            treino.setNome(nome);
            treino.setTipo(tipoSelecionado);
            treino.setDuracao(duracao);
            treino.setUserId(user.getUid());

            // Aqui podes implementar salvar também os exercícios selecionados, por exemplo num campo extra no documento

            FirebaseFirestore.getInstance()
                    .collection("treinos")
                    .add(treino)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(getContext(), "Treino salvo com sucesso!", Toast.LENGTH_SHORT).show();
                        limparCampos();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Erro ao salvar treino.", Toast.LENGTH_SHORT).show();
                    });
        });

        return view;
    }

    private void mostrarExercicios(List<Exercicio> exercicios) {
        adapterExercicios = new ExercicioAdapter(exercicios, exerciciosSelecionados -> {
            // Atualiza lista dos exercícios selecionados (opcional)
            String msg = "Exercícios selecionados: " + exerciciosSelecionados.size();
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });
        recyclerExercicios.setAdapter(adapterExercicios);
        recyclerExercicios.setVisibility(View.VISIBLE);
    }

    private void limparCampos() {
        editNomeTreino.setText("");
        editDuracao.setText("");
        spinnerTipoTreino.setSelection(0);
        spinnerParteCorpo.setVisibility(View.GONE);
        recyclerExercicios.setVisibility(View.GONE);
        adapterExercicios = null;
    }
}
