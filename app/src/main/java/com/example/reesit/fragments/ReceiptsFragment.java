package com.example.reesit.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.reesit.activities.ReceiptCreationActivity;
import com.example.reesit.databinding.FragmentReceiptsBinding;
import com.example.reesit.misc.Filter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.parceler.Parcels;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReceiptsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReceiptsFragment extends Fragment {

    private FloatingActionButton fab;


    private static final String ARG_PARAM1 = "filter";
    private static final String TAG = "ReceiptsFragment";

    private Filter filter;

    private FragmentReceiptsBinding fragmentReceiptsBinding;

    public ReceiptsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param filter Filter to be applied to receipts.
     * @return A new instance of fragment ReceiptsFragment.
     */

    public static ReceiptsFragment newInstance(Filter filter) {
        ReceiptsFragment fragment = new ReceiptsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, Parcels.wrap(filter));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            filter = (Filter) Parcels.unwrap(getArguments().getParcelable(ARG_PARAM1));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentReceiptsBinding = FragmentReceiptsBinding.inflate(inflater, container, false);
        return fragmentReceiptsBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fab = fragmentReceiptsBinding.addReceiptFab;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ReceiptCreationActivity.class);
                startActivity(intent);
            }
        });
    }
}