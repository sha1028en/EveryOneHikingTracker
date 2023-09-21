package eu.basicairdata.graziano.gpslogger.management;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;

import eu.basicairdata.graziano.gpslogger.recording.enhanced.ItemCourseEnhanced;
import eu.basicairdata.graziano.gpslogger.recording.enhanced.ItemPlaceMarkEnhanced;
import eu.basicairdata.graziano.gpslogger.recording.enhanced.ItemPlaceMarkImg;
import eu.basicairdata.graziano.gpslogger.recording.enhanced.ItemPlaceMarkType;


public class ItemTrackRecord implements Serializable {
    private LinkedList<ItemCourseEnhanced> itemCourseList;
    private LinkedList<ItemPlaceMarkImg> itemPlacemarkImgList;
    private LinkedList<ItemPlaceMarkEnhanced> itemPlaceMarkList;
    private LinkedList<ItemPlaceMarkType> itemPlaceMarkTypeList;

    public ItemTrackRecord() {
        this.itemCourseList = new LinkedList<>();
        this.itemPlaceMarkList = new LinkedList<>();
        this.itemPlacemarkImgList = new LinkedList<>();
        this.itemPlaceMarkList = new LinkedList<>();
    }

    public LinkedList<ItemPlaceMarkEnhanced> getItemPlaceMarkList() {
        return itemPlaceMarkList;
    }

    public void setItemPlaceMarkList(LinkedList<ItemPlaceMarkEnhanced> itemPlaceMarkList) {
        Collections.sort(itemPlaceMarkList);
        this.itemPlaceMarkList = itemPlaceMarkList;
    }

    public LinkedList<ItemPlaceMarkImg> getItemPlacemarkImgList() {
        return itemPlacemarkImgList;
    }

    public void setItemPlacemarkImgList(LinkedList<ItemPlaceMarkImg> itemPlacemarkImgList) {
        this.itemPlacemarkImgList = itemPlacemarkImgList;
    }

    public LinkedList<ItemCourseEnhanced> getItemCourseList() {
        return itemCourseList;
    }

    public void setItemCourseList(LinkedList<ItemCourseEnhanced> itemCourseList) {
        this.itemCourseList = itemCourseList;
    }

    public LinkedList<ItemPlaceMarkType> getItemPlaceMarkTypeList() {
        return itemPlaceMarkTypeList;
    }

    public void setItemPlaceMarkTypeList(LinkedList<ItemPlaceMarkType> itemPlaceMarkTypeList) {
        this.itemPlaceMarkTypeList = itemPlaceMarkTypeList;
    }
}
