package eu.basicairdata.graziano.gpslogger.recording.enhanced;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import java.lang.ref.WeakReference;

import eu.basicairdata.graziano.gpslogger.databinding.DialogImageDetailBinding;

public class ImageDetailDialog extends Dialog {
    private DialogImageDetailBinding bind;
    private WeakReference<Context> localContext;

    private OnRemoveBtnClickedListener listener;

    interface OnRemoveBtnClickedListener {
        void onRemoveBtnClicked(ItemPlaceMarkImgData removeImgData);
    }

    ItemPlaceMarkImgData imgData;
    public ImageDetailDialog(@NonNull Context context, @NonNull final ItemPlaceMarkImgData imgData, @NonNull final  OnRemoveBtnClickedListener listener) {
        super(context);
        this.bind = DialogImageDetailBinding.inflate(getLayoutInflater());
        this.localContext = new WeakReference<>(context);
        this.imgData = imgData;
        this.listener = listener;
    }

    @Override
    public void show() {
        this.setContentView(this.bind.getRoot());
        this.bind.imgTitle.setText(this.imgData.getPlaceMarkType());

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
