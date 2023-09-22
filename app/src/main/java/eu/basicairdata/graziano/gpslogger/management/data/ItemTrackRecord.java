package eu.basicairdata.graziano.gpslogger.management.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;


public class ItemTrackRecord implements Serializable {
    private LinkedList<ItemCourse> itemCourseList;
    private LinkedList<ItemPlaceMarkImg> itemPlacemarkImgList;
    private LinkedList<ItemPlaceMark> itemPlaceMarkList;
    private LinkedList<ItemPlaceMarkType> itemPlaceMarkTypeList;

    public ItemTrackRecord() {
        this.itemCourseList = new LinkedList<>();
        this.itemPlaceMarkList = new LinkedList<>();
        this.itemPlacemarkImgList = new LinkedList<>();
        this.itemPlaceMarkList = new LinkedList<>();
    }

    public LinkedList<ItemPlaceMark> getItemPlaceMarkList() {
        return itemPlaceMarkList;
    }

    public void setItemPlaceMarkList(LinkedList<ItemPlaceMark> itemPlaceMarkList) {
        Collections.sort(itemPlaceMarkList);
        this.itemPlaceMarkList = itemPlaceMarkList;
    }

    public LinkedList<ItemPlaceMarkImg> getItemPlacemarkImgList() {
        return itemPlacemarkImgList;
    }

    public void setItemPlacemarkImgList(LinkedList<ItemPlaceMarkImg> itemPlacemarkImgList) {
        this.itemPlacemarkImgList = itemPlacemarkImgList;
    }

    public LinkedList<ItemCourse> getItemCourseList() {
        return itemCourseList;
    }

    public void setItemCourseList(LinkedList<ItemCourse> itemCourseList) {
        this.itemCourseList = itemCourseList;
    }

    public LinkedList<ItemPlaceMarkType> getItemPlaceMarkTypeList() {
        return itemPlaceMarkTypeList;
    }

    public void setItemPlaceMarkTypeList(LinkedList<ItemPlaceMarkType> itemPlaceMarkTypeList) {
        this.itemPlaceMarkTypeList = itemPlaceMarkTypeList;
    }
}
