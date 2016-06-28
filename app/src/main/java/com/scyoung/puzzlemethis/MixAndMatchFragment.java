package com.scyoung.puzzlemethis;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.scyoung.puzzlemethis.Util.AppUtil;
import com.scyoung.puzzlemethis.Util.ImageUtil;
import com.scyoung.puzzlemethis.Util.StringUtil;
import com.scyoung.puzzlemethis.container.MixAndMatchImageItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MixAndMatchFragment extends Fragment {
    private SharedPreferences prefs;
    private OnFragmentInteractionListener mListener;
    protected int selectedItems = 0;
    private MixAndMatchImageAdapter dataAdapter;
//    protected List<CheckBox> checkBoxes = new ArrayList<CheckBox>();

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
        View view = inflater.inflate(R.layout.fragment_mix_and_match, container, false);
        displayGridView(view);
        presentButtonClick(view);
        return view;
    }

    private void displayGridView(View view) {
        ArrayList<MixAndMatchImageItem> mixAndMatchItems = new ArrayList<MixAndMatchImageItem>();
        Map<String, ?> allEntries = prefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().contains("~") && entry.getKey().contains("IMAGE")) {
                String prefCategory = entry.getKey().split("~")[0];
                String displayCategory = StringUtil.convertFromCondensedUpperCase(prefCategory);
                String displayValue = " " + entry.getKey().split("id/")[1].split("_IMAGE")[0];
                String imageFileKey = (String)entry.getKey();
                mixAndMatchItems.add(new MixAndMatchImageItem(imageFileKey, displayValue, displayCategory, false));
            }
        }
        Collections.sort(mixAndMatchItems);
        dataAdapter = new MixAndMatchImageAdapter<MixAndMatchImageItem>(getActivity(),
                R.layout.mix_and_match_image_layout, mixAndMatchItems);
        GridView gridView = (GridView) view.findViewById(R.id.mixAndMatchList);
        gridView.setAdapter(dataAdapter);
    }

    private class MixAndMatchImageAdapter<M> extends ArrayAdapter<MixAndMatchImageItem> {
        protected List<MixAndMatchImageItem> mixAndMatchList;
        private int[] colorNumberArray = getContext().getResources().getIntArray(R.array.colorNumberList);
        private int arrayMax = colorNumberArray.length;
        private int colorIndex = 0;
        private Map<String, Integer> colorCategoryMap = new HashMap<>();
        private boolean[] checkedState;
        private Object[] holders;
        private Object[] overlays;

        public MixAndMatchImageAdapter(Context context, int resource, List<MixAndMatchImageItem> mixAndMatchImageItems) {
            super(context, resource, mixAndMatchImageItems);
            this.mixAndMatchList = new ArrayList<MixAndMatchImageItem>();
            this.mixAndMatchList.addAll(mixAndMatchImageItems);
            this.checkedState = new boolean[mixAndMatchImageItems.size()];
            this.holders = new Object[mixAndMatchImageItems.size()];
            this.overlays = new Object[mixAndMatchImageItems.size()];
        }

        private class ViewHolder {
            ImageButton fileLocation;
            TextView category;
            CheckBox cb;
            ProgressBar progress;
            ImageView overlay;
            int holderPosition;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            Log.d("ConvertView", String.valueOf(position));

            if (convertView == null || holders[position] == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.mix_and_match_image_layout, null);
                holder = new ViewHolder();
                holder.fileLocation = (ImageButton) convertView.findViewById(R.id.mixAndMatchImageButton);
                holder.category = (TextView) convertView.findViewById(R.id.mixAndMatchCategoryBanner);
                holder.cb = (CheckBox) convertView.findViewById(R.id.mixAndMatchImageCheckbox);
                holder.progress = (ProgressBar) convertView.findViewById(R.id.mixAndMatchProgressBar);
                overlays[position] = convertView.findViewById(R.id.mixAndMatchOverlay);
                if (!isImageSet(holder.fileLocation)) {
                    MixAndMatchImageItem mixAndMatchImageItem = mixAndMatchList.get(position);
                    mixAndMatchImageItem.setPosition(position);
//                    mixAndMatchImageItem.setOverlay(holder.overlay);
                    setupCategoryView(mixAndMatchImageItem, holder);
                    setupCheckBoxView(mixAndMatchImageItem, holder);
                    setupImageButton(mixAndMatchImageItem, holder);
                }
                holder.holderPosition = position;
                holders[position] = holder;
            }

            return convertView;
        }

        private void setupImageButton(final MixAndMatchImageItem mixAndMatchImageItem, ViewHolder holder) {
            String fileLocation = prefs.getString(mixAndMatchImageItem.getImageFileKey(), null);
            ImageButton imageButton = holder.fileLocation;
            if (fileLocation != null && !(imageButton.getBackground() instanceof ColorDrawable)) {
                Bitmap image = ImageUtil.getScaledBitmapFromStorage(Uri.parse(fileLocation), 100, 100);
                if (image != null) {
                    holder.fileLocation.setImageBitmap(image);
                }
            }
            holder.fileLocation.setTag(holder.cb);
            holder.fileLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v.getTag();
                    MixAndMatchImageItem mixAndMatchItem = (MixAndMatchImageItem) cb.getTag();
                    boolean newState = !checkedState[mixAndMatchImageItem.getPosition()];
//                    cb.setChecked(!mixAndMatchItem.isSelected());
                    checkedState[mixAndMatchImageItem.getPosition()] = newState;
                    cb.setChecked(newState);
                }
            });
        }

        private void setupCheckBoxView(MixAndMatchImageItem mixAndMatchImageItem, ViewHolder holder) {
//            holder.cb.setChecked(mixAndMatchImageItem.isSelected());
            holder.cb.setChecked(checkedState[mixAndMatchImageItem.getPosition()]);
            holder.cb.setTag(mixAndMatchImageItem);
            holder.cb.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    MixAndMatchImageItem mixAndMatchImageItem = (MixAndMatchImageItem) buttonView.getTag();
                    if (isChecked && selectedItems >= 6) {
                        //reverse checkbox selection
                        buttonView.setChecked(false);
                        checkedState[mixAndMatchImageItem.getPosition()] = false;
                    } else {
                        // manage MixAndMatchItem
                        if (isChecked) {
                            selectedItems++;
                        } else {
                            selectedItems = selectedItems > 0 ? selectedItems - 1 : selectedItems;
                        }
                        mixAndMatchImageItem.setSelected(isChecked);
                        checkedState[mixAndMatchImageItem.getPosition()] = isChecked;
                        int visibility = isChecked ? View.VISIBLE : View.INVISIBLE;
//                        mixAndMatchImageItem.getOverlay().setVisibility(visibility);
                        ((ImageView)overlays[mixAndMatchImageItem.getPosition()]).setVisibility(visibility);
                    }
                    getActivity().findViewById(R.id.presentOptionsButton).setEnabled(selectedItems > 1);
                }
            });
        }

        private void setupCategoryView(MixAndMatchImageItem mixAndMatchImageItem, ViewHolder holder) {
            String category = mixAndMatchImageItem.getCategory();
            holder.category.setText(category);
            if (colorCategoryMap.containsKey(category)) {
                colorIndex = colorCategoryMap.get(category);
            }
            else {
                colorIndex = colorIndex < arrayMax - 1 ? colorIndex + 1 : 0;
                colorCategoryMap.put(category, colorIndex);
            }
            holder.category.setBackgroundColor(colorNumberArray[colorIndex]);
        }

        private boolean isImageSet(ImageView view) {
            return view.getBackground() instanceof ColorDrawable;
        }
    }

    private void presentButtonClick(View view) {
        Button myButton = (Button) view.findViewById(R.id.presentOptionsButton);
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> fileLocations = new ArrayList<String>();

                List<MixAndMatchImageItem> mixAndMatchList = dataAdapter.mixAndMatchList;
                for(int i=0;i<mixAndMatchList.size();i++){
                    MixAndMatchImageItem item = mixAndMatchList.get(i);
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

    public class ImageLoader extends AsyncTask<String,Integer,Boolean> {

        private ProgressBar progressBar;
        private Uri imageUri;
        private Context mContext;
        private Bitmap found;
        private ImageButton imageButton;
        private int dimension;


        public ImageLoader(Uri uri, ProgressBar progressBar, ImageButton imageButton, int dimension, Context context) {
            super();
            this.progressBar = progressBar;
            this.imageUri = uri;
            this.mContext = context;
            this.imageButton = imageButton;
            this.dimension = dimension;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            found = ImageUtil.getScaledBitmapFromStorage(imageUri,
                    dimension, dimension);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (found != null) {
                imageButton.setImageBitmap(found);
                imageButton.setBackgroundColor(Color.TRANSPARENT);
            }
            progressBar.setVisibility(View.GONE);
        }
    }
}
