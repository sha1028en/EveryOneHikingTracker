package eu.basicairdata.graziano.gpslogger;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;

import eu.basicairdata.graziano.gpslogger.databinding.ItemCourseBinding;

public class CourseNameRecyclerAdapter extends RecyclerView.Adapter<CourseNameRecyclerAdapter.CourseNameViewHolder> {
    private ItemCourseBinding bind;
    private final LinkedList<ItemCourseData> courseList;

    public CourseNameRecyclerAdapter() {
        this.courseList = new LinkedList<>();
    }

    public CourseNameRecyclerAdapter(@NonNull final LinkedList<ItemCourseData> courseList) {
        this.courseList = courseList;
    }

    @NonNull @Override
    public CourseNameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.bind = ItemCourseBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

        CourseNameViewHolder holder = new CourseNameViewHolder(this.bind.getRoot());
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CourseNameViewHolder holder, int position) {
        holder.onBind(this.courseList.get(position));
    }

    @Override
    public int getItemCount() {
        return this.courseList.size();
    }

    public void addCourseItem(@NonNull final ItemCourseData item) {
        this.courseList.add(item);
        this.notifyItemChanged(this.getItemCount());
    }

    public LinkedList<ItemCourseData> getCloneCourseList() {
        return (LinkedList<ItemCourseData>) this.courseList.clone();
    }

    public static class CourseNameViewHolder extends RecyclerView.ViewHolder {
        private ItemCourseBinding bind;

        public CourseNameViewHolder(@NonNull View itemView) {
            super(itemView);
            this.bind = ItemCourseBinding.bind(itemView);
        }

        public void onBind(ItemCourseData item) {
            this.bind.courseTitle.setText(item.getCourseName());
            this.bind.courseDistance.setText(item.getCourseDistance() + "m");
        }
    }
}
