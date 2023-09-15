package eu.basicairdata.graziano.gpslogger.recording;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;

import eu.basicairdata.graziano.gpslogger.R;
import eu.basicairdata.graziano.gpslogger.databinding.ItemCourseBinding;
import eu.basicairdata.graziano.gpslogger.management.TrackRecordManager;
import eu.basicairdata.graziano.gpslogger.recording.enhanced.ItemCourseEnhancedData;

public class CourseNameRecyclerAdapter extends RecyclerView.Adapter<CourseNameRecyclerAdapter.CourseNameViewHolder> {
    private ItemCourseBinding bind;
    private LinkedList<ItemCourseEnhancedData> courseList;
    private String selectedCourseName; // Cursor
    private ItemCourseEnhancedData selectCourse; // Cursor

    // when checkBox state changed, notify others
    private OnItemSelectListener listener;
    public interface OnItemSelectListener {
        void onItemSelected(String courseType, ItemCourseEnhancedData item);
    }

    public CourseNameRecyclerAdapter(@NonNull final OnItemSelectListener listener) {
        this.courseList = new LinkedList<>();
        this.selectedCourseName = "";
        this.listener = listener;
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
        this.bind.courseRoot.setOnClickListener(v -> {
            TrackRecordManager recordManager = TrackRecordManager.getInstance();

            if (recordManager != null && !recordManager.isRecordingCourse()) {
                // when Recording Course, CAN NOT SELECT other course
                this.updateItemSelect(position);
                this.selectCourse = this.courseList.get(position);
                this.selectedCourseName = this.selectCourse.getCourseName();
                if(this.listener != null) listener.onItemSelected(this.selectCourse.getCourseType(), this.selectCourse);
            }
            recordManager = null; // GC HURRY!
        });
    }

    @Override
    public int getItemCount() {
        if(this.courseList != null) return this.courseList.size();
        else return 0;
    }

    @Override
    public int getItemViewType(int position) {
//        return super.getItemViewType(position);
        return position;
    }

    /**
     * @param clickedCoursePosition update state Item when its pos item selected
     */
    private void updateItemSelect(int clickedCoursePosition) {
        if(this.courseList != null) {
            int index = 0;
            for(ItemCourseEnhancedData item : this.courseList) {
                item.setClicked(clickedCoursePosition == index);
                ++index;
            }
            this.notifyDataSetChanged();
        }
    }

    public void addCourseItem(@NonNull final ItemCourseEnhancedData item) {
        if(this.courseList != null) {
            this.courseList.add(item);
            this.notifyItemChanged(this.getItemCount());
        }
    }

    public void addCourseItems(@NonNull final LinkedList<ItemCourseEnhancedData> item) {
        if(this.courseList != null) {
            this.courseList.addAll(item);
            this.notifyDataSetChanged();
        }
    }

    /**
     * replace CourseItem when it found has same trackName, courseName item, replace that
     *
     * @param toReplaceItem to Replace Item
     * @return is list has changed???
     */
    public boolean replaceCourseItem(@NonNull final ItemCourseEnhancedData toReplaceItem) {
        boolean hasFound = false;

        if(this.courseList != null) {
            int index = 0;
            for(ItemCourseEnhancedData buffer : this.courseList) {
                // is it has same TrackName, CourseName?
                if(buffer.getTrackName().equals(toReplaceItem.getTrackName()) && buffer.getCourseName().equals(toReplaceItem.getCourseName())) {
                    hasFound = true;
                    break;
                }
                ++index;
            }

            if(hasFound) { // replace list
                this.courseList.set(index, toReplaceItem);
                this.notifyItemChanged(index);
            }
        }
        return hasFound;
    }

    public LinkedList<ItemCourseEnhancedData> getCloneCourseList() {
        LinkedList<ItemCourseEnhancedData> clonedCourseList = (LinkedList<ItemCourseEnhancedData>) this.courseList.clone();
        return clonedCourseList;
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

    public ItemCourseEnhancedData getSelectCourse() {
        if(this.selectCourse != null) return selectCourse;
        else return null;
    }

    public void updateCourse(@NonNull final ItemCourseEnhancedData course) {
        int i = 0;
        boolean isUpdate = false;

        if(this.courseList != null) {
            for(ItemCourseEnhancedData item : this.courseList) {
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
        ItemCourseEnhancedData toRemoveCourse = null;
        if(this.courseList != null && this.bind != null) {
            for(ItemCourseEnhancedData course : this.courseList) {
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

    public boolean removeCourse(final ItemCourseEnhancedData course) {
        boolean isRemove = false;

        if(this.courseList != null && course != null && this.bind != null) {
            isRemove = this.courseList.remove(course);
            this.notifyDataSetChanged();
        }
        return isRemove;
    }

    // INNER CLASS MIGHT BE STATIC CLASS
    // FOR PREVENT MEMORY LEAK
    public static class CourseNameViewHolder extends RecyclerView.ViewHolder {
        private final ItemCourseBinding bind;

        public CourseNameViewHolder(@NonNull View itemView) {
            super(itemView);
            this.bind = ItemCourseBinding.bind(itemView);
        }

        public void onBind(ItemCourseEnhancedData item) {
            if(this.bind != null) {
                this.bind.courseTitle.setText(item.getCourseName());
                this.bind.courseDistance.setText(String.format("%dm", (int) item.getCourseDistance()));

                if(item.getIsClicked()) { // Clicked
                    this.bind.courseTitle.setTextColor(this.bind.getRoot().getContext().getColor(R.color.colorPrimaryDark));
                    this.bind.courseDistance.setTextColor(this.bind.getRoot().getContext().getColor(R.color.colorPrimaryDark));

                } else { // isnt Clicked
                    this.bind.courseTitle.setTextColor(this.bind.getRoot().getContext().getColor(R.color.textColorDisable));
                    this.bind.courseDistance.setTextColor(this.bind.getRoot().getContext().getColor(R.color.textColorDisable));
                }
            }
        }
    }
}
