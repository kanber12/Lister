package by.kanber.lister;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    private Context context;
    private OnButtonClickListener buttonListener;
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;
    private ArrayList<Note> notes;

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        clickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        longClickListener = listener;
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public FrameLayout pinMark;
        public TextView titleTextView, timeTextView, reminderTextView;
        public ImageView passwordIcon, reminderButton;

        public NoteViewHolder(View itemView, final OnItemClickListener click, final OnItemLongClickListener longClick) {
            super(itemView);

            final TextView anchor = itemView.findViewById(R.id.anchor);
            cardView = itemView.findViewById(R.id.card_view);
            pinMark = itemView.findViewById(R.id.pinMark);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            reminderTextView = itemView.findViewById(R.id.reminderTextView);
            passwordIcon = itemView.findViewById(R.id.passwordIcon);
            reminderButton = itemView.findViewById(R.id.reminder_button);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (click != null) {
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION)
                            click.onItemClick(position);
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (longClick != null) {
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION)
                            longClick.onItemLongClick(position, anchor);
                    }

                    return true;
                }
            });
        }
    }

    public NotesAdapter(Context context, ArrayList<Note> notes) {
        this.context = context;
        this.notes = notes;
        Fragment fragment = ((MainActivity) context).getSupportFragmentManager().findFragmentByTag("notesListFragment");
        this.buttonListener = (OnButtonClickListener) fragment;
    }

    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.note_list_item, parent, false);

        return new NoteViewHolder(view, clickListener, longClickListener);
    }

    @Override
    public void onBindViewHolder(final NoteViewHolder holder, int position) {
        Note note = notes.get(position);

        if (note.isShow()) {
            holder.cardView.setVisibility(View.VISIBLE);
            holder.titleTextView.setText(note.getTitle());
            holder.titleTextView.setTextColor(Utils.getColor(context, R.attr.colorNoteTitleText));
            holder.timeTextView.setText(Utils.viewableTime(context, note.getAddTime(), Utils.KEY_ADDED));
            holder.timeTextView.setTextColor(Utils.getColor(context, R.attr.colorNoteTimeText));

            if (note.isReminderSet()) {
                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_action_set_time_true);
                drawable.setColorFilter(Utils.getColor(context, R.attr.colorReminderON), PorterDuff.Mode.MULTIPLY);
                holder.reminderButton.setImageDrawable(drawable);
            } else
                holder.reminderButton.setImageResource(R.drawable.ic_action_set_time_false);

            if (note.getNotificationTime() != 0)
                holder.reminderTextView.setText(Utils.viewableTime(context, note.getNotificationTime(), Utils.KEY_NONE));
            else
                holder.reminderTextView.setText("");

            if (note.isPasswordSet()) {
                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_has_password);
                drawable.setColorFilter(Utils.getColor(context, R.attr.colorReminderON), PorterDuff.Mode.MULTIPLY);
                holder.passwordIcon.setImageDrawable(drawable);
            } else
                holder.passwordIcon.setImageResource(android.R.color.transparent);

            if (note.isPinned()) {
                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.pinned_note_mask);
                drawable.setColorFilter(Utils.getColor(context, R.attr.colorPinnedNote), PorterDuff.Mode.MULTIPLY);
                holder.pinMark.setBackgroundDrawable(drawable);
            } else
                holder.pinMark.setBackgroundResource(android.R.color.transparent);

            holder.reminderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    buttonListener.onButtonClick(holder.getAdapterPosition());
                }
            });
        } else
            holder.cardView.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public interface OnButtonClickListener {
        void onButtonClick(int position);
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position, View view);
    }
}
