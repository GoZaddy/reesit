package com.example.reesit.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.reesit.models.Merchant;
import com.example.reesit.models.Receipt;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.nl.entityextraction.DateTimeEntity;
import com.google.mlkit.nl.entityextraction.Entity;
import com.google.mlkit.nl.entityextraction.EntityAnnotation;
import com.google.mlkit.nl.entityextraction.EntityExtraction;
import com.google.mlkit.nl.entityextraction.EntityExtractionParams;
import com.google.mlkit.nl.entityextraction.EntityExtractor;
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions;
import com.google.mlkit.vision.text.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class ReceiptTextParser {
    private static final String TAG = "ReceiptTextParser";

    public static class ReceiptParsingException extends Exception{
        public ReceiptParsingException(String message, Throwable err){
            super(message, err);
        }
    }

    public interface ReceiptParseCallback{
        public void onSuccess(Receipt receipt);

        public void onFailure(ReceiptParsingException e);
    }

    public static void parseReceiptText(Text receiptText, ReceiptParseCallback callback) {
        Receipt receipt = new Receipt();

        // to hold Exception objects
        final ReceiptParsingException[] methodException = {null};

        // extract date and time
        EntityExtractor entityExtractor =
                EntityExtraction.getClient(
                        new EntityExtractorOptions.Builder(EntityExtractorOptions.ENGLISH)
                                .build());
        entityExtractor
                .downloadModelIfNeeded()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        HashSet<Integer> entityTypesFilter = new HashSet<Integer>();
                        entityTypesFilter.add(Entity.TYPE_DATE_TIME);

                        EntityExtractionParams params = new EntityExtractionParams
                                .Builder(receiptText.getText())
                                .setEntityTypesFilter(entityTypesFilter)
                                .build();
                        entityExtractor
                                .annotate(params)
                                .addOnSuccessListener(new OnSuccessListener<List<EntityAnnotation>>() {
                                    @Override
                                    public void onSuccess(List<EntityAnnotation> entityAnnotations) {
                                        // Annotation process was successful, you can parse the EntityAnnotations list here.
                                        for (EntityAnnotation entityAnnotation : entityAnnotations) {
                                            List<Entity> entities = entityAnnotation.getEntities();
                                            for (Entity entity : entities) {
                                                if (entity.getType() == Entity.TYPE_DATE_TIME) {
                                                    DateTimeEntity dateTimeEntity = entity.asDateTimeEntity();
                                                    Log.d(TAG, "Timestamp: " + dateTimeEntity.getTimestampMillis());
                                                    receipt.setDateTimestamp(Long.toString(dateTimeEntity.getTimestampMillis()));
                                                    break;
                                                }
                                            }
                                        }

                                        receipt.setReceiptText(receiptText.getText());
                                        Merchant merchant = new Merchant((receiptText.getTextBlocks().get(0)).getLines().get(0).getText());
                                        receipt.setMerchant(merchant);


                                        for (int blockIndex = 0; blockIndex < receiptText.getTextBlocks().size(); ++blockIndex) {
                                            Text.TextBlock block = receiptText.getTextBlocks().get(blockIndex);

                                            for (int lineIndex = 0; lineIndex < block.getLines().size(); ++lineIndex) {
                                                Text.Line line = block.getLines().get(lineIndex);
                                                String lineText = line.getText();


                                                // check for reference number
                                                if (receipt.getReferenceNumber() == null || Objects.equals(receipt.getReferenceNumber(), "")) {
                                                    String trimmedText = lineText.replaceAll(" ", "").toLowerCase(Locale.ROOT);
                                                    List<String> indicators = new ArrayList<String>();
                                                    indicators.add("reference");
                                                    indicators.add("ref");
                                                    indicators.add("inv#");
                                                    for (String ind : indicators) {
                                                        if (trimmedText.contains(ind)) {
                                                            if (trimmedText.contains(":")) {
                                                                String[] parts = trimmedText.split(":");
                                                                if (parts.length > 1){
                                                                    receipt.setReferenceNumber(trimmedText.split(":")[1]);
                                                                }
                                                            } else {
                                                                receipt.setReferenceNumber(trimmedText.replace(ind, ""));
                                                            }
                                                            break;
                                                        }
                                                    }
                                                }

                                                // check for total amount
                                                if (receipt.getAmount() == null || !Validator.isValidFloat(CurrencyUtils.integerToCurrency(receipt.getAmount()))) {
                                                    receipt.setAmount(null);
                                                    // check if block
                                                    String formattedText = lineText.toLowerCase(Locale.ROOT).replaceAll(":", "");

                                                    if (RegexHelpers.findWholeWord(formattedText, "total")) {
                                                        String extractedFloat = RegexHelpers.extractFloat(formattedText);
                                                        if (extractedFloat != null) {
                                                            try {
                                                                receipt.setAmount(CurrencyUtils.stringToCurrency(extractedFloat));
                                                            } catch (CurrencyUtils.CurrencyUtilsException e) {
                                                                Log.e(TAG, e.getMessage(), e);
                                                            }
                                                        } else {

                                                            // check previous and next blocks on the same line index
                                                            Text.Line matchingLineOnPreviousBlock;
                                                            Text.Line matchingLineOnNextBlock;
                                                            try {
                                                                matchingLineOnPreviousBlock = receiptText.getTextBlocks().get(blockIndex - 1).getLines().get(lineIndex);
                                                            } catch (IndexOutOfBoundsException e){
                                                                matchingLineOnPreviousBlock = null;
                                                            }

                                                            try{
                                                                matchingLineOnNextBlock = receiptText.getTextBlocks().get(blockIndex + 1).getLines().get(lineIndex);
                                                            } catch(IndexOutOfBoundsException e){
                                                                matchingLineOnNextBlock = null;
                                                            }

                                                            if (matchingLineOnNextBlock != null){
                                                                extractedFloat = RegexHelpers.extractFloat(matchingLineOnNextBlock.getText().trim());
                                                                if (extractedFloat != null) {
                                                                    try {
                                                                        receipt.setAmount(CurrencyUtils.stringToCurrency(extractedFloat));
                                                                    } catch (CurrencyUtils.CurrencyUtilsException e) {
                                                                        Log.e(TAG, e.getMessage(), e);
                                                                    }
                                                                } else {
                                                                    if (matchingLineOnPreviousBlock != null){
                                                                        extractedFloat = RegexHelpers.extractFloat(matchingLineOnPreviousBlock.getText().trim());
                                                                        if (extractedFloat != null) {
                                                                            try {
                                                                                receipt.setAmount(CurrencyUtils.stringToCurrency(extractedFloat));
                                                                            } catch (CurrencyUtils.CurrencyUtilsException e) {
                                                                                Log.e(TAG, e.getMessage(), e);
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            } else {
                                                                if (matchingLineOnPreviousBlock != null){
                                                                    extractedFloat = RegexHelpers.extractFloat(matchingLineOnPreviousBlock.getText().trim());
                                                                    if (extractedFloat != null) {
                                                                        try {
                                                                            receipt.setAmount(CurrencyUtils.stringToCurrency(extractedFloat));
                                                                        } catch (CurrencyUtils.CurrencyUtilsException e) {
                                                                            Log.e(TAG, e.getMessage(), e);
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        // success!
                                        callback.onSuccess(receipt);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Check failure message here.
                                        methodException[0] = new ReceiptParsingException("Error extracting entities from text", e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        methodException[0] = new ReceiptParsingException("Error downloading model for entity extraction", e);
                    }
                });

        if (methodException[0] != null) {
            callback.onFailure(methodException[0]);
        }


    }
}


