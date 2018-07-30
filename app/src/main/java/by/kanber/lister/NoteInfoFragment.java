package by.kanber.lister;

import android.app.AlertDialog;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;


public class NoteInfoFragment extends Fragment {
    public static final int MODE_DELETE = 0;
    public static final int MODE_CLOSE = 1;

    private OnFragmentInteractionListener mListener;
    private FrameLayout noteInfoImageFrame;
    private TextView notFoundTextView;
    private ImageView infoPictureView;
    private ProgressBar progressBar;
    private MainActivity activity;

    private Note note;

    public NoteInfoFragment() {}

    public static NoteInfoFragment newInstance(Note n) {
        NoteInfoFragment fragment = new NoteInfoFragment();
        Bundle args = new Bundle();
        args.putParcelable("note", n);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (MainActivity) getActivity();

        if (getArguments() != null)
            note = getArguments().getParcelable("note");

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note_info, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar_actionbar);
        TextView bodyTextView = view.findViewById(R.id.bodyTextView);
        noteInfoImageFrame = view.findViewById(R.id.noteInfoImageFrame);
        notFoundTextView = view.findViewById(R.id.notFoundText);
        infoPictureView = view.findViewById(R.id.infoPictureView);
        progressBar = view.findViewById(R.id.imageLoadingProgressBar);

        activity.setSupportActionBar(toolbar);
        toolbar.setTitle(note.getTitle());
        toolbar.setNavigationIcon(R.drawable.ic_navigation_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonPressed(MODE_CLOSE);
                closeFragment();
            }
        });

        if (Utils.isEmpty(note.getBody())) {
            if (note.getPicture().equals(""))
                bodyTextView.setText(note.getTitle());
            else {
                bodyTextView.setVisibility(View.GONE);
                loadPicture();
            }
        } else {
            bodyTextView.setText(note.getBody());
            if (!note.getPicture().equals(""))
                loadPicture();
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.note_info_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.info_delete: delete(); return true;
            case R.id.info_edit: edit(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    private void loadPicture() {
        long key = System.currentTimeMillis();
        progressBar.setVisibility(View.VISIBLE);
        noteInfoImageFrame.setVisibility(View.VISIBLE);
        Glide.with(activity).load(Uri.parse(note.getPicture()))
                .apply(new RequestOptions().error(R.drawable.ic_warning).signature(new ObjectKey(key)))
                .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            notFoundTextView.setText(getResources().getText(R.string.file_not_found));
                            notFoundTextView.setVisibility(View.VISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                }).into(infoPictureView);
    }

    private void closeFragment() {
        activity.getSupportFragmentManager().popBackStack();
        activity.getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).remove(NoteInfoFragment.this).commit();
    }

    private void delete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(getString(R.string.confirm))
                .setMessage(getString(R.string.delete_note_text))
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .setPositiveButton(getString(R.string.answer_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        onButtonPressed(MODE_DELETE);
                        closeFragment();
                    }
                });

        builder.create().show();
    }

    private void edit() {
        FragmentTransaction fTrans = activity.getSupportFragmentManager().beginTransaction();
        EditNoteFragment fragment = EditNoteFragment.newInstance(EditNoteFragment.ACTION_EDIT, note, EditNoteFragment.FROM_INFO);
        fTrans.replace(R.id.container, fragment, "editNoteFragment").addToBackStack("tag");
        fTrans.commit();
    }

    public void onButtonPressed(int mode) {
        if (mListener != null)
            mListener.onNoteInfoFragmentInteraction(note, mode);
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
        void onNoteInfoFragmentInteraction(Note note, int mode);
    }
}