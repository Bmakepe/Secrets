package com.makepe.blackout.GettingStarted.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.InAppActivities.GroupChatActivity;
import com.makepe.blackout.GettingStarted.Models.GroupsModel;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {
    private Context context;
    private List<GroupsModel> groupList;

    private DatabaseReference groupReference;

    public GroupAdapter(Context context, List<GroupsModel> groupList) {
        this.context = context;
        this.groupList = groupList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_chatlist_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupsModel group = groupList.get(position);
        groupReference = FirebaseDatabase.getInstance().getReference("SecretGroups");

        getGroupDetails(group, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupID", group.getGroupID());
                context.startActivity(intent);
            }
        });
    }

    private void getGroupDetails(GroupsModel group, ViewHolder holder) {
        groupReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    GroupsModel myGroup = ds.getValue(GroupsModel.class);

                    assert myGroup != null;
                    if (myGroup.getGroupID().equals(group.getGroupID())){

                        holder.contactName.setText(myGroup.getGroupName());

                        try{
                            Picasso.get().load(myGroup.getGroupProPic()).into(holder.groupProPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.groupProPic);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView contactName, groupMessage, timeStampTV, senderName;
        CircleImageView groupProPic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            contactName = itemView.findViewById(R.id.groupChatlistName);
            groupMessage = itemView.findViewById(R.id.groupchatlistMessage);
            groupProPic = itemView.findViewById(R.id.groupChatListPropic);
            timeStampTV = itemView.findViewById(R.id.groupLastTimastamp);
            senderName = itemView.findViewById(R.id.groupLastSender);

        }
    }
}
