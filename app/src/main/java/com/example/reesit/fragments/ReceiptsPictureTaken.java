package com.example.reesit.fragments;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.reesit.R;

import org.parceler.Parcels;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReceiptsPictureTaken#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReceiptsPictureTaken extends Fragment {


    private static final String ARG_PARAM1 = "takenPictureURI";

    private Uri takenPictureURI;

    public ReceiptsPictureTaken() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param uri Uri of receipt image.
     * @return A new instance of fragment ReceiptsPictureTaken.
     */
    // TODO: Rename and change types and number of parameters
    public static ReceiptsPictureTaken newInstance(Uri uri) {
        ReceiptsPictureTaken fragment = new ReceiptsPictureTaken();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, uri);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            takenPictureURI = (Uri) Parcels.unwrap(getArguments().getParcelable(ARG_PARAM1));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_receipts_picture_taken, container, false);
    }
}