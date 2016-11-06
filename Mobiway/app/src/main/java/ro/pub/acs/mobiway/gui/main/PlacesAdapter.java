package ro.pub.acs.mobiway.gui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.acra.ACRA;

import java.util.ArrayList;

import ro.pub.acs.mobiway.R;

public class PlacesAdapter extends BaseAdapter implements Filterable {

    private Context context;
    private LayoutInflater layoutInflater;

    private ArrayList<String> data;

    @Override
    public Filter getFilter() {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("PlacesAdapter.getFilter()", "method has been invoked");

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                filterResults.values = data;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                data = (ArrayList<String>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    private static class PlaceViewHolder {
        private TextView placeInformationTextView;
    }

    public PlacesAdapter(Context context, ArrayList<String> data) {
        this.context = context;
        this.layoutInflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("PlacesAdapter.getView()", "method has been invoked");

        View view;

        PlaceViewHolder placeViewHolder;

        String place = (String)getItem(position);

        if (convertView == null) {

            //ACRA log
            ACRA.getErrorReporter().putCustomData("PlacesAdapter.getView():noContentView", "created a new view using PlaceViewHolder");

            view = layoutInflater.inflate(R.layout.place, parent, false);
            placeViewHolder = new PlaceViewHolder();
            placeViewHolder.placeInformationTextView = (TextView)view.findViewById(R.id.place_information_text_view);
            view.setTag(placeViewHolder);
        } else {
            view = convertView;
        }

        placeViewHolder = (PlaceViewHolder)view.getTag();
        placeViewHolder.placeInformationTextView.setText(place);

        return view;
    }

}