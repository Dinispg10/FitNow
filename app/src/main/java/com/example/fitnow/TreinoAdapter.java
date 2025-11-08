package com.example.fitnow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TreinoAdapter extends RecyclerView.Adapter<TreinoAdapter.ViewHolder> {

    private List<Treino> treinos = new ArrayList<>();

    public void setTreinos(List<Treino> lista) {
        treinos.clear();
        treinos.addAll(lista);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_treino, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Treino treino = treinos.get(position);
        holder.tvNome.setText(treino.getNome());
        holder.tvDetalhes.setText(treino.getTipo() + " - " + treino.getDuracao() + " min");
    }

    @Override
    public int getItemCount() {
        return treinos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvDetalhes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNome);
            tvDetalhes = itemView.findViewById(R.id.tvDetalhes);
        }
    }
}
