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
import com.example.reesit.models.Receipt;
import com.example.reesit.utils.DateTimeUtils;

import java.util.List;

public class ReceiptsAdapter extends RecyclerView.Adapter<ReceiptsAdapter.ViewHolder> {
    private Context context;
    private List<Receipt> receipts;

    public ReceiptsAdapter(Context context, List<Receipt> receipts){
        this.context = context;
        this.receipts = receipts;
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

        public ViewHolder(@NonNull ItemReceiptBinding binding) {
            super(binding.getRoot());
            receiptMerchant = binding.receiptMerchant;
            receiptAmount = binding.receiptAmount;
            referenceNumber = binding.receiptRef;
            receiptDate = binding.receiptDate;
        }

        public void bind(Receipt receipt){
            receiptMerchant.setText(receipt.getMerchant().getName());
            receiptAmount.setText(context.getString(R.string.dollar_sign_format, receipt.getAmount()));
            if (receipt.getReferenceNumber().length() > 0){
                referenceNumber.setText(context.getString(R.string.receipt_card_ref_format, receipt.getReferenceNumber()));
            } else{
                referenceNumber.setVisibility(View.GONE);
            }
            receiptDate.setText(DateTimeUtils.getDateAndTimeReceiptCard(receipt.getDateTimestamp()));
        }
    }
}
