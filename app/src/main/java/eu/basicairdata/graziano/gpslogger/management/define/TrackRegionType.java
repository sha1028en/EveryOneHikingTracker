package eu.basicairdata.graziano.gpslogger.management.define;


public enum TrackRegionType {
    SEOUL("서울"),
    INCHEON("인천"),
    GYEONGGI("경기"),
    GANGWON("강원"),
    SEJONG("세종"),
    DAEJEON("대전"),
    CHUNGCHEONG_SOUTH("충남"),
    CHUNGCHEONG_NORTH("충북"),
    DAEGU("대구"),
    GYEONGSANG_NORTH("경남"),
    GYEONGSANG_SOUTH("경북"),
    ULSAN("울산"),
    BUSAN("부산"),
    JEOLLA_NORTH("전북"),
    JEOLLA_SOUTH("전남"),
    GWANGJU("광주"),
    JEJU("제주");

    private final String regionName;

    // INIT
    private TrackRegionType(String regionName) {
        this.regionName = regionName;
    }

    public String getRegionName() {
        return this.regionName;
    }
}
