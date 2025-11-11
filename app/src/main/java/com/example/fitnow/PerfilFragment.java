package com.example.fitnow;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PerfilFragment extends Fragment {

    // UI
    private ImageView ivFoto;
    private EditText etNome;
    private Spinner spSexo;
    private EditText etDataNascimento; // yyyy-MM-dd
    private EditText etAlturaCm;       // cm
    private EditText etPesoKg;         // kg
    private TextView tvImc;
    private Button btnEscolherFoto, btnGuardar;

    // Estado
    private Uri fotoSelecionadaUri = null;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    fotoSelecionadaUri = uri;
                    ivFoto.setImageURI(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_perfil, container, false);

        ivFoto = v.findViewById(R.id.ivFotoPerfil);
        etNome = v.findViewById(R.id.etNomePerfil);
        spSexo = v.findViewById(R.id.spSexoPerfil);
        etDataNascimento = v.findViewById(R.id.etDataNascimento);
        etAlturaCm = v.findViewById(R.id.etAlturaCm);
        etPesoKg = v.findViewById(R.id.etPesoKg);
        tvImc = v.findViewById(R.id.tvImcValor);
        btnEscolherFoto = v.findViewById(R.id.btnEscolherFoto);
        btnGuardar = v.findViewById(R.id.btnGuardarPerfil);

        String[] opcoesSexo = new String[]{"", "Masculino", "Feminino", "Outro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, opcoesSexo);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSexo.setAdapter(adapter);

        etDataNascimento.setFocusable(false);
        etDataNascimento.setOnClickListener(v1 -> abrirDatePicker());

        View.OnFocusChangeListener recalc = (view, hasFocus) -> {
            if (!hasFocus) calcularImcUI();
        };
        etAlturaCm.setOnFocusChangeListener(recalc);
        etPesoKg.setOnFocusChangeListener(recalc);

        btnEscolherFoto.setOnClickListener(v12 -> pickImageLauncher.launch("image/*"));
        btnGuardar.setOnClickListener(v13 -> guardarPerfil());

        carregarPerfil();

        return v;
    }

    private void abrirDatePicker() {
        final Calendar cal = Calendar.getInstance();
        String atual = etDataNascimento.getText().toString().trim();
        if (!TextUtils.isEmpty(atual)) {
            try { cal.setTime(dateFormat.parse(atual)); } catch (ParseException ignore) {}
        }
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH);
        int d = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dlg = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar c2 = Calendar.getInstance();
                    c2.set(year, month, dayOfMonth);
                    etDataNascimento.setText(dateFormat.format(c2.getTime()));
                    calcularImcUI();
                },
                y, m, d
        );
        dlg.show();
    }

    private void calcularImcUI() {
        Double imc = calcularImc();
        if (imc == null) {
            tvImcValorSafe("(—)");
        } else {
            tvImcValorSafe(String.format(Locale.getDefault(), "%.1f", imc));
        }
    }

    private void tvImcValorSafe(String text) {
        if (isAdded()) tvImc.setText(text);
    }

    @Nullable
    private Double calcularImc() {
        try {
            String sAlt = etAlturaCm.getText().toString().trim();
            String sPeso = etPesoKg.getText().toString().trim();
            if (TextUtils.isEmpty(sAlt) || TextUtils.isEmpty(sPeso)) return null;
            int alturaCm = Integer.parseInt(sAlt);
            double pesoKg = Double.parseDouble(sPeso);
            if (alturaCm <= 0 || pesoKg <= 0) return null;
            double alturaM = alturaCm / 100.0;
            return pesoKg / (alturaM * alturaM);
        } catch (Exception e) {
            return null;
        }
    }

    private void carregarPerfil() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(this::preencherUI)
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Falha ao carregar perfil.", Toast.LENGTH_SHORT).show());
    }

    private void preencherUI(DocumentSnapshot doc) {
        if (doc != null && doc.exists()) {
            String nome = doc.getString("displayName");
            String sexo = doc.getString("sexo");
            String photoUrl = doc.getString("photoUrl");
            String birthDate = doc.getString("birthDate");
            Long heightCm = doc.getLong("heightCm");
            Double weightKg = null;
            try {
                Object w = doc.get("weightKg");
                if (w instanceof Number) weightKg = ((Number) w).doubleValue();
            } catch (Exception ignored) {}

            if (!TextUtils.isEmpty(nome)) etNome.setText(nome);

            if (!TextUtils.isEmpty(sexo)) {
                for (int i = 0; i < spSexo.getCount(); i++) {
                    if (sexo.equals(spSexo.getItemAtPosition(i))) {
                        spSexo.setSelection(i);
                        break;
                    }
                }
            }

            if (!TextUtils.isEmpty(birthDate)) etDataNascimento.setText(birthDate);
            if (heightCm != null && heightCm > 0) etAlturaCm.setText(String.valueOf(heightCm));
            if (weightKg != null && weightKg > 0) etPesoKg.setText(String.valueOf(weightKg));

            if (!TextUtils.isEmpty(photoUrl) && isAdded()) {
                Glide.with(requireContext()).load(photoUrl).into(ivFoto);
            }

            calcularImcUI();
        } else {
            tvImcValorSafe("(—)");
        }
    }

    private void guardarPerfil() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(getContext(), "Sessão expirada. Faz login.", Toast.LENGTH_SHORT).show();
            return;
        }

        String nome = etNome.getText().toString().trim();
        String sexo = spSexo.getSelectedItem() != null ? spSexo.getSelectedItem().toString() : "";
        String birthDate = etDataNascimento.getText().toString().trim();

        Integer alturaCm = null;
        Double pesoKg = null;
        try {
            String sAlt = etAlturaCm.getText().toString().trim();
            if (!TextUtils.isEmpty(sAlt)) alturaCm = Integer.parseInt(sAlt);
        } catch (Exception ignored) {}
        try {
            String sPeso = etPesoKg.getText().toString().trim();
            if (!TextUtils.isEmpty(sPeso)) pesoKg = Double.parseDouble(sPeso);
        } catch (Exception ignored) {}

        if (!TextUtils.isEmpty(birthDate)) {
            try { dateFormat.parse(birthDate); } catch (ParseException e) {
                Toast.makeText(getContext(), "Data inválida. Usa o seletor de data.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (alturaCm != null && alturaCm <= 0) {
            Toast.makeText(getContext(), "Altura inválida.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pesoKg != null && pesoKg <= 0) {
            Toast.makeText(getContext(), "Peso inválido.", Toast.LENGTH_SHORT).show();
            return;
        }

        // >>>>>>>>>>>>>>>>>> CORREÇÃO: capturar cópias finais para usar na lambda
        final String uidF = uid;
        final String nomeF = nome;
        final String sexoF = sexo;
        final String birthDateF = birthDate;
        final Integer alturaCmF = alturaCm;
        final Double pesoKgF = pesoKg;
        // <<<<<<<<<<<<<<<<<

        if (fotoSelecionadaUri != null) {
            StorageReference ref = FirebaseStorage.getInstance()
                    .getReference().child("user_photos/" + uidF + ".jpg");

            ref.putFile(fotoSelecionadaUri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) throw task.getException();
                        return ref.getDownloadUrl();
                    })
                    .addOnSuccessListener(uri ->
                            salvarNoFirestore(uidF, nomeF, sexoF, birthDateF, alturaCmF, pesoKgF, uri.toString())
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Falha ao enviar foto: " + e.getMessage(), Toast.LENGTH_LONG).show());
        } else {
            salvarNoFirestore(uidF, nomeF, sexoF, birthDateF, alturaCmF, pesoKgF, null);
        }
    }

    private void salvarNoFirestore(String uid,
                                   String nome,
                                   String sexo,
                                   String birthDate,
                                   @Nullable Integer alturaCm,
                                   @Nullable Double pesoKg,
                                   @Nullable String photoUrlNovo) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).get()
                .continueWithTask(task -> {
                    String photoUrlFinal = photoUrlNovo;
                    DocumentSnapshot doc = task.getResult();
                    if (photoUrlFinal == null && doc != null && doc.exists()) {
                        String existente = doc.getString("photoUrl");
                        if (!TextUtils.isEmpty(existente)) photoUrlFinal = existente;
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("displayName", TextUtils.isEmpty(nome) ? null : nome);
                    data.put("sexo", TextUtils.isEmpty(sexo) ? null : sexo);
                    data.put("birthDate", TextUtils.isEmpty(birthDate) ? null : birthDate);
                    if (alturaCm != null) data.put("heightCm", alturaCm); else data.put("heightCm", null);
                    if (pesoKg != null) data.put("weightKg", pesoKg); else data.put("weightKg", null);
                    if (photoUrlFinal != null) data.put("photoUrl", photoUrlFinal);

                    return db.collection("users").document(uid).set(data);
                })
                .addOnSuccessListener(aVoid -> {
                    calcularImcUI();
                    Toast.makeText(getContext(), "Perfil guardado!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Falha ao guardar: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
