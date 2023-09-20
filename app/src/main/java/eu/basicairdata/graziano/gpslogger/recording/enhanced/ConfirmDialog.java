package eu.basicairdata.graziano.gpslogger.recording.enhanced;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;

import eu.basicairdata.graziano.gpslogger.databinding.DialogConfirmBinding;

public class ConfirmDialog extends Dialog {
    private final DialogConfirmBinding bind;
    private final OnDialogActionListener dialogActionListener;
    private String userCustomComment;

    public interface OnDialogActionListener {
        void onConfirmClick();
        void onCancelClick();
    }

    public ConfirmDialog(@NonNull Context context, @NonNull final OnDialogActionListener listener) {
        super(context);
        this.bind = DialogConfirmBinding.inflate(this.getLayoutInflater());
        this.dialogActionListener = listener;
    }

    public ConfirmDialog(@NonNull Context context, @NonNull final String comment, @NonNull final OnDialogActionListener listener) {
        super(context);
        this.bind = DialogConfirmBinding.inflate(this.getLayoutInflater());
        this.dialogActionListener = listener;
        this.userCustomComment = comment;
    }

    @Override
    public void show() {
        this.setContentView(this.bind.getRoot());

        // Custom Comment
        if(this.userCustomComment == null || !this.userCustomComment.isBlank()) {
            this.bind.comment.setText(this.userCustomComment);
        }

        this.bind.confirmButton.setOnClickListener(v -> {
            this.dialogActionListener.onConfirmClick();
            this.dismiss();
        });

        this.bind.cancelButton.setOnClickListener(v -> {
            this.dialogActionListener.onCancelClick();
            this.dismiss();
        });
        super.show();
    }
}