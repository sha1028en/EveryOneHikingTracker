package eu.basicairdata.graziano.gpslogger.recording.enhanced;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import eu.basicairdata.graziano.gpslogger.R;
import eu.basicairdata.graziano.gpslogger.databinding.ItemPlacemarkEnhancedBinding;
import eu.basicairdata.graziano.gpslogger.management.PlaceMarkType;
import eu.basicairdata.graziano.gpslogger.management.TrackRecordManager;

public class PlaceMarkEnhancedRecyclerAdapter extends RecyclerView.Adapter<PlaceMarkEnhancedRecyclerAdapter.PlacemarkTypeViewHolder> {
    private LinkedList<ItemPlaceMarkEnhancedData> placeMarkDataList;
    private ItemPlacemarkEnhancedBinding bind;
    private PlacemarkTypeViewHolder.OnImageSelectedListener imgSelectedListener;

    private OnAddImageClickListener imgAddListener;
    public interface OnAddImageClickListener {
        void onAddImageClick(ItemPlaceMarkEnhancedData placemarkItem, int pos);
    }

    private PlaceMarkEnhancedRecyclerAdapter() {
        this.placeMarkDataList = new LinkedList<>();

        ItemPlaceMarkEnhancedData tmpData = new ItemPlaceMarkEnhancedData("label", "label", PlaceMarkType.ETC.name(), "label", false);
        this.placeMarkDataList.add(tmpData);
    }

    public PlaceMarkEnhancedRecyclerAdapter(
            @NonNull final PlaceMarkEnhancedRecyclerAdapter.PlacemarkTypeViewHolder.OnImageSelectedListener imageSelectListener,
            @NonNull final OnAddImageClickListener imageAddListener) {

        this();
        this.imgSelectedListener = imageSelectListener;
        this.imgAddListener = imageAddListener;
    }

    @NonNull @Override
    public PlaceMarkEnhancedRecyclerAdapter.PlacemarkTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        this.bind = ItemPlacemarkEnhancedBinding.inflate(layoutInflater, parent, false);

