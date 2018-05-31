package by.kanber.lister;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.SearchView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;


public class NotesListFragment extends Fragment implements AddNoteFragment.OnFragmentInteractionListener, NotesAdapter.OnButtonClickListener, NoteInfoFragment.OnFragmentInteractionListener,
        EditNoteFragment.OnFragmentInteractionListener, SetReminderDialog.OnDialogInteractionListener {
    public static final int MODE_DIRECT = 0;
    public static final int MODE_REVERSE = 1;
    public static final int SORT_TYPE_CREATED_TIME = 0;
    public static final int SORT_TYPE_CREATED_TIME_REVERSED = 1;
    public static final int SORT_TYPE_ALPHABETICALLY = 2;
    public static final int SORT_TYPE_ALPHABETICALLY_REVERSED = 3;
    public static final int ACTION_OPEN = 0;
    public static final int ACTION_EDIT = 1;
    public static final int ACTION_DELETE = 2;

    private FloatingActionButton fab;
    private TextView emptyListText, emptySearchText;
    private NotesAdapter adapter;
    private SearchView searchView;
    private MenuItem searchItem;
    private EditText enterPasswordEditText;
    private AlarmManager alarmManager;

    private ArrayList<Note> notes;
    private String[] sortTypes;
    private int sortType, sortTypeTmp, showCount = -1;

    public NotesListFragment() {}

    public static NotesListFragment newInstance(ArrayList<Note> n, int sort) {
        NotesListFragment fragment = new NotesListFragment();
        Bundle args = new Bundle();
        args.putInt("sort", sort);
        args.putParcelableArrayList("list", n);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sortTypes = new String[]{getString(R.string.created_time), getString(R.string.created_time_reversed), getString(R.string.alphabetically), getString(R.string.alphabetically_reversed)};
            sortType = getArguments().getInt("sort", SORT_TYPE_CREATED_TIME_REVERSED);
            notes = getArguments().getParcelableArrayList("list");
            sortNotes(false);
        }

        for (Note note : notes)
            Log.d(MainActivity.TAG, note.toString());

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes_list, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar_actionbar);
        fab = view.findViewById(R.id.fab);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        emptyListText = view.findViewById(R.id.emptyListText);
        emptySearchText = view.findViewById(R.id.emptySearchText);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        adapter = new NotesAdapter(getActivity(), notes);
        alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        checkReminderIsOut();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new NotesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (searchItem.isActionViewExpanded())
                    ((MainActivity) getActivity()).closeKeyboard();

                checkPassword(position, ACTION_OPEN);
            }
        });

        adapter.setOnItemLongClickListener(new NotesAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(final int position, View anchor) {
                PopupMenu popupMenu = new PopupMenu(getActivity(), anchor);
                popupMenu.inflate(R.menu.context_menu);

                if (notes.get(position).isPinned())
                    popupMenu.getMenu().getItem(2).setTitle(getString(R.string.unpin));
                else
                    popupMenu.getMenu().getItem(2).setTitle(getString(R.string.pin_to_top));

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId())
                        {
                            case R.id.context_delete_note: checkPassword(position, ACTION_DELETE); return true;
                            case R.id.context_edit_note: checkPassword(position, ACTION_EDIT); return true;
                            case R.id.context_pin_note: pinNote(notes.get(position)); return true;
                            default: return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fTrans = getActivity().getSupportFragmentManager().beginTransaction();
                AddNoteFragment fragment = new AddNoteFragment();
                fTrans.replace(R.id.container, fragment, "addNoteFragment").addToBackStack("tag");
                fTrans.commit();
            }
        });

        if (notes.size() == 0)
            emptyListText.setVisibility(View.VISIBLE);
        else
            emptyListText.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkReminderIsOut();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.notes_list_menu, menu);

        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                findNotes(s.toLowerCase());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (searchView.hasFocus())
                    findNotes(s.toLowerCase());

                return false;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                menu.setGroupVisible(R.id.list_group, false);
                fab.hide();
                findNotes("");

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                menu.setGroupVisible(R.id.list_group, true);
                fab.show();
                updateList(false);

                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort: showSortDialog(); return true;
            case R.id.action_settings: showSettings(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAddNoteFragmentInteraction(Note note) {
        notes.add(note);
        updateList(false);
        String message = getString(R.string.successful_adding);

        if (note.isReminderSet()) {
            message += "\n" + Utils.viewableTime(getActivity(), note.getNotificationTime(), Utils.KEY_REMINDER_SET);
            setNotification(note);
        }

        Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
        ((TextView) toast.getView().findViewById(android.R.id.message)).setGravity(Gravity.CENTER);
        toast.show();
    }

    @Override
    public void onNoteInfoFragmentInteraction(Note note, int mode) {
        switch (mode) {
            case NoteInfoFragment.MODE_DELETE: deleteNote(note); break;
            case NoteInfoFragment.MODE_CLOSE: updateList(false); break;
        }

        updateList(false);
    }

    @Override
    public void onEditNoteFragmentInteraction(Note old, Note note, boolean needChange) {
        int pos = notes.indexOf(old);
        notes.remove(pos);
        notes.add(pos, note);

        String message = getString(R.string.successful_editing);
        String additional = "";

        if (note.isReminderSet() && !old.isReminderSet() || note.getNotificationTime() != old.getNotificationTime() && note.getNotificationTime() != 0 || needChange) {
            additional = "\n" + Utils.viewableTime(getActivity(), note.getNotificationTime(), Utils.KEY_REMINDER_SET);
            cancelNotification(old);
            setNotification(note);
        }

        if (!note.isReminderSet() && old.isReminderSet()) {
            additional = "";
            cancelNotification(note);
        }

        Toast toast = Toast.makeText(getActivity(), message + additional, Toast.LENGTH_SHORT);
        ((TextView) toast.getView().findViewById(android.R.id.message)).setGravity(Gravity.CENTER);
        toast.show();
    }

    @Override
    public void onSetReminderDialogInteraction(int position, long time) {
        notes.get(position).setNotificationTime(time);
        notes.get(position).setReminderSet(true);
        Toast.makeText(getActivity(), Utils.viewableTime(getActivity(), time, Utils.KEY_REMINDER_SET), Toast.LENGTH_SHORT).show();
        setNotification(notes.get(position));
        updateList(false);
    }

    @Override
    public void onButtonClick(int position) {
        Note note = notes.get(position);

        if (!note.isReminderSet()) {
            if (note.getNotificationTime() != 0 && note.getNotificationTime() > Calendar.getInstance().getTimeInMillis()) {
                notes.get(position).setReminderSet(true);
                setNotification(note);
                updateList(false);
            } else {
                SetReminderDialog dialog = SetReminderDialog.newInstance(position);
                dialog.show(getActivity().getSupportFragmentManager(), "setReminderDialog");
            }
        } else {
            notes.get(position).setReminderSet(false);
            cancelNotification(note);
            updateList(false);
        }
    }

    public void checkReminderIsOut() {
        for (Note note : notes) {
            if (note.getNotificationTime() <= Calendar.getInstance().getTimeInMillis()) {
                note.setNotificationTime(0);
                note.setReminderSet(false);
            }
        }

        updateList(false);
    }

    private void checkPassword(int position, int action) {
        Note note = notes.get(position);

        if (note.isPasswordSet())
            showEnterPasswordDialog(note, action);
        else
            switchActions(note, action);
    }

    private void switchActions(Note note, int action) {
        switch (action) {
            case ACTION_OPEN: showNoteInfoFragment(note); break;
            case ACTION_EDIT: editNote(note); break;
            case ACTION_DELETE: showDeleteDialog(note); break;
        }
    }

    private void showNoteInfoFragment(Note note) {
        FragmentTransaction fTrans = getActivity().getSupportFragmentManager().beginTransaction();
        NoteInfoFragment fragment = NoteInfoFragment.newInstance(note);
        fTrans.replace(R.id.container, fragment, "noteInfoFragment").addToBackStack("tag");
        fTrans.commit();
    }

    private void showSettings() {
        Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void showEnterPasswordDialog(final Note note, final int action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View enterPasswordView = getActivity().getLayoutInflater().inflate(R.layout.enter_password_dialog, null);
        enterPasswordEditText = enterPasswordView.findViewById(R.id.enterPasswordEditText);

        builder.setView(enterPasswordView)
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.enter), null)
                .setCancelable(false);

        final AlertDialog enterPasswordDialog = builder.create();

        enterPasswordDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);

                enterPasswordEditText.requestFocus();
                ((MainActivity) getActivity()).openKeyboard();

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (note.getPassword().equals(enterPasswordEditText.getText().toString())) {
                            switchActions(note, action);
                            enterPasswordDialog.cancel();
                        } else {
                            enterPasswordEditText.setText("");
                            ((MainActivity) getActivity()).showCenteredToast(getString(R.string.wrong_password));
                        }
                    }
                });
            }
        });

        enterPasswordDialog.show();
    }

    private void showSortDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getString(R.string.choose_sorting))
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(getString(R.string.sort), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sortType = sortTypeTmp;
                        ((MainActivity) getActivity()).setCurrentSortType(sortType);
                        updateList(false);
                    }
                })
                .setSingleChoiceItems(sortTypes, sortType, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sortTypeTmp = which;
                    }
                });

        builder.show();
    }

    private void sortNotes(boolean isSearch) {
        switch (sortType) {
            case SORT_TYPE_CREATED_TIME: sortByCreatedTime(MODE_DIRECT); break;
            case SORT_TYPE_CREATED_TIME_REVERSED: sortByCreatedTime(MODE_REVERSE); break;
            case SORT_TYPE_ALPHABETICALLY: sortAlphabetically(MODE_DIRECT); break;
            case SORT_TYPE_ALPHABETICALLY_REVERSED: sortAlphabetically(MODE_REVERSE); break;
        }

        sortPinnedNotes();

        if (!isSearch)
            showNotes();
    }

    private void sortByCreatedTime(int mode) {
        Comparator<Note> createdTimeSorting = new Comparator<Note>() {
            public int compare(Note note1, Note note2) {
                return Long.compare(note1.getAddTime(), note2.getAddTime());
            }
        };

        Collections.sort(notes, createdTimeSorting);

        if (mode == MODE_REVERSE)
            Collections.reverse(notes);
    }

    private void sortAlphabetically(int mode) {
        Comparator<Note> alphabeticalSorting = new Comparator<Note>() {
            public int compare(Note note1, Note note2) {
                int res = String.CASE_INSENSITIVE_ORDER.compare(note1.getTitle(), note2.getTitle());

                if (res == 0)
                    res = note1.getTitle().compareTo(note2.getTitle());

                return res;
            }
        };

        Collections.sort(notes, alphabeticalSorting);

        if (mode == MODE_REVERSE)
            Collections.reverse(notes);
    }

    private void sortPinnedNotes() {
        Comparator<Note> pinComparator = new Comparator<Note>() {
            @Override
            public int compare(Note note1, Note note2) {
                if (note1.isPinned() && !note2.isPinned())
                    return -1;

                if (!note1.isPinned() && note2.isPinned())
                    return 1;

                return 0;
            }
        };

        Collections.sort(notes, pinComparator);
    }

    private void findNotes(String query) {
        showCount = 0;

        for (Note note : notes)
            if (note.getTitle().toLowerCase().contains(query)) {
                note.setShow(true);
                showCount++;
            } else
                note.setShow(false);

        updateList(true);
    }

    private void showNotes() {
        for (Note note : notes)
            if (!note.isShow())
                note.setShow(true);
    }

    private void setNotification(Note note) {
        Intent notificationIntent = new Intent(getActivity(), NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTE_ID, note.getId());
        notificationIntent.putExtra(NotificationPublisher.NOTE_TITLE, note.getTitle());
        notificationIntent.putExtra(NotificationPublisher.NOTE_BODY, note.getBody());
        notificationIntent.putExtra(NotificationPublisher.NOTE_IS_PASS, note.isPasswordSet());
        notificationIntent.putExtra(NotificationPublisher.NOTE_TIME, note.getNotificationTime());
        PendingIntent pendingIntent = getPendingIntent(note.getId(), notificationIntent);

        alarmManager.set(AlarmManager.RTC_WAKEUP, note.getNotificationTime(), pendingIntent);
    }

    private void cancelNotification(Note note) {
        Intent intent = new Intent(getActivity(), NotificationPublisher.class);
        PendingIntent pendingIntent = getPendingIntent(note.getId(), intent);
        pendingIntent.cancel();
        alarmManager.cancel(pendingIntent);
    }

    private PendingIntent getPendingIntent(int id, Intent intent) {
        return PendingIntent.getBroadcast(getActivity(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void updateList(boolean isSearch) {
        if (!isSearch) {
            ArrayList<Note> tmp = Note.insertOrUpdateDB(((MainActivity) getActivity()).getHelper(), notes);
            notes.clear();
            notes.addAll(tmp);
        }

        if (isSearch && showCount == 0)
            emptySearchText.setVisibility(View.VISIBLE);
        else
            emptySearchText.setVisibility(View.GONE);

        sortNotes(isSearch);
        adapter.notifyDataSetChanged();

        if (notes.size() == 0 && !isSearch)
            emptyListText.setVisibility(View.VISIBLE);
        else
            emptyListText.setVisibility(View.GONE);
    }

    private void showDeleteDialog(final Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                        deleteNote(note);
                    }
                });

        builder.create().show();
    }

    private void deleteNote(Note note) {
        NotificationManager manager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notes.remove(note);
        Note.deleteFromDB(((MainActivity)getActivity()).getHelper(), note);

        if (note.isReminderSet())
            cancelNotification(note);

        if (manager != null)
            manager.cancel(note.getId());

        Toast.makeText(getActivity(), getString(R.string.successful_deleting_note), Toast.LENGTH_SHORT).show();
        updateList(false);
    }

    private void editNote(Note note) {
        FragmentTransaction fTrans = getActivity().getSupportFragmentManager().beginTransaction();
        EditNoteFragment fragment = EditNoteFragment.newInstance(note, EditNoteFragment.FROM_LIST);
        fTrans.replace(R.id.container, fragment, "editNoteFragment").addToBackStack("tag");
        fTrans.commit();
    }

    private void pinNote(Note note) {
        if (note != null) {
            if (note.isPinned())
                note.setPinned(false);
            else
                note.setPinned(true);
        }

        updateList(false);
    }
}