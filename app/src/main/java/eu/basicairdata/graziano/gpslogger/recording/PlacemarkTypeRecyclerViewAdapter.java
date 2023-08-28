package eu.basicairdata.graziano.gpslogger.recording;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;

import eu.basicairdata.graziano.gpslogger.management.PlaceMarkType;
import eu.basicairdata.graziano.gpslogger.databinding.ItemPlacemarkTypeBinding;

public class PlacemarkTypeRecyclerViewAdapter extends RecyclerView.Adapter<PlacemarkTypeRecyclerViewAdapter.PlacemarkTypeViewHolder> {
    private LinkedList<ItemPlaceMarkData> placeMarkDataList;
    private ItemPlacemarkTypeBinding bind;
    private PlacemarkTypeViewHolder.OnImageSelectedListener imgSelectedListener;

    private PlacemarkTypeRecyclerViewAdapter(final String trackName) {
        this.placeMarkDataList = new LinkedList<>();

        ItemPlaceMarkData tmpData = new ItemPlaceMarkData(trackName, "나눔길 입구", PlaceMarkType.ENTRANCE.name(), "", true);
        this.placeMarkDataList.add(tmpData);

        tmpData = new ItemPlaceMarkData(trackName, "주차장", PlaceMarkType.PARKING.name(), "", true);
        this.placeMarkDataList.add(tmpData);

        tmpData = new ItemPlaceMarkData(trackName, "화장실", PlaceMarkType.TOILET.name(), "", true);
        this.placeMarkDataList.add(tmpData);

        tmpData = new ItemPlaceMarkData(trackName, "휴계공간", PlaceMarkType.REST.name(), "", true);
        this.placeMarkDataList.add(tmpData);

        tmpData = new ItemPlaceMarkData(trackName, "버스정류장", PlaceMarkType.BUS_STOP.name(), "", true);
        this.placeMarkDataList.add(tmpData);

        tmpData = new ItemPlaceMarkData(trackName,"전망데크", PlaceMarkType.OBSERVATION_DECK.name(), "", true);
        this.placeMarkDataList.add(tmpData);

        tmpData = new ItemPlaceMarkData(trackName,"기타 시설물", PlaceMarkType.ETC.name(), "", true);
        this.placeMarkDataList.add(tmpData);
    }

    public PlacemarkTypeRecyclerViewAdapter(@NonNull final String trackName, @NonNull final PlacemarkTypeViewHolder.OnImageSelectedListener listener) {
        this(trackName);

        this.imgSelectedListener = listener;
    }

    @NonNull @Override
    public PlacemarkTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        this.bind = ItemPlacemarkTypeBinding.inflate(layoutInflater, parent, false);

        PlacemarkTypeViewHolder holder = new PlacemarkTypeViewHolder(this.bind.getRoot());
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PlacemarkTypeViewHolder holder, int position) {
        holder.onBind(this.placeMarkDataList.get(position), this.imgSelectedListener);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return this.placeMarkDataList.size();
    }

    public void updatePlaceMark(@NonNull final ItemPlaceMarkData item) {
        if(this.placeMarkDataList == null) return;

        int index = 0;
        for(ItemPlaceMarkData buffer : this.placeMarkDataList) {
            if(buffer.getPlaceMarkType().equals(item.getPlaceMarkType())) {
                this.placeMarkDataList.set(index, item);
                break;
            }
            ++index;
        }
        this.notifyItemChanged(index);
    }

    public void release() {
        if(this.placeMarkDataList != null) {
            for(ItemPlaceMarkData victim : this.placeMarkDataList) {
                victim.releasePlaceMarkImages();
                victim = null;
            }
            this.placeMarkDataList.clear();
            this.placeMarkDataList = null;
        }
        this.bind = null;
    }

    public static class PlacemarkTypeViewHolder extends RecyclerView.ViewHolder {
        private ItemPlaceMarkData placeMarkData = null;
        private ItemPlacemarkTypeBinding bind;

        private OnImageSelectedListener imgSelectedListener = null;

        public interface OnImageSelectedListener {
            void onSelected(ItemPlaceMarkData placeMarkData, int pos);
        }

        public PlacemarkTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            this.bind = ItemPlacemarkTypeBinding.bind(itemView);

            this.bind.placemarkEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {

            });

            this.bind.placeamrkAddMore.setOnClickListener(v -> {

            });
        }

        public void onBind(@NonNull final ItemPlaceMarkData item, OnImageSelectedListener listener) {
            this.placeMarkData = item;
            this.bind.placemarkTypeTitle.setText(this.placeMarkData.getPlaceMarkTitle());
            this.bind.placemarkEnabled.setChecked(this.placeMarkData.getPlaceMarkEnable());
            this.bind.placemarkInformation.setText(this.placeMarkData.getPlaceMarkDesc());

            if(this.placeMarkData.getPlaceMarkImg0() != null) this.bind.placemarkPic0.setImageBitmap(this.placeMarkData.getPlaceMarkImg0());
            if(this.placeMarkData.getPlaceMarkImg1() != null) this.bind.placemarkPic1.setImageBitmap(this.placeMarkData.getPlaceMarkImg1());
            if(this.placeMarkData.getPlaceMarkImg2() != null) this.bind.placemarkPic2.setImageBitmap(this.placeMarkData.getPlaceMarkImg2());

            this.imgSelectedListener = listener;

            this.bind.placemarkPic0.setOnClickListener(v -> {
                if(this.imgSelectedListener != null) {
                    this.imgSelectedListener.onSelected(this.placeMarkData, 0);
                }
            });

            this.bind.placemarkPic1.setOnClickListener(v -> {
                if(this.imgSelectedListener != null) {
                    this.imgSelectedListener.onSelected(this.placeMarkData, 1);
                }
            });

            this.bind.placemarkPic2.setOnClickListener(v -> {
                if(this.imgSelectedListener != null) {
                    this.imgSelectedListener.onSelected(this.placeMarkData, 2);
                }
            });
        }

        public void release() {
            this.bind = null;
        }
    }
}
