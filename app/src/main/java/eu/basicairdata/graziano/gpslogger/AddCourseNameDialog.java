package eu.basicairdata.graziano.gpslogger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


import eu.basicairdata.graziano.gpslogger.databinding.DialogAddCourseBinding;
import eu.basicairdata.graziano.gpslogger.management.TrackRecordManager;


public class AddCourseNameDialog  extends DialogFragment {
    private DialogAddCourseBinding bind;
    private MessageReceiveListener callBackMessagelistener;

    interface MessageReceiveListener {
        void onReceiveMessage(final String receiveMessage);
    }

    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        bind = DialogAddCourseBinding.inflate(this.getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(this.bind.getRoot().getContext());
        builder.setTitle(R.string.dlg_add_course);

        this.bind.courseNameDescription.postDelayed(() -> {
            this.bind.courseNameDescription.requestFocus();
            InputMethodManager inputManager = (InputMethodManager) this.bind.getRoot().getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(this.bind.courseNameDescription, InputMethodManager.SHOW_IMPLICIT);
        }, 200L);

        builder.setView(this.bind.getRoot())
                .setPositiveButton(R.string.dlg_button_add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TrackRecordManager recordManager = TrackRecordManager.getInstance();
                        if(isAdded() && recordManager != null && callBackMessagelistener != null) {
                            String addedCourseName = bind.courseNameDescription.getText().toString();
                            callBackMessagelistener.onReceiveMessage(addedCourseName);
                        }
                    }
                });

        return builder.create();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onDestroyView() {
        this.callBackMessagelistener = null;
        this.bind = null;
        super.onDestroyView();
    }

    public void setOnReceiveMessage(@NonNull final MessageReceiveListener listener) {
        this.callBackMessagelistener = listener;
    }
}
