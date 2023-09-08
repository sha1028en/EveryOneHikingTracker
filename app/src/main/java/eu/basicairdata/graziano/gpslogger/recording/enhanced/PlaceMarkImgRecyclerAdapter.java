package eu.basicairdata.graziano.gpslogger.recording.enhanced;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;

import java.util.LinkedList;

import eu.basicairdata.graziano.gpslogger.databinding.ItemPlacemarkImgBinding;

public class PlaceMarkImgRecyclerAdapter extends RecyclerView.Adapter<PlaceMarkImgRecyclerAdapter.PlaceMarkImgViewHolder> {
    private LinkedList<ItemPlaceMarkImgData> placeMarkImgList;
    private ItemPlacemarkImgBinding bind;
    public PlaceMarkImgRecyclerAdapter() {
        this.placeMarkImgList = new LinkedList<>();
    }

    @NonNull @Override
    public PlaceMarkImgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        this.bind = ItemPlacemarkImgBinding.inflate(layoutInflater, parent, false);

        PlaceMarkImgViewHolder holder = new PlaceMarkImgViewHolder(this.bind.getRoot());
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceMarkImgViewHolder holder, int position) {
        holder.onBind(this.placeMarkImgList.get(position));
    }

    @Override
    public void onViewRecycled(@NonNull PlaceMarkImgViewHolder holder) {
        holder.reBind(); //onBind(this.placeMarkImgList.get(holder.getBindingAdapterPosition()));
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return this.placeMarkImgList.size();
    }

    public void setItems(@NonNull final LinkedList<ItemPlaceMarkImgData> items) {
        if(this.placeMarkImgList != null) this.placeMarkImgList.clear();
        this.placeMarkImgList = items;
        this.notifyDataSetChanged();
    }


    public static class PlaceMarkImgViewHolder extends RecyclerView.ViewHolder {
        private final ItemPlacemarkImgBinding bind;
        private ItemPlaceMarkImgData itemImg;

        public PlaceMarkImgViewHolder(@NonNull View itemView) {
            super(itemView);
            this.bind = ItemPlacemarkImgBinding.bind(itemView);
        }

        public void onBind(@NonNull final ItemPlaceMarkImgData itemImg) {
            this.itemImg = itemImg;
            Glide.with(this.bind.placemarkImgView.getContext())
                    .load(this.itemImg.getImageUrl())
                    .into(this.bind.placemarkImgView);
        }

        void reBind() {
            if(this.bind == null || this.itemImg == null) return;

            this.bind.placemarkImgView.destroyDrawingCache();
            Glide.with(this.bind.placemarkImgView.getContext())
                    .setDefaultRequestOptions(RequestOptions.noAnimation())
                    .setDefaultRequestOptions(RequestOptions.formatOf(DecodeFormat.PREFER_RGB_565))
                    .load(this.itemImg.getImageUrl())
                    .into(this.bind.placemarkImgView);
        }
    }
}
