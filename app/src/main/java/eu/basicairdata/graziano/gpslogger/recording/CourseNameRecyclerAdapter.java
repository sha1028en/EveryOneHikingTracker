package eu.basicairdata.graziano.gpslogger.recording;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;

import eu.basicairdata.graziano.gpslogger.R;
import eu.basicairdata.graziano.gpslogger.databinding.ItemCourseBinding;

public class CourseNameRecyclerAdapter extends RecyclerView.Adapter<CourseNameRecyclerAdapter.CourseNameViewHolder> {
    private ItemCourseBinding bind;
    private LinkedList<ItemCourseData> courseList;
    private String selectedCourseName; // Cursor
    private ItemCourseData selectCourse; // Cursor

    // when checkBox state changed, notify others
    interface OnItemSelectListener {
        void onItemSelected(boolean isDeck, ItemCourseData item);
    }
    private OnItemSelectListener listener;

    public CourseNameRecyclerAdapter(@NonNull final OnItemSelectListener listener) {
        this.courseList = new LinkedList<>();
        this.selectedCourseName = "";
        this.listener = listener;
    }

    @NonNull @Override
    public CourseNameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.bind = ItemCourseBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

        CourseNameViewHolder holder = new CourseNameViewHolder(this.bind.getRoot());
        this.bind.courseRoot.setOnClickListener(v -> {
            this.selectCourse = this.courseList.get(holder.getBindingAdapterPosition());
            this.selectedCourseName = this.selectCourse.getCourseName();
            this.updateItemSelect(holder.getBindingAdapterPosition());
            if(this.listener != null) listener.onItemSelected(this.selectCourse.isWoodDeck(), this.selectCourse);
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CourseNameViewHolder holder, int position) {
        holder.onBind(this.courseList.get(position));
    }

    @Override
    public int getItemCount() {
        if(this.courseList != null) return this.courseList.size();
        else return 0;
    }

    /**
     * @param clickedCoursePosition update state Item when its pos item selected
     */
    private void updateItemSelect(int clickedCoursePosition) {
        if(this.courseList != null) {
            int index = 0;
            for(ItemCourseData item : this.courseList) {
                item.setClicked(clickedCoursePosition == index);
                ++index;
            }
            this.notifyDataSetChanged();
        }
    }

    public void addCourseItem(@NonNull final ItemCourseData item) {
        if(this.courseList != null) {
            this.courseList.add(item);
            this.notifyItemChanged(this.getItemCount());
        }
    }

    public LinkedList<ItemCourseData> getCloneCourseList() {
        return (LinkedList<ItemCourseData>) this.courseList.clone();
    }

    public void release() {
        if(this.courseList != null) {
            this.courseList.clear();
            this.courseList = null;
        }
        this.selectedCourseName = null;
        this.selectCourse = null;
        this.listener = null;
        this.bind = null;
    }

    public String getSelectedCourseName() {
        return this.selectedCourseName;
    }

    public ItemCourseData getSelectCourse() {
        if(this.selectCourse != null) return selectCourse;
        else return null;
    }

    public void updateCourse(@NonNull final ItemCourseData course) {
        int i = 0;
        boolean isUpdate = false;

        if(this.courseList != null) {
            for(ItemCourseData item : this.courseList) {
                if(item.getCourseName().equals(course.getCourseName()) && item.getTrackName().equals(course.getTrackName())) {
                    isUpdate = true;
                    break;
                }
                ++i;
            }

            if(isUpdate) {
                this.selectCourse = course;
                this.courseList.set(i, course);
            }
        }
    }

    public boolean removeCourse(@NonNull final String trackName, @NonNull final String courseName) {
        boolean isRemove = false;
        ItemCourseData toRemoveCourse = null;
        if(this.courseList != null && this.bind != null) {
            for(ItemCourseData course : this.courseList) {
                if(course.getTrackName().equals(trackName) && course.getCourseName().equals(courseName)) {
                    toRemoveCourse = course;
                    break;
                }
            }
            isRemove = this.removeCourse(toRemoveCourse);

            // remove course, before select them
            // if course removed, selected course has been remove together ( update Cursor )
            if(isRemove) {
                this.selectCourse = null;
                this.selectedCourseName = "";
            }
        }
        return isRemove;
    }

    public boolean removeCourse(final ItemCourseData course) {
        boolean isRemove = false;
        if(this.courseList != null && course != null && this.bind != null) {
            isRemove = this.courseList.remove(course);
        }
        this.notifyDataSetChanged();
        return isRemove;
    }

    public static class CourseNameViewHolder extends RecyclerView.ViewHolder {
        private final ItemCourseBinding bind;

        public CourseNameViewHolder(@NonNull View itemView) {
            super(itemView);
            this.bind = ItemCourseBinding.bind(itemView);
        }

        public void onBind(ItemCourseData item) {
            if(this.bind != null) {
                this.bind.courseTitle.setText(item.getCourseName());
                this.bind.courseDistance.setText(String.format("%dm", item.getCourseDistance()));

                if(item.getIsClicked()) { // Clicked
                    this.bind.courseTitle.setTextColor(this.bind.getRoot().getContext().getColor(R.color.colorPrimaryDark));
                    this.bind.courseDistance.setTextColor(this.bind.getRoot().getContext().getColor(R.color.colorPrimaryDark));

                } else { // isnt Clicked
                    this.bind.courseTitle.setTextColor(this.bind.getRoot().getContext().getColor(R.color.textColorSecondary));
                    this.bind.courseDistance.setTextColor(this.bind.getRoot().getContext().getColor(R.color.textColorSecondary));
                }
            }
        }
    }
}
