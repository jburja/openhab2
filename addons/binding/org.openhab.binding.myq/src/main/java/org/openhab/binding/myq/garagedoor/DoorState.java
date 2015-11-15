package org.openhab.binding.myq.garagedoor;

public enum DoorState {
    CLOSED("0"),
    OPEN("1"),
    // TODO: What is state of 2?
    STOPPED("3"),
    OPENING("4"),
    CLOSING("5");

    private String state;

    DoorState(String state) {
        this.state = state;
    }

    public String getStateCode() {
        return state;
    }

    public static DoorState forCode(String code) {
        for (DoorState state : DoorState.values()) {
            if (code.equalsIgnoreCase(state.getStateCode())) {
                return state;
            }
        }
        return null;
    }
}
