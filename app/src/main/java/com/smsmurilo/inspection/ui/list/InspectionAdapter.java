package com.smsmurilo.inspection.ui.list;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.smsmurilo.inspection.R;
import com.smsmurilo.inspection.databinding.ItemInspectionBinding;
import com.smsmurilo.inspection.domain.Inspection;

import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;

class InspectionAdapter extends ListAdapter<Inspection, InspectionAdapter.Holder> {

    InspectionAdapter() {
        super(new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull Inspection a, @NonNull Inspection b) {
                return a.id.equals(b.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull Inspection a, @NonNull Inspection b) {
                return a.syncStatus.equals(b.syncStatus) && a.result.equals(b.result) && Objects.equals(a.notes, b.notes);
            }
        });
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemInspectionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bind(getItem(position));
    }

    static class Holder extends RecyclerView.ViewHolder {
        private final ItemInspectionBinding binding;

        Holder(ItemInspectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Inspection i) {
            binding.establishment.setText(i.establishment);
            binding.details.setText(binding.getRoot().getContext().getString(R.string.item_details,
                    label(i.resultEnum()), DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(i.createdAt))));
            binding.syncBadge.setText(switch (i.syncStatusEnum()) {
                case SYNCED -> "✓ Enviada";
                case PENDING -> "⏳ Pendente";
                case FAILED -> "⚠ Falhou";
            });
        }

        private static String label(Inspection.Result r) {
            return switch (r) {
                case APPROVED -> "Aprovado";
                case APPROVED_WITH_REMARKS -> "Aprovado com ressalvas";
                case INTERDICTED -> "Interditado";
            };
        }
    }
}
