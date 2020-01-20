package com.systematic.netcore.utility;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;
import com.systematic.netcore.R;
import com.systematic.netcore.objects.KeyPairForCustomer;
import com.systematic.netcore.objects.KeyPairForTowerType;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("AppCompatCustomView")
public class SpinnerSearchForTowerType extends Spinner implements DialogInterface.OnCancelListener {

    private List<KeyPairForTowerType> dataForTowerType;

    private String defaultText = "";
    private SpinnerSearchListener  listener;
    MyAdapter adapter;

    AlertDialog.Builder builder;
    Dialog dialog;
    Activity activity;
    private SettingPreference settingPreference;

    public SpinnerSearchForTowerType(Context context) {
        super(context);
        this.activity = (Activity) context;
        hideKeyboard((Activity) context);
    }

    public SpinnerSearchForTowerType(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
        this.activity = (Activity) arg0;
        hideKeyboard((Activity) arg0);
    }

    private void hideKeyboard(Activity context) {
        context.getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    @Override
    public int getSelectedItemPosition() {
        return super.getSelectedItemPosition();
    }

    @Override
    public boolean performClick() {
        builder = new AlertDialog.Builder(getContext());
        //builder.setTitle(defaultText);

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.alert_dialog_listview_search, null);
        builder.setView(view);

        Activity activity = new Activity();

        dialog = builder.create();

        final ListView listView = (ListView) view.findViewById(R.id.alertSearchListView);
        adapter = new MyAdapter(getContext(),dataForTowerType );
        listView.setAdapter(adapter);

        EditText editText = (EditText) view.findViewById(R.id.alertSearchEditText);
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        dialog.show();

        return true;
    }

    public void setDataForTowerType(List<KeyPairForTowerType> items, String allText, int position, SpinnerSearchListener listener) {

        this.dataForTowerType = items;
        this.defaultText = allText;
        this.listener = listener;

        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<String>(getContext(),
                R.layout.textview_for_spinner,
                new String[]{defaultText});
        setAdapter(adapterSpinner);

        if (position != -1) {
            KeyPairForTowerType kpc = items.get(position);
            kpc.setMySelected(true);
            onCancel(null);
        }
    }

    public interface SpinnerSearchListener {
        public void onItemsSelected(List<KeyPairForTowerType> items);
    }

    @Override
    public void onCancel(DialogInterface dialog) {

    }

    public class MyAdapter extends BaseAdapter implements Filterable {

        List<KeyPairForTowerType> arrayList;
        List<KeyPairForTowerType> mOriginalValues; // Original Values
        LayoutInflater inflater;

        public MyAdapter(Context context, List<KeyPairForTowerType> arrayList) {
            this.arrayList = arrayList;
            inflater = LayoutInflater.from(context);
            Log.i("arrayList", arrayList.size() + "");
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private class ViewHolder {
            TextView textView;
            TextView tvCustomerAddress;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;

            if (convertView == null) {

                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.alert_dialog_listview_search_single, null);
                holder.textView = (TextView) convertView.findViewById(R.id.alertTextView);
                holder.tvCustomerAddress = convertView.findViewById(R.id.tvCustomerAddress);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final KeyPairForTowerType data = arrayList.get(position);

            holder.textView.setText(data.getName());

            convertView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(getContext(), position + "", Toast.LENGTH_SHORT).show();

                    for (int i = 0; i < arrayList.size(); i++) {
                        if (i == position) {
                            //data.setSelected(true);
                            //Log.i("TAG", "On Click Selected : " + data.getName() + " : " + data.isSelected());
                            data.setSelectedPosition(i);
                            Log.i("TAG", "On Click Selected : " + data.getName() + " : " + data.getSelectedPosition());
                            //settingPreference.saveValue(SettingPreference.CUSTOMER_ID_AND_SENDER_ID,data.getId());
                            SettingPreference.Companion.putDataToSharefPref(Constants.Companion.getKeyTowerTypeID(),data.getId(), activity);
                        }
                    }

                    String spinnerText = data.getName();
                    Log.i("spinnerText", data.getName());

                    ArrayAdapter<String> adapterSpinner = new ArrayAdapter<String>(getContext(),
                            R.layout.textview_for_spinner,
                            new String[]{spinnerText});
                    setAdapter(adapterSpinner);

                    if (adapter != null)
                        adapter.notifyDataSetChanged();

                    listener.onItemsSelected(dataForTowerType);

                    dialog.cancel();
                }
            });

            return convertView;
        }

        @SuppressLint("DefaultLocale")
        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {

                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {

                    arrayList = (List<KeyPairForTowerType>) results.values; // has the filtered values
                    notifyDataSetChanged();  // notifies the data with new filtered values
                }

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                    List<KeyPairForTowerType> FilteredArrList = new ArrayList<KeyPairForTowerType>();

                    if (mOriginalValues == null) {
                        mOriginalValues = new ArrayList<KeyPairForTowerType>(arrayList); // saves the original data in mOriginalValues
                    }

                    /********
                     *
                     *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                     *  else does the Filtering and returns FilteredArrList(Filtered)
                     *
                     ********/
                    if (constraint == null || constraint.length() == 0) {

                        // set the Original result to return
                        results.count = mOriginalValues.size();
                        results.values = mOriginalValues;
                    } else {
                        constraint = constraint.toString().toLowerCase();
                        for (int i = 0; i < mOriginalValues.size(); i++) {
                            Log.i("TAG", "Filter : " + mOriginalValues.get(i).getName() + " -> " + mOriginalValues.get(i).getMySelected());
                            String data = mOriginalValues.get(i).getName();
                            if (data.toLowerCase().contains(constraint.toString())) {
                                FilteredArrList.add(mOriginalValues.get(i));
                            }
                        }
                        // set the Filtered result to return
                        results.count = FilteredArrList.size();
                        results.values = FilteredArrList;
                    }
                    return results;
                }
            };
            return filter;
        }
    }
}
