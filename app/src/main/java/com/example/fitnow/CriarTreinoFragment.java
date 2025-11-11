package com.example.fitnow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CriarTreinoFragment extends Fragment {

    private Spinner spinnerParteCorpo;
    private Spinner spinnerCategoria;
    private RecyclerView recyclerExercicios;
    private EditText editNomeTreino;
    private EditText editDuracao;
    private Button btnSalvarTreino;

    private String categoriaSelecionada = "Cardio";
    private String parteSelecionada = "";

    private ExercicioAdapter adapterExercicios;
    private final List<Exercicio> exerciciosSelecionados = new ArrayList<>();

    private final List<Exercicio> cardioExercicios = criarCardioExercicios();
    private final Map<String, List<Exercicio>> exerciciosForcaPorParte = criarExerciciosForcaPorParte();
    private final List<Exercicio> mobilidadeExercicios = criarMobilidadeExercicios();
    private final List<Exercicio> alongamentoExercicios = criarAlongamentoExercicios();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_criar_treino, container, false);

        spinnerParteCorpo = view.findViewById(R.id.spinnerParteCorpo);
        spinnerCategoria = view.findViewById(R.id.spinnerCategoria);
        recyclerExercicios = view.findViewById(R.id.recyclerExercicios);
        editNomeTreino = view.findViewById(R.id.editNomeTreino);
        editDuracao = view.findViewById(R.id.editDuracao);
        btnSalvarTreino = view.findViewById(R.id.btnSalvarTreino);

        recyclerExercicios.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerExercicios.setHasFixedSize(true);

        String[] partesCorpo = {"Peito", "Pernas", "Braços", "Ombros", "Costas"};
        String[] categorias = {"Cardio", "Força", "Mobilidade", "Alongamento"};

        ArrayAdapter<String> adapterParte = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, partesCorpo);
        adapterParte.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerParteCorpo.setAdapter(adapterParte);
        spinnerParteCorpo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                parteSelecionada = parent.getItemAtPosition(position).toString();
                if ("Força".equals(categoriaSelecionada)) {
                    mostrarExercicios(obterExerciciosForca(parteSelecionada));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nada a fazer
            }
        });
        spinnerParteCorpo.setVisibility(View.GONE);

        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, categorias);
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategoria);
        spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view12, int position, long id) {
                categoriaSelecionada = parent.getItemAtPosition(position).toString();
                atualizarCategoriaSelecionada();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nada a fazer
            }
        });
        spinnerCategoria.setSelection(0);

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
            if (exerciciosSelecionados.isEmpty()) {
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
            treino.setTipo("Personalizado");
            treino.setDuracao(duracao);
            treino.setUserId(user.getUid());
            treino.setExercicios(mapearExerciciosSelecionados());

            FirebaseFirestore.getInstance()
                    .collection("treinos")
                    .add(treino)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(getContext(), "Treino salvo com sucesso!", Toast.LENGTH_SHORT).show();
                        limparCampos();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Erro ao salvar treino.", Toast.LENGTH_SHORT).show());
        });

        return view;
    }

    private void atualizarCategoriaSelecionada() {
        switch (categoriaSelecionada) {
            case "Cardio":
                spinnerParteCorpo.setVisibility(View.GONE);
                mostrarExercicios(cardioExercicios);
                break;
            case "Força":
                spinnerParteCorpo.setVisibility(View.VISIBLE);
                garantirSelecaoParteCorpo();
                mostrarExercicios(obterExerciciosForca(parteSelecionada));
                break;
            case "Mobilidade":
                spinnerParteCorpo.setVisibility(View.GONE);
                mostrarExercicios(mobilidadeExercicios);
                break;
            case "Alongamento":
                spinnerParteCorpo.setVisibility(View.GONE);
                mostrarExercicios(alongamentoExercicios);
                break;
            default:
                spinnerParteCorpo.setVisibility(View.GONE);
                mostrarExercicios(cardioExercicios);
                break;
        }
    }

    private void garantirSelecaoParteCorpo() {
        if (spinnerParteCorpo.getSelectedItem() == null) {
            spinnerParteCorpo.setSelection(0);
        }
        if (spinnerParteCorpo.getSelectedItem() != null) {
            parteSelecionada = spinnerParteCorpo.getSelectedItem().toString();
        }
    }

    private List<Map<String, Object>> mapearExerciciosSelecionados() {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Exercicio exercicio : exerciciosSelecionados) {
            Map<String, Object> map = new HashMap<>();
            map.put("nome", exercicio.getNome());
            map.put("tempo", exercicio.getTempo());
            map.put("dificuldade", exercicio.getDificuldade());
            lista.add(map);
        }
        return lista;
    }

    private void mostrarExercicios(List<Exercicio> exercicios) {
        if (adapterExercicios == null) {
            adapterExercicios = new ExercicioAdapter(exerciciosSelecionados, selecionados -> {
                String msg = "Exercícios selecionados: " + selecionados.size();
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            });
            recyclerExercicios.setAdapter(adapterExercicios);
        }

        adapterExercicios.atualizarDados(exercicios);
        recyclerExercicios.setVisibility(exercicios == null || exercicios.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void limparCampos() {
        editNomeTreino.setText("");
        editDuracao.setText("");
        exerciciosSelecionados.clear();
        if (adapterExercicios != null) {
            adapterExercicios.atualizarDados(new ArrayList<>());
        }
        recyclerExercicios.setVisibility(View.GONE);
        spinnerParteCorpo.setVisibility(View.GONE);
        spinnerCategoria.setSelection(0);
        if (spinnerParteCorpo.getAdapter() != null) {
            spinnerParteCorpo.setSelection(0);
        }
    }

    private List<Exercicio> obterExerciciosForca(String parte) {
        List<Exercicio> lista = exerciciosForcaPorParte.get(parte);
        if (lista == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(lista);
    }

    private List<Exercicio> criarCardioExercicios() {
        List<Exercicio> lista = new ArrayList<>();
        lista.add(new Exercicio("Passadeira", "15 min", "Médio"));
        lista.add(new Exercicio("Escadas", "10 min", "Médio"));
        lista.add(new Exercicio("Bicicleta", "20 min", "Fácil"));
        lista.add(new Exercicio("Elíptico", "12 min", "Fácil"));
        return lista;
    }

    private Map<String, List<Exercicio>> criarExerciciosForcaPorParte() {
        Map<String, List<Exercicio>> mapa = new HashMap<>();

        List<Exercicio> peito = new ArrayList<>();
        peito.add(new Exercicio("Supino", "20 min", "Médio"));
        peito.add(new Exercicio("Flexão", "15 min", "Fácil"));
        mapa.put("Peito", peito);

        List<Exercicio> pernas = new ArrayList<>();
        pernas.add(new Exercicio("Agachamento", "25 min", "Difícil"));
        pernas.add(new Exercicio("Leg Press", "20 min", "Médio"));
        mapa.put("Pernas", pernas);

        List<Exercicio> bracos = new ArrayList<>();
        bracos.add(new Exercicio("Bíceps", "15 min", "Médio"));
        bracos.add(new Exercicio("Tríceps", "15 min", "Fácil"));
        mapa.put("Braços", bracos);

        List<Exercicio> ombros = new ArrayList<>();
        ombros.add(new Exercicio("Elevação Lateral", "10 min", "Fácil"));
        ombros.add(new Exercicio("Desenvolvimento", "20 min", "Médio"));
        mapa.put("Ombros", ombros);

        List<Exercicio> costas = new ArrayList<>();
        costas.add(new Exercicio("Puxada", "20 min", "Médio"));
        costas.add(new Exercicio("Remada", "25 min", "Difícil"));
        mapa.put("Costas", costas);

        return mapa;
    }

    private List<Exercicio> criarMobilidadeExercicios() {
        List<Exercicio> lista = new ArrayList<>();
        lista.add(new Exercicio("Rotação de Tronco", "10 min", "Fácil"));
        lista.add(new Exercicio("Mobilidade de Quadril", "12 min", "Médio"));
        lista.add(new Exercicio("Gato-Camelo", "8 min", "Fácil"));
        return lista;
    }

    private List<Exercicio> criarAlongamentoExercicios() {
        List<Exercicio> lista = new ArrayList<>();
        lista.add(new Exercicio("Alongamento de Posterior", "8 min", "Fácil"));
        lista.add(new Exercicio("Alongamento de Ombros", "6 min", "Fácil"));
        lista.add(new Exercicio("Alongamento de Quadríceps", "8 min", "Médio"));
        return lista;
    }
}
