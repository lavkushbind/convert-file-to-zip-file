package com.example.zipfile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;

import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
public class MainActivity extends AppCompatActivity {
    private List<Uri> selectedUris = new ArrayList<>();
    private TextView selectedFilesTextView;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button selectFilesButton = findViewById(R.id.selectFilesButton);
        selectedFilesTextView = findViewById(R.id.selectedFilesTextView);
        Button createZipButton = findViewById(R.id.createZipButton);
        selectFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilePicker();
            }
        });
        Button extractZipButton = findViewById(R.id.extractZipButton);
        extractZipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri zipFileUri = // Get the Uri of the zip file you want to extract
                        extractZipFile(zipFileUri);
            }
        });

        createZipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createZipFile();
            }
        });
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            ClipData clipData = data.getClipData();
                            if (clipData != null) {
                                for (int i = 0; i < clipData.getItemCount(); i++) {
                                    selectedUris.add(clipData.getItemAt(i).getUri());
                                }
                            } else {
                                Uri singleUri = data.getData();
                                selectedUris.add(singleUri);
                            }
                            updateSelectedFilesText();
                        }
                    }
                });
    }
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        filePickerLauncher.launch(intent);
    }
    private void updateSelectedFilesText() {
        String selectedFilesText = "Selected Files: " + selectedUris.size();
        selectedFilesTextView.setText(selectedFilesText);
    }
    private void createZipFile() {
        if (selectedUris.size() < 2) {
            return;
        }
        try {
            String zipFileName = "selected_files.zip";
            Uri zipFileUri = createZipFileUri(zipFileName);
            FileOutputStream fos = new FileOutputStream(getFileFromUri(zipFileUri));
            ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(fos);
            for (Uri uri : selectedUris) {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                String entryName = getFileName(uri);
                ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
                zipOutputStream.putArchiveEntry(entry);
                FileUtils.copyInputStreamToFile(inputStream, getFileFromUri(zipFileUri));
                zipOutputStream.closeArchiveEntry();
            }
            zipOutputStream.finish();
            zipOutputStream.close();
            showToast("Zip file created successfully");
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error creating zip file");
        }
    }
    private Uri createZipFileUri(String fileName) {
        File appDir = getExternalFilesDir(null);
        return Uri.fromFile(new File(appDir, fileName));
    }
    private File getFileFromUri(Uri uri) {
        return new File(uri.getPath());
    }
    private String getFileName(Uri uri) {
        String displayName = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (displayName == null) {
            displayName = uri.getLastPathSegment();
        }
        return displayName;
    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        View view = toast.getView();
        view.getBackground().setColorFilter(ContextCompat.getColor(com.example.zipfile.R.color.purple_200), PorterDuff.Mode.SRC_IN);
        toast.show();
    }
}
