package eu.basicairdata.graziano.gpslogger.management;



public enum PlaceMarkType {
    ENTRANCE,
    PARKING,
    TOILET,
    REST,
    BUS_STOP,
    OBSERVATION_DECK,
    ETC;

    PlaceMarkType() {}


    // if use ordinal() make it EASY... but ordinal() known DO NOT USE. As we As Can
    public int convertIntType() {
        switch (this.name()) {
            case "ENTRANCE" -> {
                return 0;
            }

            case "PARKING" -> {
                return 1;
            }

            case "TOILET" -> {
                return 2;
            }

            case "REST" -> {
                return 3;
            }

            case "BUS_STOP" -> {
                return 4;
            }

            case "OBSERVATION_DECK" -> {
                return 5;
            }

            case "ETC" -> {
                return 6;
            }

            default -> {
                return 6;
            }
        }
    }
}
