package by.kanber.lister;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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
    public static final int ACTION_SAVE = 0;
    public static final int ACTION_CLOSE = 1;
    private static final int PICK_PICTURE = 0;
    private static final int TAKE_PICTURE = 1;

    private OnFragmentInteractionListener mListener;
    private Button passwordButton, reminderButton, removePasswordButton, changePasswordButton, removeReminderButton, changeReminderButton;
    private EditText titleEditText, bodyEditText, enterPasswordEditText, repeatPasswordEditText;
    private ImageView pictureView;
    private ImageButton removePictureView;

    private Uri photo;
    private Note note, oldNote;
    private String title, body;
    private boolean alertShowed = false;
    private int from;

    public EditNoteFragment() {}

    public static EditNoteFragment newInstance(Note note, int from) {
        EditNoteFragment fragment = new EditNoteFragment();
        Bundle args = new Bundle();

        args.putParcelable("note", note);
        args.putInt("from", from);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            oldNote = getArguments().getParcelable("note");
            note = new Note(oldNote);
            from = getArguments().getInt("from");
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        view.findViewById(R.id.pin_check_box).setVisibility(View.GONE);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_toolbar_clear);
        toolbar.setTitle(getString(R.string.context_edit));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkEquality(ACTION_CLOSE);
            }
        });

        title = oldNote.getTitle();
        body = oldNote.getBody();
        titleEditText.setText(oldNote.getTitle());
        bodyEditText.setText(oldNote.getBody());

        if (!Utils.isEmpty(oldNote.getBody())) {
            passwordButton.setEnabled(true);
            passwordButton.setTextColor(getResources().getColor(R.color.textColor));
        }

        if (!oldNote.getPicture().equals("")) {
            Glide.with(getActivity()).load(Uri.parse(oldNote.getPicture())).apply(new RequestOptions().signature(new ObjectKey(System.currentTimeMillis()))).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    removePictureView.setVisibility(View.GONE);
                    ((MainActivity) getActivity()).showCenteredToast(getString(R.string.file_not_found) + "\n" + getString(R.string.successful_removing_picture));
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
                body = String.valueOf(charSequence);
                note.setBody(body);

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
                title = String.valueOf(charSequence);
                note.setTitle(title);

                if (charSequence.length() > 21)
                    titleEditText.setText(charSequence.subSequence(0, 21));
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        titleEditText.requestFocus();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                note.setPicture(data.getDataString());
                Glide.with(getActivity()).load(data.getData()).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        removePictureView.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        removePictureView.setVisibility(View.VISIBLE);
                        Toast.makeText(getActivity(), getString(R.string.successful_attaching_picture), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }).into(pictureView);
                enableButtons(false);
            }

            if (resultCode == Activity.RESULT_CANCELED)
                Toast.makeText(getActivity(), getString(R.string.picture_not_selected), Toast.LENGTH_SHORT).show();
        }

        if (requestCode == TAKE_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                note.setPicture(photo.toString());
                Glide.with(getActivity()).load(photo).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        removePictureView.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        removePictureView.setVisibility(View.VISIBLE);
                        Toast.makeText(getActivity(), getString(R.string.successful_attaching_photo), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }).into(pictureView);
                enableButtons(false);
            }

            if (resultCode == Activity.RESULT_CANCELED)
                Toast.makeText(getActivity(), getString(R.string.photo_not_selected), Toast.LENGTH_SHORT).show();
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
        if (action == ACTION_CLOSE) {
            if (note.equals(oldNote))
                closeFragment();
            else
                showCloseDialog();
        } else {
            if (note.equals(oldNote))
                closeFragment();
            else
                editNote();
        }
    }

    private void actionChoosePicture() {
        if (!note.getPicture().equals(""))
            showReplacePictureDialog();
        else
            choosePicture();
    }

    private void choosePicture() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

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
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MainActivity.EDIT_PERMISSION_GALLERY);
        else {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");

            startActivityForResult(pickIntent, PICK_PICTURE);
        }
    }

    public void takePhoto() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, MainActivity.EDIT_PERMISSION_CAMERA);
        else {
            Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String auth = getActivity().getApplicationContext().getPackageName() + ".fileprovider";
            photo = FileProvider.getUriForFile(getActivity(), auth, createImageFile());

            if (photo != null) {
                takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, photo);
                startActivityForResult(takeIntent, TAKE_PICTURE);
            }
        }
    }

    private File createImageFile(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis());
        String imageFileName = "Lister_photo_" + timeStamp;
        File storage = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;

        try {
            image = File.createTempFile(imageFileName, ".jpg", storage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    private void removePicture() {
        Toast.makeText(getActivity(), getString(R.string.successful_removing_picture), Toast.LENGTH_SHORT).show();
        note.setPicture("");
        pictureView.setImageResource(android.R.color.transparent);
        removePictureView.setVisibility(View.GONE);

        enableButtons(Utils.isEmpty(bodyEditText.getText().toString()));
    }

    public void onButtonPressed(boolean needChange) {
        if (mListener != null)
            mListener.onEditNoteFragmentInteraction(oldNote, note, needChange);
    }

    private void editNote() {
        if (!Utils.isEmpty(title)) {
            boolean needChange = false;

            if (note.isPasswordSet() && Utils.isEmpty(note.getBody()) && note.getPicture().equals("")) {
                if (!alertShowed)
                    showConfirmDialog();
                else {
                    note.setPasswordSet(false);
                    note.setPassword("");

                    if (!note.equals(oldNote) && note.getNotificationTime() == oldNote.getNotificationTime() && note.getNotificationTime() != 0)
                        needChange = true;

                    showSaveConfirmDialog(needChange);
                }
            } else {
                if (note.isReminderSet() && note.getNotificationTime() <= Calendar.getInstance().getTimeInMillis())
                    ((MainActivity) getActivity()).showCenteredToast(getString(R.string.reminder_out));
                else {
                    if (!note.equals(oldNote) && note.getNotificationTime() == oldNote.getNotificationTime() && note.isReminderSet())
                        needChange = true;

                    showSaveConfirmDialog(needChange);
                }
            }
        } else {
            titleEditText.requestFocus();
            ((MainActivity) getActivity()).showCenteredToast(getString(R.string.enter_title_warning));
        }
    }

    private void showConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getString(R.string.warning))
                .setMessage(getString(R.string.note_without_text))
                .setPositiveButton(getString(R.string.get_it), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertShowed = true;
                    }
                });

        builder.create().show();
    }

    private void showCloseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

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
                        removePicture();
                    }
                })
                .setCancelable(false);

        builder.create().show();
    }

    private void showReplacePictureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

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
        dialog.show(getActivity().getSupportFragmentManager(), "setReminderDialog");
    }

    private void showRemoveReminderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View passwordView = getActivity().getLayoutInflater().inflate(R.layout.set_password_dialog, null);
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
                ((MainActivity) getActivity()).openKeyboard();

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
                        ((MainActivity) getActivity()).closeKeyboard();
                        pswDialog.cancel();
                    }
                });
            }
        });

        pswDialog.show();
    }

    private void showRemovePasswordDialog() {
        ((MainActivity) getActivity()).closeKeyboard();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

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
        ((MainActivity) getActivity()).closeKeyboard();
        getActivity().getSupportFragmentManager().popBackStack();
        getActivity().getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).remove(EditNoteFragment.this).commit();
    }

    private void openEditedNote(boolean needChange) {
        if (from == FROM_INFO) {
            Fragment fragment = getActivity().getSupportFragmentManager().findFragmentByTag("noteInfoFragment");
            getActivity().getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).remove(fragment).commit();
            onButtonPressed(needChange);
            closeFragment();
            getActivity().getSupportFragmentManager().popBackStack();
        } else {
            onButtonPressed(needChange);
            closeFragment();
        }

        FragmentTransaction fTrans = getActivity().getSupportFragmentManager().beginTransaction();
        NoteInfoFragment fragment = NoteInfoFragment.newInstance(note);
        fTrans.replace(R.id.container, fragment, "noteInfoFragment").addToBackStack("tag");
        fTrans.commit();
    }

    private void updateButtons() {
        if (note.isPasswordSet()) {
            passwordButton.setVisibility(View.INVISIBLE);
            removePasswordButton.setVisibility(View.VISIBLE);
            changePasswordButton.setVisibility(View.VISIBLE);
        } else {
            passwordButton.setVisibility(View.VISIBLE);
            removePasswordButton.setVisibility(View.INVISIBLE);
            changePasswordButton.setVisibility(View.INVISIBLE);
        }

        if (note.isReminderSet() || note.getNotificationTime() > 0) {
            reminderButton.setVisibility(View.INVISIBLE);
            removeReminderButton.setVisibility(View.VISIBLE);
            changeReminderButton.setVisibility(View.VISIBLE);
        } else {
            reminderButton.setVisibility(View.VISIBLE);
            removeReminderButton.setVisibility(View.INVISIBLE);
            changeReminderButton.setVisibility(View.INVISIBLE);
        }
    }

    private void enableButtons(boolean statement) {
        if (statement) {
            passwordButton.setEnabled(false);
            removePasswordButton.setEnabled(false);
            changePasswordButton.setEnabled(false);
            passwordButton.setTextColor(getResources().getColor(R.color.materialGrey600));
            removePasswordButton.setTextColor(getResources().getColor(R.color.materialGrey600));
            changePasswordButton.setTextColor(getResources().getColor(R.color.materialGrey600));
        } else {
            passwordButton.setEnabled(true);
            removePasswordButton.setEnabled(true);
            changePasswordButton.setEnabled(true);
            passwordButton.setTextColor(getResources().getColor(R.color.textColor));
            removePasswordButton.setTextColor(getResources().getColor(R.color.textColor));
            changePasswordButton.setTextColor(getResources().getColor(R.color.textColor));
        }
    }

    private boolean isGoodPassword() {
        String password = enterPasswordEditText.getText().toString(), repeatedPassword = repeatPasswordEditText.getText().toString();

        if (Utils.isEmpty(password)) {
            ((MainActivity) getActivity()).showCenteredToast(getString(R.string.enter_password_warning));
            enterPasswordEditText.requestFocus();
            enterPasswordEditText.setText("");
            repeatPasswordEditText.setText("");
            return false;
        }

        if (!repeatedPassword.equals(password)) {
            ((MainActivity) getActivity()).showCenteredToast(getString(R.string.passwords_do_not_match_warning));
            repeatPasswordEditText.requestFocus();
            repeatPasswordEditText.setText("");
            return false;
        }

        if (password.length() <= 4) {
            ((MainActivity) getActivity()).showCenteredToast(getString(R.string.password_short_warning));
            enterPasswordEditText.requestFocus();
            repeatPasswordEditText.setText("");
            return false;
        }

        if (!isCorrectPassword(password)) {
            ((MainActivity) getActivity()).showCenteredToast(getString(R.string.not_correct_password_warning));
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

    @Override
    public void onSetReminderDialogInteraction(int position, long time) {
        note.setReminderSet(true);
        note.setNotificationTime(time);
        updateButtons();
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
        void onEditNoteFragmentInteraction(Note old, Note note, boolean needChange);
    }
}