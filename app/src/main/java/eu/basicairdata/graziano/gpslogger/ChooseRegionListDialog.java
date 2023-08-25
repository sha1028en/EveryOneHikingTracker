package eu.basicairdata.graziano.gpslogger;

import androidx.annotation.NonNull;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;

import eu.basicairdata.graziano.gpslogger.databinding.DialogChooseRegionListBinding;

public class ChooseRegionListDialog extends Dialog implements CompoundButton.OnCheckedChangeListener {

    private DialogChooseRegionListBinding binding;
    private onItemChooseListener chooseListener;

    interface onItemChooseListener {
        void onItemChoose(String msg, boolean isChecked);
    }

    protected ChooseRegionListDialog(@NonNull Context context, @NonNull onItemChooseListener listener) {
        super(context);
        this.chooseListener = listener;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        this.chooseListener.onItemChoose(buttonView.getText().toString(), isChecked);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = DialogChooseRegionListBinding.inflate(this.getLayoutInflater());
        setContentView(this.binding.getRoot());

        this.binding.regionChooseConfirm.setOnClickListener(v -> {
            this.chooseListener.onItemChoose(((Button) v).getText().toString(), true);
            this.dismiss();
        });

        this.binding.chooseRegionSeoul.setOnCheckedChangeListener(this);
        this.binding.chooseRegionGueonggi.setOnCheckedChangeListener(this);
        this.binding.chooseRegionIncheon.setOnCheckedChangeListener(this);
        this.binding.chooseRegionGangwon.setOnCheckedChangeListener(this);
        this.binding.chooseRegionSejong.setOnCheckedChangeListener(this);
        this.binding.chooseRegionNorthChungchung.setOnCheckedChangeListener(this);
        this.binding.chooseRegionDaejeon.setOnCheckedChangeListener(this);
        this.binding.chooseRegionSouthChungchung.setOnCheckedChangeListener(this);
        this.binding.chooseRegionDaegu.setOnCheckedChangeListener(this);
        this.binding.chooseRegionNorthGyeongsang.setOnCheckedChangeListener(this);
        this.binding.chooseRegionUlsan.setOnCheckedChangeListener(this);
        this.binding.chooseRegionSouthGyeongsang.setOnCheckedChangeListener(this);
        this.binding.chooseRegionBusan.setOnCheckedChangeListener(this);
        this.binding.chooseRegionNorthJeolla.setOnCheckedChangeListener(this);
        this.binding.chooseRegionGwangju.setOnCheckedChangeListener(this);
        this.binding.chooseRegionSouthJeolla.setOnCheckedChangeListener(this);
        this.binding.chooseRegionJeju.setOnCheckedChangeListener(this);
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