package eu.basicairdata.graziano.gpslogger;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;

import eu.basicairdata.graziano.gpslogger.databinding.ItemPlacemarkTypeBinding;

public class PlacemarkTypeRecyclerViewAdapter extends RecyclerView.Adapter<PlacemarkTypeRecyclerViewAdapter.PlacemarkTypeViewHolder> {
    LinkedList<ItemPlaceMarkData> placeMarkDataList = new LinkedList<>();
    private ItemPlacemarkTypeBinding bind;

    public PlacemarkTypeRecyclerViewAdapter() {
        ItemPlaceMarkData tmpData = new ItemPlaceMarkData("나눔길 입구", "Enterance", "", true);
        this.placeMarkDataList.add(tmpData);

        tmpData = new ItemPlaceMarkData("주차장", "Parking", "", true);
        this.placeMarkDataList.add(tmpData);

        tmpData = new ItemPlaceMarkData("화장실", "Toilet", "", true);
        this.placeMarkDataList.add(tmpData);

        tmpData = new ItemPlaceMarkData("휴계공간", "Rest", "", true);
        this.placeMarkDataList.add(tmpData);

        tmpData = new ItemPlaceMarkData("버스정류장", "BusStop", "", true);
        this.placeMarkDataList.add(tmpData);

        tmpData = new ItemPlaceMarkData("전망데크", "observation", "", true);
        this.placeMarkDataList.add(tmpData);

        tmpData = new ItemPlaceMarkData("기타 시설물", "Etc", "", true);
        this.placeMarkDataList.add(tmpData);
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
        holder.onBind(this.placeMarkDataList.get(position));
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return this.placeMarkDataList.size();
    }

    public void release() {
        if(this.placeMarkDataList != null) {
            this.placeMarkDataList.clear();
            this.placeMarkDataList = null;
        }
        this.bind = null;
    }

    public static class PlacemarkTypeViewHolder extends RecyclerView.ViewHolder {
        private ItemPlacemarkTypeBinding bind;
        public PlacemarkTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            this.bind = ItemPlacemarkTypeBinding.bind(itemView);

            this.bind.placemarkPic0.setOnClickListener(v -> {

            });

            this.bind.placemarkPic1.setOnClickListener(v -> {

            });

            this.bind.placemarkPic2.setOnClickListener(v -> {

            });

            this.bind.placemarkEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {

            });

            this.bind.placeamrkAddMore.setOnClickListener(v -> {

            });
        }

        public void onBind(ItemPlaceMarkData item) {
            this.bind.placemarkTypeTitle.setText(item.getPlaceMarkTitle());
            this.bind.placemarkEnabled.setChecked(item.getPlaceMarkEnable());
            this.bind.placemarkInformation.setText(item.getPlaceMarkDesc());

            if(item.getPlaceMarkImg0() != null) this.bind.placemarkPic0.setImageBitmap(item.getPlaceMarkImg0());
            if(item.getPlaceMarkImg1() != null) this.bind.placemarkPic1.setImageBitmap(item.getPlaceMarkImg1());
            if(item.getPlaceMarkImg2() != null) this.bind.placemarkPic2.setImageBitmap(item.getPlaceMarkImg2());
        }

        public void release() {
            this.bind = null;
        }
    }
}
