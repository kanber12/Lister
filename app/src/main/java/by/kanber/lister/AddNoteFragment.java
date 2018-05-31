package by.kanber.lister;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AddNoteFragment extends Fragment implements SetReminderDialog.OnDialogInteractionListener {
    private static final int PICK_PICTURE = 0;
    private static final int TAKE_PICTURE = 1;

    private OnFragmentInteractionListener mListener;
    private Button passwordButton, reminderButton, removePasswordButton, changePasswordButton, removeReminderButton, changeReminderButton;
    private EditText titleEditText, bodyEditText, enterPasswordEditText, repeatPasswordEditText;
    private ImageView pictureView;
    ImageButton removePictureView;

    private Uri photo;
    private String password = "", picture = "";
    private boolean isPinned = false, isReminderSet = false, isPasswordSet = false, alertShowed = false;
    private long time;

    public AddNoteFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_note, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar_actionbar);
        CheckBox pinCheckBox = view.findViewById(R.id.pin_check_box);
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
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_clear);
        toolbar.setTitle(getString(R.string.add_note));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeFragment();
            }
        });

        titleEditText.requestFocus();
        ((MainActivity) getActivity()).openKeyboard();

        View.OnClickListener listener = new View.OnClickListener() {
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

        passwordButton.setOnClickListener(listener);
        reminderButton.setOnClickListener(listener);
        removePasswordButton.setOnClickListener(listener);
        changePasswordButton.setOnClickListener(listener);
        removeReminderButton.setOnClickListener(listener);
        changeReminderButton.setOnClickListener(listener);
        removePictureView.setOnClickListener(listener);

        pinCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isPinned = b;
            }
        });

        bodyEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enableButtons(Utils.isEmpty(String.valueOf(charSequence)) && picture.equals(""));
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 21)
                    titleEditText.setText(charSequence.subSequence(0, 21));
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                picture = data.getDataString();
                pictureView.layout(0, 0, 0, 0);
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
                picture = photo.toString();
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
        inflater.inflate(R.menu.add_note_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_note_save: addNote(); return true;
            case R.id.add_attach_picture: actionChoosePicture(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    private void actionChoosePicture() {
        if (!picture.equals(""))
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
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MainActivity.ADD_PERMISSION_GALLERY);
        else {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");

            startActivityForResult(pickIntent, PICK_PICTURE);
        }
    }

    public void takePhoto() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, MainActivity.ADD_PERMISSION_CAMERA);
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
        picture = "";
        Glide.with(getActivity()).load(android.R.color.transparent).into(pictureView);
        removePictureView.setVisibility(View.GONE);

        enableButtons(Utils.isEmpty(bodyEditText.getText().toString()));
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
                        Toast.makeText(getActivity(), getString(R.string.successful_removing_picture), Toast.LENGTH_SHORT).show();
                        removePicture();
                    }
                });

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
                });

        builder.create().show();
    }

    @Override
    public void onSetReminderDialogInteraction(int position, long time) {
        this.time = time;
        isReminderSet = true;
        updateButtons();
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
                        isReminderSet = false;
                        time = 0;
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

    private void showRemovePasswordDialog()
    {
        ((MainActivity) getActivity()).closeKeyboard();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getString(R.string.confirm))
                .setMessage(getString(R.string.removing_password_text))
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.answer_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isPasswordSet = false;
                        password = "";
                        updateButtons();
                    }
                });

        builder.create().show();
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
                })
                .setCancelable(false);

        builder.create().show();
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

        this.password = password;
        isPasswordSet = true;
        return true;
    }

    private boolean isCorrectPassword(String password) {
        Pattern p = Pattern.compile("[А-Яа-яA-Za-z0-9-]+");
        Matcher m = p.matcher(password);

        return m.matches();
    }

    private void addNote() {
        String title = titleEditText.getText().toString();
        String body = bodyEditText.getText().toString();

        if (!Utils.isEmpty(title)) {
            Note note = new Note(true, title, body, password, System.currentTimeMillis(), isReminderSet, isPasswordSet, isPinned, time, picture);

            if (isPasswordSet && Utils.isEmpty(body) && picture.equals("")) {
                if (!alertShowed)
                    showConfirmDialog();
                else {
                    note.setPasswordSet(false);
                    note.setPassword("");
                    onButtonPressed(note);
                    closeFragment();
                }
            } else {
                if (isReminderSet && time <= Calendar.getInstance().getTimeInMillis())
                    ((MainActivity) getActivity()).showCenteredToast(getString(R.string.reminder_out));
                else {
                    onButtonPressed(note);
                    closeFragment();
                }
            }
        } else {
            ((MainActivity) getActivity()).showCenteredToast(getString(R.string.enter_title_warning));
            titleEditText.requestFocus();
            titleEditText.setText("");
        }
    }

    public void onButtonPressed(Note note) {
        if (mListener != null)
            mListener.onAddNoteFragmentInteraction(note);
    }

    private void closeFragment() {
        ((MainActivity) getActivity()).closeKeyboard();
        getActivity().getSupportFragmentManager().popBackStack();
        getActivity().getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).remove(AddNoteFragment.this).commit();
    }

    private void updateButtons() {
        if (isPasswordSet) {
            passwordButton.setVisibility(View.INVISIBLE);
            removePasswordButton.setVisibility(View.VISIBLE);
            changePasswordButton.setVisibility(View.VISIBLE);
        } else {
            passwordButton.setVisibility(View.VISIBLE);
            removePasswordButton.setVisibility(View.INVISIBLE);
            changePasswordButton.setVisibility(View.INVISIBLE);
        }

        if (isReminderSet) {
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Fragment fragment = ((MainActivity) context).getSupportFragmentManager().findFragmentByTag("notesListFragment");

        if (fragment instanceof OnFragmentInteractionListener)
            mListener = (OnFragmentInteractionListener) fragment;
        else
            throw new RuntimeException(fragment.toString() + " must implement OnFragmentInteractionListener");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
    void onAddNoteFragmentInteraction(Note note);
    }
}