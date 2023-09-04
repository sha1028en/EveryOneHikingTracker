package eu.basicairdata.graziano.gpslogger.tracklist;

import androidx.annotation.NonNull;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import eu.basicairdata.graziano.gpslogger.databinding.DialogChooseRegionListBinding;

public class ChooseRegionListDialog extends Dialog implements View.OnClickListener {

    private DialogChooseRegionListBinding binding;
    private onItemChooseListener chooseListener;

    @Override
    public void onClick(View v) {
        if(v instanceof TextView) {
            final String region = ((TextView) v).getText().toString();
            this.chooseListener.onItemChoose(region, true);
        }
        this.dismiss();
    }

    public interface onItemChooseListener {
        void onItemChoose(String msg, boolean isChecked);
    }

    public ChooseRegionListDialog(@NonNull Context context, @NonNull onItemChooseListener listener) {
        super(context);
        this.chooseListener = listener;
    }

//    @Override
//    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        this.chooseListener.onItemChoose(buttonView.getText().toString(), isChecked);
//        this.dismiss();
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = DialogChooseRegionListBinding.inflate(this.getLayoutInflater());
        setContentView(this.binding.getRoot());

//        this.binding.regionChooseConfirm.setOnClickListener(v -> {
//            this.chooseListener.onItemChoose(((Button) v).getText().toString(), true);
//            this.dismiss();
//        });

        this.binding.chooseRegionSeoul.setOnClickListener(this);
        this.binding.chooseRegionGueonggi.setOnClickListener(this);
        this.binding.chooseRegionIncheon.setOnClickListener(this);
        this.binding.chooseRegionGangwon.setOnClickListener(this);
        this.binding.chooseRegionSejong.setOnClickListener(this);
        this.binding.chooseRegionNorthChungchung.setOnClickListener(this);
        this.binding.chooseRegionDaejeon.setOnClickListener(this);
        this.binding.chooseRegionSouthChungchung.setOnClickListener(this);
        this.binding.chooseRegionDaegu.setOnClickListener(this);
        this.binding.chooseRegionNorthGyeongsang.setOnClickListener(this);
        this.binding.chooseRegionUlsan.setOnClickListener(this);
        this.binding.chooseRegionSouthGyeongsang.setOnClickListener(this);
        this.binding.chooseRegionBusan.setOnClickListener(this);
        this.binding.chooseRegionNorthJeolla.setOnClickListener(this);
        this.binding.chooseRegionGwangju.setOnClickListener(this);
        this.binding.chooseRegionSouthJeolla.setOnClickListener(this);
        this.binding.chooseRegionJeju.setOnClickListener(this);
    }

    /**
     * dismiss dialog and release resources
     */
    @Override
    public void dismiss() {
        this.chooseListener = null;
        this.binding = null;

        super.dismiss();
    }
}