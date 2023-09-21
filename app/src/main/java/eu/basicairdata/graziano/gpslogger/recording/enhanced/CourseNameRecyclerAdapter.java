package eu.basicairdata.graziano.gpslogger.recording.enhanced;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;

import eu.basicairdata.graziano.gpslogger.R;
import eu.basicairdata.graziano.gpslogger.databinding.ItemCourseBinding;
import eu.basicairdata.graziano.gpslogger.management.TrackRecordManager;

public class CourseNameRecyclerAdapter extends RecyclerView.Adapter<CourseNameRecyclerAdapter.CourseNameViewHolder> {
    private ItemCourseBinding bind;
    private LinkedList<ItemCourseEnhanced> courseList;
    private String selectedCourseName; // Cursor
    private ItemCourseEnhanced selectCourse; // Cursor

    // when checkBox state changed, notify others
    private OnItemSelectListener listener;
    public interface OnItemSelectListener {
        void onItemSelected(String courseType, ItemCourseEnhanced item);
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

        this.bind.courseItemRoot.setOnClickListener(v -> {
            TrackRecordManager recordManager = TrackRecordManager.getInstance();
            if (recordManager != null && !recordManager.isRecordingCourse()) {
                // when Recording Course, CAN NOT SELECT other course
                this.updateItemSelect(holder.getBindingAdapterPosition());
                this.selectCourse = this.courseList.get(holder.getBindingAdapterPosition());
                this.selectedCourseName = this.selectCourse.getCourseName();
                if(this.listener != null) listener.onItemSelected(this.selectCourse.getCourseType(), this.selectCourse);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CourseNameViewHolder holder, int position) {
        holder.onBind(this.courseList.get(holder.getBindingAdapterPosition()));
    }

    @Override
    public void onViewRecycled(@NonNull CourseNameViewHolder holder) {
        final int position = holder.getBindingAdapterPosition();
        if(position > -1) {
            holder.onBind(this.courseList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        if(this.courseList != null) return this.courseList.size();
        else return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    /**
     * @param clickedCoursePosition update state Item when its pos item selected
     */
    private void updateItemSelect(int clickedCoursePosition) {
        if(this.courseList != null) {
            int index = 0;
            for(ItemCourseEnhanced item : this.courseList) {
                item.setClicked(clickedCoursePosition == index);
                ++index;
            }
            this.notifyItemRangeChanged(0, this.courseList.size());
        }
    }

    public void addCourseItem(@NonNull final ItemCourseEnhanced item) {
        if(this.courseList != null) {
            this.courseList.add(item);
            this.notifyItemChanged(this.getItemCount());
        }
    }

    /**
     * add Course and Choose it
     * @param item to add Course item
     */
    public void addNewCourseItem(@NonNull final ItemCourseEnhanced item) {
        if(this.courseList != null) {
            this.courseList.add(item);
            this.notifyItemChanged(this.getItemCount());
        }
        this.updateItemSelect(this.getItemCount() -1);

        // update Cursor
        this.selectedCourseName = item.getCourseName();
        this.selectCourse = item;
    }

    public void addCourseItems(@NonNull final LinkedList<ItemCourseEnhanced> item) {
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
    public boolean replaceCourseItem(@NonNull final ItemCourseEnhanced toReplaceItem) {
        boolean hasFound = false;

        if(this.courseList != null) {
            int index = 0;
            for(ItemCourseEnhanced buffer : this.courseList) {
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

    public LinkedList<ItemCourseEnhanced> getCloneCourseList() {
        LinkedList<ItemCourseEnhanced> clonedCourseList = (LinkedList<ItemCourseEnhanced>) this.courseList.clone();
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

    public ItemCourseEnhanced getSelectCourse() {
        if(this.selectCourse != null) return selectCourse;
        else return null;
    }

    public void updateCourse(@NonNull final ItemCourseEnhanced course) {
        int i = 0;
        boolean isUpdate = false;

        if(this.courseList != null) {
            for(ItemCourseEnhanced item : this.courseList) {
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
        ItemCourseEnhanced toRemoveCourse = null;
        int i = 0;

        if(this.courseList != null && this.bind != null) {
            for(ItemCourseEnhanced course : this.courseList) {
                if(course.getTrackName().equals(trackName) && course.getCourseName().equals(courseName)) {
                    toRemoveCourse = course;
                    break;
                }
                ++i;
            }
            isRemove = this.removeCourse(toRemoveCourse);

            // remove course, before select them
            // if course removed, selected course has been remove together ( update Cursor )
            if(isRemove) {
                this.selectCourse = null;
                this.selectedCourseName = "";
                this.notifyItemRemoved(i);
            }
        }
        return isRemove;
    }

    public boolean removeCourse(final ItemCourseEnhanced course) {
        boolean isRemove = false;

        if(this.courseList != null && course != null && this.bind != null) {
            final int position = this.courseList.indexOf(course);

            if(position > -1) {
                isRemove = this.courseList.remove(course);
                this.notifyItemRemoved(position);
            }
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

        public void onBind(ItemCourseEnhanced item) {
            if(this.bind != null) {
                this.bind.courseTitle.setText(item.getCourseName());
                this.bind.courseDistance.setText(String.format("%dm", (int) item.getCourseDistance()));

                if(item.isClicked()) { // Clicked
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
