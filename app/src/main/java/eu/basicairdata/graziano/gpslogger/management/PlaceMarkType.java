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
                return 6;
            }

            case "PARKING" -> {
                return 5;
            }

            case "TOILET" -> {
                return 4;
            }

            case "REST" -> {
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
