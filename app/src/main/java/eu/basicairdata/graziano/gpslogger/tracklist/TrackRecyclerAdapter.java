package eu.basicairdata.graziano.gpslogger.tracklist;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;
import java.util.Vector;

import eu.basicairdata.graziano.gpslogger.R;
import eu.basicairdata.graziano.gpslogger.databinding.ItemTrackBinding;

public class TrackRecyclerAdapter extends RecyclerView.Adapter<TrackRecyclerAdapter.TrackRecyclerViewHolder>{
    private ItemTrackBinding bind;
    private final Vector<ItemTrackData> trackList;
    private OnItemSelectListener listener;


    interface OnItemSelectListener {
        void onItemSelect(ItemTrackData item, int pos);
    }

    private TrackRecyclerAdapter() {
        this.trackList = new Vector<>();
    }

    public TrackRecyclerAdapter(@NonNull final OnItemSelectListener listener) {
        this();
        this.listener = listener;
    }

    @NonNull @Override
    public TrackRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.bind = ItemTrackBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        TrackRecyclerViewHolder holder = new TrackRecyclerViewHolder(this.bind.getRoot());

        this.bind.gotoRecord.setOnClickListener(v -> {
            if(this.listener != null) {
                listener.onItemSelect(this.trackList.get(holder.getBindingAdapterPosition()), holder.getBindingAdapterPosition());
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull TrackRecyclerViewHolder holder, int position) {
        holder.onBind(this.trackList.get(position));
    }

    @Override
    public int getItemCount() {
        return this.trackList.size();
    }

    // if this method isnt overriding, sometimes detached view replaced other correct view
    // so this list might be corrupt
    // i didnt know what happened, but... problem resolved to override this method
    @Override
    public int getItemViewType(int position) {
        return position; // super.getItemViewType(position);
    }

    public void addItems(@NonNull final LinkedList<ItemTrackData> items) {
        if(this.trackList != null) {
            final int beforeRearItemCount = this.getItemCount();

            this.trackList.addAll(items);
            this.notifyItemRangeInserted(beforeRearItemCount, items.size());
        }
    }
    public void addItem(@NonNull final ItemTrackData item) {
        if(this.trackList != null) {
            this.trackList.add(item);
            this.notifyItemInserted(this.getItemCount());
        }
    }

    public void clearItems() {
        if (this.trackList != null) {
            this.trackList.clear();
            this.notifyDataSetChanged();
        }
    }

    public void release() {
        if(this.trackList != null) {
            this.trackList.clear();
        }
        this.bind = null;
        this.listener = null;
    }

    // INNER CLASS MIGHT BE STATIC CLASS
    // FOR PREVENT MEMORY LEAK
    public static class TrackRecyclerViewHolder extends RecyclerView.ViewHolder {
        private final ItemTrackBinding bind;
        public TrackRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            this.bind = ItemTrackBinding.bind(itemView);
        }

        public void onBind(ItemTrackData item) {
            if(this.bind == null || item == null) return;

            final boolean allDone  = item.isDoneCourseInfo & item.isDonePlacemarkPic;
            this.bind.gotoRecord.setBackgroundResource(allDone? R.drawable.background_round_stroke_border_32 : R.drawable.blue_round_border_32);
            this.bind.gotoRecord.setText(allDone? "수정 >" : "정보 입력 >");
            this.bind.gotoRecord.setTextColor(allDone? Color.parseColor("#0078d7"): Color.parseColor("#eeeeee"));
            this.bind.getRoot().setBackgroundResource(allDone? R.drawable.lightgray_round_border_8: R.drawable.background_round_border_8);

            this.bind.trackName.setText(item.getTrackName());
            this.bind.trackAddress.setText(item.getTrackAddress());

            this.bind.trackCourseState.setCompoundDrawablesWithIntrinsicBounds(0, 0, item.isDoneCourseInfo? R.drawable.ic_complete_24: R.drawable.ic_not_done_24, 0);
            this.bind.trackCourseState.setTextColor(item.isDoneCourseInfo?
                    this.bind.getRoot().getContext().getColor(R.color.textColorHighlight):
                    this.bind.getRoot().getContext().getColor(R.color.textColorWaring));

            this.bind.trackPictureState.setCompoundDrawablesWithIntrinsicBounds(0, 0, item.isDonePlacemarkPic? R.drawable.ic_complete_24: R.drawable.ic_not_done_24, 0);
            this.bind.trackPictureState.setTextColor(item.isDonePlacemarkPic?
                    this.bind.getRoot().getContext().getColor(R.color.textColorHighlight):
                    this.bind.getRoot().getContext().getColor(R.color.textColorWaring));
        }
    }
}
