package eu.basicairdata.graziano.gpslogger.management;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;

import eu.basicairdata.graziano.gpslogger.recording.enhanced.ItemCourseEnhancedData;
import eu.basicairdata.graziano.gpslogger.recording.enhanced.ItemPlaceMarkEnhancedData;
import eu.basicairdata.graziano.gpslogger.recording.enhanced.ItemPlaceMarkImgData;
import eu.basicairdata.graziano.gpslogger.recording.enhanced.ItemPlaceMarkTypeData;


public class ItemTrackRecord implements Serializable {
    private LinkedList<ItemCourseEnhancedData> itemCourseList;
    private LinkedList<ItemPlaceMarkImgData> itemPlacemarkImgList;
    private LinkedList<ItemPlaceMarkEnhancedData> itemPlaceMarkList;
    private LinkedList<ItemPlaceMarkTypeData> itemPlaceMarkTypeList;

    public ItemTrackRecord() {
        this.itemCourseList = new LinkedList<>();
        this.itemPlaceMarkList = new LinkedList<>();
        this.itemPlacemarkImgList = new LinkedList<>();
        this.itemPlaceMarkList = new LinkedList<>();
    }

    public LinkedList<ItemPlaceMarkEnhancedData> getItemPlaceMarkList() {
        return itemPlaceMarkList;
    }

    public void setItemPlaceMarkList(LinkedList<ItemPlaceMarkEnhancedData> itemPlaceMarkList) {
        Collections.sort(itemPlaceMarkList);
        this.itemPlaceMarkList = itemPlaceMarkList;
    }

    public LinkedList<ItemPlaceMarkImgData> getItemPlacemarkImgList() {
        return itemPlacemarkImgList;
    }

    public void setItemPlacemarkImgList(LinkedList<ItemPlaceMarkImgData> itemPlacemarkImgList) {
        this.itemPlacemarkImgList = itemPlacemarkImgList;
    }

    public LinkedList<ItemCourseEnhancedData> getItemCourseList() {
        return itemCourseList;
    }

    public void setItemCourseList(LinkedList<ItemCourseEnhancedData> itemCourseList) {
        this.itemCourseList = itemCourseList;
    }

    public LinkedList<ItemPlaceMarkTypeData> getItemPlaceMarkTypeList() {
        return itemPlaceMarkTypeList;
    }

    public void setItemPlaceMarkTypeList(LinkedList<ItemPlaceMarkTypeData> itemPlaceMarkTypeList) {
        this.itemPlaceMarkTypeList = itemPlaceMarkTypeList;
    }
}
