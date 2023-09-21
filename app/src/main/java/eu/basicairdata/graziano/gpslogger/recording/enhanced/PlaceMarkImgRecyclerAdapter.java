package eu.basicairdata.graziano.gpslogger.recording.enhanced;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;

import java.io.Serializable;
import java.util.LinkedList;

import eu.basicairdata.graziano.gpslogger.databinding.ItemPlacemarkImgBinding;

public class PlaceMarkImgRecyclerAdapter extends RecyclerView.Adapter<PlaceMarkImgRecyclerAdapter.PlaceMarkImgViewHolder> implements Serializable {
    private LinkedList<ItemPlaceMarkImg> placeMarkImgList;
    private ItemPlacemarkImgBinding bind;
    private OnImageClickListener listener;

    interface OnImageClickListener {
        void onImageClick(ItemPlaceMarkImg placemarkItem, int pos);
    }

    private PlaceMarkImgRecyclerAdapter() {
        this.placeMarkImgList = new LinkedList<>();
    }

    public PlaceMarkImgRecyclerAdapter(@NonNull final OnImageClickListener listener) {
        this();
        this.listener = listener;
    }

    @NonNull @Override
    public PlaceMarkImgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        this.bind = ItemPlacemarkImgBinding.inflate(layoutInflater, parent, false);

        PlaceMarkImgViewHolder holder = new PlaceMarkImgViewHolder(this.bind.getRoot(), this.listener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceMarkImgViewHolder holder, int position) {
        holder.onBind(this.placeMarkImgList.get(position), position +1);
    }

    @Override
    public int getItemCount() {
        return this.placeMarkImgList.size();
    }

    public void setItems(@NonNull final LinkedList<ItemPlaceMarkImg> items) {
        if(this.placeMarkImgList != null) this.placeMarkImgList.clear();
        this.placeMarkImgList = items;
        this.notifyDataSetChanged();
    }

    public static class PlaceMarkImgViewHolder extends RecyclerView.ViewHolder implements Serializable {
        private final ItemPlacemarkImgBinding bind;
        private ItemPlaceMarkImg itemImg;
        private final OnImageClickListener listener;
//        private boolean isEnable = true;

        public PlaceMarkImgViewHolder(@NonNull View itemView, @NonNull final OnImageClickListener listener) {
            super(itemView);
            this.bind = ItemPlacemarkImgBinding.bind(itemView);
            this.listener = listener;
        }

        public void onBind(@NonNull final ItemPlaceMarkImg itemImg, final int position) {
            this.itemImg = itemImg;

            Glide.with(this.bind.placemarkImgView.getContext())
                    .setDefaultRequestOptions(RequestOptions.noAnimation())
                    .setDefaultRequestOptions(RequestOptions.formatOf(DecodeFormat.PREFER_RGB_565))
                    .load(this.itemImg.getImageUrl())
                    .into(this.bind.placemarkImgView);

            this.bind.placemarkImgView.setOnClickListener(v -> {
                listener.onImageClick(this.itemImg, this.getBindingAdapterPosition());
            });

            this.bind.imgNumberTxt.setText(String.valueOf(position));
        }
    }
}
