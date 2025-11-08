package com.example.fitnow;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ExercicioAdapter extends RecyclerView.Adapter<ExercicioAdapter.ViewHolder> {

    private List<Exercicio> exercicios;
    private List<Exercicio> exerciciosSelecionados = new ArrayList<>();

    private OnSelectionChangeListener selectionChangeListener;

    public interface OnSelectionChangeListener {
        void onSelectionChanged(List<Exercicio> selecionados);
    }

    public ExercicioAdapter(List<Exercicio> exercicios, OnSelectionChangeListener listener) {
        this.exercicios = exercicios;
        this.selectionChangeListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercicio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercicio exercicio = exercicios.get(position);
        holder.tvNome.setText(exercicio.getNome());
        holder.tvTempo.setText(exercicio.getTempo());
        holder.tvDificuldade.setText(exercicio.getDificuldade());

        // Visual diferenciado para selecionado
        if (exerciciosSelecionados.contains(exercicio)) {
            holder.itemView.setBackgroundColor(Color.parseColor("#408080")); // Exemplo cor para selecionado
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.itemView.setOnClickListener(v -> {
            if (exerciciosSelecionados.contains(exercicio)) {
                exerciciosSelecionados.remove(exercicio);
            } else {
                exerciciosSelecionados.add(exercicio);
            }
            notifyItemChanged(position);
            if (selectionChangeListener != null) {
                selectionChangeListener.onSelectionChanged(exerciciosSelecionados);
            }
        });
    }

    @Override
    public int getItemCount() {
        return exercicios.size();
    }

    public List<Exercicio> getExerciciosSelecionados() {
        return exerciciosSelecionados;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvTempo, tvDificuldade;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNomeExercicio);
            tvTempo = itemView.findViewById(R.id.tvTempoExercicio);
            tvDificuldade = itemView.findViewById(R.id.tvDificuldade);
        }
    }
}
