package com.example.reesit.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reesit.R;
import com.example.reesit.databinding.ItemReceiptBinding;
import com.example.reesit.fragments.ReceiptsFragment;
import com.example.reesit.misc.Filter;
import com.example.reesit.models.Receipt;
import com.example.reesit.models.Tag;
import com.example.reesit.utils.CurrencyUtils;
import com.example.reesit.utils.DateTimeUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class ReceiptsAdapter extends RecyclerView.Adapter<ReceiptsAdapter.ViewHolder> {
    private Context context;
    private List<Receipt> receipts;
    private FilterByTagCallback callback;

    public interface FilterByTagCallback{
        public void onSelectTag(Tag tag);
    }

    public ReceiptsAdapter(Context context, List<Receipt> receipts, FilterByTagCallback callback){
        this.context = context;
        this.receipts = receipts;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReceiptBinding binding = ItemReceiptBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Receipt receipt = receipts.get(position);
        holder.bind(receipt);
    }

    @Override
    public int getItemCount() {
        return receipts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView receiptMerchant;
        private TextView receiptAmount;
        private TextView referenceNumber;
        private TextView receiptDate;
        private ChipGroup tagsChipGroup;

        public ViewHolder(@NonNull ItemReceiptBinding binding) {
            super(binding.getRoot());
            receiptMerchant = binding.receiptMerchant;
            receiptAmount = binding.receiptAmount;
            referenceNumber = binding.receiptRef;
            receiptDate = binding.receiptDate;
            tagsChipGroup = binding.tagsChipGroup;
        }

        public void bind(Receipt receipt){
            receiptMerchant.setText(receipt.getMerchant().getName());
            receiptAmount.setText(context.getString(R.string.dollar_sign_format, CurrencyUtils.integerToCurrency(receipt.getAmount())));
            if (receipt.getReferenceNumber().length() > 0){
                referenceNumber.setText(context.getString(R.string.receipt_card_ref_format, receipt.getReferenceNumber()));
            } else{
                referenceNumber.setVisibility(View.GONE);
            }
            receiptDate.setText(DateTimeUtils.getDateAndTimeReceiptCard(receipt.getDateTimestamp()));
            if (receipt.getTags() != null && receipt.getTags().size() > 0){
                tagsChipGroup.setVisibility(View.VISIBLE);

                for(Tag tag: receipt.getTags()){
                    Chip chip = new Chip(context);
                    chip.setText(tag.getName());
                    chip.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            callback.onSelectTag(tag);
                        }
                    });
                    tagsChipGroup.addView(chip);
                }
            } else {
                tagsChipGroup.setVisibility(View.GONE);
            }


        }
    }
}
