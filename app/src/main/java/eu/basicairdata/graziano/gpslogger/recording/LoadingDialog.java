package eu.basicairdata.graziano.gpslogger.recording;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.view.Window;
import android.view.WindowManager;

import java.lang.ref.WeakReference;

import eu.basicairdata.graziano.gpslogger.databinding.DialogLoadingBinding;

public class LoadingDialog extends Dialog {
    private DialogLoadingBinding bind;
    private WeakReference<Context> localContext;

    public LoadingDialog(@NonNull Context context) {
        super(context);
        this.localContext = new WeakReference<>(context);
        this.bind = DialogLoadingBinding.inflate(this.getLayoutInflater());
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(this.bind.getRoot());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // cant cancel when touch outside
        this.setCancelable(false);

        // for blank BackGround
        Window window = this.getWindow();
        if(window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if(this.localContext != null) {
            this.localContext.clear();
            this.localContext = null;
        }
        this.bind = null;
    }
}