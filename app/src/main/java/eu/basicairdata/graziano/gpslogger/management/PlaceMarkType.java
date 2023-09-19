package eu.basicairdata.graziano.gpslogger.management;

/**
 * PlaceMark Type Define
 */
public enum PlaceMarkType {
    ENTRANCE,
    PARKING,
    TOILET,
    REST_AREA,
    BUS_STOP,
    OBSERVATION_DECK,
    ETC;

    PlaceMarkType() {}


    // if use ordinal() make it EASY... but ordinal() known DO NOT USE. As we As Can
    public int convertIntType() {
        switch (this.name()) {
            case "ENTRANCE" -> {
                return 6;
            }

            case "PARKING" -> {
                return 5;
            }

            case "TOILET" -> {
                return 4;
            }

            case "REST_AREA" -> {
                return 3;
            }

            case "BUS_STOP" -> {
                return 2;
            }

            case "OBSERVATION_DECK" -> {
                return 1;
            }

            case "ETC" -> {
                return 0;
            }

            default -> {
                return 0;
            }
        }
    }
}
