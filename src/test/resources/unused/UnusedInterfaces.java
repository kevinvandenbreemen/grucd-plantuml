interface  UnusedInterface {

    void stepUp();

}

class StandsAlone {

    private UserOfStandsAlone user;

    void stepUp() {

    }

}

class UserOfStandsAlone {
    private StandsAlone standsAlone;
}