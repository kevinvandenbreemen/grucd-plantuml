interface  UnusedInterface {

    void stepUp();

}

class StandsAlone {

    private UserOfStandsAlone user;
    private UnusedInterface unused;

    void stepUp() {

    }

}

class UserOfStandsAlone extends StandsAlone {

}