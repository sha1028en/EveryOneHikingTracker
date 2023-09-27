package eu.basicairdata.graziano.gpslogger.management.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;

/**
 * A One Track's Record DataClass
 * from Server
 */
public class ItemTrackRecord implements Serializable {
    private LinkedList<ItemCourse> itemCourseList; // this track's course List
    private LinkedList<ItemPlaceMarkImg> itemPlacemarkImgList; // this track's Img List
    private LinkedList<ItemPlaceMark> itemPlaceMarkList; // this track's placemark List
    private LinkedList<ItemPlaceMarkType> itemPlaceMarkTypeList; // this track's placemark Type( kind ) List

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
