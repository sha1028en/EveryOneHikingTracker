package eu.basicairdata.graziano.gpslogger.tracklist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

    public void addItem(ItemTrackData item) {
        if(this.trackList != null) {
            this.trackList.add(item);
            this.notifyItemInserted(this.getItemCount());
        }
    }

    public void release() {
        if(this.trackList != null) {
            this.trackList.clear();
        }
        this.bind = null;
        this.listener = null;
    }

    public static class TrackRecyclerViewHolder extends RecyclerView.ViewHolder {
        private final ItemTrackBinding bind;
        public TrackRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            this.bind = ItemTrackBinding.bind(itemView);
        }

        public void onBind(ItemTrackData item) {
            if(this.bind == null || item == null) return;

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
