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
    ItemPlaceMarkImgData imgData;
    public ImageDetailDialog(@NonNull Context context, @NonNull final ItemPlaceMarkImgData imgData) {
        super(context);
        this.bind = DialogImageDetailBinding.inflate(getLayoutInflater());
        this.localContext = new WeakReference<>(context);
        this.imgData = imgData;
    }

    @Override
    public void show() {
        this.setContentView(this.bind.getRoot());

        Glide.with(this.bind.imgview)
                .load(this.imgData.getImageUrl())
                .into(this.bind.imgview);

        this.bind.removeImg.setOnClickListener(v -> {

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
    }
}
