package eu.basicairdata.graziano.gpslogger.management;

import java.util.LinkedList;

import eu.basicairdata.graziano.gpslogger.recording.enhanced.ItemCourseEnhancedData;
import eu.basicairdata.graziano.gpslogger.recording.enhanced.ItemPlaceMarkImgData;

public class ItemTrackRecord {
    private LinkedList<ItemPlaceMarkImgData> itemPlacemarkImgList;
    private LinkedList<ItemCourseEnhancedData> itemCourseList;

    public ItemTrackRecord() {
        this.itemCourseList = new LinkedList<>();
        this.itemPlacemarkImgList = new LinkedList<>();
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
}
