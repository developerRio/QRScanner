package com.originalstocks.qrscanner.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.originalstocks.qrscanner.Database.Note;
import com.originalstocks.qrscanner.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.NotesViewHolder> {

    private Context context;
    private List<Note> mNotes;
    private OnDeleteClickListener deleteClickListener;

    public RecyclerAdapter(Context context, OnDeleteClickListener listener) {
        this.context = context;
        this.deleteClickListener = listener;
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.qr_saved_list_layout, parent, false);
        return new NotesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {

        if (mNotes != null) {
            Note note = mNotes.get(position);
            holder.setQRData(note.getNote(), position);
            holder.setImagePath(note.getImagePath(), position);
            holder.setDeleteListener();

        } else {
            // Covers the case when not being ready yet
            Toast.makeText(context, "Scan the QR code to see the data here...", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public int getItemCount() {
        if (mNotes != null) {
            return mNotes.size();
        } else {
            return 0;
        }
    }

    public void setmNotes(List<Note> notes) {
        mNotes = notes;
        notifyDataSetChanged();
    }

    public interface OnDeleteClickListener {
        void OnDeleteClickListener(Note myNote);
    }

    public class NotesViewHolder extends RecyclerView.ViewHolder {

        int mPosition;
        CircleImageView qrImageView;
        TextView tittle;
        ImageView deleteButton;

        public NotesViewHolder(@NonNull View itemView) {
            super(itemView);

            qrImageView = itemView.findViewById(R.id.image_view_qr);
            tittle = itemView.findViewById(R.id.tittle_qr);
            deleteButton = itemView.findViewById(R.id.deleteButton);

        }

        public void setQRData(String note, int position) {
            tittle.setText(note);
            mPosition = position;
        }

        public void setImagePath(String imagePath, int position) {
            // Set the Image from file path
            Glide.with(context).load(imagePath).into(qrImageView);
            mPosition = position;
        }

        public void setDeleteListener() {
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (deleteClickListener != null) {
                        deleteClickListener.OnDeleteClickListener(mNotes.get(mPosition));
                    }
                }
            });
        }
    }
}
