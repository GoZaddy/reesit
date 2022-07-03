package com.example.reesit.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.reesit.R;
import com.example.reesit.activities.ReceiptCreationActivity;
import com.example.reesit.adapters.ReceiptsAdapter;
import com.example.reesit.databinding.FragmentReceiptsBinding;
import com.example.reesit.misc.Filter;
import com.example.reesit.models.Receipt;
import com.example.reesit.models.User;
import com.example.reesit.services.ReceiptService;
import com.example.reesit.utils.GetReceiptsCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReceiptsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReceiptsFragment extends Fragment {

    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private Button sortButton;
    private Button filterButton;
    private ProgressBar pageProgressBar;
    private Button loadMoreButton;
    private SearchView searchView;

    private ReceiptsAdapter adapter;


    private static final String ARG_PARAM1 = "filter";
    private static final String TAG = "ReceiptsFragment";

    private List<Receipt> receipts;

    private Filter filter;

    private FragmentReceiptsBinding fragmentReceiptsBinding;

    private ActivityResultLauncher<Intent> receiptCreationLauncher;

    private Integer skip = 0;

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


        pageProgressBar = fragmentReceiptsBinding.pageProgressBar;

        filterButton = fragmentReceiptsBinding.filterButton;
        sortButton = fragmentReceiptsBinding.sortButton;
        loadMoreButton = fragmentReceiptsBinding.loadMoreButton;
        loadMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchReceipts(false);
            }
        });

        recyclerView = fragmentReceiptsBinding.recyclerView;
        receipts = new ArrayList<>();

        adapter = new ReceiptsAdapter(getContext(), receipts);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));

        receiptCreationLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK){
                    if (result.getData() != null){
                        Receipt newReceipt = (Receipt) Parcels.unwrap(result.getData().getParcelableExtra(ReceiptCreationFinalFragment.NEW_RECEIPT_RESULT_KEY));
                        receipts.add(0, newReceipt);
                        adapter.notifyItemInserted(0);
                        recyclerView.smoothScrollToPosition(0);
                        skip += 1;
                    }
                }
            }
        });

        fab = fragmentReceiptsBinding.addReceiptFab;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                receiptCreationLauncher.launch(new Intent(getContext(), ReceiptCreationActivity.class));
            }
        });

        fetchReceipts(true);

        searchView = fragmentReceiptsBinding.searchView;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void setPageStateLoading(){
        pageProgressBar.setVisibility(View.VISIBLE);
        sortButton.setEnabled(false);
        filterButton.setEnabled(false);
    }

    private void setPageStateNotLoading(){
        pageProgressBar.setVisibility(View.INVISIBLE);
        sortButton.setEnabled(true);
        filterButton.setEnabled(true);
    }

    private void fetchReceipts(Boolean overwrite){
        setPageStateLoading();
        ReceiptService.getAllReceipts(User.fromParseUser(ParseUser.getCurrentUser()), skip, new GetReceiptsCallback() {
            @Override
            public void done(List<Receipt> receiptsResult, Boolean isLastPage, ParseException e) {
                if (e == null){
                    if (overwrite){
                        receipts.clear();
                        receipts.addAll(receiptsResult);
                        adapter.notifyDataSetChanged();
                        skip = receiptsResult.size();
                    } else {
                        int oldEndingPosition = receipts.size();
                        receipts.addAll(receiptsResult);
                        adapter.notifyItemRangeInserted(oldEndingPosition, receiptsResult.size());
                        skip += receiptsResult.size();
                    }

                    if (!isLastPage){
                        loadMoreButton.setVisibility(View.VISIBLE);
                    } else {
                        loadMoreButton.setVisibility(View.GONE);
                    }
                    setPageStateNotLoading();
                } else {
                    Toast.makeText(getContext(), R.string.receipts_get_receipts_error_message, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error getting receipts", e);
                }
            }
        });
    }
}