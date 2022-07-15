package com.example.reesit.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reesit.R;
import com.example.reesit.activities.ReceiptDetailsActivity;
import com.example.reesit.databinding.ItemReceiptBinding;
import com.example.reesit.models.Receipt;
import com.example.reesit.models.Tag;
import com.example.reesit.models.User;
import com.example.reesit.services.ReceiptService;
import com.example.reesit.utils.CurrencyUtils;
import com.example.reesit.utils.DateTimeUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.parceler.Parcels;

import java.util.List;
import java.util.Objects;

public class ReceiptsAdapter extends RecyclerView.Adapter<ReceiptsAdapter.ViewHolder> {
    private Context context;
    private List<Receipt> receipts;
    private FilterByTagCallback callback;
    private ActivityResultLauncher<Intent> updateReceiptLauncher;

    public static final String TAG = "ReceiptsAdapter";


    public interface FilterByTagCallback{
        public void onSelectTag(Tag tag);
    }

    public ReceiptsAdapter(Context context, List<Receipt> receipts, FilterByTagCallback callback, ActivityResultLauncher<Intent> updateReceiptLauncher){
        this.context = context;
        this.receipts = receipts;
        this.callback = callback;
        this.updateReceiptLauncher = updateReceiptLauncher;
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
        private MaterialCardView cardView;
        private TextView receiptMerchant;
        private TextView receiptAmount;
        private TextView referenceNumber;
        private TextView receiptDate;
        private ChipGroup tagsChipGroup;
        private ItemReceiptBinding binding;



        public ViewHolder(@NonNull ItemReceiptBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            cardView = binding.getRoot();
            receiptMerchant = binding.receiptMerchant;
            receiptAmount = binding.receiptAmount;
            referenceNumber = binding.receiptRef;
            receiptDate = binding.receiptDate;
            tagsChipGroup = binding.tagsChipGroup;
        }

        public void bind(Receipt receipt){
            cardView.setTransitionName(context.getString(R.string.container_transition_name_format, receipt.getId()));
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ReceiptDetailsActivity.class);
                    intent.putExtra(ReceiptDetailsActivity.RECEIPT_INTENT_KEY, Parcels.wrap(receipt));
                    intent.putExtra(ReceiptDetailsActivity.RECEIPT_POSITION_INTENT_KEY, getBindingAdapterPosition());
                    // Check if we're running on Android 5.0 or higher
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        // Apply activity transition
                        ActivityOptionsCompat options;
                        if (receipt.getReferenceNumber() != null){
                            options = ActivityOptionsCompat
                                    .makeSceneTransitionAnimation(
                                            (Activity) context,
                                            Pair.create((View) receiptMerchant, context.getString(R.string.merchant_transition_name_format, receipt.getId())),
                                            Pair.create((View) receiptAmount, context.getString(R.string.amount_transition_name_format, receipt.getId())),
                                            Pair.create((View) referenceNumber, context.getString(R.string.ref_transition_name_format, receipt.getId())),
                                            Pair.create((View) receiptDate, context.getString(R.string.date_transition_name_format, receipt.getId()))
                                    );
                        } else {
                            options = ActivityOptionsCompat
                                    .makeSceneTransitionAnimation(
                                            (Activity) context,
                                            Pair.create((View) receiptMerchant, context.getString(R.string.merchant_transition_name_format, receipt.getId())),
                                            Pair.create((View) receiptAmount, context.getString(R.string.amount_transition_name_format, receipt.getId())),
                                            Pair.create((View) receiptDate, context.getString(R.string.date_transition_name_format, receipt.getId()))
                                    );
                        }

                        updateReceiptLauncher.launch(intent, options);
                    } else {
                        // Swap without transition
                        updateReceiptLauncher.launch(intent);
                    }

                }
            });

            cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                    dialogBuilder.setTitle(context.getString(R.string.delete_receipt_dialog_title));
                    dialogBuilder.setMessage(context.getString(R.string.delete_receipt_dialog_message));
                    dialogBuilder.setNegativeButton(R.string.delete_receipt_dialog_negative_button_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dialogBuilder.setPositiveButton(R.string.delete_receipt_dialog_positive_button_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteReceipt(receipt);
                        }
                    });
                    dialogBuilder.show();
                    return true;
                }
            });

            receiptMerchant.setText(receipt.getMerchant().getName());
            receiptMerchant.setTransitionName(context.getString(R.string.merchant_transition_name_format, receipt.getId()));

            receiptAmount.setText(context.getString(R.string.dollar_sign_format, CurrencyUtils.integerToCurrency(receipt.getAmount())));
            receiptAmount.setTransitionName(context.getString(R.string.amount_transition_name_format, receipt.getId()));
            if (receipt.getReferenceNumber().length() > 0){
                referenceNumber.setVisibility(View.VISIBLE);
                referenceNumber.setText(context.getString(R.string.receipt_card_ref_format, receipt.getReferenceNumber()));
                referenceNumber.setTransitionName(context.getString(R.string.ref_transition_name_format, receipt.getId()));
            } else{
                referenceNumber.setVisibility(View.GONE);
            }
            receiptDate.setText(DateTimeUtils.getDateAndTimeReceiptCard(receipt.getDateTimestamp()));
            receiptDate.setTransitionName(context.getString(R.string.date_transition_name_format, receipt.getId()));
            if (receipt.getTags() != null && receipt.getTags().size() > 0){
                tagsChipGroup.removeAllViews();
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

        private void deleteReceipt(Receipt receipt){
            ReceiptService.deleteReceipt(receipt.getId(), Objects.requireNonNull(User.getCurrentUser()), new ReceiptService.DeleteReceiptCallback() {
                @Override
                public void done(Exception e) {
                    if (e == null){
                        receipts.remove(getBindingAdapterPosition());
                        notifyItemRemoved(getBindingAdapterPosition());
                        Snackbar.make(binding.getRoot(), R.string.delete_receipt_success_message, BaseTransientBottomBar.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Error encountered while deleting receipt: "+receipt.getId(), e);
                        Snackbar.make(binding.getRoot(), R.string.delete_receipt_failure_message, BaseTransientBottomBar.LENGTH_SHORT)
                                .setAction(R.string.delete_receipt_failure_retry_action_text, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        deleteReceipt(receipt);
                                    }
                                }).show();
                    }
                }
            });
        }
    }
}
