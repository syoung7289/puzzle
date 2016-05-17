package com.scyoung.puzzlemethis;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.scyoung.puzzlemethis.Util.StringUtil;
import com.scyoung.puzzlemethis.container.MixAndMatchItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MixAndMatchFragment extends Fragment {
    private SharedPreferences prefs;
    private OnFragmentInteractionListener mListener;
    protected int selectedItems = 0;
    private MixAndMatchAdapter dataAdapter;
    private List<CheckBox> checkBoxes = new ArrayList<CheckBox>();

    public MixAndMatchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_mix_and_match, container, false);
        displayListView(view);
        presentButtonClick(view);
        return view;
    }

    private void displayListView(View view) {
        ArrayList<MixAndMatchItem> mixAndMatchItems = new ArrayList<MixAndMatchItem>();
        Map<String, ?> allEntries = prefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().contains("~") && entry.getKey().contains("IMAGE")) {
                String prefCategory = entry.getKey().split("~")[0];
                String displayCategory = StringUtil.convertFromCondensedUpperCase(prefCategory);
                String displayValue = " " + entry.getKey().split("id/")[1].split("_IMAGE")[0];
                String imageFileKey = (String)entry.getKey();
                mixAndMatchItems.add(new MixAndMatchItem(imageFileKey, displayValue, displayCategory, false));

            }
        }
        Collections.sort(mixAndMatchItems);
        dataAdapter = new MixAndMatchAdapter<MixAndMatchItem>(getActivity(),
                R.layout.mix_and_match_layout, mixAndMatchItems);
        ListView listView = (ListView) view.findViewById(R.id.mixAndMatchList);
        listView.setAdapter(dataAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MixAndMatchItem selectedItem = (MixAndMatchItem)parent.getItemAtPosition(position);
                checkBoxes.get(position).setChecked(!selectedItem.isSelected());
            }
        });
    }

    private class MixAndMatchAdapter<M> extends ArrayAdapter<MixAndMatchItem> {
        protected List<MixAndMatchItem> mixAndMatchList;

        public MixAndMatchAdapter(Context context, int resource, List<MixAndMatchItem> mixAndMatchItems) {
            super(context, resource, mixAndMatchItems);
            this.mixAndMatchList = new ArrayList<MixAndMatchItem>();
            this.mixAndMatchList.addAll(mixAndMatchItems);
        }

        private class ViewHolder {
            TextView fileLocation;
            CheckBox name;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            Log.d("ConvertView", String.valueOf(position));

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.mix_and_match_layout, null);


                holder = new ViewHolder();
                holder.fileLocation = (TextView) convertView.findViewById(R.id.imageFileKey);
                holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }
            MixAndMatchItem mixAndMatchItem = mixAndMatchList.get(position);
            holder.fileLocation.setText(mixAndMatchItem.getName());
            holder.name.setText(mixAndMatchItem.getCategory());
            holder.name.setChecked(mixAndMatchItem.isSelected());
            holder.name.setTag(mixAndMatchItem);
            checkBoxes.add(position, holder.name);

            holder.name.setOnCheckedChangeListener( new CheckBox.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    MixAndMatchItem mixAndMatchItem = (MixAndMatchItem) buttonView.getTag();
                    if (isChecked && selectedItems >= 6) {
                        //reverse checkbox selection
                        buttonView.setChecked(false);
                    }
                    else {
                        // manage MixAndMatchItem
                        if (isChecked) {
                            selectedItems++;
                        } else if (!isChecked) {
                            selectedItems = selectedItems > 0 ? selectedItems - 1 : selectedItems;
                        }
                        mixAndMatchItem.setSelected(isChecked);
                    }
                    getActivity().findViewById(R.id.presentOptionsButton).setEnabled(selectedItems > 1);
                }
            });
            return convertView;
        }
    }

    private void presentButtonClick(View view) {
        Button myButton = (Button) view.findViewById(R.id.presentOptionsButton);
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> fileLocations = new ArrayList<String>();

                List<MixAndMatchItem> mixAndMatchList = dataAdapter.mixAndMatchList;
                for(int i=0;i<mixAndMatchList.size();i++){
                    MixAndMatchItem item = mixAndMatchList.get(i);
                    if(item.isSelected()){
                        fileLocations.add(item.getImageFileKey());
                    }
                }
                mListener.onMixAndMatchPresent(fileLocations);
            }
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onMixAndMatchPresent(ArrayList<String> mixAndMatchList);
    }
}
