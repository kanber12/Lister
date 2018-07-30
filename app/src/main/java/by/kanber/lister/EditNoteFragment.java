package by.kanber.lister;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EditNoteFragment extends Fragment implements SetReminderDialog.OnDialogInteractionListener {
    public static final int FROM_LIST = 0;
    public static final int FROM_INFO = 1;
    public static final int ACTION_ADD = 0;
    public static final int ACTION_EDIT = 1;
    public static final int ACTION_SAVE = 2;
    public static final int ACTION_CLOSE = 3;
    private static final int PICK_PICTURE = 0;
    private static final int TAKE_PICTURE = 1;
    private static final int TYPE_PASSWORD = 0;
    private static final int TYPE_REMINDER = 1;

    private OnFragmentInteractionListener mListener;
    private Button passwordButton, reminderButton, removePasswordButton, changePasswordButton, removeReminderButton, changeReminderButton;
    private EditText titleEditText, bodyEditText, enterPasswordEditText, repeatPasswordEditText;
    private ImageView pictureView;
    private ImageButton removePictureView;
    private MainActivity activity;

    private Uri photo;
    private Note note, oldNote;
    private boolean passwordAlertShowed = false, reminderAlertShowed = false;
    private int from, action;

    public EditNoteFragment() {}

    public static EditNoteFragment newInstance(int action, Note note, int from) {
        EditNoteFragment fragment = new EditNoteFragment();
        Bundle args = new Bundle();

        args.putInt("action", action);
        args.putParcelable("note", note);
        args.putInt("from", from);
        fragment.setArguments(args);

        return fragment;
    }

    public static EditNoteFragment newInstance(int action) {
        EditNoteFragment fragment = new EditNoteFragment();
        Bundle args = new Bundle();

        args.putInt("action", action);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (MainActivity) getActivity();

        if (getArguments() != null) {
            action = getArguments().getInt("action");

            if (action == ACTION_EDIT) {
                oldNote = getArguments().getParcelable("note");
                from = getArguments().getInt("from");
            } else
                oldNote = new Note();

            note = new Note(oldNote);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_note, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar_actionbar);
        passwordButton = view.findViewById(R.id.password_button);
        removePasswordButton = view.findViewById(R.id.remove_password_button);
        changePasswordButton = view.findViewById(R.id.change_password_button);
        reminderButton = view.findViewById(R.id.reminder_button);
        removeReminderButton = view.findViewById(R.id.remove_reminder_button);
        changeReminderButton = view.findViewById(R.id.change_reminder_button);
        titleEditText = view.findViewById(R.id.title_edit_text);
        bodyEditText = view.findViewById(R.id.body_edit_text);
        pictureView = view.findViewById(R.id.picture_view);
        removePictureView = view.findViewById(R.id.remove_picture_view);
        activity.setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_clear);
        toolbar.setTitle(getWindowTitle());

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkEquality(ACTION_CLOSE);
            }
        });

        if (action == ACTION_EDIT) {
            titleEditText.setText(oldNote.getTitle());
            bodyEditText.setText(oldNote.getBody());

            if (!Utils.isEmpty(oldNote.getBody())) {
                passwordButton.setEnabled(true);
                passwordButton.setTextColor(getResources().getColor(R.color.textColor));
            }

            if (!oldNote.getPicture().equals("")) {
                Glide.with(activity).load(Uri.parse(oldNote.getPicture())).apply(new RequestOptions().signature(new ObjectKey(System.currentTimeMillis()))).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        removePictureView.setVisibility(View.GONE);
                        activity.showCenteredToast(getString(R.string.file_not_found) + "\n" + getString(R.string.successful_removing_picture));
                        note.setPicture("");
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        removePictureView.setVisibility(View.VISIBLE);
                        return false;
                    }
                }).into(pictureView);

                passwordButton.setEnabled(true);
                passwordButton.setTextColor(getResources().getColor(R.color.textColor));
            }
        }

        updateButtons();

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.password_button: case R.id.change_password_button: showSetPasswordDialog(); break;
                    case R.id.remove_password_button: showRemovePasswordDialog(); break;
                    case R.id.reminder_button: case R.id.change_reminder_button: showSetReminderDialog(); break;
                    case R.id.remove_reminder_button: showRemoveReminderDialog(); break;
                    case R.id.remove_picture_view: showRemovePictureDialog(); break;
                }
            }
        };

        View.OnKeyListener keyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    checkEquality(ACTION_CLOSE);
                    return true;
                }

                return false;
            }
        };

        titleEditText.setOnKeyListener(keyListener);
        bodyEditText.setOnKeyListener(keyListener);
        reminderButton.setOnClickListener(clickListener);
        passwordButton.setOnClickListener(clickListener);
        removeReminderButton.setOnClickListener(clickListener);
        changeReminderButton.setOnClickListener(clickListener);
        removePasswordButton.setOnClickListener(clickListener);
        changePasswordButton.setOnClickListener(clickListener);
        removePictureView.setOnClickListener(clickListener);

        bodyEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                note.setBody(String.valueOf(charSequence));

                enableButtons(Utils.isEmpty(String.valueOf(charSequence)) && note.getPicture().equals(""));
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                note.setTitle(String.valueOf(charSequence));

                if (charSequence.length() > 21)
                    titleEditText.setText(charSequence.subSequence(0, 21));
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        titleEditText.requestFocus();

        if (action == ACTION_ADD)
            activity.openKeyboard();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == PICK_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                Glide.with(activity).load(data.getData()).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        removePictureView.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        note.setPicture(data.getDataString());
                        removePictureView.setVisibility(View.VISIBLE);
                        Toast.makeText(activity, getString(R.string.successful_attaching_picture), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }).into(pictureView);
                enableButtons(false);
            }

            if (resultCode == Activity.RESULT_CANCELED)
                Toast.makeText(activity, getString(R.string.picture_not_selected), Toast.LENGTH_SHORT).show();
        }

        if (requestCode == TAKE_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                Glide.with(activity).load(photo).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        removePictureView.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        note.setPicture(photo.toString());
                        removePictureView.setVisibility(View.VISIBLE);
                        Toast.makeText(activity, getString(R.string.successful_attaching_photo), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }).into(pictureView);
                enableButtons(false);
            }

            if (resultCode == Activity.RESULT_CANCELED)
                Toast.makeText(activity, getString(R.string.photo_not_selected), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.edit_note_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_note_save: checkEquality(ACTION_SAVE); return true;
            case R.id.edit_attach_picture: actionChoosePicture(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    private void checkEquality(int action) {
        if (note.equals(oldNote) && action == ACTION_CLOSE)
            closeFragment();
        else {
            if (action == ACTION_CLOSE)
                showCloseDialog();
            else {
                if (this.action == ACTION_EDIT) {
                    if (note.equals(oldNote))
                        closeFragment();
                    else
                        editNote();
                } else
                    addNote();
            }
        }
    }

    private void actionChoosePicture() {
        if (!note.getPicture().equals(""))
            showReplacePictureDialog();
        else
            choosePicture();
    }

    private void choosePicture() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle(getString(R.string.attach_picture))
                .setItems(new String[] {getString(R.string.choose_from_gallery), getString(R.string.take_photo)}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: chooseFromGallery(); break;
                            case 1: takePhoto(); break;
                        }
                    }
                });

        builder.create().show();
    }

    public void chooseFromGallery() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MainActivity.EDIT_PERMISSION_GALLERY);
        else {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");

            startActivityForResult(pickIntent, PICK_PICTURE);
        }
    }

    public void takePhoto() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, MainActivity.EDIT_PERMISSION_CAMERA);
        else {
            Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String auth = activity.getApplicationContext().getPackageName() + ".fileprovider";
            photo = FileProvider.getUriForFile(activity, auth, createImageFile());

            if (photo != null) {
                takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, photo);
                startActivityForResult(takeIntent, TAKE_PICTURE);
            }
        }
    }

    private File createImageFile(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis());
        String imageFileName = "Lister_photo_" + timeStamp;
        File storage = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;

        try {
            image = File.createTempFile(imageFileName, ".jpg", storage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    private void removePicture() {
        note.setPicture("");
        Glide.with(activity).load(android.R.color.transparent).into(pictureView);
        removePictureView.setVisibility(View.GONE);

        enableButtons(Utils.isEmpty(bodyEditText.getText().toString()));
    }

    private void onButtonPressed(boolean needChange) {
        if (mListener != null)
            mListener.onEditNoteInteraction(oldNote, note, needChange);
    }

    private void onButtonPressed() {
        if (mListener != null)
            mListener.onAddNoteInteraction(note);
    }

    private void editNote() {
        if (!Utils.isEmpty(note.getTitle())) {
            boolean needChange = false;

            if (isPasswordRequired() && isReminderNotOut()) {
                if (!note.equals(oldNote) && note.getNotificationTime() == oldNote.getNotificationTime() && note.getNotificationTime() != 0)
                    needChange = true;

                showSaveConfirmDialog(needChange);
            }
        } else {
            titleEditText.requestFocus();
            activity.showCenteredToast(getString(R.string.enter_title_warning));
        }
    }

    private void addNote() {
        if (!Utils.isEmpty(note.getTitle())) {
            note.setAddTime(System.currentTimeMillis());

            if (isPasswordRequired() && isReminderNotOut()) {
                onButtonPressed();
                closeFragment();
            }
        } else {
            activity.showCenteredToast(getString(R.string.enter_title_warning));
            titleEditText.requestFocus();
            titleEditText.setText("");
        }
    }

    private void showConfirmDialog(final int type) {
        String message = "";

        switch (type) {
            case TYPE_PASSWORD: message = getString(R.string.note_without_text); break;
            case TYPE_REMINDER: message = getString(R.string.reminder_out); break;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle(getString(R.string.warning))
                .setMessage(message)
                .setPositiveButton(getString(R.string.got_it), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (type) {
                            case TYPE_PASSWORD: passwordAlertShowed = true; break;
                            case TYPE_REMINDER: reminderAlertShowed = true; break;
                        }
                    }
                });

        builder.create().show();
    }

    private void showCloseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle(getString(R.string.confirm))
                .setMessage(getString(R.string.discarding_text))
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(getString(R.string.answer_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        closeFragment();
                    }
                })
                .setCancelable(false);

        builder.create().show();
    }

    private void showRemovePictureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle(getString(R.string.confirm))
                .setMessage(getString(R.string.remove_picture_text))
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(getString(R.string.answer_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(activity, getString(R.string.successful_removing_picture), Toast.LENGTH_SHORT).show();
                        removePicture();
                    }
                })
                .setCancelable(false);

        builder.create().show();
    }

    private void showReplacePictureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle(getString(R.string.confirm))
                .setMessage(getString(R.string.picture_replacing_text))
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(getString(R.string.answer_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        choosePicture();
                    }
                })
                .setCancelable(false);

        builder.create().show();
    }

    private void showSaveConfirmDialog(final boolean needChange) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle(getString(R.string.confirm))
                .setMessage(getString(R.string.save_text))
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(getString(R.string.answer_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openEditedNote(needChange);
                    }
                })
                .setCancelable(false);

        builder.create().show();
    }

    private void showSetReminderDialog() {
        SetReminderDialog dialog = new SetReminderDialog();
        dialog.show(activity.getSupportFragmentManager(), "setReminderDialog");
    }

    private void showRemoveReminderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle(getString(R.string.confirm))
                .setMessage(getString(R.string.removing_reminder_text))
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.answer_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        note.setReminderSet(false);
                        note.setNotificationTime(0);
                        updateButtons();
                    }
                });

        builder.create().show();
    }

    private void showSetPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View passwordView = activity.getLayoutInflater().inflate(R.layout.set_password_dialog, null);
        enterPasswordEditText = passwordView.findViewById(R.id.setPasswordEditText);
        repeatPasswordEditText = passwordView.findViewById(R.id.repeatPasswordEditText);

        builder.setView(passwordView)
                .setTitle(getString(R.string.set_password))
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.set), null)
                .setCancelable(false);

        final AlertDialog pswDialog = builder.create();

        pswDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button posButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                Button negButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);

                enterPasswordEditText.requestFocus();
                activity.openKeyboard();

                posButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isGoodPassword()) {
                            updateButtons();
                            pswDialog.cancel();
                        }
                    }
                });

                negButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.closeKeyboard();
                        pswDialog.cancel();
                    }
                });
            }
        });

        pswDialog.show();
    }

    private void showRemovePasswordDialog() {
        activity.closeKeyboard();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle(getString(R.string.confirm))
                .setMessage(getString(R.string.removing_password_text))
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.answer_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        note.setPasswordSet(false);
                        note.setPassword("");
                        updateButtons();
                    }
                });

        builder.create().show();
    }

    private void closeFragment() {
        activity.closeKeyboard();
        activity.getSupportFragmentManager().popBackStack();
        activity.getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).remove(EditNoteFragment.this).commit();
    }

    private void openEditedNote(boolean needChange) {
        if (from == FROM_INFO) {
            Fragment fragment = activity.getSupportFragmentManager().findFragmentByTag("noteInfoFragment");
            activity.getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).remove(fragment).commit();
            onButtonPressed(needChange);
            closeFragment();
            activity.getSupportFragmentManager().popBackStack();
        } else {
            onButtonPressed(needChange);
            closeFragment();
        }

        FragmentTransaction fTrans = activity.getSupportFragmentManager().beginTransaction();
        NoteInfoFragment fragment = NoteInfoFragment.newInstance(note);
        fTrans.replace(R.id.container, fragment, "noteInfoFragment").addToBackStack("tag");
        fTrans.commit();
    }

    private void updateButtons() {
        int setPassBtnVis = note.isPasswordSet() ? View.GONE : View.VISIBLE;
        int changePassBtnVis = note.isPasswordSet() ? View.VISIBLE : View.GONE;
        int setRemBtnVis = note.isReminderSet() || note.getNotificationTime() > 0 ? View.GONE : View.VISIBLE;
        int changeRemBtnVis = note.isReminderSet() || note.getNotificationTime() > 0 ? View.VISIBLE : View.GONE;

        passwordButton.setVisibility(setPassBtnVis);
        removePasswordButton.setVisibility(changePassBtnVis);
        changePasswordButton.setVisibility(changePassBtnVis);

        reminderButton.setVisibility(setRemBtnVis);
        removeReminderButton.setVisibility(changeRemBtnVis);
        changeReminderButton.setVisibility(changeRemBtnVis);
    }

    private void enableButtons(boolean statement) {
        passwordButton.setEnabled(!statement);
        removePasswordButton.setEnabled(!statement);
        changePasswordButton.setEnabled(!statement);

        if (statement) {
            passwordButton.setTextColor(getResources().getColor(R.color.materialGrey600));
            removePasswordButton.setTextColor(getResources().getColor(R.color.materialGrey600));
            changePasswordButton.setTextColor(getResources().getColor(R.color.materialGrey600));
        } else {
            passwordButton.setTextColor(getResources().getColor(R.color.textColor));
            removePasswordButton.setTextColor(getResources().getColor(R.color.textColor));
            changePasswordButton.setTextColor(getResources().getColor(R.color.textColor));
        }
    }

    private boolean isGoodPassword() {
        String password = enterPasswordEditText.getText().toString(), repeatedPassword = repeatPasswordEditText.getText().toString();

        if (Utils.isEmpty(password)) {
            activity.showCenteredToast(getString(R.string.enter_password_warning));
            enterPasswordEditText.requestFocus();
            enterPasswordEditText.setText("");
            repeatPasswordEditText.setText("");
            return false;
        }

        if (!repeatedPassword.equals(password)) {
            activity.showCenteredToast(getString(R.string.passwords_do_not_match_warning));
            repeatPasswordEditText.requestFocus();
            repeatPasswordEditText.setText("");
            return false;
        }

        if (password.length() <= 4) {
            activity.showCenteredToast(getString(R.string.password_short_warning));
            enterPasswordEditText.requestFocus();
            repeatPasswordEditText.setText("");
            return false;
        }

        if (!isCorrectPassword(password)) {
            activity.showCenteredToast(getString(R.string.not_correct_password_warning));
            enterPasswordEditText.requestFocus();
            enterPasswordEditText.setText("");
            repeatPasswordEditText.setText("");
            return false;
        }

        note.setPassword(password);
        note.setPasswordSet(true);
        return true;
    }

    private boolean isCorrectPassword(String password) {
        Pattern p = Pattern.compile("[А-Яа-яA-Za-z0-9-]+");
        Matcher m = p.matcher(password);

        return m.matches();
    }

    private boolean isPasswordRequired() {
        if (note.isPasswordSet() && Utils.isEmpty(note.getBody()) && note.getPicture().equals("")) {
            if (!passwordAlertShowed) {
                showConfirmDialog(TYPE_PASSWORD);

                return false;
            } else {
                note.setPasswordSet(false);
                note.setPassword("");

                return true;
            }
        }

        return true;
    }

    private boolean isReminderNotOut() {
        if (note.isReminderSet() && note.getNotificationTime() <= Calendar.getInstance().getTimeInMillis()) {
            if (!reminderAlertShowed) {
                showConfirmDialog(TYPE_REMINDER);

                return false;
            } else {
                note.setReminderSet(false);
                note.setNotificationTime(0);

                return true;
            }
        }

        return true;
    }

    @Override
    public void onSetReminderDialogInteraction(int position, long time) {
        note.setReminderSet(true);
        note.setNotificationTime(time);
        updateButtons();
    }

    private String getWindowTitle() {
        if (action == ACTION_ADD)
            return getString(R.string.add_note);
        else
            return getString(R.string.context_edit);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Fragment fragment = ((MainActivity) context).getSupportFragmentManager().findFragmentByTag("notesListFragment");
        if (fragment instanceof OnFragmentInteractionListener)
            mListener = (OnFragmentInteractionListener) fragment;
        else
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onEditNoteInteraction(Note old, Note note, boolean needChange);

        void onAddNoteInteraction(Note note);
    }
}