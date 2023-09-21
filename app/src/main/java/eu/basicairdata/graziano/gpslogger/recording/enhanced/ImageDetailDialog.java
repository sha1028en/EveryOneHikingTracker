package eu.basicairdata.graziano.gpslogger.recording.enhanced;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import java.io.Serializable;
import java.lang.ref.WeakReference;

import eu.basicairdata.graziano.gpslogger.databinding.DialogImageDetailBinding;

public class ImageDetailDialog extends Dialog implements Serializable {
    private DialogImageDetailBinding bind;
    private WeakReference<Context> localContext;

    private OnRemoveBtnClickedListener listener;

    interface OnRemoveBtnClickedListener {
        void onRemoveBtnClicked(ItemPlaceMarkImg removeImgData);
    }

    ItemPlaceMarkImg imgData;
    public ImageDetailDialog(@NonNull Context context, @NonNull final ItemPlaceMarkImg imgData, @NonNull final  OnRemoveBtnClickedListener listener) {
        super(context);
        this.bind = DialogImageDetailBinding.inflate(getLayoutInflater());
        this.localContext = new WeakReference<>(context);
        this.imgData = imgData;
        this.listener = listener;
    }

    private String convertPlacemarkTypeToName(String type) {
        if(type == null) return "기타 시설물";
        final String placemarkType;

        switch (type) {
            case "ENTRANCE" -> placemarkType = "나눔길 입구";
            case "PARKING" -> placemarkType = "주차장";
            case "TOILET" -> placemarkType = "화장실";
            case "REST_AREA" -> placemarkType = "쉼터";
            case "BUS_STOP" -> placemarkType = "버스";
            case "OBSERVATION_DECK" -> placemarkType = "전망대";
            case "ETC" -> placemarkType = "기타 시설물";
            default -> placemarkType = "기타 시설물";
        }
        return placemarkType;
    }

    @Override
    public void show() {
        this.setContentView(this.bind.getRoot());
        this.bind.imgTitle.setText(this.convertPlacemarkTypeToName(this.imgData.getPlaceMarkType()));

        Glide.with(this.bind.imgview)
                .load(this.imgData.getImageUrl())
                .into(this.bind.imgview);

        this.bind.removeImg.setOnClickListener(v -> {
            if(this.listener != null) this.listener.onRemoveBtnClicked(this.imgData);
            this.dismiss();
        });

        this.bind.cancel.setOnClickListener(v -> {
            this.dismiss();
        });

        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();

        if(this.localContext != null) {
            this.localContext.clear();
            this.localContext = null;
        }

        this.bind = null;
        this.imgData = null;
        this.listener = null;
    }
}
