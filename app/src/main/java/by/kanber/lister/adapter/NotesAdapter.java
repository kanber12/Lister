package by.kanber.lister.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import by.kanber.lister.R;
import by.kanber.lister.util.Utils;
import by.kanber.lister.model.Note;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> implements NoteTouchHelperAdapter {
    private Context context;
    private OnButtonClickListener buttonListener;
    private OnItemClickListener clickListener;
    private OnOptionsClickListener optionsClickListener;
    private OnMoveCompleteListener moveCompleteListener;
    private ArrayList<Note> notes;

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        clickListener = listener;
    }

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        buttonListener = listener;
    }

    public void setOnOptionsClickListener(OnOptionsClickListener listener) {
        optionsClickListener = listener;
    }

    public void setOnMoveCompleteListener(OnMoveCompleteListener listener) {
        moveCompleteListener = listener;
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public TextView titleTextView, timeTextView, reminderTextView;
        public ImageView passwordIcon, reminderButton, noteOptions;

        public NoteViewHolder(View itemView, final OnItemClickListener itemClickListener, final OnButtonClickListener buttonClickListener, final OnOptionsClickListener optionsClickListener) {
            super(itemView);

            cardView = itemView.findViewById(R.id.card_view);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            timeTextView = itemView.findViewById(R.id.time_text_view);
            reminderTextView = itemView.findViewById(R.id.reminder_text_view);
            passwordIcon = itemView.findViewById(R.id.password_icon);
            reminderButton = itemView.findViewById(R.id.reminder_button);
            noteOptions = itemView.findViewById(R.id.note_options);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (itemClickListener != null) {
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION)
                            itemClickListener.onItemClick(position);
                    }
                }
            });

            reminderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (buttonClickListener != null) {
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION)
                            buttonClickListener.onButtonClick(position);
                    }
                }
            });

            noteOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (optionsClickListener != null) {
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION)
                            optionsClickListener.onOptionsClick(position, noteOptions);
                    }
                }
            });
        }
    }

    public NotesAdapter(Context context, ArrayList<Note> notes) {
        this.context = context;
        this.notes = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.note_list_item, parent, false);

        return new NoteViewHolder(view, clickListener, buttonListener, optionsClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final NoteViewHolder holder, int position) {
        Note note = notes.get(position);

        if (note.isShow()) {
            holder.cardView.setVisibility(View.VISIBLE);
            holder.titleTextView.setText(note.getTitle());
            holder.titleTextView.setTextColor(Utils.getColor(context, R.attr.colorNoteTitleText));
            holder.timeTextView.setText(Utils.viewableTime(context, note.getAddTime(), Utils.KEY_ADDED));
            holder.timeTextView.setTextColor(Utils.getColor(context, R.attr.colorNoteTimeText));

            if (note.isReminderSet()) {
                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_action_reminder_on);

                if (drawable != null) {
                    drawable.setColorFilter(Utils.getColor(context, R.attr.colorReminderON), PorterDuff.Mode.MULTIPLY);
                    holder.reminderButton.setImageDrawable(drawable);
                }
            } else {
                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_action_reminder_off);

                if (drawable != null) {
                    drawable.setColorFilter(Utils.getColor(context, R.attr.colorReminderOFF), PorterDuff.Mode.MULTIPLY);
                    holder.reminderButton.setImageDrawable(drawable);
                }
            }

            if (note.getNotificationTime() != 0)
                holder.reminderTextView.setText(Utils.viewableTime(context, note.getNotificationTime(), Utils.KEY_NONE));
            else
                holder.reminderTextView.setText("");

            if (note.isPasswordSet()) {
                holder.passwordIcon.setVisibility(View.VISIBLE);
                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_has_password);

                if (drawable != null) {
                    drawable.setColorFilter(Utils.getColor(context, R.attr.colorReminderON), PorterDuff.Mode.MULTIPLY);
                    holder.passwordIcon.setImageDrawable(drawable);
                }
            } else {
                holder.passwordIcon.setImageResource(android.R.color.transparent);
                holder.passwordIcon.setVisibility(View.GONE);
            }

            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_options);

            if (drawable != null) {
                drawable.setColorFilter(Utils.getColor(context, R.attr.colorReminderON), PorterDuff.Mode.MULTIPLY);
                holder.noteOptions.setImageDrawable(drawable);
            }
        } else
            holder.cardView.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public boolean onNoteMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition)
            for (int i = fromPosition; i < toPosition; i++)
                Collections.swap(notes, i, i + 1);
        else
            for (int i = fromPosition; i > toPosition; i--)
                Collections.swap(notes, i, i - 1);

        moveCompleteListener.onMoveComplete();
        notifyItemMoved(fromPosition, toPosition);

        return true;
    }

    public interface OnButtonClickListener {
        void onButtonClick(int position);
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnOptionsClickListener {
        void onOptionsClick(int position, View view);
    }

    public interface OnMoveCompleteListener {
        void onMoveComplete();
    }
}