package com.example.smartalert_p20191;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

//insert sth firebase
//eidos,comments,photo,timestamp
public class SubmissionFragment extends Fragment {
    public SubmissionFragment() {
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_submission, container, false);
        Spinner spinner = view.findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.spinner_items, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        return view;
    }
}

//prosthiki listener gia na diaxeiristoume tis epiloges
/*spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
@Override
public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Handle the selection
        String selectedItem = (String) parent.getItemAtPosition(position);
        // Do something with the selected item
        }

@Override
public void onNothingSelected(AdapterView<?> parent) {
        // Handle the case where no item is selected (optional)
        }
        });*/