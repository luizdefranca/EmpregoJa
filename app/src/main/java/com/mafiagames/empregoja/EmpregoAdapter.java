package com.mafiagames.empregoja;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Victor on 27/01/2017.
 */

public class EmpregoAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Emprego> mDataSource;

    public EmpregoAdapter(Context context, ArrayList<Emprego> items) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {

            convertView = mInflater.inflate(R.layout.list_item_emprego, parent, false);

            holder = new ViewHolder();
            //holder.thumbnailImageView = (ImageView) convertView.findViewById(R.id.recipe_list_thumbnail);
            holder.titleTextView = (TextView) convertView.findViewById(R.id.emprego_list_title);
            holder.subtitleTextView = (TextView) convertView.findViewById(R.id.emprego_list_subtitle);
            holder.detailTextView = (TextView) convertView.findViewById(R.id.emprego_list_detail);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TextView titleTextView = holder.titleTextView;
        TextView subtitleTextView = holder.subtitleTextView;
        TextView detailTextView = holder.detailTextView;
        //ImageView thumbnailImageView = holder.thumbnailImageView;

        // 1
        Emprego emprego = (Emprego) getItem(position);

        // 2
        titleTextView.setText(emprego.jobtitle);
        subtitleTextView.setText(Html.fromHtml(emprego.snippet));
        detailTextView.setText(emprego.city);

/*        Typeface detailTypeFace = Typeface.createFromAsset(mContext.getAssets(), "fonts/FiraSansCondensed-Bold.ttf");
        detailTextView.setTypeface(detailTypeFace);*/

        return convertView;
    }

    private static class ViewHolder {
        public TextView titleTextView;
        public TextView subtitleTextView;
        public TextView detailTextView;
        public ImageView thumbnailImageView;
    }
}
