package com.scyoung.puzzlemethis;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.scyoung.puzzlemethis.Util.StringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CategoryFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private Button goButton;
    private AutoCompleteTextView categorySelection;
    private List<String> CATEGORIES = new ArrayList<String>();
    private SharedPreferences prefs;

    public CategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getActivity().getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
        Log.d("FRAG", "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d("FRAG", "onCreateView");
        View view = inflater.inflate(R.layout.fragment_category, container, false);
        buildCategoriesList();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, CATEGORIES);
        categorySelection = (AutoCompleteTextView)view.findViewById(R.id.categoryToPass);
        categorySelection.setAdapter(adapter);
        goButton = (Button)view.findViewById(R.id.categoryGoButton);
        goButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String category = categorySelection.getText().toString();
                mListener.onCategorySelected(category);
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("FRAG", "onAttach");
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("FRAG", "onDetach");
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onCategorySelected(String categoryName);
    }

    private void buildCategoriesList() {
        Set<String> uniqueCategories = new HashSet<String>();
        Map<String, ?> allEntries = prefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String prefCategory = entry.getKey().split("~")[0];
            String displayCategory = StringUtil.convertFromCondensedUpperCase(prefCategory);
            uniqueCategories.add(displayCategory);
        }
        CATEGORIES = new ArrayList<String>(uniqueCategories);
    }
}