        PlaceMarkEnhancedRecyclerAdapter.PlacemarkTypeViewHolder holder = new PlaceMarkEnhancedRecyclerAdapter.PlacemarkTypeViewHolder(this.bind.getRoot());
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceMarkEnhancedRecyclerAdapter.PlacemarkTypeViewHolder holder, int position) {
        if(holder.getBindingAdapterPosition() != 0) {
            holder.onBind(this.placeMarkDataList.get(holder.getBindingAdapterPosition()), this.imgSelectedListener);

            this.bind.placemarkEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                holder.setViewEnableEvent(isChecked, false);

                // update POI state
                TrackRecordManager recordManager = TrackRecordManager.getInstance();
                if(recordManager != null) {
                    ItemPlaceMarkEnhancedData item = this.placeMarkDataList.get(position);
                    recordManager.updatePlaceMark(item.getTrackName(), item.getPlaceMarkTitle(), isChecked);
                }
            });

            this.bind.placemarkAddImg.setOnClickListener(v -> {
                this.imgAddListener.onAddImageClick(this.placeMarkDataList.get(position), position);
            });

        } else {
            holder.onAddPlaceMarkInfoBind();
        }
    }

    public LinkedList<ItemPlaceMarkEnhancedData> getClonedList() {
        return (LinkedList<ItemPlaceMarkEnhancedData>) this.placeMarkDataList.clone();
    }

    @Override
    public int getItemCount() {
        return this.placeMarkDataList.size();
    }

    public int getItemViewType(int position) {
        return position; // super.getItemViewType(position);
    }

    /**
     * add item at last position
     * @param item added Item data
     */
    public void addPlaceMark(@NonNull final ItemPlaceMarkEnhancedData item) {
        if(this.placeMarkDataList == null) return;

        this.placeMarkDataList.add(item);
//        this.notifyDataSetChanged();
        this.notifyItemInserted(this.placeMarkDataList.size());
    }

    public void addPlaceMark(@NonNull final ItemPlaceMarkEnhancedData item, final int pos) {
        if(pos < 0) throw new IllegalArgumentException("PlacemarkTypeRecyclerViewAdapter.addPlaceMark(item, pos) : wrong value pos " + pos);

        this.placeMarkDataList.add(pos, item);
        Collections.sort(this.placeMarkDataList);
        this.notifyItemInserted(pos);
    }

    public void addPlaceMark(@NonNull final List<ItemPlaceMarkEnhancedData> items) {
        if(this.placeMarkDataList == null) return;

        this.placeMarkDataList.addAll(items);
        this.notifyDataSetChanged();
    }

    public void addPlaceMarkImg(@NonNull final LinkedList<ItemPlaceMarkImgData> items) {
        if(this.placeMarkDataList == null || this.placeMarkDataList.isEmpty()) return;

        for(ItemPlaceMarkImgData item : items) {
            ItemPlaceMarkEnhancedData toUpdatePlaceMark = null;
            boolean hasFind = false;
            int i = 0;

            for(ItemPlaceMarkEnhancedData placemark : this.placeMarkDataList) {
                if(placemark.getPlaceMarkType().equals(item.getPlaceMarkType()) && !placemark.getPlaceMarkTitle().equals("label")) {
                    toUpdatePlaceMark = placemark;
                    toUpdatePlaceMark.addPlaceMarkImgItemList(item);
                    hasFind = true;
                    break;
                }
                ++i;
            }

            if(hasFind) {
                this.placeMarkDataList.set(i, toUpdatePlaceMark);
                this.notifyItemChanged(i);
            }
        }
    }

    public void addPlaceMarkImg(@NonNull final ItemPlaceMarkImgData item) {
        if(this.placeMarkDataList == null || this.placeMarkDataList.isEmpty()) return;
        ItemPlaceMarkEnhancedData toUpdatePlaceMark = null;
        boolean hasFind = false;
        int i = 0;

        for(ItemPlaceMarkEnhancedData placemark : this.placeMarkDataList) {
            if(placemark.getPlaceMarkType().equals(item.getPlaceMarkType()) && !placemark.getPlaceMarkTitle().equals("label")) {
                toUpdatePlaceMark = placemark;
                toUpdatePlaceMark.addPlaceMarkImgItemList(item);
                hasFind = true;
                break;
            }
            ++i;
        }

        if(hasFind) {
            this.placeMarkDataList.set(i, toUpdatePlaceMark);
            this.notifyItemChanged(i);
        }
    }

    public void removePlaceMarkImg(@NonNull final ItemPlaceMarkImgData item) {
        if(this.placeMarkDataList == null || this.placeMarkDataList.isEmpty()) return;
        ItemPlaceMarkEnhancedData toRemovePlaceMark = null;
        boolean hasFind = false;
        int i = 0;

        for(ItemPlaceMarkEnhancedData placemark : this.placeMarkDataList) {
            if(placemark.getPlaceMarkType().equals(item.getPlaceMarkType()) && placemark.getPlaceMarkTitle().equals("label")) {
                toRemovePlaceMark = placemark;
                toRemovePlaceMark.removePlaceMarkImgItemList(item);
                hasFind = true;
                break;
            }
            ++i;
        }

        if(hasFind) {
            this.placeMarkDataList.set(i, toRemovePlaceMark);
            this.notifyItemChanged(i);
        }
    }

    public void updatePlaceMark(@NonNull final ItemPlaceMarkEnhancedData item) {
        if(this.placeMarkDataList == null) return;

        int index = 0;
        for(ItemPlaceMarkEnhancedData buffer : this.placeMarkDataList) {
            if(buffer.getPlaceMarkType().equals(item.getPlaceMarkType()) && buffer.getPlaceMarkTitle().equals(item.getPlaceMarkTitle())) {
                this.placeMarkDataList.set(index, item);
                break;
            }
            ++index;
        }
        this.notifyDataSetChanged();
    }

    private int getItemPosition(@NonNull final ItemPlaceMarkEnhancedData item) {
        if(this.placeMarkDataList == null || this.placeMarkDataList.isEmpty()) return -1;

        int pos = this.placeMarkDataList.indexOf(item);
        return pos;
    }

    public void setPlaceMarksIsHidden(boolean isHide) {
        if(this.bind == null || this.placeMarkDataList == null) return;
        for(ItemPlaceMarkEnhancedData item : this.placeMarkDataList) {
            item.setPlaceMarkHidden(isHide);
        }
        this.notifyDataSetChanged();
    }

    public void release() {
        if(this.placeMarkDataList != null) {
            this.placeMarkDataList.clear();
            this.placeMarkDataList = null;
        }
        this.bind = null;
    }

    // INNER CLASS MIGHT BE STATIC CLASS
    // FOR PREVENT MEMORY LEAK
    public static class PlacemarkTypeViewHolder extends RecyclerView.ViewHolder {
        private ItemPlaceMarkEnhancedData placeMarkData = null;
        private final ItemPlacemarkEnhancedBinding bind;
        private PlaceMarkImgRecyclerAdapter imgRecyclerAdapter;
        private PlaceMarkEnhancedRecyclerAdapter.PlacemarkTypeViewHolder.OnImageSelectedListener imgSelectedListener = null;

        public interface OnImageSelectedListener {
            void onImageSelect(ItemPlaceMarkImgData imgData, int pos);
        }
        public void setViewEnableEvent(final boolean isEnableEvent, final boolean alsoSwitchEventChange) {
            if(alsoSwitchEventChange) {
                this.bind.placemarkEnabled.setEnabled(isEnableEvent);
                this.bind.placemarkEnabled.setFocusable(isEnableEvent);
                this.bind.placemarkEnabled.setClickable(isEnableEvent);
            }
            this.bind.placemarkEnhancedLayout.setBackgroundResource(isEnableEvent? R.drawable.background_round_border_16: R.drawable.gray_round_border_16);
            this.bind.placemarkAddImg.setEnabled(isEnableEvent);
            this.bind.placemarkAddImg.setFocusable(isEnableEvent);
            this.bind.placemarkAddImg.setClickable(isEnableEvent);

            this.bind.placemarkImgList.setEnabled(isEnableEvent);
            this.bind.placemarkImgList.setFocusable(isEnableEvent);
            this.bind.placemarkImgList.setClickable(isEnableEvent);
        }

        public PlacemarkTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            this.bind = ItemPlacemarkEnhancedBinding.bind(itemView);
        }

        public void onAddPlaceMarkInfoBind() {
            this.bind.placemarkAddInfoLayout.setVisibility(View.VISIBLE);
            this.bind.placemarkEnhancedLayout.setVisibility(View.GONE);

            final String information = this.bind.placemarkAddInfo.getText().toString();
            SpannableString emphaticInformation = new SpannableString(information);
            emphaticInformation.setSpan(new ForegroundColorSpan(Color.parseColor("#880000")), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            emphaticInformation.setSpan(new StyleSpan(Typeface.BOLD), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            this.bind.placemarkAddInfo.setText(emphaticInformation);
        }

        public void onBind(@NonNull final ItemPlaceMarkEnhancedData item, PlaceMarkEnhancedRecyclerAdapter.PlacemarkTypeViewHolder.OnImageSelectedListener listener) {
            this.placeMarkData = item;

            this.bind.placemarkTypeTitle.setText(this.placeMarkData.getPlaceMarkTitle());
            this.bind.placemarkEnabled.setChecked(this.placeMarkData.getPlaceMarkEnable());
            this.imgRecyclerAdapter = new PlaceMarkImgRecyclerAdapter(new PlaceMarkImgRecyclerAdapter.OnImageClickListener() {
                @Override
                public void onImageClick(ItemPlaceMarkImgData placemarkItem, int pos) {
                    listener.onImageSelect(placemarkItem, pos);
                }
            });
            this.imgRecyclerAdapter.setItems(this.placeMarkData.getPlaceMarkImgItemList());
            this.bind.placemarkImgList.setAdapter(this.imgRecyclerAdapter);

            LinearLayoutManager layoutManager = new LinearLayoutManager(this.bind.getRoot().getContext(), LinearLayoutManager.HORIZONTAL, false);
            this.bind.placemarkImgList.setLayoutManager(layoutManager);

            if(item.getPlaceMarkType().equals(PlaceMarkType.ENTRANCE.name())) {
                this.bind.placemarkEnabled.setVisibility(View.GONE);
                this.bind.placemarkEnabled.setEnabled(false);
                this.bind.placemarkEnabled.setClickable(false);
                this.bind.placemarkEnabled.setFocusable(false);
            }

            // when it hide by bottom sheet, DONT ACTIVE CLICK EVENT
            if(item.isPlaceMarkHidden()) {
                this.setViewEnableEvent(item.isPlaceMarkEnable(), true);

            } else {
                this.setViewEnableEvent(item.isPlaceMarkEnable(), false);
            }
            this.imgSelectedListener = listener;
        }
    }
}
