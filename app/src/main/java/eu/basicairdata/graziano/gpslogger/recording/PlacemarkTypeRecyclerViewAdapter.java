package eu.basicairdata.graziano.gpslogger.recording;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.Collections;
import java.util.LinkedList;

import eu.basicairdata.graziano.gpslogger.R;
import eu.basicairdata.graziano.gpslogger.management.PlaceMarkType;
import eu.basicairdata.graziano.gpslogger.databinding.ItemPlacemarkTypeBinding;

public class PlacemarkTypeRecyclerViewAdapter extends RecyclerView.Adapter<PlacemarkTypeRecyclerViewAdapter.PlacemarkTypeViewHolder> {
    private LinkedList<ItemPlaceMarkData> placeMarkDataList;
    private ItemPlacemarkTypeBinding bind;
    private PlacemarkTypeViewHolder.OnImageSelectedListener imgSelectedListener;

    private PlacemarkTypeRecyclerViewAdapter() {
        this.placeMarkDataList = new LinkedList<>();

        ItemPlaceMarkData tmpData = new ItemPlaceMarkData("label", "label", PlaceMarkType.ETC.name(), "label", false);
        this.placeMarkDataList.add(tmpData);
//
//        tmpData = new ItemPlaceMarkData(trackName, "주차장", PlaceMarkType.PARKING.name(), "", true);
//        this.placeMarkDataList.add(tmpData);
//
//        tmpData = new ItemPlaceMarkData(trackName, "화장실 1", PlaceMarkType.TOILET.name(), "", true);
//        this.placeMarkDataList.add(tmpData);
//
//        tmpData = new ItemPlaceMarkData(trackName, "휴계공간 1", PlaceMarkType.REST.name(), "", true);
//        this.placeMarkDataList.add(tmpData);
//
//        tmpData = new ItemPlaceMarkData(trackName, "버스정류장", PlaceMarkType.BUS_STOP.name(), "", true);
//        this.placeMarkDataList.add(tmpData);
//
//        tmpData = new ItemPlaceMarkData(trackName,"전망데크 1", PlaceMarkType.OBSERVATION_DECK.name(), "", true);
//        this.placeMarkDataList.add(tmpData);
//
//        tmpData = new ItemPlaceMarkData(trackName,"기타 시설물 1", PlaceMarkType.ETC.name(), "", true);
//        this.placeMarkDataList.add(tmpData);
    }

