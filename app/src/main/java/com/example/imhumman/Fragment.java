package com.example.imhumman;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;

public class Fragment extends DialogFragment {
    private String infoToEdit;
    private TextInputEditText edtInput;
    private Button btnAddWritten;
    private dialogFragmentListener listener;
    private TextWatcher InputWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

//            if (charSequence.length() > passwordInputLayout.getCounterMaxLength()) {
//                passwordInputLayout.setError("password is too long");
//            } else {
//                passwordInputLayout.setError(null);
//            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
    private View.OnClickListener addInfoListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String usernameValue = edtInput.getText().toString();

            listener.applyText(usernameValue);
            dismiss();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            infoToEdit = getArguments().getString("info");
        }

        return inflater.inflate(R.layout.fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        super.onViewCreated(view, savedInstanceState);

        btnAddWritten = view.findViewById(R.id.btnAddWritten);
        btnAddWritten.setOnClickListener(addInfoListener);

        edtInput = view.findViewById(R.id.edtInput);
        edtInput.setText(infoToEdit);
        edtInput.addTextChangedListener(InputWatcher);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (dialogFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement fragment");
        }
    }

    public interface dialogFragmentListener {
        void applyText(String txt);
    }

}