    public PlacemarkTypeRecyclerViewAdapter(@NonNull final PlacemarkTypeViewHolder.OnImageSelectedListener listener) {
        this();
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
        if(holder.getBindingAdapterPosition() != 0) {
            holder.onBind(this.placeMarkDataList.get(holder.getBindingAdapterPosition()), this.imgSelectedListener);

            this.bind.placemarkEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> holder.setViewEnableEvent(isChecked, false));

            this.bind.placeamrkAddMore.setOnClickListener(v -> {
                if(this.bind.placeamrkAddMore.getVisibility() != View.VISIBLE || this.placeMarkDataList == null || this.placeMarkDataList.isEmpty()) return;
                ItemPlaceMarkData currentItem = this.placeMarkDataList.get(holder.getBindingAdapterPosition());

                int index = currentItem.getPlaceMarkType().equals(PlaceMarkType.ETC.name())? 0 : 1;
                for(ItemPlaceMarkData buffer : this.placeMarkDataList) {
                    if(buffer.getPlaceMarkType().equals(currentItem.getPlaceMarkType())) {
                        ++index;
                    }
                }
                final String newPlacemarkParent = currentItem.getTrackName();
                final String newPlacemarkTitle = currentItem.getPlaceMarkTitle().substring(0, currentItem.getPlaceMarkTitle().length() -3) + String.format(" %02d", index);
                final String newPlacemarkType = currentItem.getPlaceMarkType();
                final String newPlacemarkDesc = ""; // currentItem.getPlaceMarkDesc().substring(0, currentItem.getPlaceMarkDesc().length() -2) + " " + index;
                ItemPlaceMarkData item = new ItemPlaceMarkData(newPlacemarkParent, newPlacemarkTitle, newPlacemarkType, newPlacemarkDesc, true);

                this.addPlaceMark(item, position +1);
            });

        } else {
            holder.onAddPlaceMarkInfoBind();
        }
    }

    @Override
    public void onViewRecycled(@NonNull PlacemarkTypeViewHolder holder) {
        super.onViewRecycled(holder);
        holder.replaceImg();
//        holder.onBind(this.placeMarkDataList.get(holder.getBindingAdapterPosition()), this.imgSelectedListener);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return this.placeMarkDataList.size();
    }

    // if this method isnt overriding, sometimes detached view replaced other correct view
    // so this list might be corrupt
    // i didnt know what happened, but... problem resolved to override this method
    @Override
    public int getItemViewType(int position) {
        return position; // super.getItemViewType(position);
    }

    /**
     * add item at last position
     * @param item added Item data
     */
    public void addPlaceMark(@NonNull final ItemPlaceMarkData item) {
        if(this.placeMarkDataList == null) return;

        this.placeMarkDataList.add(item);
//        this.notifyDataSetChanged();
        this.notifyItemInserted(this.placeMarkDataList.size());
    }

    public void addPlaceMark(@NonNull final ItemPlaceMarkData item, final int pos) {
        if(pos < 0) throw new IllegalArgumentException("PlacemarkTypeRecyclerViewAdapter.addPlaceMark(item, pos) : wrong value pos " + pos);

        this.placeMarkDataList.add(pos, item);
        Collections.sort(this.placeMarkDataList);
        this.notifyDataSetChanged();
//        this.notifyItemInserted(pos);
    }

    public void updatePlaceMark(@NonNull final ItemPlaceMarkData item) {
        if(this.placeMarkDataList == null) return;

        int index = 0;
        for(ItemPlaceMarkData buffer : this.placeMarkDataList) {
            if(buffer.getPlaceMarkType().equals(item.getPlaceMarkType()) && buffer.getPlaceMarkTitle().equals(item.getPlaceMarkTitle())) {
                this.placeMarkDataList.set(index, item);
                break;
            }
            ++index;
        }
        this.notifyDataSetChanged();
    }

    private int getItemPosition(@NonNull final ItemPlaceMarkData item) {
        if(this.placeMarkDataList == null || this.placeMarkDataList.isEmpty()) return -1;

        int pos = this.placeMarkDataList.indexOf(item);
        return pos;
    }

    public void setPlaceMarksIsHidden(boolean isHide) {
        if(this.bind == null || this.placeMarkDataList == null) return;
        for(ItemPlaceMarkData item : this.placeMarkDataList) {
            item.setPlaceMarkHidden(isHide);
        }
        this.notifyDataSetChanged();
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

    // INNER CLASS MIGHT BE STATIC CLASS
    // FOR PREVENT MEMORY LEAK
    public static class PlacemarkTypeViewHolder extends RecyclerView.ViewHolder {
        private ItemPlaceMarkData placeMarkData = null;
        private final ItemPlacemarkTypeBinding bind;
        private OnImageSelectedListener imgSelectedListener = null;

        public interface OnImageSelectedListener {
            void onSelected(ItemPlaceMarkData placeMarkData, int pos);
        }

        public void setViewEnableEvent(final boolean isEnableEvent, final boolean alsoSwitchEventChange) {
            this.bind.placemarkDisabledLayout.setVisibility(isEnableEvent? View.GONE: View.VISIBLE);
            this.bind.placemarkLayout.setBackgroundResource(isEnableEvent? R.drawable.background_round_border_8 : R.drawable.gray_round_border_16);

            this.bind.placeamrkAddMore.setEnabled(isEnableEvent);
            this.bind.placeamrkAddMore.setFocusable(isEnableEvent);
            this.bind.placeamrkAddMore.setClickable(isEnableEvent);

            this.bind.placemarkPic0.setEnabled(isEnableEvent);
            this.bind.placemarkPic0.setFocusable(isEnableEvent);
            this.bind.placemarkPic0.setClickable(isEnableEvent);

            this.bind.placemarkPic1.setEnabled(isEnableEvent);
            this.bind.placemarkPic1.setFocusable(isEnableEvent);
            this.bind.placemarkPic1.setClickable(isEnableEvent);

            this.bind.placemarkPic2.setEnabled(isEnableEvent);
            this.bind.placemarkPic2.setFocusable(isEnableEvent);
            this.bind.placemarkPic2.setClickable(isEnableEvent);

            if(alsoSwitchEventChange) {
                this.bind.placemarkEnabled.setEnabled(isEnableEvent);
                this.bind.placemarkEnabled.setFocusable(isEnableEvent);
                this.bind.placemarkEnabled.setClickable(isEnableEvent);
            }
        }

        public PlacemarkTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            this.bind = ItemPlacemarkTypeBinding.bind(itemView);
        }

        public void onAddPlaceMarkInfoBind() {
            this.bind.placemarkAddInfoLayout.setVisibility(View.VISIBLE);
            this.bind.placemarkLayout.setVisibility(View.GONE);

            final String information = this.bind.placemarkAddInfo.getText().toString();
            SpannableString emphaticInformation = new SpannableString(information);
            emphaticInformation.setSpan(new ForegroundColorSpan(Color.parseColor("#880000")), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            emphaticInformation.setSpan(new StyleSpan(Typeface.BOLD), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            this.bind.placemarkAddInfo.setText(emphaticInformation);
        }

        public void replaceImg() {
            if(this.placeMarkData == null || this.bind == null) return;

            if(this.placeMarkData.getPlaceMarkImg0() != null) this.bind.placemarkPic0.setImageBitmap(this.placeMarkData.getPlaceMarkImg0());
            if(this.placeMarkData.getPlaceMarkImg1() != null) this.bind.placemarkPic1.setImageBitmap(this.placeMarkData.getPlaceMarkImg1());
            if(this.placeMarkData.getPlaceMarkImg2() != null) this.bind.placemarkPic2.setImageBitmap(this.placeMarkData.getPlaceMarkImg2());
        }


        public void onBind(@NonNull final ItemPlaceMarkData item, OnImageSelectedListener listener) {
            this.placeMarkData = item;
            this.bind.placemarkTypeTitle.setText(this.placeMarkData.getPlaceMarkTitle());
            this.bind.placemarkEnabled.setChecked(this.placeMarkData.getPlaceMarkEnable());
//            this.bind.placemarkInformation.setText(this.placeMarkData.getPlaceMarkDesc());


            // NOT FAR FEATURE WILL BE CHANGE TO SET IMAGE
            // TODO IMAGE WILL BE LOADED BY SERVER
            if(this.placeMarkData.getPlaceMarkImg0() != null) {
                Glide.with(this.bind.placemarkPic0)
                        .load(this.placeMarkData.getPlaceMarkImg0())
                        .into(this.bind.placemarkPic0);
            }

            if(this.placeMarkData.getPlaceMarkImg1() != null) {
                Glide.with(this.bind.placemarkPic1)
                        .load(this.placeMarkData.getPlaceMarkImg1())
                        .into(this.bind.placemarkPic1);
            }

            if(this.placeMarkData.getPlaceMarkImg2() != null) {
                Glide.with(this.bind.placemarkPic2)
                        .load(this.placeMarkData.getPlaceMarkImg2())
                        .into(this.bind.placemarkPic2);
            }

            if(this.placeMarkData.getPlaceMarkType().equals(PlaceMarkType.ENTRANCE.name()) ||
                    this.placeMarkData.getPlaceMarkType().equals(PlaceMarkType.PARKING.name()) ||
                    this.placeMarkData.getPlaceMarkType().equals(PlaceMarkType.BUS_STOP.name())) {
                this.bind.placeamrkAddMore.setVisibility(View.GONE);

            } else {
                this.bind.placeamrkAddMore.setVisibility(View.VISIBLE);
            }

            // when it hide by bottom sheet, DONT ACTIVE CLICK EVENT
            if(item.isPlaceMarkHidden()) {
                this.setViewEnableEvent(false, true);

            } else {
                this.setViewEnableEvent(item.isPlaceMarkEnable(), true);
            }
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
    }
}
